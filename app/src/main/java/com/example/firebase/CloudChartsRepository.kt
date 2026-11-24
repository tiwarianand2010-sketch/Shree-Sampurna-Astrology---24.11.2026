package com.example.firebase

import android.content.Context
import androidx.annotation.Keep
import com.example.R
import com.example.model.BirthInput
import com.example.model.DstRule
import com.example.model.KundaliResult
import com.example.model.Place
import com.google.firebase.Firebase
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.IgnoreExtraProperties
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ServerTimestamp
import com.google.firebase.firestore.snapshots
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await

@Keep
@IgnoreExtraProperties
data class SavedKundali(
    @DocumentId val id: String = "",
    val userId: String = "",
    val chartName: String = "",
    val birthYear: Long = 1990L,
    val birthMonth: Long = 1L,
    val birthDay: Long = 1L,
    val birthHour: Long = 0L,
    val birthMinute: Long = 0L,
    val placeId: String = "",
    val placeName: String = "",
    val latitude: Double = 28.6139,
    val longitude: Double = 77.2090,
    val utcOffsetHours: Double = 5.5,
    val lagnaRashi: String = "",
    val moonRashi: String = "",
    val notes: String = "",
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
) {
    fun toBirthInput(): BirthInput = BirthInput(
        name = chartName.ifBlank { "Kundali" },
        year = birthYear.toInt(),
        month = birthMonth.toInt(),
        day = birthDay.toInt(),
        hour = birthHour.toInt(),
        minute = birthMinute.toInt(),
        place = Place(
            id = placeId.ifBlank { "custom" },
            nameHi = placeName,
            nameEn = placeName,
            nameZh = placeName,
            countryCode = "IN",
            latitude = latitude,
            longitude = longitude,
            baseUtcOffsetHours = utcOffsetHours,
            dstRule = DstRule.NONE
        ),
        customUtcOffsetHours = utcOffsetHours
    )
}

class CloudChartsRepository(
    private val db: FirebaseFirestore,
    private val auth: FirebaseAuth = Firebase.auth
) {
    constructor(context: Context) : this(
        FirebaseFirestore.getInstance(
            context.applicationContext.getString(R.string.firestore_database_id)
        )
    )

    private fun requireUserId(): String =
        auth.currentUser?.uid
            ?: throw IllegalStateException("User must be signed in with Google before accessing cloud Kundalis.")

    fun observeSavedCharts(userId: String, limitCount: Long = 500L): Flow<List<SavedKundali>> = flow {
        val path = "users/$userId/kundalis"
        emitAll(
            db.collection("users")
                .document(userId)
                .collection("kundalis")
                .orderBy("updatedAt", Query.Direction.DESCENDING)
                .limit(limitCount)
                .snapshots()
                .map { snapshot ->
                    snapshot.toObjects(
                        SavedKundali::class.java,
                        DocumentSnapshot.ServerTimestampBehavior.ESTIMATE
                    )
                }
                .catch { error ->
                    val firestoreError = generateSequence(error) { it.cause }
                        .filterIsInstance<com.google.firebase.firestore.FirebaseFirestoreException>()
                        .firstOrNull()
                    if (firestoreError == null && error is CancellationException) throw error
                    val exceptionToLog = firestoreError ?: (error as? Exception)
                    if (exceptionToLog != null && auth.currentUser?.uid == userId) {
                        handleFirestoreError(exceptionToLog, OperationType.LIST, path)
                    }
                    throw error
                }
        )
    }

    suspend fun saveChart(
        userId: String,
        result: KundaliResult,
        notes: String = ""
    ): Result<String> {
        var docPath = "users/$userId/kundalis"
        return try {
            val uid = requireUserId()
            check(uid == userId) { "Signed-in user changed; refusing cross-user write." }
            val input = result.input
            val cleanName = input.name.trim().ifBlank { "Kundali" }.take(120)
            val cleanPlaceId = input.place.id.trim().ifBlank { "custom" }.take(80)
            val cleanPlaceName = input.place.nameEn.trim().ifBlank { input.place.nameHi }.take(120)
            val moonRashi = result.planets.first { it.planet == com.example.model.PlanetId.MOON }.rashi.name
            val cleanNotes = notes.trim().take(500)

            val docRef = db.collection("users").document(uid).collection("kundalis").document()
            docPath = docRef.path

            val payload = mapOf(
                "userId" to uid,
                "chartName" to cleanName,
                "birthYear" to input.year.toLong(),
                "birthMonth" to input.month.toLong(),
                "birthDay" to input.day.toLong(),
                "birthHour" to input.hour.toLong(),
                "birthMinute" to input.minute.toLong(),
                "placeId" to cleanPlaceId,
                "placeName" to cleanPlaceName,
                "latitude" to input.place.latitude.coerceIn(-66.0, 66.0),
                "longitude" to input.place.longitude.coerceIn(-180.0, 180.0),
                "utcOffsetHours" to result.effectiveUtcOffsetHours.coerceIn(-12.0, 14.0),
                "lagnaRashi" to result.lagnaRashi.name,
                "moonRashi" to moonRashi,
                "notes" to cleanNotes,
                "createdAt" to FieldValue.serverTimestamp(),
                "updatedAt" to FieldValue.serverTimestamp()
            )
            docRef.set(payload).await()
            Result.success(docRef.id)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, docPath)
            Result.failure(e)
        }
    }

    suspend fun updateNotes(userId: String, kundaliId: String, chartName: String, notes: String): Result<Unit> {
        var docPath = "users/$userId/kundalis/$kundaliId"
        return try {
            val uid = requireUserId()
            check(uid == userId) { "Signed-in user changed." }
            require(kundaliId.isNotBlank()) { "Document ID cannot be blank" }
            val docRef = db.collection("users").document(uid).collection("kundalis").document(kundaliId)
            docPath = docRef.path
            docRef.update(
                mapOf(
                    "chartName" to chartName.trim().ifBlank { "Kundali" }.take(120),
                    "notes" to notes.trim().take(500),
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            ).await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, docPath)
            Result.failure(e)
        }
    }

    suspend fun deleteChart(userId: String, kundaliId: String): Result<Unit> {
        var docPath = "users/$userId/kundalis/$kundaliId"
        return try {
            val uid = requireUserId()
            check(uid == userId) { "Signed-in user changed." }
            require(kundaliId.isNotBlank()) { "Document ID cannot be blank" }
            val docRef = db.collection("users").document(uid).collection("kundalis").document(kundaliId)
            docPath = docRef.path
            docRef.delete().await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.DELETE, docPath)
            Result.failure(e)
        }
    }
}

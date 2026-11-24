package com.example.core

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import kotlinx.coroutines.tasks.await
import java.util.TimeZone
import kotlin.math.round

data class ResolvedGpsLocation(
    val latitude: Double,
    val longitude: Double,
    val utcOffsetHours: Double
)

object LocationHelper {

    fun hasCoarseLocationPermission(context: Context): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }

    @SuppressLint("MissingPermission")
    suspend fun fetchCurrentLocation(context: Context): Result<ResolvedGpsLocation> {
        if (!hasCoarseLocationPermission(context)) {
            return Result.failure(IllegalStateException("Location permission not granted."))
        }
        return try {
            val client = LocationServices.getFusedLocationProviderClient(context)
            val cts = CancellationTokenSource()
            val location = try {
                client.getCurrentLocation(Priority.PRIORITY_BALANCED_POWER_ACCURACY, cts.token).await()
                    ?: client.lastLocation.await()
            } finally {
                cts.cancel()
            }
            if (location == null) {
                Result.failure(
                    IllegalStateException("GPS location unavailable on this device. Please ensure Location services are enabled or choose a district manually.")
                )
            } else {
                val rawOffsetHours = TimeZone.getDefault().getOffset(System.currentTimeMillis()) / 3_600_000.0
                val roundedOffset = round(rawOffsetHours * 2.0) / 2.0
                Result.success(
                    ResolvedGpsLocation(
                        latitude = location.latitude.coerceIn(-66.0, 66.0),
                        longitude = location.longitude.coerceIn(-180.0, 180.0),
                        utcOffsetHours = roundedOffset
                    )
                )
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

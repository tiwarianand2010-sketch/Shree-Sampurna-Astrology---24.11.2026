package com.example.firebase

import android.util.Log
import com.example.core.AppLanguage
import com.example.core.AstroStrings
import com.example.model.KundaliResult
import com.example.model.PlanetId
import com.google.firebase.Firebase
import com.google.firebase.ai.ai
import com.google.firebase.ai.type.Content
import com.google.firebase.ai.type.content
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withTimeout

enum class AiAstrologerSpecialty(val titleEn: String, val titleHi: String) {
    VEDIC("Vedic Parashari & KP", "वैदिक पाराशरी एवं केपी"),
    WESTERN("Western Tropical", "पाश्चात्य ट्रॉपिकल ज्योतिष"),
    NUMEROLOGY("Numerology & Lo Shu", "अंकशास्त्र एवं लो-शू ग्रिड"),
    VASTU_FENG_SHUI("Vastu & Feng Shui", "वास्तु एवं फ़ेंग शुई")
}

data class AiChatMessage(
    val id: String,
    val isUser: Boolean,
    val text: String,
    val specialty: AiAstrologerSpecialty
)

class AiAstrologerRepository {

    companion object {
        private const val TAG = "AiAstrologerRepo"
        private const val TIMEOUT_MS = 60_000L
    }

    private val model by lazy {
        Firebase.ai.generativeModel(modelName = "gemini-3.8-flash")
    }

    fun buildChartSummaryContext(result: KundaliResult, specialty: AiAstrologerSpecialty, language: AppLanguage): String {
        val sb = StringBuilder()
        sb.appendLine("You are an expert astrologer in ${specialty.titleEn} at Shri Sampurna Astrology.")
        sb.appendLine("Respond naturally and respectfully in ${language.nativeName} (${language.code}).")
        sb.appendLine("Chart Name: ${result.input.name}")
        sb.appendLine("Birth Date/Time: ${result.input.year}-${result.input.month}-${result.input.day} ${"%02d:%02d".format(result.input.hour, result.input.minute)} (UTC${result.effectiveUtcOffsetHours})")
        sb.appendLine("Place: ${result.input.place.nameEn} (Lat ${result.input.place.latitude}, Lon ${result.input.place.longitude})")
        sb.appendLine("Vedic Lagna (Lahiri): ${result.lagnaRashi.name} ${result.lagnaDmsFormatted} | Lagnesh: ${result.summary.lagnesh.name}")
        sb.appendLine("Sidereal Planets:")
        result.planets.forEach { p ->
            sb.appendLine("  ${p.planet.name}: ${p.rashi.name} ${p.dmsFormatted}, House ${p.houseFromLagna}, Nakshatra ${AstroStrings.nakshatraName(p.nakshatraIndex, AppLanguage.EN)} (${p.nakshatraPada}), Dignity=${p.dignity.name}, Retro=${p.isRetrograde}, Combust=${p.isCombust}, D9=${p.navamsaRashi.name}")
        }
        result.currentMahadasha?.let { m ->
            sb.appendLine("Current Vimshottari: Maha=${m.lord.name} (${m.startDateStr}..${m.endDateStr}), Antar=${result.currentAntardasha?.lord?.name}, Pratyantar=${result.currentPratyantardasha?.lord?.name}")
        }
        result.ashtakavarga?.let { av ->
            sb.appendLine("Sarvashtakavarga Bindus (Aries..Pisces): ${av.sarvashtakavargaByRashi.joinToString(",")}")
        }
        result.kpAnalysis?.let { kp ->
            sb.appendLine("KP Ayanamsa: ${"%.2f".format(kp.kpAyanamsaDeg)}°. Cusps 1=${kp.cusps[0].rashi.name} (${kp.cusps[0].kp.subLord}), 7=${kp.cusps[6].rashi.name} (${kp.cusps[6].kp.subLord}), 10=${kp.cusps[9].rashi.name} (${kp.cusps[9].kp.subLord}). Ruling Planets: ${kp.rulingPlanets.distinctRulers.joinToString(",")}")
        }
        result.westernChart?.let { w ->
            sb.appendLine("Western Tropical: Sun=${w.sunSign.name}, Moon=${w.moonSign.name}, Asc=${w.ascendantSign.name}, MC=${w.midheavenSign.name}; Elements F=${w.fireCount}/E=${w.earthCount}/A=${w.airCount}/W=${w.waterCount}")
        }
        result.numerology?.let { n ->
            sb.appendLine("Numerology: Mulank=${n.mulank}, Bhagyank=${n.bhagyank}, ChaldeanName=${n.chaldeanNameNumber}, Kua=${n.kuaNumber}, Missing=${n.missingNumbers.joinToString(",")}")
        }
        result.vastuFengShui?.let { v ->
            sb.appendLine("Vastu/FengShui: ${v.kuaGroupEn}, Wealth=${v.wealthDirectionEn}, Entrance=${v.entranceAdviceEn}, Puja=${v.pujaDirectionEn}")
        }
        sb.appendLine("Give grounded, compassionate, actionable guidance strictly rooted in the chart parameters above.")
        return sb.toString()
    }

    fun streamChatReply(
        result: KundaliResult,
        specialty: AiAstrologerSpecialty,
        language: AppLanguage,
        priorHistory: List<AiChatMessage>,
        userQuestion: String
    ): Flow<String> = flow {
        val systemContext = buildChartSummaryContext(result, specialty, language)
        val historyContents = mutableListOf<Content>()
        historyContents.add(content("user") { text(systemContext) })
        historyContents.add(
            content("model") {
                text("Understood. I have carefully studied the complete chart and planetary placements. Please ask your question.")
            }
        )
        priorHistory.takeLast(8).forEach { msg ->
            historyContents.add(
                content(if (msg.isUser) "user" else "model") { text(msg.text) }
            )
        }

        val chat = model.startChat(history = historyContents)
        val sb = StringBuilder()
        withTimeout(TIMEOUT_MS) {
            chat.sendMessageStream(userQuestion).collect { chunk ->
                val txt = chunk.text.orEmpty()
                if (txt.isNotEmpty()) {
                    sb.append(txt)
                    emit(sb.toString())
                }
            }
        }
        if (sb.isEmpty()) {
            throw IllegalStateException("AI astrologer returned an empty response.")
        }
    }.catch { e ->
        when (e) {
            is TimeoutCancellationException -> {
                Log.e(TAG, "AI request timed out", e)
                throw IllegalStateException("AI consultation timed out after 60s. Please try again.")
            }
            is CancellationException -> throw e
            else -> {
                Log.e(TAG, "Firebase AI Logic error", e)
                throw e
            }
        }
    }
}

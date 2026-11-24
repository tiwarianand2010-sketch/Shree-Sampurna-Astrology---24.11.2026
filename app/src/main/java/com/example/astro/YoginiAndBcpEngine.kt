package com.example.astro

import com.example.core.AppLanguage
import com.example.core.AstroStrings
import com.example.model.BcpYearRow
import com.example.model.BhriguChakraReport
import com.example.model.ExtendedPanchangDetails
import com.example.model.KundaliResult
import com.example.model.PhaladeshSection
import com.example.model.PlanetId
import com.example.model.Rashi
import com.example.model.YoginiAntardasha
import com.example.model.YoginiMahadasha
import com.example.model.YoginiPratyantardasha
import java.util.Calendar
import java.util.TimeZone
import kotlin.math.abs
import kotlin.math.acos
import kotlin.math.asin
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.floor
import kotlin.math.sin
import kotlin.math.tan

object YoginiAndBcpEngine {

    private const val TROPICAL_YEAR_DAYS = 365.24219878

    // 8 Yoginis: Mangala(1,Moon), Pingala(2,Sun), Dhanya(3,Jupiter), Bhramari(4,Mars),
    // Bhadrika(5,Mercury), Ulka(6,Saturn), Siddha(7,Venus), Sankata(8,Rahu)
    private data class YoginiSpec(val name: String, val years: Int, val lord: PlanetId)

    private val yoginiSequence = listOf(
        YoginiSpec("Mangala", 1, PlanetId.MOON),
        YoginiSpec("Pingala", 2, PlanetId.SUN),
        YoginiSpec("Dhanya", 3, PlanetId.JUPITER),
        YoginiSpec("Bhramari", 4, PlanetId.MARS),
        YoginiSpec("Bhadrika", 5, PlanetId.MERCURY),
        YoginiSpec("Ulka", 6, PlanetId.SATURN),
        YoginiSpec("Siddha", 7, PlanetId.VENUS),
        YoginiSpec("Sankata", 8, PlanetId.RAHU)
    )

    fun computeExtendedPanchang(result: KundaliResult): ExtendedPanchangDetails {
        val inp = result.input
        val (sunriseH, sunsetH) = EphemerisEngine.sunriseAndSunsetHours(
            inp.year, inp.month, inp.day, inp.place.latitude, inp.place.longitude, result.effectiveUtcOffsetHours
        )
        val dayDur = (sunsetH - sunriseH).coerceAtLeast(1.0)
        val dinH = floor(dayDur).toInt()
        val dinM = kotlin.math.round((dayDur - dinH) * 60.0).toInt().coerceIn(0, 59)
        val dinmaanStr = "%02dh %02dm (%.2f hrs)".format(dinH, dinM, dayDur)

        val segDay = dayDur / 8.0
        val weekday = result.panchang.weekdayIndex // 0=Sun..6=Sat
        // Yamaganda slots (1-based): Sun=5, Mon=4, Tue=3, Wed=2, Thu=1, Fri=7, Sat=6
        val yamaSlot = intArrayOf(5, 4, 3, 2, 1, 7, 6)[weekday] - 1
        val yamaStart = sunriseH + yamaSlot * segDay
        val yamagandaStr = "${TimeUtils.formatHoursToHhMm(yamaStart)} – ${TimeUtils.formatHoursToHhMm(yamaStart + segDay)}"

        // Gulika Kaal slots (1-based): Sun=7, Mon=6, Tue=5, Wed=4, Thu=3, Fri=2, Sat=1
        val gulikaSlot = intArrayOf(7, 6, 5, 4, 3, 2, 1)[weekday] - 1
        val gulikaStart = sunriseH + gulikaSlot * segDay
        val gulikaStr = "${TimeUtils.formatHoursToHhMm(gulikaStart)} – ${TimeUtils.formatHoursToHhMm(gulikaStart + segDay)}"

        // Approximate Moonrise & Moonset from Moon elongation & local solar transit
        val sun = result.planets.first { it.planet == PlanetId.SUN }
        val moon = result.planets.first { it.planet == PlanetId.MOON }
        val elongationDeg = EphemerisEngine.normalize360(moon.siderealLongitude - sun.siderealLongitude)
        val lagHours = (elongationDeg / 360.0) * 24.84
        val moonriseH = (sunriseH + lagHours) % 24.0
        val moonsetH = (moonriseH + 12.2) % 24.0
        val moonriseStr = TimeUtils.formatHoursToHhMm(moonriseH)
        val moonsetStr = TimeUtils.formatHoursToHhMm(moonsetH)

        // Vedic Masa (from Sun's solar sign adjusted with Amanta/Purnimanta lunar month estimation)
        val sunSignIdx = sun.rashi.ordinal
        val vedicMasas = listOf(
            "Vaishakha (वैशाख)", "Jyeshtha (ज्येष्ठ)", "Ashadha (आषाढ़)", "Shravana (श्रावण)",
            "Bhadrapada (भाद्रपद)", "Ashwin (आश्विन)", "Kartik (कार्तिक)", "Margashirsha (मार्गशीर्ष)",
            "Pausha (पौष)", "Magha (माघ)", "Phalguna (फाल्गुन)", "Chaitra (चैत्र)"
        )
        val vedicMasa = vedicMasas[sunSignIdx]
        val vikramSamvat = if (inp.month >= 4 || (inp.month == 3 && inp.day >= 22)) inp.year + 57 else inp.year + 56
        val shakaSamvat = if (inp.month >= 4 || (inp.month == 3 && inp.day >= 22)) inp.year - 78 else inp.year - 79

        val sunTrop = EphemerisEngine.normalize360(sun.siderealLongitude + result.ayanamsaDeg)
        val ayana = if (sunTrop >= 270.0 || sunTrop < 90.0) "Uttarayana (उत्तरायण)" else "Dakshinayana (दक्षिणायण)"
        val ritu = when (sunSignIdx) {
            11, 0 -> "Vasanta (वसंत / Spring)"
            1, 2 -> "Grishma (ग्रीष्म / Summer)"
            3, 4 -> "Varsha (वर्षा / Monsoon)"
            5, 6 -> "Sharad (शरद / Autumn)"
            7, 8 -> "Hemanta (हेमंत / Pre-Winter)"
            else -> "Shishira (शिशिर / Winter)"
        }

        // Baladi Avastha (odd signs: 0-6 Bala, 6-12 Kumara, 12-18 Yuva, 18-24 Vriddha, 24-30 Mrita; even signs reversed)
        val baladiMap = result.planets.associate { pl ->
            val odd = pl.rashi.ordinal % 2 == 0
            val idx = floor(pl.degreeInRashi / 6.0).toInt().coerceIn(0, 4)
            val adj = if (odd) idx else 4 - idx
            val label = when (adj) {
                0 -> "Bala (बाल - Infant)"
                1 -> "Kumara (कुमार - Youthful)"
                2 -> "Yuva (युवा - Prime/Full)"
                3 -> "Vriddha (वृद्ध - Aged)"
                else -> "Mrita (मृत - Dormant)"
            }
            pl.planet to label
        }

        val vargottama = result.planets.filter { it.rashi == it.navamsaRashi }.map { it.planet }.toSet()

        // Bhava Chalit mid-cusp shifts vs D1 house
        val kpCusps = result.kpAnalysis?.cusps
        val chalitMap = result.planets.associate { pl ->
            val h = if (kpCusps != null && kpCusps.size == 12) {
                var found = pl.houseFromLagna
                for (i in 0 until 12) {
                    val cCurr = EphemerisEngine.normalize360(kpCusps[i].kpLongitude - 15.0)
                    val cNext = EphemerisEngine.normalize360(kpCusps[(i + 1) % 12].kpLongitude - 15.0)
                    val pLon = EphemerisEngine.normalize360(pl.siderealLongitude)
                    val inside = if (cCurr < cNext) pLon >= cCurr && pLon < cNext else pLon >= cCurr || pLon < cNext
                    if (inside) { found = i + 1; break }
                }
                found
            } else pl.houseFromLagna
            pl.planet to h
        }

        val shifts = result.planets.mapNotNull { pl ->
            val ch = chalitMap[pl.planet] ?: pl.houseFromLagna
            if (ch != pl.houseFromLagna) "${pl.planet.name}: D1 H${pl.houseFromLagna} -> Chalit H$ch" else null
        }

        val mdLord = result.currentMahadasha?.lord ?: result.lagnaRashi.lord
        val adLord = result.currentAntardasha?.lord ?: mdLord
        val mdHouse = result.planets.firstOrNull { it.planet == mdLord }?.houseFromLagna ?: 1
        val adHouse = result.planets.firstOrNull { it.planet == adLord }?.houseFromLagna ?: 1
        val dashaInsights = listOf(
            "Career & Status" to "Active ${mdLord.name}-${adLord.name} period activates Houses $mdHouse & $adHouse. Focus on disciplined execution and leadership.",
            "Business & Ventures" to "Mercury/Jupiter alignment and active Dasha house $adHouse reward structured expansion and written contracts.",
            "Finance & Wealth" to "House 2/11 significations under ${mdLord.name} favor steady wealth accumulation; avoid speculative over-leverage.",
            "Marriage & Harmony" to "House 7 themes balanced through patient communication; Venus/Jupiter transits support domestic peace.",
            "Children & Creativity" to "House 5 creative output and progeny well-being strengthen during benefic ${adLord.name} sub-cycles.",
            "Education & Wisdom" to "Study, certifications, and analytical research thrive when Mercury and 5th/9th house lords are honored.",
            "Property & Vehicles" to "House 4 real-estate and asset acquisition gain momentum when 4th lord and Mars receive supportive transits.",
            "Travel & Relocation" to "Houses 3, 9, and 12 indicate purposeful journeys and spiritual pilgrimages during movable-sign transits.",
            "Spirituality & Sadhana" to "Daily meditation, mantra japa for ${mdLord.name}, and charitable seva deepen inner clarity."
        )

        return ExtendedPanchangDetails(
            moonriseStr = moonriseStr,
            moonsetStr = moonsetStr,
            dinmaanStr = dinmaanStr,
            yamagandaStr = yamagandaStr,
            gulikaStr = gulikaStr,
            vedicMasa = vedicMasa,
            vikramSamvat = vikramSamvat,
            shakaSamvat = shakaSamvat,
            ayana = ayana,
            ritu = ritu,
            sunSign = sun.rashi,
            moonSign = moon.rashi,
            moonPada = moon.nakshatraPada,
            moonStarLord = moon.nakshatraLord,
            pakshaLabel = if (result.panchang.isShuklaPaksha) "Shukla Paksha (शुक्ल पक्ष)" else "Krishna Paksha (कृष्ण पक्ष)",
            baladiAvasthas = baladiMap,
            vargottamaPlanets = vargottama,
            chalitHouseShifts = shifts,
            bhavaChalitMap = chalitMap,
            dashaLifeAreaInsights = dashaInsights
        )
    }

    fun computeYoginiDasha(result: KundaliResult, nowMillis: Long = System.currentTimeMillis()): List<YoginiMahadasha> {
        val moon = result.planets.first { it.planet == PlanetId.MOON }
        val nakSpan = 360.0 / 27.0
        val nakIdx0 = moon.nakshatraIndex // 0..26 (Ashwini=0..Revati=26)
        val nakNum1 = nakIdx0 + 1
        val posInNak = moon.siderealLongitude - nakIdx0 * nakSpan
        val elapsedFrac = (posInNak / nakSpan).coerceIn(0.0, 1.0)
        // Classical formula: (Nakshatra_Num + 3) % 8 => 1=Mangala, 2=Pingala, ..., 0=Sankata (index 7)
        val startIdx = ((nakNum1 + 3 - 1) % 8 + 8) % 8
        val startYogini = yoginiSequence[startIdx]
        var cursorJd = result.julianDay - (startYogini.years * elapsedFrac) * TROPICAL_YEAR_DAYS

        val calNow = Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply { timeInMillis = nowMillis }
        val nowJd = TimeUtils.toJulianDay(
            calNow.get(Calendar.YEAR), calNow.get(Calendar.MONTH) + 1, calNow.get(Calendar.DAY_OF_MONTH),
            calNow.get(Calendar.HOUR_OF_DAY), calNow.get(Calendar.MINUTE), 0.0, 0.0
        )

        // Generate 3 cycles (108 years) so current lifetime period is always present
        val list = mutableListOf<YoginiMahadasha>()
        for (i in 0 until 24) {
            val mdSpec = yoginiSequence[(startIdx + i) % 8]
            val mdStart = cursorJd
            val mdEnd = mdStart + mdSpec.years * TROPICAL_YEAR_DAYS
            val mdIdx = yoginiSequence.indexOf(mdSpec)

            var adCursor = mdStart
            val antars = mutableListOf<YoginiAntardasha>()
            for (j in 0 until 8) {
                val adSpec = yoginiSequence[(mdIdx + j) % 8]
                val adYears = mdSpec.years.toDouble() * adSpec.years.toDouble() / 36.0
                val adStart = adCursor
                val adEnd = adStart + adYears * TROPICAL_YEAR_DAYS

                var pdCursor = adStart
                val prats = mutableListOf<YoginiPratyantardasha>()
                val adIdx = yoginiSequence.indexOf(adSpec)
                for (k in 0 until 8) {
                    val pdSpec = yoginiSequence[(adIdx + k) % 8]
                    val pdYears = adYears * pdSpec.years.toDouble() / 36.0
                    val pdStart = pdCursor
                    val pdEnd = pdStart + pdYears * TROPICAL_YEAR_DAYS
                    prats.add(
                        YoginiPratyantardasha(
                            lordName = pdSpec.name,
                            startDateStr = TimeUtils.jdToLocalDateString(pdStart, result.effectiveUtcOffsetHours),
                            endDateStr = TimeUtils.jdToLocalDateString(pdEnd, result.effectiveUtcOffsetHours),
                            isCurrent = nowJd >= pdStart && nowJd < pdEnd
                        )
                    )
                    pdCursor = pdEnd
                }
                antars.add(
                    YoginiAntardasha(
                        lordName = adSpec.name,
                        startDateStr = TimeUtils.jdToLocalDateString(adStart, result.effectiveUtcOffsetHours),
                        endDateStr = TimeUtils.jdToLocalDateString(adEnd, result.effectiveUtcOffsetHours),
                        isCurrent = nowJd >= adStart && nowJd < adEnd,
                        pratyantars = prats
                    )
                )
                adCursor = adEnd
            }

            list.add(
                YoginiMahadasha(
                    name = mdSpec.name,
                    planetLord = mdSpec.lord,
                    years = mdSpec.years,
                    startDateStr = TimeUtils.jdToLocalDateString(mdStart, result.effectiveUtcOffsetHours),
                    endDateStr = TimeUtils.jdToLocalDateString(mdEnd, result.effectiveUtcOffsetHours),
                    isCurrent = nowJd >= mdStart && nowJd < mdEnd,
                    antardashas = antars
                )
            )
            cursorJd = mdEnd
        }
        return list
    }

    fun computeBhriguChakraPaddhati(result: KundaliResult, nowMillis: Long = System.currentTimeMillis()): BhriguChakraReport {
        val calNow = Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply { timeInMillis = nowMillis }
        val nowJd = TimeUtils.toJulianDay(
            calNow.get(Calendar.YEAR), calNow.get(Calendar.MONTH) + 1, calNow.get(Calendar.DAY_OF_MONTH),
            calNow.get(Calendar.HOUR_OF_DAY), calNow.get(Calendar.MINUTE), 0.0, 0.0
        )
        val exactAge = ((nowJd - result.julianDay) / TROPICAL_YEAR_DAYS).coerceAtLeast(0.0)
        val runningYearNum = floor(exactAge).toInt() + 1 // 1st year of life = Age 0..1 -> House 1
        val progressPct = ((exactAge - floor(exactAge)) * 100.0).toInt().coerceIn(0, 99)
        val activeHouse = ((runningYearNum - 1) % 12) + 1
        val activeRashi = Rashi.fromIndex(result.lagnaRashi.ordinal + activeHouse - 1)
        val activeOccupants = result.planets.filter { it.houseFromLagna == activeHouse }.map { it.planet }

        val rows = (1..84).map { yr ->
            val house = ((yr - 1) % 12) + 1
            val rashi = Rashi.fromIndex(result.lagnaRashi.ordinal + house - 1)
            val occ = result.planets.filter { it.houseFromLagna == house }.map { it.planet }
            BcpYearRow(
                age = yr - 1,
                calendarYear = result.input.year + yr - 1,
                activeHouse = house,
                rashi = rashi,
                houseLord = rashi.lord,
                occupants = occ,
                isCurrentYear = yr == runningYearNum
            )
        }

        val occStr = activeOccupants.joinToString(", ") { it.name }.ifEmpty { "None (Lord ${activeRashi.lord.name} governs)" }
        val summary = "Year #$runningYearNum of life activates House $activeHouse (${activeRashi.name}), ruled by ${activeRashi.lord.name}. Occupants: $occStr. Progress through current birth year: $progressPct%."
        return BhriguChakraReport(
            exactAgeYears = exactAge,
            currentActiveAgeInt = runningYearNum - 1,
            currentActiveHouse = activeHouse,
            yearProgressPercent = progressPct,
            currentHouseSummary = summary,
            rows = rows
        )
    }

    fun computePhaladesh(
        result: KundaliResult,
        panchangExt: ExtendedPanchangDetails,
        jaimini: com.example.model.JaiminiReport,
        yoginiCurrent: YoginiMahadasha?,
        yogas: List<com.example.model.VedicYogaEntry>
    ): List<PhaladeshSection> {
        val lagnaLord = result.lagnaRashi.lord
        val lagnaLordPos = result.planets.first { it.planet == lagnaLord }
        val moon = result.planets.first { it.planet == PlanetId.MOON }
        val sun = result.planets.first { it.planet == PlanetId.SUN }
        val jup = result.planets.first { it.planet == PlanetId.JUPITER }
        val sat = result.planets.first { it.planet == PlanetId.SATURN }
        val ak = jaimini.charaKarakas.firstOrNull()?.planet ?: PlanetId.SUN

        val next12Themes = result.upcomingTransits1Year.take(5).joinToString("; ") {
            "${it.planet.name} -> ${it.toRashi.name} (${it.changeDateStr})"
        }.ifEmpty { "Steady planetary transits across current signs" }

        return listOf(
            PhaladeshSection(
                "1. Ascendant (Lagna) & Personality",
                "${result.lagnaRashi.name} Lagna at ${result.lagnaDmsFormatted} (${AstroStrings.nakshatraName(result.lagnaNakshatraIndex, AppLanguage.EN)} P${result.lagnaPada}) shapes an authentic, resilient temperament anchored in the ${AstroStrings.bhavaTitle(1, AppLanguage.EN)}."
            ),
            PhaladeshSection(
                "2. Lagna Lord (Lagnesh) Strength",
                "Lagnesh ${lagnaLord.name} is placed in House ${lagnaLordPos.houseFromLagna} (${lagnaLordPos.rashi.name}, ${lagnaLordPos.dignity.name}, ${panchangExt.baladiAvasthas[lagnaLord] ?: "Active"}), directly steering vitality, self-effort, and lifelong direction."
            ),
            PhaladeshSection(
                "3. Moon Sign (Chandra Rashi) & Mind",
                "Moon in ${moon.rashi.name} (House ${moon.houseFromLagna}, ${AstroStrings.nakshatraName(moon.nakshatraIndex, AppLanguage.EN)} P${moon.nakshatraPada}) governs emotional intelligence, intuition, and receptivity to public goodwill."
            ),
            PhaladeshSection(
                "4. Sun Placement & Soul Authority",
                "Sun in ${sun.rashi.name} (House ${sun.houseFromLagna}, ${sun.dignity.name}) shapes Dharma, father/mentor bonds, administrative confidence, and public reputation."
            ),
            PhaladeshSection(
                "5. Major Slow Planets (Jupiter & Saturn)",
                "Jupiter in ${jup.rashi.name} (House ${jup.houseFromLagna}) expands wisdom and protection, while Saturn in ${sat.rashi.name} (House ${sat.houseFromLagna}) builds enduring karma through patience and duty."
            ),
            PhaladeshSection(
                "6. Active Vimshottari & Yogini Dasha",
                "Running Vimshottari: ${result.currentMahadasha?.lord?.name ?: "-"} / ${result.currentAntardasha?.lord?.name ?: "-"} / ${result.currentPratyantardasha?.lord?.name ?: "-"}; Yogini Mahadasha: ${yoginiCurrent?.name ?: "-"} (${yoginiCurrent?.planetLord?.name ?: "-"})."
            ),
            PhaladeshSection(
                "7. Next 12 Months Transit Themes",
                "Key gochar sign shifts ahead: $next12Themes. Current Sade Sati status: ${AstroStrings.sadeSatiLabel(result.sadeSatiPhase, AppLanguage.EN)}."
            ),
            PhaladeshSection(
                "8. Birth Nakshatra, Pada & Dispositor",
                "Janma Nakshatra ${AstroStrings.nakshatraName(moon.nakshatraIndex, AppLanguage.EN)} (Pada ${moon.nakshatraPada}) is ruled by ${moon.nakshatraLord.name}; dispositor ${moon.rashi.lord.name} sits in House ${result.planets.first { it.planet == moon.rashi.lord }.houseFromLagna}."
            ),
            PhaladeshSection(
                "9. Panchang Birth Signature",
                "Born on ${AstroStrings.weekdayName(result.panchang.weekdayIndex, AppLanguage.EN)}, ${AstroStrings.tithiName(result.panchang.tithiNumber, AppLanguage.EN)} (${panchangExt.pakshaLabel}), ${AstroStrings.yogaName(result.panchang.yogaIndex, AppLanguage.EN)} Yoga, signifying innate rhythm with natural cycles."
            ),
            PhaladeshSection(
                "10. Jaimini Atmakaraka, Karakamsa & Arudha",
                "Atmakaraka ${ak.name} anchors the D9 Karakamsa in ${jaimini.karakamsaRashi.name}; Arudha Lagna (AL) in ${jaimini.arudhaPadas.firstOrNull()?.rashi?.name ?: "-"} shapes worldly image and status."
            ),
            PhaladeshSection(
                "11. Classical Elements, Yogas & Key Remedies",
                "Strongest Planet: ${result.summary.strongestPlanet.name}; Strongest House: H${result.summary.strongestHouse}. Key Yogas detected: ${yogas.take(4).joinToString(", ") { it.name }.ifEmpty { "Balanced planetary spread" }}. Honor Lagnesh ${lagnaLord.name} and Dasha lord ${result.currentMahadasha?.lord?.name ?: lagnaLord.name} via mantra and weekly charity."
            )
        )
    }
}

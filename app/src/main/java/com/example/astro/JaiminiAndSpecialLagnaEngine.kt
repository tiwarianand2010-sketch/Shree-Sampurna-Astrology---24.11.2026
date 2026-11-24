package com.example.astro

import com.example.core.AppLanguage
import com.example.core.AstroStrings
import com.example.model.ExtendedVargaChart
import com.example.model.JaiminiArgalaRow
import com.example.model.JaiminiArudhaPada
import com.example.model.JaiminiCharaDashaPeriod
import com.example.model.JaiminiCharaKaraka
import com.example.model.JaiminiReport
import com.example.model.KundaliResult
import com.example.model.PlanetId
import com.example.model.Rashi
import com.example.model.SpecialLagnaRow
import java.util.Calendar
import java.util.TimeZone
import kotlin.math.floor

object JaiminiAndSpecialLagnaEngine {

    private const val TROPICAL_YEAR_DAYS = 365.24219878

    // Movable signs (0,3,6,9) forward; Dual/Odd etc. per KN Rao Chara Dasha standard
    private fun isClockwiseChara(rashi: Rashi): Boolean {
        // Group A (odd-footed / clockwise): Mesha(0), Vrishabha(1), Mithuna(2), Tula(6), Vrischika(7), Dhanu(8)
        return rashi.ordinal in listOf(0, 1, 2, 6, 7, 8)
    }

    fun rashiDrishtiFrom(rashi: Rashi): List<Rashi> {
        val idx = rashi.ordinal
        return when (idx % 3) {
            0 -> { // Movable aspects all Fixed signs except adjacent (idx + 1)
                listOf(1, 4, 7, 10).filter { it != (idx + 1) % 12 }.map { Rashi.fromIndex(it) }
            }
            1 -> { // Fixed aspects all Movable signs except adjacent ((idx + 11) % 12)
                listOf(0, 3, 6, 9).filter { it != (idx + 11) % 12 }.map { Rashi.fromIndex(it) }
            }
            else -> { // Dual aspects other 3 Dual signs
                listOf(2, 5, 8, 11).filter { it != idx }.map { Rashi.fromIndex(it) }
            }
        }
    }

    fun computeJaimini(result: KundaliResult, nowMillis: Long = System.currentTimeMillis()): JaiminiReport {
        val byId = result.planets.associateBy { it.planet }

        // 1. 8 Chara Karakas (Sun..Rahu): Rahu degree reckoned from end of sign (30 - degInRashi)
        val karakaDefs = listOf(
            "AK" to "Atmakaraka (Soul / King)",
            "AmK" to "Amatyakaraka (Career / Minister)",
            "BK" to "Bhratrikaraka (Siblings / Courage)",
            "MK" to "Matrikaraka (Mother / Mind)",
            "PiK" to "Pitrikaraka (Father / Mentor)",
            "PK" to "Putrakaraka (Children / Intellect)",
            "GK" to "Gnatikaraka (Obstacles / Health)",
            "DK" to "Darakaraka (Spouse / Partner)"
        )
        val ranked8 = PlanetId.entries.take(8).sortedByDescending { p ->
            val pos = byId.getValue(p)
            if (p == PlanetId.RAHU) 30.0 - pos.degreeInRashi else pos.degreeInRashi
        }
        val charaKarakas = ranked8.mapIndexed { idx, p ->
            val pos = byId.getValue(p)
            val effDeg = if (p == PlanetId.RAHU) 30.0 - pos.degreeInRashi else pos.degreeInRashi
            JaiminiCharaKaraka(
                karakaCode = karakaDefs[idx].first,
                karakaTitle = karakaDefs[idx].second,
                planet = p,
                degInSignFormatted = TimeUtils.formatDms(effDeg),
                d1Rashi = pos.rashi,
                d9Rashi = pos.navamsaRashi
            )
        }

        val akPlanet = charaKarakas.first().planet
        val karakamsa = byId.getValue(akPlanet).navamsaRashi

        // 2. Arudha Padas A1..A12 (including AL=A1 and UL=A12) with classical Parashara exception
        fun computeArudhaForHouse(h: Int): Rashi {
            val houseSignIdx = (result.lagnaRashi.ordinal + h - 1) % 12
            val lord = Rashi.fromIndex(houseSignIdx).lord
            val lordSignIdx = byId.getValue(lord).rashi.ordinal
            val dist = ((lordSignIdx - houseSignIdx + 12) % 12)
            var arudhaIdx = (lordSignIdx + dist) % 12
            // Exception: if Arudha falls in the same house (0 steps) or 7th from it (6 steps), shift +10 (to 10th)
            val relFromHouse = (arudhaIdx - houseSignIdx + 12) % 12
            if (relFromHouse == 0 || relFromHouse == 6) {
                arudhaIdx = (arudhaIdx + 9) % 12
            }
            return Rashi.fromIndex(arudhaIdx)
        }

        val arudhas = (1..12).map { h ->
            val r = computeArudhaForHouse(h)
            val code = when (h) {
                1 -> "AL (A1)"
                12 -> "UL (A12)"
                else -> "A$h"
            }
            JaiminiArudhaPada(
                code = code,
                houseNum = h,
                rashi = r,
                houseFromLagna = ((r.ordinal - result.lagnaRashi.ordinal + 12) % 12) + 1
            )
        }
        val upapada = arudhas.last().rashi

        // 3. Chara Dasha (KN Rao scheme)
        val calNow = Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply { timeInMillis = nowMillis }
        val nowJd = TimeUtils.toJulianDay(
            calNow.get(Calendar.YEAR), calNow.get(Calendar.MONTH) + 1, calNow.get(Calendar.DAY_OF_MONTH),
            calNow.get(Calendar.HOUR_OF_DAY), calNow.get(Calendar.MINUTE), 0.0, 0.0
        )

        val ninthFromLagna = Rashi.fromIndex(result.lagnaRashi.ordinal + 8)
        val directOrder = isClockwiseChara(ninthFromLagna)
        val charaSignOrder = (0 until 12).map { step ->
            if (directOrder) Rashi.fromIndex(result.lagnaRashi.ordinal + step)
            else Rashi.fromIndex(result.lagnaRashi.ordinal - step)
        }

        fun charaYearsForSign(sign: Rashi): Int {
            val lord = sign.lord
            val lordIdx = byId.getValue(lord).rashi.ordinal
            if (lordIdx == sign.ordinal) return 12
            val forward = isClockwiseChara(sign)
            val steps = if (forward) (lordIdx - sign.ordinal + 12) % 12 else (sign.ordinal - lordIdx + 12) % 12
            return steps.coerceIn(1, 12)
        }

        var cursorJd = result.julianDay
        val charaDashas = charaSignOrder.map { sign ->
            val yrs = charaYearsForSign(sign)
            val mdStart = cursorJd
            val mdEnd = mdStart + yrs * TROPICAL_YEAR_DAYS
            val subYearsEach = yrs.toDouble() / 12.0
            val subDirect = isClockwiseChara(sign)
            var adCursor = mdStart
            val subs = (1..12).map { s ->
                val adSign = if (subDirect) Rashi.fromIndex(sign.ordinal + s) else Rashi.fromIndex(sign.ordinal - s)
                val adStart = adCursor
                val adEnd = adStart + subYearsEach * TROPICAL_YEAR_DAYS
                adCursor = adEnd
                Triple(
                    adSign,
                    TimeUtils.jdToLocalDateString(adStart, result.effectiveUtcOffsetHours),
                    TimeUtils.jdToLocalDateString(adEnd, result.effectiveUtcOffsetHours)
                )
            }
            cursorJd = mdEnd
            JaiminiCharaDashaPeriod(
                rashi = sign,
                years = yrs,
                startDateStr = TimeUtils.jdToLocalDateString(mdStart, result.effectiveUtcOffsetHours),
                endDateStr = TimeUtils.jdToLocalDateString(mdEnd, result.effectiveUtcOffsetHours),
                isCurrent = nowJd >= mdStart && nowJd < mdEnd,
                subSigns = subs
            )
        }

        // 4. Sign-based Argala & Virodhargala for houses 1, 4, 7, 10, 2, 5, 9, 11
        val keyHouses = listOf(1, 2, 4, 5, 7, 9, 10, 11)
        val argalaPairs = listOf(2 to 12, 4 to 10, 11 to 3, 5 to 9)
        val planetsInHouse = (1..12).associateWith { h ->
            result.planets.filter { it.houseFromLagna == h }.map { it.planet }
        }
        val argalas = keyHouses.map { h ->
            val sign = Rashi.fromIndex(result.lagnaRashi.ordinal + h - 1)
            val supList = mutableListOf<String>()
            val obsList = mutableListOf<String>()
            var net = 0
            for ((supOff, obsOff) in argalaPairs) {
                val supH = ((h - 1 + supOff - 1) % 12) + 1
                val obsH = ((h - 1 + obsOff - 1) % 12) + 1
                val supP = planetsInHouse.getValue(supH)
                val obsP = planetsInHouse.getValue(obsH)
                if (supP.isNotEmpty()) {
                    supList.add("H$supH(${supP.joinToString(",") { it.name.take(2) }})")
                }
                if (obsP.isNotEmpty()) {
                    obsList.add("H$obsH(${obsP.joinToString(",") { it.name.take(2) }})")
                }
                if (supP.size > obsP.size) net++
                else if (obsP.size > supP.size) net--
            }
            val verdict = when {
                net > 0 -> "Supportive Argala (+$net)"
                net < 0 -> "Obstructed ($net)"
                else -> "Balanced"
            }
            JaiminiArgalaRow(
                targetHouse = h,
                argalaSign = sign,
                argalaSources = supList.joinToString(" ").ifEmpty { "None" },
                virodhargalaObstructions = obsList.joinToString(" ").ifEmpty { "None" },
                netVerdict = verdict
            )
        }

        val currentChara = charaDashas.firstOrNull { it.isCurrent }
        val notes = listOf(
            "Atmakaraka (${akPlanet.name}) places Karakamsa in ${karakamsa.name}, highlighting the soul's spiritual and vocational north star.",
            "Upapada Lagna (UL) in ${upapada.name} (${((upapada.ordinal - result.lagnaRashi.ordinal + 12) % 12) + 1}th from Lagna) governs marriage stability and partnership grace.",
            "Lagna Rashi ${result.lagnaRashi.name} casts Jaimini Rashi Drishti onto: ${rashiDrishtiFrom(result.lagnaRashi).joinToString(", ") { it.name }}.",
            "Active Chara Mahadasha: ${currentChara?.rashi?.name ?: charaDashas.first().rashi.name} (${currentChara?.startDateStr ?: ""} to ${currentChara?.endDateStr ?: ""})."
        )

        return JaiminiReport(
            lagnaRashi = result.lagnaRashi,
            karakamsaRashi = karakamsa,
            upapadaRashi = upapada,
            lagnaRashiDrishti = rashiDrishtiFrom(result.lagnaRashi),
            charaKarakas = charaKarakas,
            arudhaPadas = arudhas,
            charaDashas = charaDashas,
            argalas = argalas,
            synthesisNotes = notes
        )
    }

    fun computeSpecialLagnas(result: KundaliResult): List<SpecialLagnaRow> {
        val inp = result.input
        val (sunriseH, _) = EphemerisEngine.sunriseAndSunsetHours(
            inp.year, inp.month, inp.day, inp.place.latitude, inp.place.longitude, result.effectiveUtcOffsetHours
        )
        val sunriseJd = TimeUtils.toJulianDay(
            inp.year, inp.month, inp.day,
            floor(sunriseH).toInt(), ((sunriseH - floor(sunriseH)) * 60.0).toInt(),
            0.0, result.effectiveUtcOffsetHours
        )
        val sunAtSunrise = EphemerisEngine.planetSiderealLongitude(PlanetId.SUN, sunriseJd)
        val localBirthH = inp.hour + inp.minute / 60.0
        val hrsSinceSunrise = ((localBirthH - sunriseH) + 24.0) % 24.0
        val ghatis = hrsSinceSunrise * 2.5
        val vighatis = ghatis * 60.0

        val moon = result.planets.first { it.planet == PlanetId.MOON }
        val rahu = result.planets.first { it.planet == PlanetId.RAHU }

        // Classical formulas (degrees)
        val bhavaLagna = EphemerisEngine.normalize360(sunAtSunrise + ghatis * 6.0)
        val horaLagna = EphemerisEngine.normalize360(sunAtSunrise + ghatis * 15.0)
        val ghatiLagna = EphemerisEngine.normalize360(sunAtSunrise + ghatis * 30.0)
        val vighatiLagna = EphemerisEngine.normalize360(result.lagnaLongitude + vighatis * 6.0)
        val shriLagna = EphemerisEngine.normalize360(result.lagnaLongitude + ((moon.siderealLongitude % (360.0 / 27.0)) / (360.0 / 27.0)) * 360.0)
        val pranapadaLagna = EphemerisEngine.normalize360(sunAtSunrise + vighatis * 2.0 + if (sunAtSunrise.toInt() / 30 % 3 == 1) 240.0 else if (sunAtSunrise.toInt() / 30 % 3 == 2) 120.0 else 0.0)
        // Varnada Lagna using Hora Lagna + Janma Lagna odd/even symmetry
        val blOdd = result.lagnaRashi.ordinal % 2 == 0
        val hlIdx = floor(horaLagna / 30.0).toInt() % 12
        val hlOdd = hlIdx % 2 == 0
        val cntLagna = if (blOdd) result.lagnaRashi.ordinal + 1 else 12 - result.lagnaRashi.ordinal
        val cntHora = if (hlOdd) hlIdx + 1 else 12 - hlIdx
        val varnadaIdx = if (blOdd == hlOdd) (cntLagna + cntHora - 1) % 12 else (kotlin.math.abs(cntLagna - cntHora) - 1 + 12) % 12
        val varnadaLagna = EphemerisEngine.normalize360(varnadaIdx * 30.0 + (result.lagnaLongitude % 30.0))

        // Indu Lagna: Kalas of 9th lord from Lagna + 9th lord from Moon
        val induKalas = mapOf(
            PlanetId.SUN to 30, PlanetId.MOON to 16, PlanetId.MARS to 6,
            PlanetId.MERCURY to 8, PlanetId.JUPITER to 10, PlanetId.VENUS to 12, PlanetId.SATURN to 1
        )
        val h9LagnaLord = Rashi.fromIndex(result.lagnaRashi.ordinal + 8).lord
        val h9MoonLord = Rashi.fromIndex(moon.rashi.ordinal + 8).lord
        val induSum = ((induKalas[h9LagnaLord] ?: 10) + (induKalas[h9MoonLord] ?: 10)) % 12
        val induRem = if (induSum == 0) 12 else induSum
        val induIdx = (moon.rashi.ordinal + induRem - 1) % 12
        val induLagna = EphemerisEngine.normalize360(induIdx * 30.0 + moon.degreeInRashi)

        // Bhrigu Bindu = exact midpoint (Moon + Rahu) / 2
        val bhriguBindu = EphemerisEngine.normalize360((moon.siderealLongitude + rahu.siderealLongitude) / 2.0)

        fun buildRow(code: String, name: String, lon: Double): SpecialLagnaRow {
            val rIdx = floor(lon / 30.0).toInt() % 12
            val deg = lon - rIdx * 30.0
            val nakIdx = floor(lon / (360.0 / 27.0)).toInt() % 27
            return SpecialLagnaRow(
                code = code,
                name = name,
                longitude = lon,
                rashi = Rashi.fromIndex(rIdx),
                dmsFormatted = TimeUtils.formatDms(deg),
                houseFromLagna = ((rIdx - result.lagnaRashi.ordinal + 12) % 12) + 1,
                nakshatraName = AstroStrings.nakshatraName(nakIdx, AppLanguage.EN)
            )
        }

        return listOf(
            buildRow("BL", "Bhava Lagna", bhavaLagna),
            buildRow("HL", "Hora Lagna (Wealth)", horaLagna),
            buildRow("GL", "Ghati Lagna (Power)", ghatiLagna),
            buildRow("VGL", "Vighati Lagna", vighatiLagna),
            buildRow("VL", "Varnada Lagna (Vocation)", varnadaLagna),
            buildRow("SL", "Shri Lagna (Prosperity)", shriLagna),
            buildRow("PL", "Pranapada Lagna", pranapadaLagna),
            buildRow("IL", "Indu Lagna (Sudden/Sustained Wealth)", induLagna),
            buildRow("BB", "Bhrigu Bindu (Destiny Point)", bhriguBindu)
        )
    }

    /**
     * Computes harmonic divisional sign D_n for harmonic values like 6, 8, 28, 50
     * alongside the classical D1..D60 tables so PDF section 11 provides all 20 charts.
     */
    private fun harmonicVargaSign(siderealLon: Double, harmonic: Int): Rashi {
        val norm = EphemerisEngine.normalize360(siderealLon)
        val signIdx = floor(norm / 30.0).toInt() % 12
        val deg = norm - signIdx * 30.0
        val part = floor(deg / (30.0 / harmonic)).toInt().coerceIn(0, harmonic - 1)
        val isOdd = (signIdx % 2 == 0)
        val start = when (harmonic) {
            6 -> if (isOdd) Rashi.MESHA.ordinal else Rashi.TULA.ordinal
            8 -> when (signIdx % 3) {
                0 -> Rashi.MESHA.ordinal
                1 -> Rashi.DHANU.ordinal
                else -> Rashi.SIMHA.ordinal
            }
            28 -> if (isOdd) Rashi.MESHA.ordinal else Rashi.MAKARA.ordinal
            else -> signIdx
        }
        return Rashi.fromIndex(start + part)
    }

    fun computeAll20DivisionalCharts(result: KundaliResult): List<ExtendedVargaChart> {
        val baseByCode = result.shodashvarga.associateBy { it.division.code }
        val orderedCodes = listOf(
            "D1", "D2", "D3", "D4", "D6", "D7", "D8", "D9", "D10", "D12",
            "D16", "D20", "D24", "D27", "D28", "D30", "D40", "D45", "D50", "D60"
        )
        val extraTitles = mapOf(
            "D6" to "Shashtamsa (D6) — Health & Obstacles",
            "D8" to "Ashtamsa (D8) — Longevity & Hidden",
            "D28" to "Bhamsa/Saptavimsamsa-28 (D28)",
            "D50" to "Panchashamsa (D50) — Subtle Karma"
        )
        return orderedCodes.map { code ->
            val existing = baseByCode[code]
            if (existing != null) {
                ExtendedVargaChart(
                    code = code,
                    title = AstroStrings.divisionTitle(existing.division, AppLanguage.EN),
                    lagnaRashi = existing.lagnaRashi,
                    planetRashis = existing.planetRashis
                )
            } else {
                val n = code.removePrefix("D").toInt()
                val lagnaR = harmonicVargaSign(result.lagnaLongitude, n)
                val map = result.planets.associate { p -> p.planet to harmonicVargaSign(p.siderealLongitude, n) }
                ExtendedVargaChart(
                    code = code,
                    title = extraTitles[code] ?: code,
                    lagnaRashi = lagnaR,
                    planetRashis = map
                )
            }
        }
    }
}

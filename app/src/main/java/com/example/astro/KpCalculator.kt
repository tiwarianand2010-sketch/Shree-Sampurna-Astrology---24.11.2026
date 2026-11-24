package com.example.astro

import com.example.model.KpAnalysis
import com.example.model.KpHouseCusp
import com.example.model.KpPlanetInfo
import com.example.model.KpRulingPlanets
import com.example.model.KpSignification
import com.example.model.PlanetId
import com.example.model.Rashi
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.atan
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.floor
import kotlin.math.sin
import kotlin.math.tan

/**
 * Krishnamurti Paddhati (KP) System Engine:
 * - KP New Ayanamsa (~Lahiri - 6 arcminutes)
 * - Placidus house cusps (siderealized)
 * - Exact 249 Sub-Lord & Sub-Sub-Lord divisions
 * - 4-Grade Significators + Ruling Planets + 1..249 Horary lookup
 */
object KpCalculator {

    private const val J2000 = 2451545.0
    private val nakSpan = 360.0 / 27.0 // 13°20'

    data class Sub249Entry(
        val number: Int,
        val startDeg: Double,
        val endDeg: Double,
        val rashi: Rashi,
        val signLord: PlanetId,
        val starLord: PlanetId,
        val subLord: PlanetId
    )

    // Precompute exact 249 KP sub-lord boundaries
    val table249: List<Sub249Entry> by lazy {
        val list = mutableListOf<Sub249Entry>()
        var num = 1
        for (nakIdx in 0 until 27) {
            val nakStart = nakIdx * nakSpan
            val starLord = PlanetId.dashaOrder[nakIdx % 9]
            val starIdx = PlanetId.dashaOrder.indexOf(starLord)
            var cursor = nakStart
            for (s in 0 until 9) {
                val subLord = PlanetId.dashaOrder[(starIdx + s) % 9]
                val subWidth = nakSpan * (PlanetId.dashaYears.getValue(subLord) / 120.0)
                val subEnd = cursor + subWidth
                // If this sub crosses a 30° sign boundary, KP splits into two entries (total = 249)
                val signStart = floor(cursor / 30.0).toInt()
                val signEnd = floor((subEnd - 1e-7) / 30.0).toInt()
                if (signEnd > signStart && signEnd * 30.0 < subEnd - 1e-7) {
                    val splitPoint = signEnd * 30.0
                    val r1 = Rashi.fromIndex(signStart)
                    list.add(Sub249Entry(num++, cursor, splitPoint, r1, r1.lord, starLord, subLord))
                    val r2 = Rashi.fromIndex(signEnd)
                    list.add(Sub249Entry(num++, splitPoint, subEnd, r2, r2.lord, starLord, subLord))
                } else {
                    val r = Rashi.fromIndex(signStart)
                    list.add(Sub249Entry(num++, cursor, subEnd, r, r.lord, starLord, subLord))
                }
                cursor = subEnd
            }
        }
        list
    }

    fun kpAyanamsa(jd: Double): Double {
        // KP New Ayanamsa at J2000 is ~23.753° (~6' less than Lahiri)
        val tYears = (jd - J2000) / 365.25
        val tCent = tYears / 100.0
        return 23.7530556 + (50.290966 / 3600.0) * tYears + 0.000111 * tCent * tCent
    }

    fun computeSignification(kpLon: Double): KpSignification {
        val lon = EphemerisEngine.normalize360(kpLon)
        val signIdx = floor(lon / 30.0).toInt() % 12
        val signLord = Rashi.fromIndex(signIdx).lord

        val nakIdx = floor(lon / nakSpan).toInt() % 27
        val starLord = PlanetId.dashaOrder[nakIdx % 9]
        val starIdx = PlanetId.dashaOrder.indexOf(starLord)

        val offsetInNak = lon - nakIdx * nakSpan
        var cursor = 0.0
        var subLord = starLord
        var subStartInNak = 0.0
        var subWidth = nakSpan
        for (s in 0 until 9) {
            val candidate = PlanetId.dashaOrder[(starIdx + s) % 9]
            val w = nakSpan * (PlanetId.dashaYears.getValue(candidate) / 120.0)
            if (offsetInNak < cursor + w || s == 8) {
                subLord = candidate
                subStartInNak = cursor
                subWidth = w
                break
            }
            cursor += w
        }

        // Sub-Sub Lord inside sub span
        val offsetInSub = (offsetInNak - subStartInNak).coerceAtLeast(0.0)
        val subStartIdx = PlanetId.dashaOrder.indexOf(subLord)
        var subSubLord = subLord
        var ssCursor = 0.0
        for (ss in 0 until 9) {
            val cand = PlanetId.dashaOrder[(subStartIdx + ss) % 9]
            val w = subWidth * (PlanetId.dashaYears.getValue(cand) / 120.0)
            if (offsetInSub < ssCursor + w || ss == 8) {
                subSubLord = cand
                break
            }
            ssCursor += w
        }

        val entry = table249.firstOrNull { lon >= it.startDeg - 1e-7 && lon < it.endDeg + 1e-7 }
            ?: table249.first()

        return KpSignification(
            signLord = signLord,
            starLord = starLord,
            subLord = subLord,
            subSubLord = subSubLord,
            subNumber249 = entry.number
        )
    }

    /**
     * Computes Placidus tropical cusps (1..12) and subtracts KP ayanamsa.
     */
    fun computePlacidusCuspsKp(jd: Double, latDeg: Double, lonDeg: Double): List<Double> {
        val ayanamsa = kpAyanamsa(jd)
        val t = (jd - J2000) / 36525.0
        val gmstDeg = EphemerisEngine.normalize360(
            280.46061837 + 360.98564736629 * (jd - J2000) +
                0.000387933 * t * t - t * t * t / 38710000.0
        )
        val ramcDeg = EphemerisEngine.normalize360(gmstDeg + lonDeg)
        val ramc = Math.toRadians(ramcDeg)
        val eps = Math.toRadians(EphemerisEngine.obliquityDeg(jd))
        val lat = Math.toRadians(latDeg.coerceIn(-62.0, 62.0))

        // 10th house (MC)
        val mcTrop = EphemerisEngine.normalize360(
            Math.toDegrees(atan2(sin(ramc), cos(ramc) * cos(eps)))
        )
        // Adjust quadrant of MC to match RAMC
        val mcAligned = alignQuadrant(mcTrop, ramcDeg)

        // 1st house (Ascendant)
        val ascY = cos(ramc)
        val ascX = -(sin(ramc) * cos(eps) + tan(lat) * sin(eps))
        val ascTrop = EphemerisEngine.normalize360(Math.toDegrees(atan2(ascY, ascX)))

        fun placidusIntermediate(houseOffsetDeg: Double, factor: Double, isDiurnal: Boolean): Double {
            var oa = Math.toRadians(EphemerisEngine.normalize360(ramcDeg + houseOffsetDeg))
            for (iter in 0 until 12) {
                val lonGuess = atan2(sin(oa), cos(oa) * cos(eps))
                val dec = kotlin.math.asin((sin(eps) * sin(lonGuess)).coerceIn(-0.999, 0.999))
                val ad = kotlin.math.asin((tan(lat) * tan(dec)).coerceIn(-0.999, 0.999))
                val semiArc = if (isDiurnal) (PI / 2.0) + ad else (PI / 2.0) - ad
                val targetOa = ramc + if (isDiurnal) semiArc * factor else (PI / 2.0) + semiArc * factor
                val newLon = Math.toDegrees(atan2(sin(targetOa), cos(targetOa) * cos(eps)))
                val raGuess = atan2(sin(Math.toRadians(newLon)) * cos(eps), cos(Math.toRadians(newLon)))
                oa = if (isDiurnal) raGuess - ad * factor else raGuess + ad * (1.0 - factor)
            }
            val finalLon = EphemerisEngine.normalize360(
                Math.toDegrees(atan2(sin(oa), cos(oa) * cos(eps)))
            )
            return alignQuadrant(finalLon, EphemerisEngine.normalize360(ramcDeg + houseOffsetDeg))
        }

        // Cusps 11, 12, 2, 3 via iterative Placidus; opposite cusps +180
        val c11 = placidusIntermediate(30.0, 1.0 / 3.0, true)
        val c12 = placidusIntermediate(60.0, 2.0 / 3.0, true)
        val c2 = placidusIntermediate(120.0, 1.0 / 3.0, false)
        val c3 = placidusIntermediate(150.0, 2.0 / 3.0, false)

        val tropCusps = listOf(
            ascTrop,                         // 1
            c2,                              // 2
            c3,                              // 3
            EphemerisEngine.normalize360(mcAligned + 180.0), // 4 (IC)
            EphemerisEngine.normalize360(c11 + 180.0),       // 5
            EphemerisEngine.normalize360(c12 + 180.0),       // 6
            EphemerisEngine.normalize360(ascTrop + 180.0),   // 7
            EphemerisEngine.normalize360(c2 + 180.0),        // 8
            EphemerisEngine.normalize360(c3 + 180.0),        // 9
            mcAligned,                       // 10 (MC)
            c11,                             // 11
            c12                              // 12
        )

        // Ensure strict monotonic progression starting from Ascendant
        val ordered = tropCusps.toMutableList()
        for (i in 1 until 12) {
            val diff = EphemerisEngine.normalize360(ordered[i] - ordered[i - 1])
            if (diff > 90.0 || diff < 3.0) {
                ordered[i] = EphemerisEngine.normalize360(ascTrop + i * 30.0)
            }
        }
        return ordered.map { EphemerisEngine.normalize360(it - ayanamsa) }
    }

    private fun alignQuadrant(computedDeg: Double, refDeg: Double): Double {
        val c = EphemerisEngine.normalize360(computedDeg)
        val r = EphemerisEngine.normalize360(refDeg)
        val diff = abs(c - r)
        return if (diff > 90.0 && diff < 270.0) EphemerisEngine.normalize360(c + 180.0) else c
    }

    fun calculate(
        jd: Double,
        latDeg: Double,
        lonDeg: Double,
        weekdayIndex: Int,
        prashnaNumber: Int? = null
    ): KpAnalysis {
        val ayanamsa = kpAyanamsa(jd)
        val cuspsLon = computePlacidusCuspsKp(jd, latDeg, lonDeg).toMutableList()

        // If KP Horary number 1..249 is provided, override Lagna cusp 1 with that number's start degree!
        if (prashnaNumber != null && prashnaNumber in 1..249) {
            val horaryEntry = table249[prashnaNumber - 1]
            val shift = horaryEntry.startDeg - cuspsLon[0]
            for (i in 0 until 12) {
                cuspsLon[i] = EphemerisEngine.normalize360(cuspsLon[i] + shift)
            }
        }

        fun houseForLongitude(lon: Double): Int {
            val p = EphemerisEngine.normalize360(lon)
            for (h in 0 until 12) {
                val start = cuspsLon[h]
                val end = cuspsLon[(h + 1) % 12]
                val inHouse = if (start < end) {
                    p >= start && p < end
                } else {
                    p >= start || p < end
                }
                if (inHouse) return h + 1
            }
            return 1
        }

        val rawPlanets = PlanetId.entries.map { p ->
            val pLon = EphemerisEngine.normalize360(EphemerisEngine.planetTropicalLongitude(p, jd) - ayanamsa)
            val house = houseForLongitude(pLon)
            val sig = computeSignification(pLon)
            Triple(p, pLon, Pair(house, sig))
        }

        // Owned houses per planet from cusp rashis (1..12)
        val cuspRashis = cuspsLon.map { Rashi.fromIndex(floor(it / 30.0).toInt() % 12) }
        val housesOwned = PlanetId.entries.associateWith { p ->
            (1..12).filter { h -> cuspRashis[h - 1].lord == p }
        }

        val planetHouseMap = rawPlanets.associate { it.first to it.third.first }

        // KP 4-grade significators per planet: houses of star lord (A/B) + houses occupied/owned by planet (C/D)
        val kpPlanets = rawPlanets.map { (p, pLon, info) ->
            val (kpHouse, sig) = info
            val starOccupied = planetHouseMap[sig.starLord]
            val starOwned = housesOwned[sig.starLord].orEmpty()
            val selfOwned = housesOwned[p].orEmpty()
            val signifies = buildList {
                if (starOccupied != null) add(starOccupied)
                addAll(starOwned)
                add(kpHouse)
                addAll(selfOwned)
            }.distinct().sorted()

            val rIdx = floor(pLon / 30.0).toInt() % 12
            KpPlanetInfo(
                planet = p,
                kpLongitude = pLon,
                rashi = Rashi.fromIndex(rIdx),
                dmsFormatted = TimeUtils.formatDms(pLon - rIdx * 30.0),
                kpHouse = kpHouse,
                kp = sig,
                signifiesHouses = signifies
            )
        }

        val kpCusps = cuspsLon.mapIndexed { idx, cLon ->
            val houseNum = idx + 1
            val rIdx = floor(cLon / 30.0).toInt() % 12
            val sig = computeSignification(cLon)
            val signifs = kpPlanets.filter { houseNum in it.signifiesHouses }.map { it.planet }
            KpHouseCusp(
                houseNumber = houseNum,
                kpLongitude = cLon,
                rashi = Rashi.fromIndex(rIdx),
                dmsFormatted = TimeUtils.formatDms(cLon - rIdx * 30.0),
                kp = sig,
                significatorPlanets = signifs
            )
        }

        // Ruling Planets at moment
        val weekdayLord = listOf(
            PlanetId.SUN, PlanetId.MOON, PlanetId.MARS, PlanetId.MERCURY,
            PlanetId.JUPITER, PlanetId.VENUS, PlanetId.SATURN
        )[((weekdayIndex % 7) + 7) % 7]
        val moonKp = kpPlanets.first { it.planet == PlanetId.MOON }.kp
        val lagnaKp = kpCusps.first().kp
        val distinct = listOf(
            weekdayLord,
            moonKp.starLord,
            moonKp.signLord,
            lagnaKp.starLord,
            lagnaKp.signLord
        ).distinct()

        return KpAnalysis(
            kpAyanamsaDeg = ayanamsa,
            cusps = kpCusps,
            planets = kpPlanets,
            rulingPlanets = KpRulingPlanets(
                dayLord = weekdayLord,
                moonStarLord = moonKp.starLord,
                moonSignLord = moonKp.signLord,
                lagnaStarLord = lagnaKp.starLord,
                lagnaSignLord = lagnaKp.signLord,
                distinctRulers = distinct
            ),
            prashnaNumber = prashnaNumber
        )
    }
}

package com.example.astro

import com.example.data.ShlokaDatabase
import com.example.model.AntardashaPeriod
import com.example.model.BhavaAnalysis
import com.example.model.BhavaBalaEntry
import com.example.model.BirthInput
import com.example.model.ChoghadiyaNature
import com.example.model.ChoghadiyaSlot
import com.example.model.DayHoroscopeEntry
import com.example.model.Dignity
import com.example.model.FunctionalNature
import com.example.model.KundaliResult
import com.example.model.KundaliSummary
import com.example.model.MahadashaPeriod
import com.example.model.PanchangInfo
import com.example.model.PlanetId
import com.example.model.PlanetPosition
import com.example.model.PlanetSummaryItem
import com.example.model.Rashi
import com.example.model.SadeSatiPhase
import com.example.model.ShadbalaEntry
import com.example.model.UpcomingTransitEntry
import java.util.Calendar
import java.util.TimeZone
import kotlin.math.abs
import kotlin.math.floor

object VedicCalculator {

    private const val TROPICAL_YEAR_DAYS = 365.24219878

    // Nakshatra lords repeat in 9-planet Ketu->Mercury sequence across 27 nakshatras
    private fun nakshatraLord(nakIdx: Int): PlanetId =
        PlanetId.dashaOrder[((nakIdx % 9) + 9) % 9]

    private val exaltedRashi = mapOf(
        PlanetId.SUN to Rashi.MESHA,
        PlanetId.MOON to Rashi.VRISHABHA,
        PlanetId.MARS to Rashi.MAKARA,
        PlanetId.MERCURY to Rashi.KANYA,
        PlanetId.JUPITER to Rashi.KARKA,
        PlanetId.VENUS to Rashi.MEENA,
        PlanetId.SATURN to Rashi.TULA,
        PlanetId.RAHU to Rashi.VRISHABHA,
        PlanetId.KETU to Rashi.VRISCHIKA
    )

    private val debilitatedRashi = mapOf(
        PlanetId.SUN to Rashi.TULA,
        PlanetId.MOON to Rashi.VRISCHIKA,
        PlanetId.MARS to Rashi.KARKA,
        PlanetId.MERCURY to Rashi.MEENA,
        PlanetId.JUPITER to Rashi.MAKARA,
        PlanetId.VENUS to Rashi.KANYA,
        PlanetId.SATURN to Rashi.MESHA,
        PlanetId.RAHU to Rashi.VRISCHIKA,
        PlanetId.KETU to Rashi.VRISHABHA
    )

    private val naturalFriends = mapOf(
        PlanetId.SUN to setOf(PlanetId.MOON, PlanetId.MARS, PlanetId.JUPITER),
        PlanetId.MOON to setOf(PlanetId.SUN, PlanetId.MERCURY),
        PlanetId.MARS to setOf(PlanetId.SUN, PlanetId.MOON, PlanetId.JUPITER),
        PlanetId.MERCURY to setOf(PlanetId.SUN, PlanetId.VENUS),
        PlanetId.JUPITER to setOf(PlanetId.SUN, PlanetId.MOON, PlanetId.MARS),
        PlanetId.VENUS to setOf(PlanetId.MERCURY, PlanetId.SATURN),
        PlanetId.SATURN to setOf(PlanetId.MERCURY, PlanetId.VENUS),
        PlanetId.RAHU to setOf(PlanetId.VENUS, PlanetId.SATURN, PlanetId.MERCURY),
        PlanetId.KETU to setOf(PlanetId.MARS, PlanetId.JUPITER)
    )

    private val naturalEnemies = mapOf(
        PlanetId.SUN to setOf(PlanetId.VENUS, PlanetId.SATURN),
        PlanetId.MOON to emptySet(),
        PlanetId.MARS to setOf(PlanetId.MERCURY),
        PlanetId.MERCURY to setOf(PlanetId.MOON),
        PlanetId.JUPITER to setOf(PlanetId.MERCURY, PlanetId.VENUS),
        PlanetId.VENUS to setOf(PlanetId.SUN, PlanetId.MOON),
        PlanetId.SATURN to setOf(PlanetId.SUN, PlanetId.MOON, PlanetId.MARS),
        PlanetId.RAHU to setOf(PlanetId.SUN, PlanetId.MOON, PlanetId.MARS),
        PlanetId.KETU to setOf(PlanetId.SUN, PlanetId.MOON)
    )

    fun calculateNavamsaRashi(siderealLongitude: Double): Rashi {
        val navamsaIdx = floor(EphemerisEngine.normalize360(siderealLongitude) / (30.0 / 9.0)).toInt() % 12
        return Rashi.fromIndex(navamsaIdx)
    }

    fun computeDignity(planet: PlanetId, rashi: Rashi, degInRashi: Double): Dignity {
        if (exaltedRashi[planet] == rashi) return Dignity.EXALTED
        if (debilitatedRashi[planet] == rashi) return Dignity.DEBILITATED
        // Mooltrikona checks
        val isMooltrikona = when (planet) {
            PlanetId.SUN -> rashi == Rashi.SIMHA && degInRashi <= 20.0
            PlanetId.MOON -> rashi == Rashi.VRISHABHA && degInRashi >= 3.0
            PlanetId.MARS -> rashi == Rashi.MESHA && degInRashi <= 12.0
            PlanetId.MERCURY -> rashi == Rashi.KANYA && degInRashi in 15.0..20.0
            PlanetId.JUPITER -> rashi == Rashi.DHANU && degInRashi <= 10.0
            PlanetId.VENUS -> rashi == Rashi.TULA && degInRashi <= 15.0
            PlanetId.SATURN -> rashi == Rashi.KUMBHA && degInRashi <= 20.0
            else -> false
        }
        if (isMooltrikona) return Dignity.MOOLTRIKONA
        if (rashi.lord == planet) return Dignity.OWN_SIGN
        val signLord = rashi.lord
        return when {
            signLord in (naturalFriends[planet] ?: emptySet()) -> Dignity.FRIEND
            signLord in (naturalEnemies[planet] ?: emptySet()) -> Dignity.ENEMY
            else -> Dignity.NEUTRAL
        }
    }

    private fun angularDist(lon1: Double, lon2: Double): Double {
        val diff = abs(EphemerisEngine.normalize360(lon1 - lon2))
        return if (diff > 180.0) 360.0 - diff else diff
    }

    private fun isCombust(planet: PlanetId, planetLon: Double, sunLon: Double, isRetro: Boolean): Boolean {
        val dist = angularDist(planetLon, sunLon)
        return when (planet) {
            PlanetId.MOON -> dist <= 12.0
            PlanetId.MARS -> dist <= 17.0
            PlanetId.MERCURY -> dist <= if (isRetro) 12.0 else 14.0
            PlanetId.JUPITER -> dist <= 11.0
            PlanetId.VENUS -> dist <= if (isRetro) 8.0 else 10.0
            PlanetId.SATURN -> dist <= 15.0
            else -> false
        }
    }

    fun calculateKundali(
        input: BirthInput,
        referenceNowUtcMillis: Long = System.currentTimeMillis(),
        prashnaNumber: Int? = null
    ): KundaliResult {
        val effectiveOffset = input.customUtcOffsetHours ?: TimeUtils.effectiveUtcOffset(
            year = input.year,
            month = input.month,
            day = input.day,
            hour = input.hour,
            baseOffset = input.place.baseUtcOffsetHours,
            dstRule = input.place.dstRule
        )

        val jd = TimeUtils.toJulianDay(
            year = input.year,
            month = input.month,
            day = input.day,
            hour = input.hour,
            minute = input.minute,
            utcOffsetHours = effectiveOffset
        )

        val ayanamsa = EphemerisEngine.lahiriAyanamsa(jd)
        val lagnaLon = EphemerisEngine.lagnaSiderealLongitude(
            jd,
            input.place.latitude,
            input.place.longitude
        )
        val lagnaRashiIdx = floor(lagnaLon / 30.0).toInt() % 12
        val lagnaRashi = Rashi.fromIndex(lagnaRashiIdx)
        val lagnaDegInRashi = lagnaLon - lagnaRashiIdx * 30.0
        val lagnaNakIdx = floor(lagnaLon / (360.0 / 27.0)).toInt() % 27
        val lagnaPada = (floor((lagnaLon % (360.0 / 27.0)) / (360.0 / 108.0)).toInt() + 1).coerceIn(1, 4)

        val sunLon = EphemerisEngine.planetSiderealLongitude(PlanetId.SUN, jd)
        val moonLon = EphemerisEngine.planetSiderealLongitude(PlanetId.MOON, jd)
        val moonRashiIdx = floor(moonLon / 30.0).toInt() % 12

        val planets = PlanetId.entries.map { p ->
            val lon = when (p) {
                PlanetId.SUN -> sunLon
                PlanetId.MOON -> moonLon
                else -> EphemerisEngine.planetSiderealLongitude(p, jd)
            }
            val lonNext = EphemerisEngine.planetSiderealLongitude(p, jd + 0.1)
            var delta = lonNext - lon
            if (delta > 180.0) delta -= 360.0
            if (delta < -180.0) delta += 360.0
            val speedPerDay = delta * 10.0

            val rashiIdx = floor(lon / 30.0).toInt() % 12
            val rashi = Rashi.fromIndex(rashiIdx)
            val degInRashi = lon - rashiIdx * 30.0
            val nakIdx = floor(lon / (360.0 / 27.0)).toInt() % 27
            val pada = (floor((lon % (360.0 / 27.0)) / (360.0 / 108.0)).toInt() + 1).coerceIn(1, 4)

            val isRetro = when (p) {
                PlanetId.SUN, PlanetId.MOON -> false
                PlanetId.RAHU, PlanetId.KETU -> true
                else -> speedPerDay < 0.0
            }

            PlanetPosition(
                planet = p,
                siderealLongitude = lon,
                rashi = rashi,
                degreeInRashi = degInRashi,
                dmsFormatted = TimeUtils.formatDms(degInRashi),
                nakshatraIndex = nakIdx,
                nakshatraPada = pada,
                nakshatraLord = nakshatraLord(nakIdx),
                houseFromLagna = ((rashiIdx - lagnaRashiIdx + 12) % 12) + 1,
                houseFromMoon = ((rashiIdx - moonRashiIdx + 12) % 12) + 1,
                navamsaRashi = calculateNavamsaRashi(lon),
                isRetrograde = isRetro,
                isCombust = isCombust(p, lon, sunLon, isRetro),
                dignity = computeDignity(p, rashi, degInRashi),
                speedDegPerDay = speedPerDay
            )
        }

        val calNow = Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply {
            timeInMillis = referenceNowUtcMillis
        }
        val nowJd = TimeUtils.toJulianDay(
            year = calNow.get(Calendar.YEAR),
            month = calNow.get(Calendar.MONTH) + 1,
            day = calNow.get(Calendar.DAY_OF_MONTH),
            hour = calNow.get(Calendar.HOUR_OF_DAY),
            minute = calNow.get(Calendar.MINUTE),
            utcOffsetHours = 0.0
        )

        val dashas = computeVimshottariDasha(
            birthJd = jd,
            moonLongitude = moonLon,
            utcOffsetHours = effectiveOffset,
            nowJd = nowJd
        )
        val currentMaha = dashas.firstOrNull { it.isCurrent }
        val currentAntar = currentMaha?.antardashas?.firstOrNull { it.isCurrent }
        val currentPratyantar = currentAntar?.pratyantardashas?.firstOrNull { it.isCurrent }

        val panchang = computePanchang(
            year = input.year,
            month = input.month,
            day = input.day,
            hour = input.hour,
            minute = input.minute,
            lat = input.place.latitude,
            lon = input.place.longitude,
            utcOffsetHours = effectiveOffset,
            sunSiderealLon = sunLon,
            moonSiderealLon = moonLon
        )

        val bhavaAnalyses = computeBhavaAnalyses(lagnaRashi, planets)
        val shadbala = computeShadbala(planets, lagnaRashiIdx, moonLon, sunLon)
        val bhavaBala = computeBhavaBala(lagnaRashi, planets, shadbala)
        val summary = computeSummary(lagnaRashi, planets, bhavaAnalyses, shadbala, bhavaBala)

        val gocharOffset = TimeUtils.effectiveUtcOffset(
            year = calNow.get(Calendar.YEAR),
            month = calNow.get(Calendar.MONTH) + 1,
            day = calNow.get(Calendar.DAY_OF_MONTH),
            hour = calNow.get(Calendar.HOUR_OF_DAY),
            baseOffset = input.place.baseUtcOffsetHours,
            dstRule = input.place.dstRule
        )
        val gocharDateStr = TimeUtils.jdToLocalDateString(nowJd, gocharOffset)
        val gocharSunLon = EphemerisEngine.planetSiderealLongitude(PlanetId.SUN, nowJd)
        val gocharPlanets = PlanetId.entries.map { p ->
            val gLon = EphemerisEngine.planetSiderealLongitude(p, nowJd)
            val gNext = EphemerisEngine.planetSiderealLongitude(p, nowJd + 0.1)
            var d = gNext - gLon
            if (d > 180.0) d -= 360.0
            if (d < -180.0) d += 360.0
            val speed = d * 10.0
            val rIdx = floor(gLon / 30.0).toInt() % 12
            val rashi = Rashi.fromIndex(rIdx)
            val deg = gLon - rIdx * 30.0
            val nIdx = floor(gLon / (360.0 / 27.0)).toInt() % 27
            val pada = (floor((gLon % (360.0 / 27.0)) / (360.0 / 108.0)).toInt() + 1).coerceIn(1, 4)
            val retro = p == PlanetId.RAHU || p == PlanetId.KETU || (p !in listOf(PlanetId.SUN, PlanetId.MOON) && speed < 0)
            PlanetPosition(
                planet = p,
                siderealLongitude = gLon,
                rashi = rashi,
                degreeInRashi = deg,
                dmsFormatted = TimeUtils.formatDms(deg),
                nakshatraIndex = nIdx,
                nakshatraPada = pada,
                nakshatraLord = nakshatraLord(nIdx),
                houseFromLagna = ((rIdx - lagnaRashiIdx + 12) % 12) + 1,
                houseFromMoon = ((rIdx - moonRashiIdx + 12) % 12) + 1,
                navamsaRashi = calculateNavamsaRashi(gLon),
                isRetrograde = retro,
                isCombust = isCombust(p, gLon, gocharSunLon, retro),
                dignity = computeDignity(p, rashi, deg),
                speedDegPerDay = speed
            )
        }

        val saturnGochar = gocharPlanets.first { it.planet == PlanetId.SATURN }
        val saturnFromMoon = saturnGochar.houseFromMoon
        val sadeSati = when (saturnFromMoon) {
            12 -> SadeSatiPhase.RISING_12TH
            1 -> SadeSatiPhase.PEAK_1ST
            2 -> SadeSatiPhase.SETTING_2ND
            4 -> SadeSatiPhase.DHAIYA_4TH
            8 -> SadeSatiPhase.DHAIYA_8TH
            else -> SadeSatiPhase.NONE
        }

        val birthMoonNak = floor(moonLon / (360.0 / 27.0)).toInt() % 27
        val threeDayHoroscope = (0..2).map { offset ->
            val targetJd = nowJd + offset
            val mLon = EphemerisEngine.planetSiderealLongitude(PlanetId.MOON, targetJd)
            val mRashiIdx = floor(mLon / 30.0).toInt() % 12
            val mNakIdx = floor(mLon / (360.0 / 27.0)).toInt() % 27
            val houseFromNatalMoon = ((mRashiIdx - moonRashiIdx + 12) % 12) + 1
            val chandraGood = houseFromNatalMoon !in listOf(4, 8, 12)
            val countNak = ((mNakIdx - birthMoonNak + 27) % 27) + 1
            val tara = ((countNak - 1) % 9) + 1
            val taraGood = tara !in listOf(3, 5, 7)
            DayHoroscopeEntry(
                dayOffset = offset,
                dateStr = TimeUtils.jdToLocalDateString(targetJd, gocharOffset),
                moonRashi = Rashi.fromIndex(mRashiIdx),
                moonNakshatraIndex = mNakIdx,
                chandraHouseFromBirthMoon = houseFromNatalMoon,
                isChandraBalaGood = chandraGood,
                taraBalaNumber = tara,
                isTaraBalaGood = taraGood
            )
        }

        val upcoming1Year = scanUpcomingSignChanges(nowJd, gocharOffset, maxDays = 365)
        val upcoming30Days = upcoming1Year.filter { it.daysFromNow <= 30 }

        val shodashvarga = ShodashvargaEngine.computeAllShodashvarga(lagnaLon, planets)
        val ashtakavarga = AshtakavargaEngine.calculate(lagnaRashi, planets)
        val kpAnalysis = KpCalculator.calculate(
            jd = jd,
            latDeg = input.place.latitude,
            lonDeg = input.place.longitude,
            weekdayIndex = panchang.weekdayIndex,
            prashnaNumber = prashnaNumber
        )
        val lalKitab = LalKitabAndStoreEngine.calculateLalKitab(planets)
        val westernChart = WesternNumerologyVastuEngine.calculateWestern(
            jd = jd,
            latDeg = input.place.latitude,
            lonDeg = input.place.longitude
        )
        val (numerology, vastuFengShui) = WesternNumerologyVastuEngine.calculateNumerologyAndVastu(
            name = input.name,
            day = input.day,
            month = input.month,
            year = input.year
        )

        return KundaliResult(
            input = input,
            effectiveUtcOffsetHours = effectiveOffset,
            julianDay = jd,
            ayanamsaDeg = ayanamsa,
            lagnaLongitude = lagnaLon,
            lagnaRashi = lagnaRashi,
            lagnaDmsFormatted = TimeUtils.formatDms(lagnaDegInRashi),
            lagnaNakshatraIndex = lagnaNakIdx,
            lagnaPada = lagnaPada,
            planets = planets,
            mahadashas = dashas,
            currentMahadasha = currentMaha,
            currentAntardasha = currentAntar,
            currentPratyantardasha = currentPratyantar,
            panchang = panchang,
            bhavaAnalyses = bhavaAnalyses,
            summary = summary,
            shadbala = shadbala,
            bhavaBala = bhavaBala,
            gocharDateStr = gocharDateStr,
            gocharPlanets = gocharPlanets,
            sadeSatiPhase = sadeSati,
            saturnHouseFromMoon = saturnFromMoon,
            threeDayHoroscope = threeDayHoroscope,
            upcomingTransits30Days = upcoming30Days,
            upcomingTransits1Year = upcoming1Year,
            shodashvarga = shodashvarga,
            ashtakavarga = ashtakavarga,
            kpAnalysis = kpAnalysis,
            lalKitab = lalKitab,
            westernChart = westernChart,
            numerology = numerology,
            vastuFengShui = vastuFengShui
        )
    }

    private fun computeVimshottariDasha(
        birthJd: Double,
        moonLongitude: Double,
        utcOffsetHours: Double,
        nowJd: Double
    ): List<MahadashaPeriod> {
        val nakSpan = 360.0 / 27.0
        val nakIdx = floor(moonLongitude / nakSpan).toInt() % 27
        val posInNak = moonLongitude - nakIdx * nakSpan
        val remainingFraction = 1.0 - (posInNak / nakSpan)

        val startLord = nakshatraLord(nakIdx)
        val startOrderIdx = PlanetId.dashaOrder.indexOf(startLord)

        val firstYears = PlanetId.dashaYears.getValue(startLord)
        val elapsedYearsAtBirth = firstYears * (1.0 - remainingFraction)
        var cursorJd = birthJd - elapsedYearsAtBirth * TROPICAL_YEAR_DAYS

        val result = mutableListOf<MahadashaPeriod>()
        for (i in 0 until 9) {
            val lord = PlanetId.dashaOrder[(startOrderIdx + i) % 9]
            val mahaYears = PlanetId.dashaYears.getValue(lord)
            val mahaStartJd = cursorJd
            val mahaEndJd = mahaStartJd + mahaYears * TROPICAL_YEAR_DAYS

            val antars = mutableListOf<AntardashaPeriod>()
            val mahaIdx = PlanetId.dashaOrder.indexOf(lord)
            var antarCursor = mahaStartJd
            for (j in 0 until 9) {
                val antarLord = PlanetId.dashaOrder[(mahaIdx + j) % 9]
                val antarYears = mahaYears * PlanetId.dashaYears.getValue(antarLord) / 120.0
                val antarStart = antarCursor
                val antarEnd = antarStart + antarYears * TROPICAL_YEAR_DAYS

                val pratyantars = mutableListOf<com.example.model.PratyantardashaPeriod>()
                val antarIdx = PlanetId.dashaOrder.indexOf(antarLord)
                var pratCursor = antarStart
                for (k in 0 until 9) {
                    val pratLord = PlanetId.dashaOrder[(antarIdx + k) % 9]
                    val pratYears = antarYears * PlanetId.dashaYears.getValue(pratLord) / 120.0
                    val pratStart = pratCursor
                    val pratEnd = pratStart + pratYears * TROPICAL_YEAR_DAYS
                    pratyantars.add(
                        com.example.model.PratyantardashaPeriod(
                            lord = pratLord,
                            antardashaLord = antarLord,
                            mahadashaLord = lord,
                            startJd = pratStart,
                            endJd = pratEnd,
                            startDateStr = TimeUtils.jdToLocalDateString(pratStart, utcOffsetHours),
                            endDateStr = TimeUtils.jdToLocalDateString(pratEnd, utcOffsetHours),
                            isCurrent = nowJd >= pratStart && nowJd < pratEnd
                        )
                    )
                    pratCursor = pratEnd
                }

                antars.add(
                    AntardashaPeriod(
                        lord = antarLord,
                        mahadashaLord = lord,
                        startJd = antarStart,
                        endJd = antarEnd,
                        startDateStr = TimeUtils.jdToLocalDateString(antarStart, utcOffsetHours),
                        endDateStr = TimeUtils.jdToLocalDateString(antarEnd, utcOffsetHours),
                        isCurrent = nowJd >= antarStart && nowJd < antarEnd,
                        pratyantardashas = pratyantars
                    )
                )
                antarCursor = antarEnd
            }

            result.add(
                MahadashaPeriod(
                    lord = lord,
                    startJd = mahaStartJd,
                    endJd = mahaEndJd,
                    startDateStr = TimeUtils.jdToLocalDateString(mahaStartJd, utcOffsetHours),
                    endDateStr = TimeUtils.jdToLocalDateString(mahaEndJd, utcOffsetHours),
                    isCurrent = nowJd >= mahaStartJd && nowJd < mahaEndJd,
                    antardashas = antars
                )
            )
            cursorJd = mahaEndJd
        }
        return result
    }

    private fun computePanchang(
        year: Int,
        month: Int,
        day: Int,
        hour: Int,
        minute: Int,
        lat: Double,
        lon: Double,
        utcOffsetHours: Double,
        sunSiderealLon: Double,
        moonSiderealLon: Double
    ): PanchangInfo {
        val weekday = TimeUtils.dayOfWeek(year, month, day)
        val diff = EphemerisEngine.normalize360(moonSiderealLon - sunSiderealLon)
        val tithiNumber = (floor(diff / 12.0).toInt() + 1).coerceIn(1, 30)
        val tithiNameIdx = (tithiNumber - 1) % 15
        val isShukla = tithiNumber <= 15
        val nakIdx = floor(moonSiderealLon / (360.0 / 27.0)).toInt() % 27
        val yogaIdx = floor(EphemerisEngine.normalize360(moonSiderealLon + sunSiderealLon) / (360.0 / 27.0)).toInt() % 27

        val halfTithiIdx = floor(diff / 6.0).toInt() // 0..59
        val karanaIdx = when {
            halfTithiIdx == 0 -> 10 // Kimstughna
            halfTithiIdx >= 57 -> 7 + (halfTithiIdx - 57) // Shakuni, Chatushpada, Naga
            else -> (halfTithiIdx - 1) % 7 // Bava .. Vishti
        }

        val (sunriseH, sunsetH) = EphemerisEngine.sunriseAndSunsetHours(year, month, day, lat, lon, utcOffsetHours)
        val dayLen = (sunsetH - sunriseH).coerceAtLeast(1.0)
        val nightLen = (24.0 - dayLen).coerceAtLeast(1.0)
        val segDay = dayLen / 8.0
        val segNight = nightLen / 8.0

        // Rahukaal slot (1-based: Sun=8, Mon=2, Tue=7, Wed=5, Thu=6, Fri=4, Sat=3)
        val rahuSlot = intArrayOf(8, 2, 7, 5, 6, 4, 3)[weekday] - 1
        val rahuStart = sunriseH + rahuSlot * segDay
        val rahuEnd = rahuStart + segDay

        // Abhijit Muhurat (8th of 15 daytime muhurtas, centered on solar noon)
        val noonH = (sunriseH + sunsetH) / 2.0
        val muhurtaHalf = dayLen / 30.0
        val abhijitStart = noonH - muhurtaHalf
        val abhijitEnd = noonH + muhurtaHalf

        // Day Choghadiya starting nature index per weekday
        // Sequence: Udveg, Char, Labh, Amrit, Kaal, Shubh, Rog (indices 0..6)
        val daySeq = listOf(
            ChoghadiyaNature.UDVEG, ChoghadiyaNature.CHAR, ChoghadiyaNature.LABH,
            ChoghadiyaNature.AMRIT, ChoghadiyaNature.KAAL, ChoghadiyaNature.SHUBH, ChoghadiyaNature.ROG
        )
        val dayStartOffset = intArrayOf(0, 3, 6, 2, 5, 1, 4)[weekday]
        val dayChoghadiya = (0 until 8).map { idx ->
            val nature = daySeq[(dayStartOffset + idx) % 7]
            val st = sunriseH + idx * segDay
            ChoghadiyaSlot(
                nature = nature,
                startTimeStr = TimeUtils.formatHoursToHhMm(st),
                endTimeStr = TimeUtils.formatHoursToHhMm(st + segDay),
                isGood = nature in listOf(ChoghadiyaNature.AMRIT, ChoghadiyaNature.SHUBH, ChoghadiyaNature.LABH, ChoghadiyaNature.CHAR)
            )
        }

        val nightSeq = listOf(
            ChoghadiyaNature.SHUBH, ChoghadiyaNature.AMRIT, ChoghadiyaNature.CHAR,
            ChoghadiyaNature.ROG, ChoghadiyaNature.KAAL, ChoghadiyaNature.LABH, ChoghadiyaNature.UDVEG
        )
        val nightStartOffset = intArrayOf(0, 2, 4, 6, 1, 3, 5)[weekday]
        val nightChoghadiya = (0 until 8).map { idx ->
            val nature = nightSeq[(nightStartOffset + idx) % 7]
            val st = sunsetH + idx * segNight
            ChoghadiyaSlot(
                nature = nature,
                startTimeStr = TimeUtils.formatHoursToHhMm(st),
                endTimeStr = TimeUtils.formatHoursToHhMm(st + segNight),
                isGood = nature in listOf(ChoghadiyaNature.AMRIT, ChoghadiyaNature.SHUBH, ChoghadiyaNature.LABH, ChoghadiyaNature.CHAR)
            )
        }

        // Hora (Chaldean order starting with weekday ruler at sunrise)
        val chaldean = listOf(
            PlanetId.SUN, PlanetId.VENUS, PlanetId.MERCURY, PlanetId.MOON,
            PlanetId.SATURN, PlanetId.JUPITER, PlanetId.MARS
        )
        val weekdayRuler = listOf(
            PlanetId.SUN, PlanetId.MOON, PlanetId.MARS, PlanetId.MERCURY,
            PlanetId.JUPITER, PlanetId.VENUS, PlanetId.SATURN
        )[weekday]
        val startChaldeanIdx = chaldean.indexOf(weekdayRuler)
        val localHour = hour + minute / 60.0
        val hoursSinceSunrise = ((localHour - sunriseH) + 24.0) % 24.0
        val horaIdx = if (localHour >= sunriseH && localHour < sunsetH) {
            floor((localHour - sunriseH) / (dayLen / 12.0)).toInt().coerceIn(0, 11)
        } else {
            val nightElapsed = if (localHour >= sunsetH) localHour - sunsetH else (24.0 - sunsetH) + localHour
            12 + floor(nightElapsed / (nightLen / 12.0)).toInt().coerceIn(0, 11)
        }
        val currentHora = chaldean[(startChaldeanIdx + horaIdx) % 7]

        return PanchangInfo(
            weekdayIndex = weekday,
            tithiNumber = tithiNumber,
            tithiNameIndex = tithiNameIdx,
            isShuklaPaksha = isShukla,
            nakshatraIndex = nakIdx,
            yogaIndex = yogaIdx,
            karanaIndex = karanaIdx,
            sunriseStr = TimeUtils.formatHoursToHhMm(sunriseH),
            sunsetStr = TimeUtils.formatHoursToHhMm(sunsetH),
            rahukaalStr = "${TimeUtils.formatHoursToHhMm(rahuStart)} – ${TimeUtils.formatHoursToHhMm(rahuEnd)}",
            abhijitStr = "${TimeUtils.formatHoursToHhMm(abhijitStart)} – ${TimeUtils.formatHoursToHhMm(abhijitEnd)}",
            dayChoghadiya = dayChoghadiya,
            nightChoghadiya = nightChoghadiya,
            currentHoraLord = currentHora
        )
    }

    private fun aspectsFromPlanet(planet: PlanetId, fromHouse: Int): Set<Int> {
        val offsets = when (planet) {
            PlanetId.MARS -> listOf(4, 7, 8)
            PlanetId.JUPITER -> listOf(5, 7, 9)
            PlanetId.SATURN -> listOf(3, 7, 10)
            PlanetId.RAHU, PlanetId.KETU -> listOf(5, 7, 9)
            else -> listOf(7)
        }
        return offsets.map { offset -> ((fromHouse - 1 + offset - 1) % 12) + 1 }.toSet()
    }

    private fun computeBhavaAnalyses(
        lagnaRashi: Rashi,
        planets: List<PlanetPosition>
    ): List<BhavaAnalysis> {
        val planetById = planets.associateBy { it.planet }
        return (1..12).map { house ->
            val sign = Rashi.fromIndex(lagnaRashi.ordinal + house - 1)
            val lord = sign.lord
            val lordHouse = planetById.getValue(lord).houseFromLagna
            val occupants = planets.filter { it.houseFromLagna == house }.map { it.planet }
            val aspecting = planets.filter { p ->
                p.houseFromLagna != house && house in aspectsFromPlanet(p.planet, p.houseFromLagna)
            }.map { it.planet }

            val yutiHi = mutableListOf<String>()
            val yutiEn = mutableListOf<String>()
            val yutiZh = mutableListOf<String>()
            if (occupants.size >= 2) {
                yutiHi.add("युति योग: ${occupants.joinToString(" + ") { com.example.core.AstroStrings.planetName(it, com.example.core.AppLanguage.HI) }}")
                yutiEn.add("Conjunction: ${occupants.joinToString(" + ") { com.example.core.AstroStrings.planetName(it, com.example.core.AppLanguage.EN) }}")
                yutiZh.add("合相: ${occupants.joinToString(" + ") { com.example.core.AstroStrings.planetName(it, com.example.core.AppLanguage.ZH) }}")
            }

            val shlokas = ShlokaDatabase.matchBhavaShlokas(house, lord, lordHouse, occupants, sign)
            BhavaAnalysis(
                houseNumber = house,
                signRashi = sign,
                lord = lord,
                lordPlacedInHouse = lordHouse,
                occupants = occupants,
                aspectingPlanets = aspecting,
                conjunctionDescriptionsHi = yutiHi,
                conjunctionDescriptionsEn = yutiEn,
                conjunctionDescriptionsZh = yutiZh,
                matchedShlokas = shlokas
            )
        }
    }

    private fun computeShadbala(
        planets: List<PlanetPosition>,
        lagnaRashiIdx: Int,
        moonLon: Double,
        sunLon: Double
    ): List<ShadbalaEntry> {
        val sevenPlanets = listOf(
            PlanetId.SUN, PlanetId.MOON, PlanetId.MARS,
            PlanetId.MERCURY, PlanetId.JUPITER, PlanetId.VENUS, PlanetId.SATURN
        )
        val naisargikaMap = mapOf(
            PlanetId.SUN to 60.0,
            PlanetId.MOON to 51.43,
            PlanetId.VENUS to 42.86,
            PlanetId.JUPITER to 34.29,
            PlanetId.MERCURY to 25.71,
            PlanetId.MARS to 17.14,
            PlanetId.SATURN to 8.57
        )
        val debilitationDeg = mapOf(
            PlanetId.SUN to 190.0,
            PlanetId.MOON to 213.0,
            PlanetId.MARS to 118.0,
            PlanetId.MERCURY to 345.0,
            PlanetId.JUPITER to 275.0,
            PlanetId.VENUS to 177.0,
            PlanetId.SATURN to 20.0
        )
        val digPeakHouse = mapOf(
            PlanetId.SUN to 10,
            PlanetId.MARS to 10,
            PlanetId.JUPITER to 1,
            PlanetId.MERCURY to 1,
            PlanetId.MOON to 4,
            PlanetId.VENUS to 4,
            PlanetId.SATURN to 7
        )

        val moonSunDiff = EphemerisEngine.normalize360(moonLon - sunLon)
        val pakshaFactor = if (moonSunDiff <= 180.0) moonSunDiff / 180.0 else (360.0 - moonSunDiff) / 180.0

        val posMap = planets.associateBy { it.planet }
        return sevenPlanets.map { p ->
            val pos = posMap.getValue(p)
            // Uchcha / Sthana Bala (0..60)
            val deb = debilitationDeg.getValue(p)
            val distFromDeb = angularDist(pos.siderealLongitude, deb)
            val uchchaBala = (distFromDeb / 180.0) * 60.0
            val ownBoost = when (pos.dignity) {
                Dignity.EXALTED -> 30.0
                Dignity.MOOLTRIKONA -> 25.0
                Dignity.OWN_SIGN -> 20.0
                Dignity.FRIEND -> 12.0
                else -> 5.0
            }
            val sthana = uchchaBala + ownBoost

            // Dig Bala (0..60)
            val peakH = digPeakHouse.getValue(p)
            val houseDiff = abs(pos.houseFromLagna - peakH).let { if (it > 6) 12 - it else it }
            val dig = ((6 - houseDiff) / 6.0) * 60.0

            // Kendradi Bala (Kendra=60, Panapara=30, Apoklima=15)
            val kendradi = when (pos.houseFromLagna) {
                1, 4, 7, 10 -> 60.0
                2, 5, 8, 11 -> 30.0
                else -> 15.0
            }

            val naisargika = naisargikaMap.getValue(p)
            val paksha = when (p) {
                PlanetId.MOON, PlanetId.JUPITER, PlanetId.VENUS, PlanetId.MERCURY -> pakshaFactor * 60.0
                else -> (1.0 - pakshaFactor) * 60.0
            }
            val total = sthana + dig + kendradi + naisargika + paksha
            val norm = (total / 320.0).toFloat().coerceIn(0.15f, 1.0f)

            ShadbalaEntry(
                planet = p,
                sthanaBala = sthana,
                digBala = dig,
                kendradiBala = kendradi,
                naisargikaBala = naisargika,
                pakshaBala = paksha,
                totalScore = total,
                normalizedStrength = norm
            )
        }
    }

    private fun computeBhavaBala(
        lagnaRashi: Rashi,
        planets: List<PlanetPosition>,
        shadbala: List<ShadbalaEntry>
    ): List<BhavaBalaEntry> {
        val shadbalaMap = shadbala.associateBy { it.planet }
        val naturalBenefics = setOf(PlanetId.JUPITER, PlanetId.VENUS, PlanetId.MOON, PlanetId.MERCURY)
        return (1..12).map { house ->
            val sign = Rashi.fromIndex(lagnaRashi.ordinal + house - 1)
            val lord = sign.lord
            val adhipati = (shadbalaMap[lord]?.totalScore ?: 120.0) * 0.65
            val dig = when (house) {
                1, 4, 7, 10 -> 55.0
                5, 9 -> 45.0
                2, 11 -> 35.0
                else -> 20.0
            }
            var drishti = 0.0
            for (p in planets) {
                val aspecting = (p.houseFromLagna == house) || (house in aspectsFromPlanet(p.planet, p.houseFromLagna))
                if (aspecting) {
                    drishti += if (p.planet in naturalBenefics) 28.0 else -12.0
                }
            }
            val total = (adhipati + dig + drishti).coerceAtLeast(30.0)
            BhavaBalaEntry(
                houseNumber = house,
                adhipatiBala = adhipati,
                digBala = dig,
                drishtiBala = drishti,
                totalScore = total,
                normalizedStrength = (total / 290.0).toFloat().coerceIn(0.15f, 1.0f)
            )
        }
    }

    private fun computeSummary(
        lagnaRashi: Rashi,
        planets: List<PlanetPosition>,
        bhavas: List<BhavaAnalysis>,
        shadbala: List<ShadbalaEntry>,
        bhavaBala: List<BhavaBalaEntry>
    ): KundaliSummary {
        val seven = listOf(
            PlanetId.SUN, PlanetId.MOON, PlanetId.MARS,
            PlanetId.MERCURY, PlanetId.JUPITER, PlanetId.VENUS, PlanetId.SATURN
        )
        val housesOwnedByPlanet = seven.associateWith { p ->
            (1..12).filter { h -> Rashi.fromIndex(lagnaRashi.ordinal + h - 1).lord == p }
        }
        val posMap = planets.associateBy { it.planet }

        val kendras = setOf(1, 4, 7, 10)
        val trikonas = setOf(1, 5, 9)
        val trik = setOf(6, 8, 12)

        val summaries = seven.map { p ->
            val owned = housesOwnedByPlanet.getValue(p)
            val ownsKendra = owned.any { it in kendras && it != 1 }
            val ownsTrikona = owned.any { it in trikonas }
            val ownsTrik = owned.any { it in trik }
            val isMaraka = owned.any { it == 2 || it == 7 }

            val nature = when {
                p == lagnaRashi.lord -> FunctionalNature.BENEFIC
                ownsKendra && ownsTrikona -> FunctionalNature.YOGAKARAKA
                ownsTrikona && !ownsTrik -> FunctionalNature.BENEFIC
                ownsTrik && !ownsTrikona -> FunctionalNature.MALEFIC
                else -> FunctionalNature.NEUTRAL
            }
            PlanetSummaryItem(
                planet = p,
                functionalNature = nature,
                isMarakesh = isMaraka,
                housesOwned = owned,
                placedInHouse = posMap.getValue(p).houseFromLagna
            )
        }

        val marakesh = summaries.filter { it.isMarakesh }.map { it.planet }
        val marakaOccupants = planets.filter { it.houseFromLagna == 2 || it.houseFromLagna == 7 }.map { it.planet }
        val strongestP = shadbala.maxByOrNull { it.totalScore }?.planet ?: lagnaRashi.lord
        val weakestP = shadbala.minByOrNull { it.totalScore }?.planet ?: PlanetId.SATURN
        val strongestH = bhavaBala.maxByOrNull { it.totalScore }?.houseNumber ?: 1
        val weakestH = bhavaBala.minByOrNull { it.totalScore }?.houseNumber ?: 12

        val matchedCount = bhavas.sumOf { it.matchedShlokas.size }

        return KundaliSummary(
            lagnaRashi = lagnaRashi,
            lagnesh = lagnaRashi.lord,
            planetSummaries = summaries,
            marakeshPlanets = marakesh,
            marakaHouseOccupants = marakaOccupants,
            strongestPlanet = strongestP,
            weakestPlanet = weakestP,
            strongestHouse = strongestH,
            weakestHouse = weakestH,
            matchedConditionsCount = matchedCount,
            totalConditionsCount = 12
        )
    }

    private fun scanUpcomingSignChanges(
        startJd: Double,
        utcOffsetHours: Double,
        maxDays: Int
    ): List<UpcomingTransitEntry> {
        val planetsToTrack = listOf(
            PlanetId.SUN, PlanetId.MARS, PlanetId.MERCURY,
            PlanetId.JUPITER, PlanetId.VENUS, PlanetId.SATURN, PlanetId.RAHU
        )
        val prevRashis = planetsToTrack.associateWith { p ->
            val lon = EphemerisEngine.planetSiderealLongitude(p, startJd)
            Rashi.fromIndex(floor(lon / 30.0).toInt() % 12)
        }.toMutableMap()

        val events = mutableListOf<UpcomingTransitEntry>()
        for (d in 1..maxDays) {
            val jd = startJd + d
            for (p in planetsToTrack) {
                val lon = EphemerisEngine.planetSiderealLongitude(p, jd)
                val newRashi = Rashi.fromIndex(floor(lon / 30.0).toInt() % 12)
                val prev = prevRashis.getValue(p)
                if (newRashi != prev) {
                    events.add(
                        UpcomingTransitEntry(
                            planet = p,
                            fromRashi = prev,
                            toRashi = newRashi,
                            changeDateStr = TimeUtils.jdToLocalDateString(jd, utcOffsetHours),
                            daysFromNow = d
                        )
                    )
                    prevRashis[p] = newRashi
                }
            }
        }
        return events
    }
}

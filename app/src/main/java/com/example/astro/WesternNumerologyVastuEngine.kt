package com.example.astro

import com.example.model.KootScore
import com.example.model.KundaliResult
import com.example.model.ManglikCheck
import com.example.model.MatchingResult
import com.example.model.NumerologyResult
import com.example.model.PlanetId
import com.example.model.Rashi
import com.example.model.VastuFengShuiResult
import com.example.model.WesternAspect
import com.example.model.WesternChartResult
import com.example.model.WesternPlanet
import kotlin.math.abs
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.floor
import kotlin.math.sin
import kotlin.math.tan

object WesternNumerologyVastuEngine {

    private fun reduceDigit(n: Int): Int {
        var cur = abs(n)
        while (cur > 9) {
            cur = cur.toString().sumOf { it - '0' }
        }
        return if (cur == 0) 9 else cur
    }

    fun calculateWestern(
        jd: Double,
        latDeg: Double,
        lonDeg: Double
    ): WesternChartResult {
        val t = (jd - 2451545.0) / 36525.0
        val gmstDeg = EphemerisEngine.normalize360(
            280.46061837 + 360.98564736629 * (jd - 2451545.0) +
                0.000387933 * t * t - t * t * t / 38710000.0
        )
        val ramcDeg = EphemerisEngine.normalize360(gmstDeg + lonDeg)
        val ramc = Math.toRadians(ramcDeg)
        val eps = Math.toRadians(EphemerisEngine.obliquityDeg(jd))
        val lat = Math.toRadians(latDeg.coerceIn(-66.0, 66.0))

        val ascTrop = EphemerisEngine.normalize360(
            Math.toDegrees(atan2(cos(ramc), -(sin(ramc) * cos(eps) + tan(lat) * sin(eps))))
        )
        val mcRaw = EphemerisEngine.normalize360(
            Math.toDegrees(atan2(sin(ramc), cos(ramc) * cos(eps)))
        )
        val mcTrop = if (abs(mcRaw - ramcDeg).let { it > 90.0 && it < 270.0 }) {
            EphemerisEngine.normalize360(mcRaw + 180.0)
        } else mcRaw

        val elements = listOf("Fire", "Earth", "Air", "Water")
        val modalities = listOf("Cardinal", "Fixed", "Mutable")

        val westernPlanets = PlanetId.entries.map { p ->
            val lon = EphemerisEngine.planetTropicalLongitude(p, jd)
            val signIdx = floor(lon / 30.0).toInt() % 12
            val deg = lon - signIdx * 30.0
            WesternPlanet(
                planet = p,
                tropicalLongitude = lon,
                rashi = Rashi.fromIndex(signIdx),
                degreeInSign = deg,
                dmsFormatted = TimeUtils.formatDms(deg),
                element = elements[signIdx % 4],
                modality = modalities[signIdx % 3]
            )
        }

        val majorPlanets = westernPlanets.take(7) // Sun..Saturn
        val aspectDefs = listOf(
            Triple("Conjunction (0°)", 0.0, true),
            Triple("Sextile (60°)", 60.0, true),
            Triple("Square (90°)", 90.0, false),
            Triple("Trine (120°)", 120.0, true),
            Triple("Opposition (180°)", 180.0, false)
        )
        val aspects = mutableListOf<WesternAspect>()
        for (i in majorPlanets.indices) {
            for (j in i + 1 until majorPlanets.size) {
                val p1 = majorPlanets[i]
                val p2 = majorPlanets[j]
                val rawDiff = abs(p1.tropicalLongitude - p2.tropicalLongitude)
                val angle = if (rawDiff > 180.0) 360.0 - rawDiff else rawDiff
                for ((name, target, harm) in aspectDefs) {
                    val orb = abs(angle - target)
                    if (orb <= 6.5) {
                        aspects.add(WesternAspect(p1.planet, p2.planet, name, target, orb, harm))
                        break
                    }
                }
            }
        }

        return WesternChartResult(
            ascendantLon = ascTrop,
            ascendantSign = Rashi.fromIndex(floor(ascTrop / 30.0).toInt() % 12),
            midheavenLon = mcTrop,
            midheavenSign = Rashi.fromIndex(floor(mcTrop / 30.0).toInt() % 12),
            sunSign = westernPlanets.first { it.planet == PlanetId.SUN }.rashi,
            moonSign = westernPlanets.first { it.planet == PlanetId.MOON }.rashi,
            planets = westernPlanets,
            aspects = aspects,
            fireCount = westernPlanets.count { it.element == "Fire" },
            earthCount = westernPlanets.count { it.element == "Earth" },
            airCount = westernPlanets.count { it.element == "Air" },
            waterCount = westernPlanets.count { it.element == "Water" }
        )
    }

    private val chaldeanMap = mapOf(
        'A' to 1, 'I' to 1, 'J' to 1, 'Q' to 1, 'Y' to 1,
        'B' to 2, 'K' to 2, 'R' to 2,
        'C' to 3, 'G' to 3, 'L' to 3, 'S' to 3,
        'D' to 4, 'M' to 4, 'T' to 4,
        'E' to 5, 'H' to 5, 'N' to 5, 'X' to 5,
        'U' to 6, 'V' to 6, 'W' to 6,
        'O' to 7, 'Z' to 7,
        'F' to 8, 'P' to 8
    )

    fun calculateNumerologyAndVastu(name: String, day: Int, month: Int, year: Int): Pair<NumerologyResult, VastuFengShuiResult> {
        val mulank = reduceDigit(day)
        val digitsAll = "%02d%02d%04d".format(day, month, year)
        val bhagyank = reduceDigit(digitsAll.sumOf { it - '0' })

        // Lo Shu grid counts (excluding 0)
        val loShuCounts = (1..9).associateWith { d ->
            digitsAll.count { (it - '0') == d }
        }.toMutableMap()
        loShuCounts[mulank] = loShuCounts.getValue(mulank) + 1
        loShuCounts[bhagyank] = loShuCounts.getValue(bhagyank) + 1
        val missing = (1..9).filter { loShuCounts.getValue(it) == 0 }

        val cleanName = name.uppercase().filter { it in 'A'..'Z' }
        val chaldeanSum = cleanName.sumOf { chaldeanMap[it] ?: 0 }
        val chaldeanNum = if (chaldeanSum > 0) reduceDigit(chaldeanSum) else mulank
        val pythagoreanSum = cleanName.sumOf { ((it - 'A') % 9) + 1 }
        val pythagoreanNum = if (pythagoreanSum > 0) reduceDigit(pythagoreanSum) else bhagyank

        // Feng Shui Kua Number (Male-standard year digit sum; East group 1,3,4,9; West group 2,6,7,8)
        val yearSum = reduceDigit(year.toString().sumOf { it - '0' })
        var kua = 11 - yearSum
        if (kua > 9) kua = reduceDigit(kua)
        if (kua == 5) kua = 2

        val colorsHi = when (mulank) {
            1 -> "सुनहरा, नारंगी"
            2 -> "सफेद, हल्का हरा"
            3 -> "पीला, केसरिया"
            4 -> "नीला, खाकी"
            5 -> "हरा, हल्का भूरा"
            6 -> "सफेद, हल्का गुलाबी"
            7 -> "हल्का पीला, धुंधला सफेद"
            8 -> "गहरा नीला, बैंगनी"
            else -> "लाल, सिंदूरी"
        }
        val colorsEn = when (mulank) {
            1 -> "Gold, Orange"
            2 -> "White, Mint Green"
            3 -> "Yellow, Saffron"
            4 -> "Sky Blue, Khaki"
            5 -> "Emerald Green, Beige"
            6 -> "Pure White, Rose"
            7 -> "Cream, Soft Yellow"
            8 -> "Deep Blue, Indigo"
            else -> "Crimson, Coral Red"
        }
        val daysHi = when (mulank) {
            1 -> "रविवार, सोमवार"
            2 -> "सोमवार, शुक्रवार"
            3 -> "गुरुवार, मंगलवार"
            4 -> "रविवार, शनिवार"
            5 -> "बुधवार, शुक्रवार"
            6 -> "शुक्रवार, बुधवार"
            7 -> "सोमवार, गुरुवार"
            8 -> "शनिवार, बुधवार"
            else -> "मंगलवार, गुरुवार"
        }
        val daysEn = when (mulank) {
            1 -> "Sunday, Monday"
            2 -> "Monday, Friday"
            3 -> "Thursday, Tuesday"
            4 -> "Sunday, Saturday"
            5 -> "Wednesday, Friday"
            6 -> "Friday, Wednesday"
            7 -> "Monday, Thursday"
            8 -> "Saturday, Wednesday"
            else -> "Tuesday, Thursday"
        }
        val luckyNums = listOf(mulank, bhagyank, reduceDigit(mulank + bhagyank)).distinct()

        val numerology = NumerologyResult(
            name = name,
            mulank = mulank,
            bhagyank = bhagyank,
            kuaNumber = kua,
            chaldeanNameNumber = chaldeanNum,
            pythagoreanDestiny = pythagoreanNum,
            loShuCounts = loShuCounts,
            missingNumbers = missing,
            luckyColorsHi = colorsHi,
            luckyColorsEn = colorsEn,
            luckyDaysHi = daysHi,
            luckyDaysEn = daysEn,
            luckyNumbers = luckyNums
        )

        val isEastGroup = kua in listOf(1, 3, 4, 9)
        val vastus = VastuFengShuiResult(
            kuaNumber = kua,
            kuaGroupEn = if (isEastGroup) "East Group (Kua $kua)" else "West Group (Kua $kua)",
            kuaGroupHi = if (isEastGroup) "पूर्व समूह (कुआ $kua)" else "पश्चिम समूह (कुआ $kua)",
            wealthDirectionEn = if (isEastGroup) "South-East & North" else "North-West & West",
            wealthDirectionHi = if (isEastGroup) "आग्नेय (दक्षिण-पूर्व) एवं उत्तर" else "वायव्य (उत्तर-पश्चिम) एवं पश्चिम",
            entranceAdviceEn = if (isEastGroup) "East, North, or South-East facing main door" else "West, North-West, or North-East main door",
            entranceAdviceHi = if (isEastGroup) "पूर्व, उत्तर या आग्नेय मुख्य द्वार श्रेष्ठ" else "पश्चिम, वायव्य या ईशान मुख्य द्वार श्रेष्ठ",
            kitchenDirectionEn = "South-East (Agni Kon) facing East while cooking",
            kitchenDirectionHi = "आग्नेय कोण (दक्षिण-पूर्व), पूर्व मुख कर भोजन निर्माण",
            bedroomDirectionEn = if (isEastGroup) "South-West / South with head towards East or South" else "South-West / North-West with head towards South or West",
            bedroomDirectionHi = if (isEastGroup) "नैऋत्य (दक्षिण-पश्चिम), पूर्व या दक्षिण सिरहाना" else "नैऋत्य/वायव्य, दक्षिण या पश्चिम सिरहाना",
            pujaDirectionEn = "North-East (Ishaan Kon) clear of heavy storage",
            pujaDirectionHi = "ईशान कोण (उत्तर-पूर्व), स्वच्छ एवं हल्का रखें",
            elementTipsEn = buildList {
                if (4 in missing) add("Add lush green indoor Bamboo / Wood element in East (missing #4 in Lo Shu).")
                if (1 in missing) add("Place a small clean water bowl / North fountain to activate Career (#1).")
                if (6 in missing) add("Keep a brass windchime or metal bell in North-West (#6 Mentor energy).")
                add("Keep center Brahmasthan bright, ventilated, and completely uncluttered.")
            },
            elementTipsHi = buildList {
                if (4 in missing) add("लो-शू में 4 अनुपस्थित: पूर्व दिशा में हरे पौधे या बांस रखें।")
                if (1 in missing) add("लो-शू में 1 अनुपस्थित: उत्तर दिशा में स्वच्छ जल कलश रखें।")
                if (6 in missing) add("लो-शू में 6 अनुपस्थित: उत्तर-पश्चिम में पीतल की घंटी लगाएँ।")
                add("घर का मध्य भाग (ब्रह्मस्थान) खुला, उजाला व भार-रहित रखें।")
            }
        )
        return numerology to vastus
    }

    // --- Ashtakoot Kundali Matching (36 Gunas) + Manglik Check ---
    // Sign groups for Varna (1=Brahmin[Cancer,Scorpio,Pisces], 2=Kshatriya[Aries,Leo,Sag], 3=Vaishya[Taurus,Virgo,Cap], 4=Shudra[Gem,Lib,Aq])
    private val rashiVarnaRank = intArrayOf(3, 2, 1, 4, 3, 2, 1, 4, 3, 2, 1, 4) // 4=Brahmin..1=Shudra
    private val rashiVashya = intArrayOf(0, 0, 1, 2, 3, 1, 1, 4, 0, 2, 1, 2) // 0=Chatushpada,1=Manava,2=Jalachara,3=Vanachara,4=Keeta

    // 27 Nakshatra Yonis (0..13)
    private val nakYoni = intArrayOf(
        0, 1, 2, 3, 3, 4, 5, 2, 5, 6, 6, 7, 8, 9, 8, 9, 10, 10, 4, 11, 12, 11, 13, 0, 13, 7, 1
    )
    private val yoniEnemies = setOf(
        0 to 8, 8 to 0, 1 to 13, 13 to 1, 2 to 11, 11 to 2, 3 to 12, 12 to 3,
        4 to 10, 10 to 4, 5 to 6, 6 to 5, 7 to 9, 9 to 7
    )
    // 27 Nakshatra Ganas: 0=Deva, 1=Manushya, 2=Rakshasa
    private val nakGana = intArrayOf(
        0, 1, 2, 1, 0, 1, 0, 0, 2, 2, 1, 1, 0, 2, 0, 2, 0, 2, 2, 1, 1, 0, 2, 2, 1, 1, 0
    )
    // 27 Nakshatra Nadis: 0=Adi, 1=Madhya, 2=Antya
    private val nakNadi = intArrayOf(
        0, 1, 2, 2, 1, 0, 0, 1, 2, 2, 1, 0, 0, 1, 2, 2, 1, 0, 0, 1, 2, 2, 1, 0, 0, 1, 2
    )

    private val friendlyLords = mapOf(
        PlanetId.SUN to setOf(PlanetId.MOON, PlanetId.MARS, PlanetId.JUPITER),
        PlanetId.MOON to setOf(PlanetId.SUN, PlanetId.MERCURY),
        PlanetId.MARS to setOf(PlanetId.SUN, PlanetId.MOON, PlanetId.JUPITER),
        PlanetId.MERCURY to setOf(PlanetId.SUN, PlanetId.VENUS),
        PlanetId.JUPITER to setOf(PlanetId.SUN, PlanetId.MOON, PlanetId.MARS),
        PlanetId.VENUS to setOf(PlanetId.MERCURY, PlanetId.SATURN),
        PlanetId.SATURN to setOf(PlanetId.MERCURY, PlanetId.VENUS)
    )
    private val enemyLords = mapOf(
        PlanetId.SUN to setOf(PlanetId.VENUS, PlanetId.SATURN),
        PlanetId.MOON to emptySet(),
        PlanetId.MARS to setOf(PlanetId.MERCURY),
        PlanetId.MERCURY to setOf(PlanetId.MOON),
        PlanetId.JUPITER to setOf(PlanetId.MERCURY, PlanetId.VENUS),
        PlanetId.VENUS to setOf(PlanetId.SUN, PlanetId.MOON),
        PlanetId.SATURN to setOf(PlanetId.SUN, PlanetId.MOON, PlanetId.MARS)
    )

    fun calculateMatching(boy: KundaliResult, girl: KundaliResult): MatchingResult {
        val boyMoon = boy.planets.first { it.planet == PlanetId.MOON }
        val girlMoon = girl.planets.first { it.planet == PlanetId.MOON }

        val bR = boyMoon.rashi
        val gR = girlMoon.rashi
        val bN = boyMoon.nakshatraIndex
        val gN = girlMoon.nakshatraIndex

        // 1. Varna (1 pt)
        val bVarna = rashiVarnaRank[bR.ordinal]
        val gVarna = rashiVarnaRank[gR.ordinal]
        val varnaPts = if (bVarna >= gVarna) 1.0 else 0.0

        // 2. Vashya (2 pts)
        val bVash = rashiVashya[bR.ordinal]
        val gVash = rashiVashya[gR.ordinal]
        val vashyaPts = when {
            bVash == gVash -> 2.0
            (bVash == 1 && gVash == 0) || (bVash == 0 && gVash == 1) -> 1.0
            else -> 0.5
        }

        // 3. Tara (3 pts)
        val t1 = (((gN - bN + 27) % 27) % 9) + 1
        val t2 = (((bN - gN + 27) % 27) % 9) + 1
        val badTara = setOf(3, 5, 7)
        val taraPts = when {
            t1 !in badTara && t2 !in badTara -> 3.0
            t1 !in badTara || t2 !in badTara -> 1.5
            else -> 0.0
        }

        // 4. Yoni (4 pts)
        val bYoni = nakYoni[bN]
        val gYoni = nakYoni[gN]
        val yoniPts = when {
            bYoni == gYoni -> 4.0
            (bYoni to gYoni) in yoniEnemies -> 0.0
            else -> 2.5
        }

        // 5. Graha Maitri (5 pts)
        val bLord = bR.lord
        val gLord = gR.lord
        fun rel(a: PlanetId, b: PlanetId): Int = when {
            a == b -> 2
            b in (friendlyLords[a] ?: emptySet()) -> 1
            b in (enemyLords[a] ?: emptySet()) -> -1
            else -> 0
        }
        val sumRel = rel(bLord, gLord) + rel(gLord, bLord)
        val maitriPts = when {
            bLord == gLord -> 5.0
            sumRel >= 2 -> 5.0
            sumRel == 1 -> 4.0
            sumRel == 0 -> 3.0
            sumRel == -1 -> 1.0
            else -> 0.5
        }

        // 6. Gana (6 pts)
        val bGana = nakGana[bN]
        val gGana = nakGana[gN]
        val ganaNamesHi = listOf("देव", "मनुष्य", "राक्षस")
        val ganaNamesEn = listOf("Deva", "Manushya", "Rakshasa")
        val ganaPts = when {
            bGana == gGana -> 6.0
            (bGana == 0 && gGana == 1) || (bGana == 1 && gGana == 0) -> 5.0
            (bGana == 2 && gGana == 0) || (bGana == 0 && gGana == 2) -> 1.0
            else -> 0.0
        }

        // 7. Bhakoot (7 pts)
        val distBG = ((gR.ordinal - bR.ordinal + 12) % 12) + 1
        val badBhakoot = distBG in listOf(2, 12, 5, 9, 6, 8)
        val bhakootException = (bLord == gLord) || (maitriPts >= 4.0)
        val bhakootPts = if (!badBhakoot || bhakootException) 7.0 else 0.0

        // 8. Nadi (8 pts)
        val bNadi = nakNadi[bN]
        val gNadi = nakNadi[gN]
        val nadiNamesHi = listOf("आदि", "मध्य", "अंत्य")
        val nadiNamesEn = listOf("Adi", "Madhya", "Antya")
        val nadiException = (bR == gR && bN != gN) || (bN == gN && boyMoon.nakshatraPada != girlMoon.nakshatraPada)
        val nadiPts = when {
            bNadi != gNadi -> 8.0
            nadiException -> 4.0 // Partial cancellation noted
            else -> 0.0
        }

        val koots = listOf(
            KootScore("Varna", "वर्ण", 1.0, varnaPts, "Rank $bVarna", "Rank $gVarna", "Work/temperament harmony", "कार्य-स्वभाव सामंजस्य"),
            KootScore("Vashya", "वश्य", 2.0, vashyaPts, "Grp $bVash", "Grp $gVash", "Mutual attraction & balance", "परस्पर आकर्षण एवं नियंत्रण"),
            KootScore("Tara", "तारा", 3.0, taraPts, "T-$t1", "T-$t2", "Destiny & wellbeing synergy", "भाग्य एवं स्वास्थ्य संयोग"),
            KootScore("Yoni", "योनि", 4.0, yoniPts, "Y-$bYoni", "Y-$gYoni", "Physical & mental compatibility", "प्रकृति एवं मानसिक अनुकूलता"),
            KootScore("Graha Maitri", "ग्रह मैत्री", 5.0, maitriPts, bLord.name, gLord.name, "Planetary friendship of Moon lords", "राशि स्वामियों की मैत्री"),
            KootScore("Gana", "गण", 6.0, ganaPts, ganaNamesEn[bGana], ganaNamesEn[gGana], "Nature (${ganaNamesHi[bGana]} - ${ganaNamesHi[gGana]})", "गण मिलान (${ganaNamesHi[bGana]} - ${ganaNamesHi[gGana]})"),
            KootScore("Bhakoot", "भकूट", 7.0, bhakootPts, bR.name, gR.name, if (badBhakoot && bhakootException) "Dosha cancelled by Maitri" else "Emotional & family growth", if (badBhakoot && bhakootException) "मित्रत्व से भकूट परिहार" else "पारिवारिक व आर्थिक समृद्धि"),
            KootScore("Nadi", "नाड़ी", 8.0, nadiPts, nadiNamesEn[bNadi], nadiNamesEn[gNadi], if (bNadi == gNadi && nadiException) "Pada/Rashi exception applies" else "Health & progeny vitality", if (bNadi == gNadi && nadiException) "चरण/राशि भेद से आंशिक परिहार" else "स्वास्थ्य एवं संतान बल")
        )

        val total = koots.sumOf { it.obtainedPoints }
        val boyManglik = checkManglik(boy)
        val girlManglik = checkManglik(girl)

        val verdictEn = when {
            total >= 25.0 -> "Excellent Match (${"%.1f".format(total)}/36) — Strong long-term harmony."
            total >= 18.0 -> "Recommended Match (${"%.1f".format(total)}/36) — Meets minimum 18-Guna threshold."
            else -> "Low Score (${"%.1f".format(total)}/36) — Consult an astrologer before finalizing."
        }
        val verdictHi = when {
            total >= 25.0 -> "उत्तम गुण मिलान (${"%.1f".format(total)}/36) — शुभ एवं सुखी दांपत्य योग।"
            total >= 18.0 -> "मांगलिक मिलान सहित स्वीकार्य (${"%.1f".format(total)}/36) — १८+ गुण प्राप्त।"
            else -> "न्यून गुण (${"%.1f".format(total)}/36) — निर्णय से पूर्व ज्योतिषी परामर्श अवश्य लें।"
        }

        return MatchingResult(boy, girl, koots, total, 36.0, boyManglik, girlManglik, verdictEn, verdictHi)
    }

    private fun checkManglik(chart: KundaliResult): ManglikCheck {
        val mars = chart.planets.first { it.planet == PlanetId.MARS }
        val jup = chart.planets.first { it.planet == PlanetId.JUPITER }
        val manglikHouses = setOf(1, 2, 4, 7, 8, 12)
        val lagnaMatch = if (mars.houseFromLagna in manglikHouses) listOf(mars.houseFromLagna) else emptyList()
        val moonMatch = if (mars.houseFromMoon in manglikHouses) listOf(mars.houseFromMoon) else emptyList()
        val isManglik = lagnaMatch.isNotEmpty() || moonMatch.isNotEmpty()
        val cancelled = isManglik && (
            mars.rashi == Rashi.MESHA || mars.rashi == Rashi.VRISCHIKA || mars.rashi == Rashi.MAKARA ||
                abs(mars.houseFromLagna - jup.houseFromLagna).let { it == 0 || it == 4 || it == 6 || it == 8 }
            )
        val summaryEn = when {
            !isManglik -> "Non-Manglik (Mars H${mars.houseFromLagna})"
            cancelled -> "Anshik/Cancelled Manglik (Mars H${mars.houseFromLagna}, own/Jupiter aspect)"
            else -> "Manglik Dosha active (Mars Lagna H${mars.houseFromLagna}, Moon H${mars.houseFromMoon})"
        }
        val summaryHi = when {
            !isManglik -> "अमांगलिक (मंगल भाव ${mars.houseFromLagna})"
            cancelled -> "आंशिक/परिहार मांगलिक (मंगल भाव ${mars.houseFromLagna}, स्व/गुरु प्रभाव)"
            else -> "मांगलिक योग प्रभावी (लग्न से ${mars.houseFromLagna}, चंद्र से ${mars.houseFromMoon})"
        }
        return ManglikCheck(isManglik, cancelled, lagnaMatch, moonMatch, summaryEn, summaryHi)
    }
}

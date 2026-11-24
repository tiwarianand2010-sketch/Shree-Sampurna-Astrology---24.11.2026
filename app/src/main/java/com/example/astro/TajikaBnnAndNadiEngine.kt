package com.example.astro

import com.example.core.AppLanguage
import com.example.core.AstroStrings
import com.example.model.BnnDirectionRow
import com.example.model.BnnReport
import com.example.model.ComprehensiveReportBundle
import com.example.model.CrystalAndRemedyReport
import com.example.model.Dignity
import com.example.model.FunctionalNature
import com.example.model.GemstoneReport
import com.example.model.KpExtendedReport
import com.example.model.KpFourStepRow
import com.example.model.KpScorecardItem
import com.example.model.KundaliResult
import com.example.model.LalKitabExtendedReport
import com.example.model.LalKitabVarshphalRow
import com.example.model.LoShuExtendedReport
import com.example.model.NadiLifeAreaRow
import com.example.model.NadiPlanetDetail
import com.example.model.NadiReport
import com.example.model.PlanetId
import com.example.model.Rashi
import com.example.model.TajikaVarshaphalReport
import com.example.model.VedicNumerologyReport
import java.util.Calendar
import java.util.TimeZone
import kotlin.math.abs
import kotlin.math.floor

object TajikaBnnAndNadiEngine {

    private fun reduceDigit(n: Int): Int {
        var cur = abs(n)
        while (cur > 9) cur = cur.toString().sumOf { it - '0' }
        return if (cur == 0) 9 else cur
    }

    fun computeNadiReport(result: KundaliResult): NadiReport {
        val kp = result.kpAnalysis
        val mdLord = result.currentMahadasha?.lord ?: result.lagnaRashi.lord
        val adLord = result.currentAntardasha?.lord ?: mdLord
        val pdLord = result.currentPratyantardasha?.lord ?: adLord

        val rows = result.planets.map { pos ->
            val kpInfo = kp?.planets?.firstOrNull { it.planet == pos.planet }
            val starLord = kpInfo?.kp?.starLord ?: pos.nakshatraLord
            val subLord = kpInfo?.kp?.subLord ?: pos.nakshatraLord
            val subSubLord = kpInfo?.kp?.subSubLord ?: pos.nakshatraLord
            // Praana / 4th level derived from sub-sub progress
            val praanaIdx = (PlanetId.dashaOrder.indexOf(subSubLord) + pos.nakshatraPada - 1) % 9
            val praanaLord = PlanetId.dashaOrder[praanaIdx]
            val hits = kpInfo?.signifiesHouses ?: listOf(pos.houseFromLagna)
            val hasGood = hits.any { it in listOf(2, 5, 9, 10, 11) }
            val hasTrik = hits.any { it in listOf(6, 8, 12) }
            val quality = when {
                hasGood && !hasTrik -> "Highly Supportive"
                hasGood && hasTrik -> "Dynamic / Effort-Reward"
                hasTrik -> "Caution / Obstacles"
                else -> "Neutral / Steady"
            }
            NadiPlanetDetail(pos.planet, starLord, subLord, subSubLord, praanaLord, hits, quality)
        }

        val activeHouses = rows.filter { it.planet in setOf(mdLord, adLord, pdLord) }
            .flatMap { it.hitHouses }.toSet()

        fun scoreArea(area: String, positive: List<Int>, negative: List<Int>): NadiLifeAreaRow {
            val posHits = positive.filter { it in activeHouses }
            val negHits = negative.filter { it in activeHouses }
            val pct = (52 + posHits.size * 15 - negHits.size * 8).coerceIn(28, 96)
            val dir = when {
                pct >= 72 -> "Upward Expansion"
                pct >= 55 -> "Steady Progress"
                else -> "Disciplined Consolidation"
            }
            val act = posHits.ifEmpty { positive.take(2) }.joinToString(",") { "H$it" }
            val ins = "Running ${mdLord.name}-${adLord.name}-${pdLord.name} triggers $act ($dir)."
            return NadiLifeAreaRow(area, dir, pct, act, ins)
        }

        val areas = listOf(
            scoreArea("Career & Authority", listOf(2, 6, 10, 11), listOf(8, 12)),
            scoreArea("Wealth & Cashflow", listOf(2, 6, 11), listOf(8, 12)),
            scoreArea("Business & Enterprise", listOf(2, 7, 10, 11), listOf(6, 12)),
            scoreArea("Marriage & Partnership", listOf(2, 7, 11), listOf(1, 6, 10)),
            scoreArea("Education & Wisdom", listOf(4, 5, 9, 11), listOf(8, 12)),
            scoreArea("Property & Assets", listOf(4, 11, 12), listOf(3, 8)),
            scoreArea("Health & Vitality", listOf(1, 5, 11), listOf(6, 8, 12)),
            scoreArea("Foreign / Long Travel", listOf(3, 9, 12), listOf(4, 6))
        )

        return NadiReport(
            rows = rows,
            dashaLifeAreas = areas,
            activeChainSummary = "Active Nadi Chain: ${mdLord.name} (MD) -> ${adLord.name} (AD) -> ${pdLord.name} (PD) activates houses ${activeHouses.sorted().joinToString(", ") { "H$it" }}"
        )
    }

    fun computeKpExtended(result: KundaliResult): KpExtendedReport {
        val kp = result.kpAnalysis
        val pMap = kp?.planets?.associateBy { it.planet } ?: emptyMap()

        val fourStepRows = PlanetId.entries.map { pl ->
            val info = pMap[pl]
            val selfH = info?.let { listOf(it.kpHouse) } ?: listOf(1)
            val starH = info?.let { pMap[it.kp.starLord]?.signifiesHouses } ?: emptyList()
            val subH = info?.let { pMap[it.kp.subLord]?.signifiesHouses } ?: emptyList()
            val subStarH = info?.let {
                val sl = it.kp.subLord
                val ssl = pMap[sl]?.kp?.starLord
                pMap[ssl]?.signifiesHouses ?: emptyList()
            } ?: emptyList()
            val sixFold = (selfH + starH + subH + subStarH).distinct().sorted()
            KpFourStepRow(pl, selfH, starH, subH, subStarH, sixFold.joinToString(",") { "H$it" })
        }

        // Part of Fortune: Day = Asc + Moon - Sun; Night = Asc + Sun - Moon
        val sun = result.planets.first { it.planet == PlanetId.SUN }
        val moon = result.planets.first { it.planet == PlanetId.MOON }
        val isDay = sun.houseFromLagna in 7..12
        val pofLon = if (isDay) {
            EphemerisEngine.normalize360(result.lagnaLongitude + moon.siderealLongitude - sun.siderealLongitude)
        } else {
            EphemerisEngine.normalize360(result.lagnaLongitude + sun.siderealLongitude - moon.siderealLongitude)
        }
        val pofRashi = Rashi.fromIndex(floor(pofLon / 30.0).toInt() % 12)
        val pofHouse = ((pofRashi.ordinal - result.lagnaRashi.ordinal + 12) % 12) + 1

        // Current Transit Ruling Planets
        val calNow = Calendar.getInstance()
        val nowDow = calNow.get(Calendar.DAY_OF_WEEK) - 1
        val currentKp = KpCalculator.calculate(
            jd = TimeUtils.toJulianDay(
                calNow.get(Calendar.YEAR), calNow.get(Calendar.MONTH) + 1, calNow.get(Calendar.DAY_OF_MONTH),
                calNow.get(Calendar.HOUR_OF_DAY), calNow.get(Calendar.MINUTE), 0.0, result.effectiveUtcOffsetHours
            ),
            latDeg = result.input.place.latitude,
            lonDeg = result.input.place.longitude,
            weekdayIndex = nowDow
        )

        val transitBrief = result.gocharPlanets.map { gp ->
            val starL = pMap[gp.planet]?.kp?.starLord ?: gp.nakshatraLord
            "${gp.planet.name}: ${gp.rashi.name} ${gp.dmsFormatted} (Natal H${gp.houseFromLagna}, Star: ${starL.name})"
        }
        val cuspAlignments = kp?.cusps?.take(6)?.map { c ->
            "Cusp ${c.houseNumber} (${c.rashi.name}, Sub ${c.kp.subLord.name}): Signified by ${c.significatorPlanets.joinToString(",") { it.name }.ifEmpty { c.kp.signLord.name }}"
        } ?: emptyList()

        fun buildScore(area: String, cuspNum: Int, req: List<Int>): KpScorecardItem {
            val cusp = kp?.cusps?.firstOrNull { it.houseNumber == cuspNum }
            val subLord = cusp?.kp?.subLord ?: result.lagnaRashi.lord
            val subSig = pMap[subLord]?.signifiesHouses ?: emptyList()
            val matches = req.count { it in subSig }
            val pct = (45 + matches * 18).coerceIn(35, 96)
            val status = when {
                pct >= 75 -> "Strongly Promised"
                pct >= 58 -> "Supported"
                else -> "Requires Effort"
            }
            val timing = "${subLord.name} / ${kp?.rulingPlanets?.distinctRulers?.firstOrNull()?.name ?: subLord.name} Dasha-Transit window"
            return KpScorecardItem(area, cuspNum, req.joinToString(","), subLord, pct, status, timing)
        }

        val scorecards = listOf(
            buildScore("Career", 10, listOf(2, 6, 10, 11)),
            buildScore("Education", 4, listOf(4, 5, 9, 11)),
            buildScore("Job / Service", 6, listOf(2, 6, 10, 11)),
            buildScore("Business", 7, listOf(2, 7, 10, 11)),
            buildScore("Wealth", 2, listOf(2, 6, 11)),
            buildScore("Promotion", 11, listOf(2, 6, 10, 11)),
            buildScore("Marriage", 7, listOf(2, 7, 11)),
            buildScore("Love Marriage", 5, listOf(5, 7, 11)),
            buildScore("Children", 5, listOf(2, 5, 11)),
            buildScore("Property", 4, listOf(4, 11, 12)),
            buildScore("Foreign Travel", 9, listOf(3, 9, 12)),
            buildScore("Litigation Win", 6, listOf(6, 10, 11))
        )

        val strongAreas = scorecards.filter { it.scorePct >= 75 }.joinToString(", ") { it.area }.ifEmpty { "Steady Balanced Progress" }
        val summary = "KP Analysis highlights strong cusp sub-lord promise for: $strongAreas. Current Ruling Planets (${currentKp.rulingPlanets.distinctRulers.joinToString(",") { it.name }}) corroborate timing triggers."

        return KpExtendedReport(
            fourStepRows = fourStepRows,
            partOfFortuneLon = pofLon,
            partOfFortuneRashi = pofRashi,
            partOfFortuneHouse = pofHouse,
            currentRulingPlanets = currentKp.rulingPlanets,
            transitPlanetsBrief = transitBrief,
            transitCuspAlignments = cuspAlignments,
            predictionSummary = summary,
            scorecards = scorecards
        )
    }

    fun computeGemstoneReport(result: KundaliResult): GemstoneReport {
        val gemNames = mapOf(
            PlanetId.SUN to "Natural Ruby (माणिक्य)",
            PlanetId.MOON to "Natural Pearl (मोती)",
            PlanetId.MARS to "Red Coral (मूंगा)",
            PlanetId.MERCURY to "Emerald (पन्ना)",
            PlanetId.JUPITER to "Yellow Sapphire (पुखराज)",
            PlanetId.VENUS to "Diamond / White Opal (हीरा/ओपल)",
            PlanetId.SATURN to "Blue Sapphire (नीलम)"
        )
        val wearNotes = mapOf(
            PlanetId.SUN to "Ring Finger • Gold/Copper • Sunday • Om Ghrini Suryaya Namah",
            PlanetId.MOON to "Little Finger • Silver • Monday • Om Som Somaya Namah",
            PlanetId.MARS to "Ring Finger • Copper/Gold • Tuesday • Om Ang Angarakaya Namah",
            PlanetId.MERCURY to "Little Finger • Gold • Wednesday • Om Bum Budhaya Namah",
            PlanetId.JUPITER to "Index Finger • Gold • Thursday • Om Brim Brihaspataye Namah",
            PlanetId.VENUS to "Ring/Middle Finger • Platinum/Silver • Friday • Om Shum Shukraya Namah",
            PlanetId.SATURN to "Middle Finger • Silver/Panchdhatu • Saturday • Om Sham Shanicharaya Namah"
        )

        val lagnaLord = result.lagnaRashi.lord
        val h5Lord = Rashi.fromIndex(result.lagnaRashi.ordinal + 4).lord
        val h9Lord = Rashi.fromIndex(result.lagnaRashi.ordinal + 8).lord
        val trikonaLords = listOf(lagnaLord, h9Lord, h5Lord).distinct()
        val sumMap = result.summary.planetSummaries.associateBy { it.planet }

        val primaryP = trikonaLords.first()
        val secondaryP = trikonaLords.getOrElse(1) {
            result.summary.planetSummaries.firstOrNull { it.functionalNature == FunctionalNature.YOGAKARAKA }?.planet ?: h5Lord
        }
        val extraP = trikonaLords.getOrElse(2) { h5Lord }

        val avoids = result.summary.planetSummaries
            .filter { it.isMarakesh || it.functionalNature == FunctionalNature.MALEFIC }
            .filter { it.planet != lagnaLord }
            .mapNotNull { gemNames[it.planet]?.let { g -> "Avoid $g (${it.planet.name} — Marakesh/Trik lord)" } }
            .distinct()

        return GemstoneReport(
            primaryGem = gemNames[primaryP] ?: "Yellow Sapphire",
            primaryPlanet = primaryP,
            primaryScore = 92,
            secondaryGem = gemNames[secondaryP] ?: "Emerald",
            secondaryPlanet = secondaryP,
            secondaryScore = 84,
            additionalGem = gemNames[extraP] ?: "Pearl",
            fingerMetalDayMantra = wearNotes[primaryP] ?: "Index Finger • Gold • Thursday",
            avoidGems = avoids.ifEmpty { listOf("Consult astrologer before wearing Blue Sapphire (Saturn) or Hessonite (Rahu).") },
            extendedOptions = listOf(
                "Uparatna alternatives: Red Garnet (Ruby), Moonstone (Pearl), Peridot (Emerald), Citrine/Sunela (Yellow Sapphire), White Zircon (Diamond).",
                "Purify in raw milk + Gangajal before Shukla Paksha sunrise wearing."
            )
        )
    }

    fun computeVedicNumerologyAndLoShu(result: KundaliResult): Pair<VedicNumerologyReport, LoShuExtendedReport> {
        val base = result.numerology
        val mulank = base?.mulank ?: reduceDigit(result.input.day)
        val bhagyank = base?.bhagyank ?: 9
        val dashaPlanetNum = when (result.currentMahadasha?.lord) {
            PlanetId.SUN -> 1; PlanetId.MOON -> 2; PlanetId.JUPITER -> 3; PlanetId.RAHU -> 4
            PlanetId.MERCURY -> 5; PlanetId.VENUS -> 6; PlanetId.KETU -> 7; PlanetId.SATURN -> 8
            else -> 9
        }
        val counts = (base?.loShuCounts ?: emptyMap()).toMutableMap()
        counts[dashaPlanetNum] = (counts[dashaPlanetNum] ?: 0) + 1

        val activePlanes = mutableListOf<String>()
        val weaknessPlanes = mutableListOf<String>()
        val planeDefs = listOf(
            "Mental Plane (4-9-2)" to listOf(4, 9, 2),
            "Emotional Plane (3-5-7)" to listOf(3, 5, 7),
            "Practical Plane (8-1-6)" to listOf(8, 1, 6),
            "Thought Plane (4-3-8)" to listOf(4, 3, 8),
            "Willpower Plane (9-5-1)" to listOf(9, 5, 1),
            "Action Plane (2-7-6)" to listOf(2, 7, 6),
            "Golden Rajyoga (4-5-6)" to listOf(4, 5, 6),
            "Silver Rajyoga (2-5-8)" to listOf(2, 5, 8)
        )
        planeDefs.forEach { (label, digits) ->
            if (digits.all { (counts[it] ?: 0) > 0 }) activePlanes.add(label)
            else if (digits.count { (counts[it] ?: 0) == 0 } >= 2) weaknessPlanes.add("Partial $label")
        }

        val yogas = activePlanes.ifEmpty { listOf("Balanced Single-Digit Supports (${mulank}, ${bhagyank})") }
        val missing = (1..9).filter { (counts[it] ?: 0) == 0 }
        val missingRemedies = missing.map { d ->
            when (d) {
                1 -> "Missing 1 (Sun): Offer water to Sun, keep North sector bright."
                2 -> "Missing 2 (Moon): Keep silver glass of water by bedside, honor mother."
                3 -> "Missing 3 (Jupiter): Wear yellow on Thursdays, apply saffron tilak."
                4 -> "Missing 4 (Rahu/Wood): Keep green bamboo in East, Tulsi care."
                5 -> "Missing 5 (Mercury): Feed cows green fodder Wednesdays, green agate."
                6 -> "Missing 6 (Venus): Wear crisp white/cream Fridays, windchime NW."
                7 -> "Missing 7 (Ketu): Feed dogs, meditation & silver bracelet."
                8 -> "Missing 8 (Saturn): Serve elders, crystal tortoise in NE."
                else -> "Missing 9 (Mars): Red thread on wrist, Surya Namaskar."
            }
        }

        val months = listOf("Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec")
        val targetYear = 2026
        val pYear = reduceDigit(result.input.day + result.input.month + targetYear)
        val monthlyEnergy = months.mapIndexed { idx, mName ->
            val mNum = reduceDigit(pYear + idx + 1)
            val score = (62 + ((mulank + mNum) % 5) * 8).coerceIn(58, 96)
            val lDates = listOf(mulank, bhagyank, mNum).distinct().map { d -> "${d}, ${d + 9}, ${d + 18}" }.first()
            Triple("$mName $targetYear (PY-$mNum)", score, "Lucky dates: $lDates • Focus: Harmony & Action")
        }

        val vedicNum = VedicNumerologyReport(
            mulank = mulank,
            bhagyank = bhagyank,
            activeDashaNum = dashaPlanetNum,
            gridCounts = counts,
            activePlanes = activePlanes.ifEmpty { listOf("Willpower Seed ($mulank/$bhagyank)") },
            gridYogas = yogas,
            missingRemedies = missingRemedies.ifEmpty { listOf("All 9 Vedic digits present — complete chart symmetry!") },
            nameVibrationNote = "Chaldean Name Number ${base?.chaldeanNameNumber ?: mulank} & Pythagorean Destiny ${base?.pythagoreanDestiny ?: bhagyank}; harmonize name total with friendly ${mulank} or 5/6.",
            supportNumbersByArea = listOf(
                "Career" to "${mulank}, ${reduceDigit(mulank + 4)}, 5",
                "Finance" to "${bhagyank}, 6, 8",
                "Relationships" to "2, 6, ${mulank}",
                "Travel" to "3, 5, 7",
                "Business" to "5, 6, 9",
                "Property" to "4, 8, ${bhagyank}"
            ),
            monthlyEnergy = monthlyEnergy
        )

        val cal = Calendar.getInstance()
        val pMonth = reduceDigit(pYear + cal.get(Calendar.MONTH) + 1)
        val pDay = reduceDigit(pMonth + cal.get(Calendar.DAY_OF_MONTH))
        val repeated = counts.filter { it.value >= 2 }.map { "${it.key} x${it.value}" }
        val triangles = buildList {
            if ((counts[4] ?: 0) > 0 && (counts[9] ?: 0) > 0 && (counts[3] ?: 0) > 0) add("Upper-Left Thought/Fire Triangle (4-9-3)")
            if ((counts[2] ?: 0) > 0 && (counts[7] ?: 0) > 0 && (counts[6] ?: 0) > 0) add("Right Action Triangle (2-7-6)")
            if ((counts[8] ?: 0) > 0 && (counts[1] ?: 0) > 0 && (counts[6] ?: 0) > 0) add("Base Stability Line (8-1-6)")
        }.ifEmpty { listOf("Core Driver-Conductor Axis ($mulank-$bhagyank)") }

        val loShu = LoShuExtendedReport(
            driver = mulank,
            conductor = bhagyank,
            nameNumber = base?.chaldeanNameNumber ?: mulank,
            personalYear = pYear,
            personalMonth = pMonth,
            personalDay = pDay,
            gridCounts = counts,
            friendlyNumbers = base?.luckyNumbers ?: listOf(mulank, bhagyank),
            missingDigits = missing,
            repeatedDigits = repeated,
            activePlanes = activePlanes,
            weaknessPlanes = weaknessPlanes,
            mysticTriangles = triangles,
            nameCorrectionTip = "Align signature & brand spelling to sum toward ${base?.luckyNumbers?.firstOrNull() ?: 5} or 6 for auspicious vibration."
        )
        return vedicNum to loShu
    }

    fun computeLalKitabExtended(result: KundaliResult): LalKitabExtendedReport {
        val currentAge = (2026 - result.input.year).coerceAtLeast(1)
        // Standard Lal Kitab Varshphal annual progression table (cycle 1..12)
        val lkVarshShift = intArrayOf(1, 4, 9, 3, 6, 12, 8, 5, 11, 2, 7, 10)
        val ageMod = (currentAge - 1) % 12
        val rows = result.planets.map { pl ->
            val vHouse = ((pl.houseFromLagna - 1 + lkVarshShift[ageMod]) % 12) + 1
            val drishtiTarget = when (pl.planet) {
                PlanetId.MARS -> ((pl.houseFromLagna - 1 + 3) % 12) + 1
                PlanetId.JUPITER -> ((pl.houseFromLagna - 1 + 4) % 12) + 1
                PlanetId.SATURN -> ((pl.houseFromLagna - 1 + 9) % 12) + 1
                else -> ((pl.houseFromLagna - 1 + 6) % 12) + 1
            }
            val note = "Natal Khana #${pl.houseFromLagna} -> Varshphal Khana #$vHouse (Drishti on H$drishtiTarget)"
            LalKitabVarshphalRow(pl.planet, pl.houseFromLagna, vHouse, drishtiTarget, note)
        }
        val highlights = listOf(
            "Age Cycle $currentAge activates Varshphal cycle index #${ageMod + 1} for year 2026.",
            "In Lal Kitab, House 1 gazes at H7 (100%), House 4/10 and House 3/11 share karmic reciprocity."
        )
        return LalKitabExtendedReport(
            activeAgeCycleNote = "Age $currentAge Years • Lal Kitab Varshphal Cycle (${ageMod + 1}/12)",
            varshphal2026Rows = rows,
            drishtiHighlights = highlights
        )
    }

    fun computeBnnReport(result: KundaliResult): BnnReport {
        val dirDefs = listOf(
            Triple("East (Dharma)", "Fire (1,5,9)", setOf(Rashi.MESHA, Rashi.SIMHA, Rashi.DHANU)),
            Triple("South (Artha)", "Earth (2,6,10)", setOf(Rashi.VRISHABHA, Rashi.KANYA, Rashi.MAKARA)),
            Triple("West (Kama)", "Air (3,7,11)", setOf(Rashi.MITHUNA, Rashi.TULA, Rashi.KUMBHA)),
            Triple("North (Moksha)", "Water (4,8,12)", setOf(Rashi.KARKA, Rashi.VRISCHIKA, Rashi.MEENA))
        )

        val dRows = dirDefs.map { (dir, elem, rashis) ->
            val normal = result.planets.filter { it.rashi in rashis }.map { it.planet }
            // Retrograde planets cast karmic influence to previous sign
            val withRetro = result.planets.filter { pl ->
                val effRashi = if (pl.isRetrograde && pl.planet !in listOf(PlanetId.RAHU, PlanetId.KETU)) {
                    Rashi.fromIndex(pl.rashi.ordinal - 1)
                } else pl.rashi
                effRashi in rashis
            }.map { it.planet }
            val note = when {
                dir.startsWith("East") -> "Vitality, Dharma, Self-Identity & Fame"
                dir.startsWith("South") -> "Wealth, Profession, Karma & Material Craft"
                dir.startsWith("West") -> "Partnerships, Commerce, Desires & Networks"
                else -> "Intuition, Heritage, Moksha & Healing"
            }
            BnnDirectionRow(dir, elem, normal, withRetro, note)
        }

        val moon = result.planets.first { it.planet == PlanetId.MOON }
        val rahu = result.planets.first { it.planet == PlanetId.RAHU }
        val jup = result.planets.first { it.planet == PlanetId.JUPITER }
        val bbLon = EphemerisEngine.normalize360((moon.siderealLongitude + rahu.siderealLongitude) / 2.0)
        val bbRashi = Rashi.fromIndex(floor(bbLon / 30.0).toInt() % 12)
        val bbHouse = ((bbRashi.ordinal - result.lagnaRashi.ordinal + 12) % 12) + 1

        val jupDir = dRows.first { jup.rashi.ordinal % 4 == dRows.indexOf(it) }
        val jupLink = "Jeeva Karaka Jupiter sits in ${jup.rashi.name} (${jupDir.direction}), linking directly with ${jupDir.planetsNormal.joinToString(", ") { it.name }}."
        val degSeq = result.planets.sortedBy { it.degreeInRashi }.joinToString(" -> ") { "${it.planet.name}(${it.degreeInRashi.toInt()}°)" }

        val predictions = listOf(
            "Profession & Karma: Saturn's directional group governs career trajectory alongside Venus/Mercury supports.",
            "Jeeva Karaka Jupiter (${jup.rashi.name}): ${jupLink}",
            "Planetary Degree Sequence: $degSeq reveals early-to-late karmic unfolding."
        )
        val jupProgressions = (0..5).map { yrOff ->
            val yr = 2026 + yrOff
            val pSign = Rashi.fromIndex(jup.rashi.ordinal + yrOff)
            Triple(yr.toString(), "Jupiter -> ${pSign.name}", "Triggers Natal H${((pSign.ordinal - result.lagnaRashi.ordinal + 12) % 12) + 1} (${pSign.lord.name})")
        }

        return BnnReport(
            bhriguBinduLon = bbLon,
            bhriguBinduRashi = bbRashi,
            bhriguBinduHouse = bbHouse,
            directionRows = dRows,
            jupiterTrineLink = jupLink,
            degreeOrderSequence = degSeq,
            predictionNotes = predictions,
            progressionRows = jupProgressions
        )
    }

    fun computeTajikaVarshaphal2026(result: KundaliResult): TajikaVarshaphalReport {
        val targetYear = 2026
        val natalSunLon = result.planets.first { it.planet == PlanetId.SUN }.siderealLongitude

        // Binary search exact Solar Return JD in targetYear when Sun returns to natalSunLon
        var jdLow = TimeUtils.toJulianDay(targetYear, result.input.month, result.input.day, 0, 0, 0.0, result.effectiveUtcOffsetHours) - 3.0
        var jdHigh = jdLow + 6.0
        repeat(28) {
            val mid = (jdLow + jdHigh) / 2.0
            val sLon = EphemerisEngine.planetSiderealLongitude(PlanetId.SUN, mid)
            var diff = sLon - natalSunLon
            if (diff > 180.0) diff -= 360.0
            if (diff < -180.0) diff += 360.0
            if (diff < 0.0) jdLow = mid else jdHigh = mid
        }
        val srJd = (jdLow + jdHigh) / 2.0
        val srDate = TimeUtils.jdToLocalDateString(srJd, result.effectiveUtcOffsetHours)
        val localDayFrac = (((srJd + result.effectiveUtcOffsetHours / 24.0 + 0.5) % 1.0) + 1.0) % 1.0
        val srTime = TimeUtils.formatHoursToHhMm(localDayFrac * 24.0)

        val vLagnaLon = EphemerisEngine.lagnaSiderealLongitude(srJd, result.input.place.latitude, result.input.place.longitude)
        val vLagnaRashiIdx = floor(vLagnaLon / 30.0).toInt() % 12
        val vLagna = Rashi.fromIndex(vLagnaRashiIdx)
        val vLagnaDeg = TimeUtils.formatDms(vLagnaLon - vLagnaRashiIdx * 30.0)

        val yearsCompleted = (targetYear - result.input.year).coerceAtLeast(0)
        val munthaIdx = (result.lagnaRashi.ordinal + yearsCompleted) % 12
        val munthaRashi = Rashi.fromIndex(munthaIdx)
        val munthaHouse = ((munthaIdx - vLagnaRashiIdx + 12) % 12) + 1

        val varshaPlanets = PlanetId.entries.map { pl ->
            val pLon = EphemerisEngine.planetSiderealLongitude(pl, srJd)
            val r = Rashi.fromIndex(floor(pLon / 30.0).toInt() % 12)
            val h = ((r.ordinal - vLagnaRashiIdx + 12) % 12) + 1
            Triple(pl, r, h)
        }

        // Panchadhikari & Varsha Swami (Strongest among Muntha Lord, Janma Lagnesh, Varsha Lagnesh, Tri-Rashi Pati, Din/Ratri Pati)
        val varshaSwami = munthaRashi.lord
        val panchadhikari = listOf(
            "Muntha Swami" to munthaRashi.lord.name,
            "Varsha Lagna Swami" to vLagna.lord.name,
            "Janma Lagna Swami" to result.lagnaRashi.lord.name,
            "Year Ruler (Varshesh)" to varshaSwami.name
        )

        // Key Sahams
        val sunV = EphemerisEngine.planetSiderealLongitude(PlanetId.SUN, srJd)
        val moonV = EphemerisEngine.planetSiderealLongitude(PlanetId.MOON, srJd)
        val jupV = EphemerisEngine.planetSiderealLongitude(PlanetId.JUPITER, srJd)
        val venV = EphemerisEngine.planetSiderealLongitude(PlanetId.VENUS, srJd)
        val merV = EphemerisEngine.planetSiderealLongitude(PlanetId.MERCURY, srJd)
        fun saham(a: Double, b: Double): Pair<Rashi, Int> {
            val sLon = EphemerisEngine.normalize360(a - b + vLagnaLon)
            val r = Rashi.fromIndex(floor(sLon / 30.0).toInt() % 12)
            return r to (((r.ordinal - vLagnaRashiIdx + 12) % 12) + 1)
        }
        val sahams = listOf(
            "Punya Saham (Fortune)" to saham(moonV, sunV),
            "Vidya Saham (Learning)" to saham(sunV, moonV),
            "Vivaha Saham (Relationships)" to saham(venV, jupV),
            "Dhan / Arth Saham (Wealth)" to saham(jupV, sunV),
            "Karya / Karma Saham (Career)" to saham(sunV, merV)
        ).map { (name, pair) -> Triple(name, pair.first, pair.second) }

        // Mudda Dasha (365.25 days divided by Vimshottari proportion)
        val moonNakIdx = floor(moonV / (360.0 / 27.0)).toInt() % 27
        val startMuddaOrder = PlanetId.dashaOrder.indexOf(PlanetId.dashaOrder[moonNakIdx % 9])
        var mCursor = srJd
        val muddas = (0 until 9).map { i ->
            val lord = PlanetId.dashaOrder[(startMuddaOrder + i) % 9]
            val days = 365.242 * (PlanetId.dashaYears.getValue(lord) / 120.0)
            val mEnd = mCursor + days
            val row = Triple(
                lord,
                TimeUtils.jdToLocalDateString(mCursor, result.effectiveUtcOffsetHours),
                TimeUtils.jdToLocalDateString(mEnd, result.effectiveUtcOffsetHours)
            )
            mCursor = mEnd
            row
        }

        val munthaTheme = if (munthaHouse in listOf(1, 2, 3, 5, 9, 10, 11)) {
            "Auspicious Muntha in House $munthaHouse supports growth, recognition & constructive progress."
        } else {
            "Muntha in House $munthaHouse advises patience, health care & steady planning."
        }

        return TajikaVarshaphalReport(
            targetYear = targetYear,
            solarReturnDateTimeStr = "$srDate at $srTime",
            varshaLagna = vLagna,
            varshaLagnaDeg = vLagnaDeg,
            munthaRashi = munthaRashi,
            munthaHouse = munthaHouse,
            munthaLord = munthaRashi.lord,
            varshaSwami = varshaSwami,
            panchadhikari = panchadhikari,
            varshaPlanets = varshaPlanets,
            sahams = sahams,
            muddaDashas = muddas,
            lifeAreaSummaries = listOf(
                "Annual Muntha Focus" to munthaTheme,
                "Career & Finance" to "Varsha Swami ${varshaSwami.name} & Karma Saham in ${sahams[4].second.name} guide steady milestones.",
                "Health & Inner Balance" to "Follow weekly mantra for ${varshaSwami.name} and honor Muntha lord ${munthaRashi.lord.name}."
            ),
            monthlyGuidance = muddas.take(6).map { (lord, s, e) ->
                "Mudda ${lord.name} ($s to $e): Align decisions with ${lord.name}'s houses."
            },
            remedies = listOf(
                "Propitiate Varshesh (${varshaSwami.name}) on its weekday with charitable offerings.",
                "Recite daily Surya Arghya near solar return birthday and donate grains on Muntha lord's day."
            )
        )
    }

    fun computeCrystalAndRemedies(result: KundaliResult): CrystalAndRemedyReport {
        val mdLord = result.currentMahadasha?.lord ?: result.lagnaRashi.lord
        val lagnesh = result.lagnaRashi.lord
        val crystals = listOf(
            Triple("Clear Quartz (Sphatik)", "North-East Altar • Enhances clarity & meditation", "Hold during morning mantra to calm fluctuating Moon."),
            Triple("Citrine / Yellow Calcite", "South-East Desk • Abundance & Jupiter harmony", "Supports running ${mdLord.name} Dasha decision-making."),
            Triple("Amethyst / Labradorite", "West / Study Space • Focus & Saturn steadiness", "Grounds restless energy and fosters disciplined study.")
        )

        val mantras = listOf(
            Triple(PlanetId.SUN, "ॐ घृणि सूर्याय नमः (Om Ghrini Suryaya Namah)", "108x Sunday Sunrise"),
            Triple(PlanetId.MOON, "ॐ सों सोमाय नमः (Om Som Somaya Namah)", "108x Monday Evening"),
            Triple(PlanetId.MARS, "ॐ अं अंगारकाय नमः (Om Ang Angarakaya Namah)", "108x Tuesday"),
            Triple(PlanetId.MERCURY, "ॐ बुं बुधाय नमः (Om Bum Budhaya Namah)", "108x Wednesday"),
            Triple(PlanetId.JUPITER, "ॐ बृं बृहस्पतये नमः (Om Brim Brihaspataye Namah)", "108x Thursday"),
            Triple(PlanetId.VENUS, "ॐ शुं शुक्राय नमः (Om Shum Shukraya Namah)", "108x Friday"),
            Triple(PlanetId.SATURN, "ॐ शं शनैश्चराय नमः (Om Sham Shanicharaya Namah)", "108x Saturday"),
            Triple(PlanetId.RAHU, "ॐ रां राहवे नमः (Om Ram Rahave Namah)", "108x Dusk / Wed"),
            Triple(PlanetId.KETU, "ॐ कें केतवे नमः (Om Kem Ketave Namah)", "108x Dawn / Tue")
        )

        val seva = result.lalKitab?.remediesEn.orEmpty() + listOf(
            "Honor Lagnesh ${lagnesh.name} & Dasha Lord ${mdLord.name} with weekly food charity.",
            "Feed birds daily 7-grain mix and keep North-East Puja corner bright and serene."
        )

        val tracker = listOf(
            "Sunday (Sun)" to "Surya Arghya (Copper vessel) + 108x Gayatri / Sun Mantra [Days 1-40]",
            "Monday (Moon)" to "Milk/Water offering + 5-min Breathwork [ ] Wk1 [ ] Wk2 [ ] Wk3 [ ] Wk4",
            "Tuesday (Mars)" to "Hanuman Chalisa + Mindful Speech [ ] Wk1 [ ] Wk2 [ ] Wk3 [ ] Wk4",
            "Wednesday (Mercury)" to "Green Moong Seva + Gratitude Journal [ ] Wk1 [ ] Wk2 [ ] Wk3 [ ] Wk4",
            "Thursday (Jupiter)" to "Saffron Tilak + Vishnu/Guru Mantra [ ] Wk1 [ ] Wk2 [ ] Wk3 [ ] Wk4",
            "Friday (Venus)" to "White Sweet Charity + Clean Workspace [ ] Wk1 [ ] Wk2 [ ] Wk3 [ ] Wk4",
            "Saturday (Saturn)" to "Sesame Oil Lamp + Elder Seva [ ] Wk1 [ ] Wk2 [ ] Wk3 [ ] Wk4"
        )

        return CrystalAndRemedyReport(crystals, mantras, seva.distinct(), tracker)
    }
}

object ComprehensiveReportBuilder {
    fun build(result: KundaliResult): ComprehensiveReportBundle {
        val panchangExt = YoginiAndBcpEngine.computeExtendedPanchang(result)
        val yogini = YoginiAndBcpEngine.computeYoginiDasha(result)
        val bcp = YoginiAndBcpEngine.computeBhriguChakraPaddhati(result)
        val jaimini = JaiminiAndSpecialLagnaEngine.computeJaimini(result)
        val specialLagnas = JaiminiAndSpecialLagnaEngine.computeSpecialLagnas(result)
        val all20Divs = JaiminiAndSpecialLagnaEngine.computeAll20DivisionalCharts(result)
        val yogas = YogaAndChakraEngine.computeVedicYogas(result)
        val phaladesh = YoginiAndBcpEngine.computePhaladesh(
            result, panchangExt, jaimini, yogini.firstOrNull { it.isCurrent }, yogas
        )
        val (reducedSav, avShodhanas) = YogaAndChakraEngine.computeAshtakavargaReductions(result)
        val shadbalaExt = YogaAndChakraEngine.computeExtendedShadbala(result)
        val sbcKota = YogaAndChakraEngine.computeSarvatobhadraAndKota(result)
        val nadi = TajikaBnnAndNadiEngine.computeNadiReport(result)
        val kpExt = TajikaBnnAndNadiEngine.computeKpExtended(result)
        val gems = TajikaBnnAndNadiEngine.computeGemstoneReport(result)
        val (vedicNum, loShu) = TajikaBnnAndNadiEngine.computeVedicNumerologyAndLoShu(result)
        val lkExt = TajikaBnnAndNadiEngine.computeLalKitabExtended(result)
        val bnn = TajikaBnnAndNadiEngine.computeBnnReport(result)
        val tajika = TajikaBnnAndNadiEngine.computeTajikaVarshaphal2026(result)
        val crystalRem = TajikaBnnAndNadiEngine.computeCrystalAndRemedies(result)

        return ComprehensiveReportBundle(
            panchangExt = panchangExt,
            phaladesh = phaladesh,
            yoginiDashas = yogini,
            bcp = bcp,
            jaimini = jaimini,
            nadi = nadi,
            kpExt = kpExt,
            specialLagnas = specialLagnas,
            vedicYogas = yogas,
            all20DivisionalCharts = all20Divs,
            avReducedSarva = reducedSav,
            avShodhanas = avShodhanas,
            shadbalaExt = shadbalaExt,
            gemstones = gems,
            vedicNumerology = vedicNum,
            loShuExt = loShu,
            sbcAndKota = sbcKota,
            lalKitabExt = lkExt,
            bnn = bnn,
            tajika2026 = tajika,
            crystalAndRemedies = crystalRem
        )
    }
}

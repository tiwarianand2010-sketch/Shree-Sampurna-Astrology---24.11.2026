package com.example.pdf

import android.graphics.Canvas
import com.example.core.AstrologerAccount
import com.example.model.ComprehensiveReportBundle
import com.example.model.KundaliResult
import com.example.model.PlanetId
import com.example.model.Rashi
import com.example.pdf.PdfCanvasHelpers.CONTENT_WIDTH
import com.example.pdf.PdfCanvasHelpers.MARGIN
import com.example.pdf.PdfCanvasHelpers.PAGE_WIDTH
import com.example.pdf.PdfCanvasHelpers.Paints
import com.example.pdf.PdfCanvasHelpers.draw3x3NumberGrid
import com.example.pdf.PdfCanvasHelpers.drawHouseBasedNorthChart
import com.example.pdf.PdfCanvasHelpers.drawNorthIndianVectorChart
import com.example.pdf.PdfCanvasHelpers.drawPageChrome
import com.example.pdf.PdfCanvasHelpers.drawWrappedText

internal object PdfSectionsPart2 {

    // 12. Ashtakavarga (SAV + Reduced SAV + Trikona/Ekadhipatya + Shodhya Pinda), Shadbala & Bhavabala (Module 12)
    fun drawModule12AshtakavargaShadbalaPage(
        c: Canvas, p: Paints, astrologer: AstrologerAccount,
        result: KundaliResult, bundle: ComprehensiveReportBundle,
        pageNo: Int, totalPages: Int
    ) {
        var y = drawPageChrome(c, p, astrologer, "12. Ashtakavarga Shodhana, Shodhya Pinda, Shadbala & Bhavabala", result.input.name, pageNo, totalPages)
        val av = result.ashtakavarga
        c.drawText("Sarvashtakavarga vs Reduced SAV (After Trikona & Ekadhipatya Shodhana)", MARGIN, y, p.sectionRed)
        y += 14f
        Rashi.entries.forEachIndexed { idx, r ->
            val raw = av?.sarvashtakavargaByRashi?.getOrNull(idx) ?: 28
            val red = bundle.avReducedSarva.getOrNull(idx) ?: 0
            if (idx % 2 == 0) c.drawRect(MARGIN, y - 8f, PAGE_WIDTH - MARGIN, y + 5f, p.bgCard)
            c.drawText("${r.number}. ${r.name}", MARGIN + 4f, y + 1f, p.subBold)
            val barW = (raw * 4.5f).coerceAtMost(195f)
            c.drawRect(MARGIN + 115f, y - 6f, MARGIN + 115f + barW, y + 3f, if (raw >= 28) p.bgHeaderBlue else p.bgRed)
            c.drawText("Raw: $raw • Reduced: $red", MARGIN + 330f, y + 1f, p.body)
            y += 15f
        }

        y += 8f
        c.drawText("Shodhya Pinda & Reduction Matrix (7 Planets)", MARGIN, y, p.sectionRed)
        y += 14f
        bundle.avShodhanas.forEachIndexed { idx, row ->
            if (idx % 2 == 0) c.drawRect(MARGIN, y - 8f, PAGE_WIDTH - MARGIN, y + 5f, p.bgCard)
            c.drawText(row.planet.name, MARGIN + 4f, y + 1f, p.subBold)
            c.drawText("BAV:${row.rawTotal} -> Tri:${row.trikonaReducedTotal} -> Eka:${row.ekadhipatyaReducedTotal}", MARGIN + 85f, y + 1f, p.body)
            c.drawText("R-Pinda:${row.rashiPinda} • G-Pinda:${row.grahaPinda} • Shodhya:${row.shodhyaPinda}", MARGIN + 275f, y + 1f, p.bodyRed)
            y += 15f
        }

        y += 10f
        c.drawText("Full Shadbala (Virupas, Rupas, Ratio & Rank)", MARGIN, y, p.sectionRed)
        y += 14f
        bundle.shadbalaExt.forEachIndexed { idx, sb ->
            if (idx % 2 == 0) c.drawRect(MARGIN, y - 8f, PAGE_WIDTH - MARGIN, y + 5f, p.bgCard)
            c.drawText("#${sb.rank} ${sb.planet.name}", MARGIN + 4f, y + 1f, p.subBold)
            c.drawText("Sth:${"%.0f".format(sb.sthana)} Dig:${"%.0f".format(sb.dig)} Kaal:${"%.0f".format(sb.kaala)} Che:${"%.0f".format(sb.cheshta)} Nais:${"%.0f".format(sb.naisargika)} Drik:${"%.0f".format(sb.drik)}", MARGIN + 88f, y + 1f, p.small)
            c.drawText("${"%.1f".format(sb.totalRupas)}/${"%.1f".format(sb.requiredRupas)}R (${"%.2f".format(sb.ratio)}x)", MARGIN + 395f, y + 1f, if (sb.ratio >= 1.0) p.bodyGreen else p.bodyRed)
            y += 15f
        }

        y += 10f
        val bhavaLine = result.bhavaBala.joinToString(" | ") { "H${it.houseNumber}:${"%.0f".format(it.totalScore)}" }
        drawWrappedText(c, "Bhavabala (House Strength Virupas): $bhavaLine", MARGIN, y, CONTENT_WIDTH, p.small, 11f)
    }

    // 13 & 14. Gemstones + Vedic Numerology & 12-Month Energy (Modules 13 & 14)
    fun drawModule13And14GemsAndVedicNumPage(
        c: Canvas, p: Paints, astrologer: AstrologerAccount,
        result: KundaliResult, bundle: ComprehensiveReportBundle,
        pageNo: Int, totalPages: Int
    ) {
        val g = bundle.gemstones
        val vn = bundle.vedicNumerology
        var y = drawPageChrome(c, p, astrologer, "13 & 14. Gemstone Protocol, Vedic Numerology & 12-Month Energy", result.input.name, pageNo, totalPages)

        c.drawText("13. Personalized Gemstone Suitability & Wearing Protocol", MARGIN, y, p.sectionRed)
        y += 14f
        c.drawRect(MARGIN, y, PAGE_WIDTH - MARGIN, y + 66f, p.bgLightBlue)
        c.drawRect(MARGIN, y, PAGE_WIDTH - MARGIN, y + 66f, p.goldStroke)
        c.drawText("Primary (Score ${g.primaryScore}%): ${g.primaryGem} (${g.primaryPlanet.name})", MARGIN + 8f, y + 16f, p.titleDarkBlue)
        c.drawText("Secondary (${g.secondaryScore}%): ${g.secondaryGem} • Supporting: ${g.additionalGem}", MARGIN + 8f, y + 32f, p.subBold)
        c.drawText("Wearing Protocol: ${g.fingerMetalDayMantra}", MARGIN + 8f, y + 47f, p.bodyGreen)
        c.drawText("Caution / Avoid: ${g.avoidGems.firstOrNull() ?: "None"}", MARGIN + 8f, y + 60f, p.bodyRed)
        y += 78f

        c.drawText("14. Vedic Numerology 3x3 Grid, Yogas & 12-Month 2026 Energy", MARGIN, y, p.sectionRed)
        y += 12f
        c.drawText("Mulank: ${vn.mulank} • Bhagyank: ${vn.bhagyank} • Active Dasha Num: ${vn.activeDashaNum}", MARGIN, y, p.subBold)
        y += 10f
        draw3x3NumberGrid(c, p, MARGIN, y, 66f, 44f, listOf(listOf(3, 1, 9), listOf(6, 7, 5), listOf(2, 8, 4)), vn.gridCounts)
        // Right side text beside 3x3 grid
        var ry = y + 10f
        c.drawText("Active Planes & Grid Yogas:", MARGIN + 215f, ry, p.subBold); ry += 13f
        vn.activePlanes.take(4).forEach { pl ->
            c.drawText("• $pl", MARGIN + 215f, ry, p.body); ry += 12f
        }
        ry += 4f
        c.drawText("Support Numbers by Domain:", MARGIN + 215f, ry, p.subBold); ry += 12f
        vn.supportNumbersByArea.forEach { (area, nums) ->
            c.drawText("$area: $nums", MARGIN + 215f, ry, p.small); ry += 11f
        }
        y += 142f
        y = drawWrappedText(c, vn.nameVibrationNote, MARGIN, y, CONTENT_WIDTH, p.body) + 8f

        c.drawText("12-Month Numerology Energy & Lucky Dates (2026)", MARGIN, y, p.sectionRed)
        y += 14f
        vn.monthlyEnergy.forEachIndexed { idx, (mLabel, score, tip) ->
            if (idx % 2 == 0) c.drawRect(MARGIN, y - 8f, PAGE_WIDTH - MARGIN, y + 5f, p.bgCard)
            c.drawText(mLabel, MARGIN + 6f, y + 1f, p.subBold)
            c.drawText("$score%", MARGIN + 150f, y + 1f, if (score >= 78) p.bodyGreen else p.bodyRed)
            c.drawText(tip, MARGIN + 210f, y + 1f, p.body)
            y += 15f
        }
    }

    // 15. Lo Shu Grid Numerology & Arrows / Triangles (Module 15)
    fun drawModule15LoShuExtendedPage(
        c: Canvas, p: Paints, astrologer: AstrologerAccount,
        result: KundaliResult, bundle: ComprehensiveReportBundle,
        pageNo: Int, totalPages: Int
    ) {
        val ls = bundle.loShuExt
        var y = drawPageChrome(c, p, astrologer, "15. Lo Shu Grid Numerology, Planes & Name Correction", result.input.name, pageNo, totalPages)
        c.drawText("Driver: ${ls.driver} • Conductor: ${ls.conductor} • Name#: ${ls.nameNumber} • Personal Year/Mo/Day: ${ls.personalYear}/${ls.personalMonth}/${ls.personalDay}", MARGIN, y, p.subBold)
        y += 14f
        draw3x3NumberGrid(c, p, (PAGE_WIDTH - 255f) / 2f, y, 85f, 56f, listOf(listOf(4, 9, 2), listOf(3, 5, 7), listOf(8, 1, 6)), ls.gridCounts)
        y += 182f

        val infoRows = listOf(
            "Friendly Numbers" to ls.friendlyNumbers.joinToString(", "),
            "Repeated Digits" to ls.repeatedDigits.joinToString(", ").ifEmpty { "Balanced Singles" },
            "Missing Digits" to ls.missingDigits.joinToString(", ").ifEmpty { "None (Complete Lo Shu!)" },
            "Active Lo Shu Planes" to ls.activePlanes.joinToString(" | ").ifEmpty { "Building" },
            "Weakness Planes" to ls.weaknessPlanes.joinToString(" | ").ifEmpty { "None" },
            "Mystic Triangles" to ls.mysticTriangles.joinToString(" | ")
        )
        infoRows.forEachIndexed { idx, (k, v) ->
            if (idx % 2 == 0) c.drawRect(MARGIN, y - 8f, PAGE_WIDTH - MARGIN, y + 5f, p.bgCard)
            c.drawText(k, MARGIN + 6f, y + 1f, p.subBold)
            c.drawText(v.take(64), MARGIN + 150f, y + 1f, p.body)
            y += 18f
        }
        y += 10f
        y = drawWrappedText(c, "Name Correction & Remedies: ${ls.nameCorrectionTip}", MARGIN, y, CONTENT_WIDTH, p.bodyRed) + 8f
        bundle.vedicNumerology.missingRemedies.take(6).forEach { rem ->
            if (y < 785f) {
                y = drawWrappedText(c, "• $rem", MARGIN, y, CONTENT_WIDTH, p.body) + 2f
            }
        }
    }

    // 16. Sarvatobhadra Chakra & Kota Chakra (Module 16)
    fun drawModule16SarvatobhadraKotaPage(
        c: Canvas, p: Paints, astrologer: AstrologerAccount,
        result: KundaliResult, bundle: ComprehensiveReportBundle,
        pageNo: Int, totalPages: Int
    ) {
        val sk = bundle.sbcAndKota
        var y = drawPageChrome(c, p, astrologer, "16. Sarvatobhadra Chakra (Vedha/Latta) & Kota Chakra", result.input.name, pageNo, totalPages)

        c.drawText("Sarvatobhadra Chakra Elements & Transit Vedha Analysis", MARGIN, y, p.sectionRed)
        y += 14f
        sk.sbcElements.forEachIndexed { idx, (k, v) ->
            if (idx % 2 == 0) c.drawRect(MARGIN, y - 8f, PAGE_WIDTH - MARGIN, y + 5f, p.bgCard)
            c.drawText(k, MARGIN + 6f, y + 1f, p.subBold)
            c.drawText(v, MARGIN + 170f, y + 1f, p.body)
            y += 18f
        }
        y += 8f
        c.drawText("Vedha & Latta Diagnostics:", MARGIN, y, p.subBold); y += 14f
        sk.vedhaHits.forEach { hit ->
            y = drawWrappedText(c, "• $hit", MARGIN + 6f, y, CONTENT_WIDTH - 10f, p.body) + 2f
        }
        y = drawWrappedText(c, "Latta: ${sk.lattaNote}", MARGIN + 6f, y + 2f, CONTENT_WIDTH - 10f, p.bodyRed) + 14f

        c.drawText("Kota Chakra (Fortress Transit Protection)", MARGIN, y, p.sectionRed)
        y += 16f
        c.drawText("Kota Swami (Fort Ruler): ${sk.kotaSwami.name} • Kota Pal (Commander): ${sk.kotaPal.name}", MARGIN, y, p.subBold)
        y += 18f
        listOf(
            "1. Stambha (Inner Pillar)" to sk.stambhaZone,
            "2. Madhya (Courtyard)" to sk.madhyaZone,
            "3. Prakaara (Fort Wall)" to sk.prakaaraZone,
            "4. Bahya (Outer Boundary)" to sk.bahyaZone
        ).forEachIndexed { idx, (zone, occ) ->
            if (idx % 2 == 0) c.drawRect(MARGIN, y - 8f, PAGE_WIDTH - MARGIN, y + 5f, p.bgCard)
            c.drawText(zone, MARGIN + 6f, y + 1f, p.subBold)
            c.drawText(occ, MARGIN + 190f, y + 1f, p.body)
            y += 18f
        }
        y += 10f
        drawWrappedText(c, "Kota Verdict: ${sk.kotaVerdict}", MARGIN, y, CONTENT_WIDTH, p.bodyGreen)
    }

    // 17. Complete Jaimini Analysis (Module 17)
    fun drawModule17JaiminiPage(
        c: Canvas, p: Paints, astrologer: AstrologerAccount,
        result: KundaliResult, bundle: ComprehensiveReportBundle,
        pageNo: Int, totalPages: Int
    ) {
        val j = bundle.jaimini
        var y = drawPageChrome(c, p, astrologer, "17. Jaimini Analysis: Karakamsa, UL, Chara Dasha & Argala", result.input.name, pageNo, totalPages)

        c.drawText("Jaimini Core: Lagna ${j.lagnaRashi.name} • Karakamsa ${j.karakamsaRashi.name} • Upapada (UL) ${j.upapadaRashi.name}", MARGIN, y, p.titleDarkBlue)
        y += 14f
        c.drawText("Lagna Rashi Drishti: ${j.lagnaRashiDrishti.joinToString(", ") { it.name }}", MARGIN, y, p.subBold)
        y += 16f

        c.drawText("Arudha Padas (AL / A1 to A12 / UL)", MARGIN, y, p.sectionRed)
        y += 14f
        j.arudhaPadas.chunked(3).forEachIndexed { rIdx, trio ->
            if (rIdx % 2 == 0) c.drawRect(MARGIN, y - 8f, PAGE_WIDTH - MARGIN, y + 5f, p.bgCard)
            trio.forEachIndexed { cIdx, ap ->
                c.drawText("${ap.code}: ${ap.rashi.name} (H${ap.houseFromLagna})", MARGIN + cIdx * 172f, y + 1f, p.body)
            }
            y += 16f
        }

        y += 10f
        c.drawText("Jaimini Chara Dasha Timeline (12 Signs)", MARGIN, y, p.sectionRed)
        y += 14f
        j.charaDashas.forEachIndexed { idx, cd ->
            if (cd.isCurrent) c.drawRect(MARGIN, y - 8f, PAGE_WIDTH - MARGIN, y + 5f, p.bgGoldBanner)
            else if (idx % 2 == 0) c.drawRect(MARGIN, y - 8f, PAGE_WIDTH - MARGIN, y + 5f, p.bgCard)
            c.drawText("${cd.rashi.name} (${cd.years} yrs)", MARGIN + 6f, y + 1f, if (cd.isCurrent) p.bodyRed else p.subBold)
            c.drawText("${cd.startDateStr} -> ${cd.endDateStr}", MARGIN + 145f, y + 1f, p.body)
            c.drawText("Antars: ${cd.subSigns.take(4).joinToString(",") { it.first.name.take(3) }}...", MARGIN + 335f, y + 1f, p.small)
            y += 15f
        }

        y += 10f
        c.drawText("Sign-Based Argala & Virodhargala Summary", MARGIN, y, p.sectionRed)
        y += 14f
        j.argalas.forEachIndexed { idx, arg ->
            if (y < 775f) {
                c.drawText("H${arg.targetHouse} (${arg.argalaSign.name.take(4)}): Support[${arg.argalaSources}] vs Obs[${arg.virodhargalaObstructions}] -> ${arg.netVerdict}", MARGIN + 4f, y, p.small)
                y += 13f
            }
        }
    }

    // 18 & 19. Lal Kitab Chart/Varshphal 2026 & BNN Analysis (Modules 18 & 19)
    fun drawModule18And19LalKitabAndBnnPage(
        c: Canvas, p: Paints, astrologer: AstrologerAccount,
        result: KundaliResult, bundle: ComprehensiveReportBundle,
        pageNo: Int, totalPages: Int
    ) {
        val lkExt = bundle.lalKitabExt
        val bnn = bundle.bnn
        var y = drawPageChrome(c, p, astrologer, "18 & 19. Lal Kitab 2026 Varshphal & BNN (Bhrigu Nandi Nadi)", result.input.name, pageNo, totalPages)

        c.drawText("18. Lal Kitab 2026 Varshphal & Drishti (${lkExt.activeAgeCycleNote})", MARGIN, y, p.sectionRed)
        y += 10f
        val natHouses = result.planets.groupBy { it.houseFromLagna }.mapValues { (_, l) -> l.map { it.planet } }
        val varshHouses = lkExt.varshphal2026Rows.groupBy { it.varshphal2026House }.mapValues { (_, l) -> l.map { it.planet } }
        drawHouseBasedNorthChart(c, p, MARGIN, y + 10f, 128f, Rashi.MESHA, natHouses)
        drawHouseBasedNorthChart(c, p, MARGIN + 140f, y + 10f, 128f, Rashi.MESHA, varshHouses)
        c.drawText("Natal Kal Purush Chart", MARGIN + 10f, y + 7f, p.subBold)
        c.drawText("2026 Varshphal Chart", MARGIN + 150f, y + 7f, p.subBold)

        var ry = y + 14f
        lkExt.varshphal2026Rows.forEach { row ->
            c.drawText("${row.planet.name.take(4)}: H${row.natalHouse}->2026 H${row.varshphal2026House} (Drishti H${row.drishtiTargetHouse})", MARGIN + 282f, ry, p.small)
            ry += 13f
        }
        y += 150f

        c.drawText("19. Bhrigu Nandi Nadi (BNN) Directions, Trines & Progressions", MARGIN, y, p.sectionRed)
        y += 14f
        c.drawText("Bhrigu Bindu (BB): ${bnn.bhriguBinduRashi.name} (H${bnn.bhriguBinduHouse}) • Degree Order: ${bnn.degreeOrderSequence.take(56)}", MARGIN, y, p.small)
        y += 14f
        bnn.directionRows.forEachIndexed { idx, dr ->
            if (idx % 2 == 0) c.drawRect(MARGIN, y - 8f, PAGE_WIDTH - MARGIN, y + 5f, p.bgCard)
            c.drawText("${dr.direction} [${dr.trineElement}]", MARGIN + 4f, y + 1f, p.subBold)
            c.drawText("Norm: ${dr.planetsNormal.joinToString(",") { it.name.take(3) }.ifEmpty { "—" }}", MARGIN + 185f, y + 1f, p.body)
            c.drawText("Retro-Adj: ${dr.planetsWithRetro.joinToString(",") { it.name.take(3) }.ifEmpty { "—" }}", MARGIN + 345f, y + 1f, p.bodyRed)
            y += 16f
        }
        y += 6f
        y = drawWrappedText(c, bnn.jupiterTrineLink, MARGIN, y, CONTENT_WIDTH, p.bodyGreen) + 8f

        c.drawText("BNN Jupiter Multi-Year Progression Overlay (2026–2031)", MARGIN, y, p.subBold)
        y += 13f
        bnn.progressionRows.forEach { (yr, prog, note) ->
            c.drawText("$yr • $prog — $note", MARGIN + 6f, y, p.small)
            y += 12f
        }
    }

    // 20. Tajika Varshaphal 2026 Solar Return (Module 20)
    fun drawModule20TajikaVarshaphalPage(
        c: Canvas, p: Paints, astrologer: AstrologerAccount,
        result: KundaliResult, bundle: ComprehensiveReportBundle,
        pageNo: Int, totalPages: Int
    ) {
        val tv = bundle.tajika2026
        var y = drawPageChrome(c, p, astrologer, "20. Tajika Varshaphal (${tv.targetYear} Solar Return Annual Horoscope)", result.input.name, pageNo, totalPages)

        c.drawRect(MARGIN, y, PAGE_WIDTH - MARGIN, y + 52f, p.bgLightBlue)
        c.drawRect(MARGIN, y, PAGE_WIDTH - MARGIN, y + 52f, p.goldStroke)
        c.drawText("${tv.targetYear} Solar Return: ${tv.solarReturnDateTimeStr}", MARGIN + 10f, y + 17f, p.titleDarkBlue)
        c.drawText("Varsha Lagna: ${tv.varshaLagna.name} ${tv.varshaLagnaDeg} • Muntha: ${tv.munthaRashi.name} (H${tv.munthaHouse}) • Varshesh: ${tv.varshaSwami.name}", MARGIN + 10f, y + 35f, p.subBold)
        y += 62f

        val vByRashi = tv.varshaPlanets.groupBy({ it.second }, { it.first })
        drawNorthIndianVectorChart(c, p, MARGIN, y, 165f, tv.varshaLagna, vByRashi)

        var ry = y + 12f
        c.drawText("Panchadhikari & Varsha Planets:", MARGIN + 180f, ry, p.sectionRed); ry += 14f
        tv.panchadhikari.forEach { (k, v) ->
            c.drawText("$k: $v", MARGIN + 180f, ry, p.body); ry += 13f
        }
        ry += 6f
        tv.varshaPlanets.forEach { (pl, r, h) ->
            c.drawText("${pl.name.take(4)}: ${r.name.take(5)} (H$h)", MARGIN + 180f, ry, p.small); ry += 12f
        }
        y += 184f

        c.drawText("Tajika Sahams & Mudda Dasha Windows (${tv.targetYear})", MARGIN, y, p.sectionRed)
        y += 14f
        tv.sahams.forEachIndexed { idx, (sName, r, h) ->
            if (idx % 2 == 0) c.drawRect(MARGIN, y - 8f, PAGE_WIDTH - MARGIN, y + 5f, p.bgCard)
            c.drawText(sName, MARGIN + 6f, y + 1f, p.subBold)
            c.drawText("${r.name} (Varsha House $h)", MARGIN + 230f, y + 1f, p.body)
            y += 15f
        }

        y += 10f
        c.drawText("Mudda Dasha Sub-Periods:", MARGIN, y, p.subBold); y += 13f
        tv.muddaDashas.chunked(2).forEach { pair ->
            val line = pair.joinToString("   |   ") { (pl, s, e) -> "${pl.name.take(4)}: $s..$e" }
            c.drawText(line, MARGIN + 6f, y, p.small)
            y += 12f
        }
        y += 8f
        tv.lifeAreaSummaries.forEach { (title, txt) ->
            y = drawWrappedText(c, "• $title: $txt", MARGIN, y, CONTENT_WIDTH, p.body) + 3f
        }
        tv.remedies.forEach { rem ->
            y = drawWrappedText(c, "Remedy: $rem", MARGIN, y, CONTENT_WIDTH, p.bodyGreen) + 2f
        }
    }

    // 21. Crystal & Reflection Guide + Vedic Mantras + Seva + 40-Day Remedy Tracker (Module 21)
    fun drawModule21CrystalsAndRemedyTrackerPage(
        c: Canvas, p: Paints, astrologer: AstrologerAccount,
        result: KundaliResult, bundle: ComprehensiveReportBundle,
        pageNo: Int, totalPages: Int
    ) {
        val cr = bundle.crystalAndRemedies
        var y = drawPageChrome(c, p, astrologer, "21. Crystal Guide, Vedic Mantras, Seva & 40-Day Remedy Tracker", result.input.name, pageNo, totalPages)

        c.drawText("Personalized Crystal & Mindfulness Reflection Guide", MARGIN, y, p.sectionRed)
        y += 14f
        cr.recommendedCrystals.forEachIndexed { idx, (cName, usage, note) ->
            if (idx % 2 == 0) c.drawRect(MARGIN, y - 8f, PAGE_WIDTH - MARGIN, y + 16f, p.bgCard)
            c.drawText("$cName — $usage", MARGIN + 6f, y + 1f, p.subBold)
            c.drawText("Reflection: $note", MARGIN + 6f, y + 12f, p.small)
            y += 24f
        }

        y += 8f
        c.drawText("Navagraha Vedic Mantras & Seva/Charity Recommendations", MARGIN, y, p.sectionRed)
        y += 14f
        cr.planetMantras.forEachIndexed { idx, (pl, mantra, sched) ->
            if (idx % 2 == 0) c.drawRect(MARGIN, y - 8f, PAGE_WIDTH - MARGIN, y + 5f, p.bgCard)
            c.drawText(pl.name, MARGIN + 6f, y + 1f, p.subBold)
            c.drawText(mantra, MARGIN + 80f, y + 1f, p.bodyRed)
            c.drawText(sched, MARGIN + 380f, y + 1f, p.small)
            y += 15f
        }

        y += 10f
        c.drawText("40-Day / Weekly Practical Remedy Tracker", MARGIN, y, p.sectionRed)
        y += 14f
        cr.weeklyRemedySchedule.forEachIndexed { idx, (day, practice) ->
            if (idx % 2 == 0) c.drawRect(MARGIN, y - 8f, PAGE_WIDTH - MARGIN, y + 5f, p.bgCard)
            c.drawText(day, MARGIN + 6f, y + 1f, p.subBold)
            c.drawText(practice, MARGIN + 125f, y + 1f, p.body)
            y += 16f
        }
        y += 10f
        cr.lalKitabAndSevaUpays.take(4).forEach { upay ->
            if (y < 785f) {
                y = drawWrappedText(c, "• Seva/Upay: $upay", MARGIN, y, CONTENT_WIDTH, p.bodyGreen) + 2f
            }
        }
    }
}

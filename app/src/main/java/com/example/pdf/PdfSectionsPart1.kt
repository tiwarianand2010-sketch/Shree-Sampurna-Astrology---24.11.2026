package com.example.pdf

import android.graphics.Canvas
import com.example.core.AppLanguage
import com.example.core.AstroStrings
import com.example.core.AstrologerAccount
import com.example.model.ChartDivision
import com.example.model.ComprehensiveReportBundle
import com.example.model.KundaliResult
import com.example.model.PlanetId
import com.example.model.Rashi
import com.example.pdf.PdfCanvasHelpers.CONTENT_WIDTH
import com.example.pdf.PdfCanvasHelpers.MARGIN
import com.example.pdf.PdfCanvasHelpers.PAGE_WIDTH
import com.example.pdf.PdfCanvasHelpers.Paints
import com.example.pdf.PdfCanvasHelpers.drawHouseBasedNorthChart
import com.example.pdf.PdfCanvasHelpers.drawNorthIndianVectorChart
import com.example.pdf.PdfCanvasHelpers.drawPageChrome
import com.example.pdf.PdfCanvasHelpers.drawWrappedText

internal object PdfSectionsPart1 {

    // 1. Cover & Complete Birth Panchang + Birth Details (Module 1)
    fun drawModule1BirthPanchangPage(
        c: Canvas, p: Paints, astrologer: AstrologerAccount,
        result: KundaliResult, bundle: ComprehensiveReportBundle,
        pageNo: Int, totalPages: Int, isBrihat: Boolean
    ) {
        val ext = bundle.panchangExt
        var y = drawPageChrome(
            c, p, astrologer,
            if (isBrihat) "1. Birth Panchang & Birth Details (Brihat 250p Compendium)" else "1. Birth Panchang & Birth Details",
            result.input.name, pageNo, totalPages
        )

        // Prominent Astrologer & Studio Header Card right at top of Page 1
        c.drawRect(MARGIN, y, PAGE_WIDTH - MARGIN, y + 48f, p.bgGoldBanner)
        c.drawRect(MARGIN, y, PAGE_WIDTH - MARGIN, y + 48f, p.goldStroke)
        c.drawText("PREPARED BY ASTROLOGER: ${astrologer.effectiveHeaderName()}", MARGIN + 10f, y + 18f, p.titleDarkBlue)
        val contactLine = buildString {
            append(astrologer.studioName)
            if (astrologer.mobileNumber.isNotBlank()) append(" • Mobile: ${astrologer.mobileNumber}")
            if (astrologer.email.isNotBlank()) append(" • ${astrologer.email}")
        }
        c.drawText(contactLine.take(78), MARGIN + 10f, y + 36f, p.bodyRed)
        y += 58f

        // Native Identity Box
        c.drawRect(MARGIN, y, PAGE_WIDTH - MARGIN, y + 64f, p.bgLightBlue)
        c.drawRect(MARGIN, y, PAGE_WIDTH - MARGIN, y + 64f, p.goldStroke)
        c.drawText("Native: ${result.input.name}", MARGIN + 10f, y + 18f, p.sectionRed)
        c.drawText(
            "DOB & Time: %04d-%02d-%02d  %02d:%02d (UTC%+.1f) • Place: ${result.input.place.nameEn} (%.4f°, %.4f°)".format(
                result.input.year, result.input.month, result.input.day,
                result.input.hour, result.input.minute, result.effectiveUtcOffsetHours,
                result.input.place.latitude, result.input.place.longitude
            ),
            MARGIN + 10f, y + 36f, p.body
        )
        c.drawText(
            "Lagna: ${result.lagnaRashi.name} ${result.lagnaDmsFormatted} • Lahiri Ayanamsa: ${"%.4f".format(result.ayanamsaDeg)}°",
            MARGIN + 10f, y + 52f, p.subBold
        )
        y += 76f

        c.drawText("1. Detailed Birth Panchang & Shubh-Ashubh Muhurat", MARGIN, y, p.sectionRed)
        y += 14f
        val rows = listOf(
            "Sun / Moon Sign" to "${ext.sunSign.name} Sun • ${ext.moonSign.name} Moon (Pada ${ext.moonPada})",
            "Tithi & Paksha" to "${AstroStrings.tithiName(result.panchang.tithiNumber, AppLanguage.EN)} (${ext.pakshaLabel})",
            "Nakshatra & Lord" to "${AstroStrings.nakshatraName(result.panchang.nakshatraIndex, AppLanguage.EN)} (Pada ${ext.moonPada}) • Lord: ${ext.moonStarLord.name}",
            "Yoga / Karana" to "${AstroStrings.yogaName(result.panchang.yogaIndex, AppLanguage.EN)} • ${AstroStrings.karanaName(result.panchang.karanaIndex, AppLanguage.EN)}",
            "Sunrise / Sunset" to "${result.panchang.sunriseStr} / ${result.panchang.sunsetStr} • Dinmaan: ${ext.dinmaanStr}",
            "Moonrise / Moonset" to "${ext.moonriseStr} / ${ext.moonsetStr} • Vaar: ${AstroStrings.weekdayName(result.panchang.weekdayIndex, AppLanguage.EN)}",
            "Vedic Masa & Samvat" to "${ext.vedicMasa} • Vikram Samvat ${ext.vikramSamvat} • Shaka ${ext.shakaSamvat}",
            "Ayana & Ritu" to "${ext.ayana} • ${ext.ritu}",
            "Abhijit (Shubh)" to "${result.panchang.abhijitStr} • Hora: ${result.panchang.currentHoraLord.name}",
            "Rahukaal / Yamaganda" to "Rahu: ${result.panchang.rahukaalStr} • Yama: ${ext.yamagandaStr}",
            "Gulika Kaal" to "${ext.gulikaStr} • Sade Sati: ${AstroStrings.sadeSatiLabel(result.sadeSatiPhase, AppLanguage.EN)}"
        )
        rows.forEachIndexed { idx, (k, v) ->
            if (idx % 2 == 0) c.drawRect(MARGIN, y - 9f, PAGE_WIDTH - MARGIN, y + 6f, p.bgCard)
            c.drawText(k, MARGIN + 6f, y + 2f, p.subBold)
            c.drawText(v.take(60), MARGIN + 150f, y + 2f, p.body)
            y += 18f
        }

        y += 8f
        val d1Map = result.planets.groupBy { it.rashi }.mapValues { (_, l) -> l.map { it.planet } }
        drawNorthIndianVectorChart(c, p, (PAGE_WIDTH - 190f) / 2f, y, 190f, result.lagnaRashi, d1Map)
    }

    // 2. Charts (D1, D9, Chandra, Bhava Chalit), Baladi/Vargottama Table & Dasha Life Areas (Module 2)
    fun drawModule2ChartsAndPositionsPage(
        c: Canvas, p: Paints, astrologer: AstrologerAccount,
        result: KundaliResult, bundle: ComprehensiveReportBundle,
        pageNo: Int, totalPages: Int
    ) {
        val ext = bundle.panchangExt
        var y = drawPageChrome(c, p, astrologer, "2. Planetary Positions, 4 Charts & Baladi Avastha", result.input.name, pageNo, totalPages)

        val d1Map = result.planets.groupBy { it.rashi }.mapValues { (_, l) -> l.map { it.planet } }
        val d9Lagna = result.shodashvarga.firstOrNull { it.division == ChartDivision.NAVAMSA_D9 }?.lagnaRashi ?: result.lagnaRashi
        val d9Map = result.planets.groupBy { it.navamsaRashi }.mapValues { (_, l) -> l.map { it.planet } }
        val moonRashi = result.planets.first { it.planet == PlanetId.MOON }.rashi
        val chalitGrouped = ext.bhavaChalitMap.entries.groupBy({ it.value }, { it.key })

        // 4 Charts Grid (2x2)
        val sz = 112f
        val col1 = MARGIN + 8f
        val col2 = MARGIN + 140f
        val col3 = MARGIN + 272f
        val col4 = MARGIN + 404f
        c.drawText("Lagna (D1)", col1, y, p.subBold)
        c.drawText("Navamsa (D9)", col2, y, p.subBold)
        c.drawText("Chandra Lagna", col3, y, p.subBold)
        c.drawText("Bhava Chalit", col4, y, p.subBold)
        y += 6f
        drawNorthIndianVectorChart(c, p, col1, y, sz, result.lagnaRashi, d1Map)
        drawNorthIndianVectorChart(c, p, col2, y, sz, d9Lagna, d9Map)
        drawNorthIndianVectorChart(c, p, col3, y, sz, moonRashi, d1Map)
        drawHouseBasedNorthChart(c, p, col4, y, sz, result.lagnaRashi, chalitGrouped)
        y += sz + 18f

        c.drawText("Planetary Longitudes, Nakshatra, Baladi Avastha, Vargottama & Chalit Shift", MARGIN, y, p.sectionRed)
        y += 14f
        c.drawRect(MARGIN, y, PAGE_WIDTH - MARGIN, y + 18f, p.bgHeaderBlue)
        c.drawText("Planet", MARGIN + 4f, y + 12f, p.hdrWhite)
        c.drawText("Rashi & Deg", MARGIN + 68f, y + 12f, p.hdrWhite)
        c.drawText("Nak (P) Lord", MARGIN + 178f, y + 12f, p.hdrWhite)
        c.drawText("Baladi Avastha", MARGIN + 292f, y + 12f, p.hdrWhite)
        c.drawText("Varg / Chalit", MARGIN + 422f, y + 12f, p.hdrWhite)
        y += 24f

        result.planets.forEachIndexed { idx, pl ->
            if (idx % 2 == 0) c.drawRect(MARGIN, y - 9f, PAGE_WIDTH - MARGIN, y + 6f, p.bgCard)
            val rf = if (pl.isRetrograde && pl.planet !in listOf(PlanetId.RAHU, PlanetId.KETU)) "(R)" else ""
            c.drawText("${pl.planet.name.take(4)}$rf H${pl.houseFromLagna}", MARGIN + 4f, y + 1f, p.subBold)
            c.drawText("${pl.rashi.name.take(4)} ${pl.dmsFormatted}", MARGIN + 68f, y + 1f, p.body)
            c.drawText("${AstroStrings.nakshatraName(pl.nakshatraIndex, AppLanguage.EN).take(8)} (${pl.nakshatraPada}) ${pl.nakshatraLord.name.take(3)}", MARGIN + 178f, y + 1f, p.body)
            c.drawText((ext.baladiAvasthas[pl.planet] ?: "Active").substringBefore(" ("), MARGIN + 292f, y + 1f, p.body)
            val isVg = if (pl.planet in ext.vargottamaPlanets) "VG" else "D9:${pl.navamsaRashi.name.take(3)}"
            val ch = ext.bhavaChalitMap[pl.planet] ?: pl.houseFromLagna
            c.drawText("$isVg • Ch:H$ch", MARGIN + 422f, y + 1f, if (pl.planet in ext.vargottamaPlanets) p.bodyGreen else p.body)
            y += 19f
        }

        y += 10f
        c.drawText("Bhava Chalit Shifts & Active Vimshottari Timeline", MARGIN, y, p.sectionRed)
        y += 14f
        val shiftText = ext.chalitHouseShifts.joinToString(" | ").ifEmpty { "All planets stay in same D1/Bhava Chalit house." }
        y = drawWrappedText(c, "Chalit Shifts: $shiftText", MARGIN, y, CONTENT_WIDTH, p.body) + 6f

        ext.dashaLifeAreaInsights.take(6).forEach { (area, insight) ->
            y = drawWrappedText(c, "• $area: $insight", MARGIN, y, CONTENT_WIDTH, p.small) + 2f
        }
    }

    // 3. General Vedic Predictions / Phaladesh (Module 3)
    fun drawModule3PhaladeshPage(
        c: Canvas, p: Paints, astrologer: AstrologerAccount,
        result: KundaliResult, bundle: ComprehensiveReportBundle,
        pageNo: Int, totalPages: Int
    ) {
        var y = drawPageChrome(c, p, astrologer, "3. General Vedic Predictions (Phaladesh)", result.input.name, pageNo, totalPages)
        c.drawText("Comprehensive Vedic Phaladesh Synthesis", MARGIN, y, p.sectionRed)
        y += 16f
        bundle.phaladesh.forEach { sec ->
            if (y < 760f) {
                c.drawText(sec.topic, MARGIN, y, p.subBold)
                y += 12f
                y = drawWrappedText(c, sec.prediction, MARGIN + 6f, y, CONTENT_WIDTH - 6f, p.body) + 6f
            }
        }
        y += 4f
        if (y < 750f) {
            c.drawText("Life Areas Under Current Vimshottari Dasha (${result.currentMahadasha?.lord?.name ?: "-"})", MARGIN, y, p.sectionRed)
            y += 14f
            bundle.panchangExt.dashaLifeAreaInsights.takeLast(3).forEach { (area, ins) ->
                if (y < 785f) {
                    y = drawWrappedText(c, "• $area: $ins", MARGIN, y, CONTENT_WIDTH, p.body) + 3f
                }
            }
        }
    }

    // 4. Yogini Dasha (Module 4)
    fun drawModule4YoginiDashaPage(
        c: Canvas, p: Paints, astrologer: AstrologerAccount,
        result: KundaliResult, bundle: ComprehensiveReportBundle,
        pageNo: Int, totalPages: Int
    ) {
        var y = drawPageChrome(c, p, astrologer, "4. Yogini Dasha (36-Year Cycle — MD / AD / PD)", result.input.name, pageNo, totalPages)
        c.drawText("Yogini Mahadasha Timeline (36-Year Repeating Cycle)", MARGIN, y, p.sectionRed)
        y += 16f
        bundle.yoginiDashas.take(12).forEachIndexed { idx, ym ->
            if (idx % 2 == 0) c.drawRect(MARGIN, y - 9f, PAGE_WIDTH - MARGIN, y + 6f, p.bgCard)
            c.drawText("${ym.name} (${ym.planetLord.name.take(3)}, ${ym.years}y)", MARGIN + 6f, y + 1f, p.subBold)
            c.drawText("${ym.startDateStr} -> ${ym.endDateStr}", MARGIN + 170f, y + 1f, p.body)
            if (ym.isCurrent) c.drawText("ACTIVE", MARGIN + 390f, y + 1f, p.bodyGreen)
            y += 18f
        }
        y += 12f
        val curY = bundle.yoginiDashas.firstOrNull { it.isCurrent } ?: bundle.yoginiDashas.first()
        c.drawText("Active Yogini Antardashas (${curY.name} Mahadasha)", MARGIN, y, p.sectionRed)
        y += 14f
        curY.antardashas.forEach { ad ->
            c.drawText("${curY.name} / ${ad.lordName}: ${ad.startDateStr} to ${ad.endDateStr}" + if (ad.isCurrent) " [NOW]" else "", MARGIN + 6f, y, if (ad.isCurrent) p.bodyGreen else p.body)
            y += 14f
        }
        val curAd = curY.antardashas.firstOrNull { it.isCurrent } ?: curY.antardashas.first()
        y += 8f
        c.drawText("Active Pratyantardasha (${curY.name}-${curAd.lordName}):", MARGIN, y, p.subBold)
        y += 13f
        curAd.pratyantars.forEach { pd ->
            if (y < 785f) {
                c.drawText("${pd.lordName}: ${pd.startDateStr} - ${pd.endDateStr}" + if (pd.isCurrent) " *" else "", MARGIN + 10f, y, if (pd.isCurrent) p.bodyRed else p.small)
                y += 12f
            }
        }
    }

    // 5. Bhrigu Chakra Paddhati (Module 5)
    fun drawModule5BcpPage(
        c: Canvas, p: Paints, astrologer: AstrologerAccount,
        result: KundaliResult, bundle: ComprehensiveReportBundle,
        pageNo: Int, totalPages: Int
    ) {
        val bcp = bundle.bcp
        var y = drawPageChrome(c, p, astrologer, "5. Bhrigu Chakra Paddhati (BCP Annual House Cycle)", result.input.name, pageNo, totalPages)
        c.drawRect(MARGIN, y, PAGE_WIDTH - MARGIN, y + 54f, p.bgLightBlue)
        c.drawRect(MARGIN, y, PAGE_WIDTH - MARGIN, y + 54f, p.goldStroke)
        c.drawText("Age: ${"%.2f".format(bcp.exactAgeYears)} yrs • Active Year #${bcp.currentActiveAgeInt + 1} -> House ${bcp.currentActiveHouse} (${bcp.yearProgressPercent}% Complete)", MARGIN + 10f, y + 18f, p.titleDarkBlue)
        drawWrappedText(c, bcp.currentHouseSummary, MARGIN + 10f, y + 34f, CONTENT_WIDTH - 20f, p.body)
        y += 68f

        c.drawText("12-Year Repeating BCP Timeline (Surrounding Current Age)", MARGIN, y, p.sectionRed)
        y += 14f
        c.drawRect(MARGIN, y, PAGE_WIDTH - MARGIN, y + 18f, p.bgHeaderBlue)
        c.drawText("Age / Yr", MARGIN + 6f, y + 12f, p.hdrWhite)
        c.drawText("House", MARGIN + 95f, y + 12f, p.hdrWhite)
        c.drawText("Rashi", MARGIN + 150f, y + 12f, p.hdrWhite)
        c.drawText("Lord", MARGIN + 245f, y + 12f, p.hdrWhite)
        c.drawText("Occupants in BCP House", MARGIN + 330f, y + 12f, p.hdrWhite)
        y += 24f

        val startIdx = (bcp.currentActiveAgeInt - 6).coerceAtLeast(0)
        bcp.rows.drop(startIdx).take(26).forEachIndexed { idx, row ->
            if (row.isCurrentYear) c.drawRect(MARGIN, y - 9f, PAGE_WIDTH - MARGIN, y + 6f, p.bgGoldBanner)
            else if (idx % 2 == 0) c.drawRect(MARGIN, y - 9f, PAGE_WIDTH - MARGIN, y + 6f, p.bgCard)
            c.drawText("Age ${row.age} (${row.calendarYear})", MARGIN + 6f, y + 1f, if (row.isCurrentYear) p.bodyRed else p.subBold)
            c.drawText("H${row.activeHouse}", MARGIN + 95f, y + 1f, p.body)
            c.drawText(row.rashi.name, MARGIN + 150f, y + 1f, p.body)
            c.drawText(row.houseLord.name, MARGIN + 245f, y + 1f, p.body)
            c.drawText(row.occupants.joinToString(", ") { it.name }.ifEmpty { "—" }, MARGIN + 330f, y + 1f, p.body)
            y += 18f
        }
    }

    // 6. Placidus Bhava Cusps & 8 Jaimini Chara Karakas (Module 6)
    fun drawModule6CuspsAndKarakasPage(
        c: Canvas, p: Paints, astrologer: AstrologerAccount,
        result: KundaliResult, bundle: ComprehensiveReportBundle,
        pageNo: Int, totalPages: Int
    ) {
        var y = drawPageChrome(c, p, astrologer, "6. Placidus Bhava Cusps & 8 Jaimini Chara Karakas", result.input.name, pageNo, totalPages)
        c.drawText("12 Placidus House Cusps (Degree, Nakshatra, Star & Sub Lord)", MARGIN, y, p.sectionRed)
        y += 14f
        result.kpAnalysis?.cusps?.forEachIndexed { idx, cusp ->
            if (idx % 2 == 0) c.drawRect(MARGIN, y - 9f, PAGE_WIDTH - MARGIN, y + 6f, p.bgCard)
            val nakIdx = kotlin.math.floor(cusp.kpLongitude / (360.0 / 27.0)).toInt() % 27
            c.drawText("Cusp ${cusp.houseNumber}", MARGIN + 6f, y + 1f, p.subBold)
            c.drawText("${cusp.rashi.name} ${cusp.dmsFormatted}", MARGIN + 65f, y + 1f, p.body)
            c.drawText(AstroStrings.nakshatraName(nakIdx, AppLanguage.EN), MARGIN + 195f, y + 1f, p.body)
            c.drawText("Star: ${cusp.kp.starLord.name.take(3)} • Sub: ${cusp.kp.subLord.name.take(3)} (#${cusp.kp.subNumber249})", MARGIN + 315f, y + 1f, p.bodyRed)
            y += 17f
        }

        y += 14f
        c.drawText("8 Jaimini Chara Karakas (AK to DK — Descending Sign Degrees)", MARGIN, y, p.sectionRed)
        y += 14f
        bundle.jaimini.charaKarakas.forEachIndexed { idx, ck ->
            if (idx % 2 == 0) c.drawRect(MARGIN, y - 9f, PAGE_WIDTH - MARGIN, y + 6f, p.bgCard)
            c.drawText("${ck.karakaCode} — ${ck.karakaTitle.substringBefore(" (")}", MARGIN + 6f, y + 1f, p.subBold)
            c.drawText(ck.planet.name, MARGIN + 185f, y + 1f, p.bodyRed)
            c.drawText(ck.degInSignFormatted, MARGIN + 265f, y + 1f, p.body)
            c.drawText("D1:${ck.d1Rashi.name.take(4)} • D9:${ck.d9Rashi.name.take(4)}", MARGIN + 365f, y + 1f, p.body)
            y += 18f
        }
    }

    // 7. Nadi Analysis & Nadi Dasha Analysis (Module 7)
    fun drawModule7NadiPage(
        c: Canvas, p: Paints, astrologer: AstrologerAccount,
        result: KundaliResult, bundle: ComprehensiveReportBundle,
        pageNo: Int, totalPages: Int
    ) {
        val nadi = bundle.nadi
        var y = drawPageChrome(c, p, astrologer, "7. Nakshatra Nadi & Nadi Dasha Analysis", result.input.name, pageNo, totalPages)
        y = drawWrappedText(c, nadi.activeChainSummary, MARGIN, y, CONTENT_WIDTH, p.subBold) + 8f

        c.drawText("Planet -> Star Lord -> Sub Lord -> Sub-Sub -> Praana Nadi Script", MARGIN, y, p.sectionRed)
        y += 14f
        nadi.rows.forEachIndexed { idx, r ->
            if (idx % 2 == 0) c.drawRect(MARGIN, y - 9f, PAGE_WIDTH - MARGIN, y + 6f, p.bgCard)
            c.drawText(r.planet.name, MARGIN + 4f, y + 1f, p.subBold)
            c.drawText("${r.starLord.name.take(3)}->${r.subLord.name.take(3)}->${r.subSubLord.name.take(3)}->${r.praanaLord.name.take(3)}", MARGIN + 72f, y + 1f, p.body)
            c.drawText("H:${r.hitHouses.joinToString(",")}", MARGIN + 255f, y + 1f, p.bodyRed)
            c.drawText(r.qualityLabel, MARGIN + 400f, y + 1f, p.small)
            y += 18f
        }

        y += 12f
        c.drawText("Nadi Dasha Life-Area Activation & Confidence Score", MARGIN, y, p.sectionRed)
        y += 14f
        nadi.dashaLifeAreas.forEachIndexed { idx, area ->
            if (idx % 2 == 0) c.drawRect(MARGIN, y - 9f, PAGE_WIDTH - MARGIN, y + 6f, p.bgCard)
            c.drawText(area.lifeArea, MARGIN + 6f, y + 1f, p.subBold)
            c.drawText("${area.confidencePct}% • ${area.direction}", MARGIN + 170f, y + 1f, if (area.confidencePct >= 70) p.bodyGreen else p.body)
            c.drawText(area.activatedHouses, MARGIN + 350f, y + 1f, p.bodyRed)
            y += 18f
        }
    }

    // 8. KP System Analysis, 4-Step Theory, PoF, Transits & Guidance (Module 8)
    fun drawModule8KpExtendedPage(
        c: Canvas, p: Paints, astrologer: AstrologerAccount,
        result: KundaliResult, bundle: ComprehensiveReportBundle,
        pageNo: Int, totalPages: Int
    ) {
        val kpExt = bundle.kpExt
        var y = drawPageChrome(c, p, astrologer, "8. KP System: 4-Step Theory, Part of Fortune & Scorecards", result.input.name, pageNo, totalPages)
        c.drawText("Part of Fortune (Pars Fortuna): ${kpExt.partOfFortuneRashi.name} (House ${kpExt.partOfFortuneHouse}) • KP Ayanamsa: ${"%.4f".format(result.kpAnalysis?.kpAyanamsaDeg ?: 24.0)}°", MARGIN, y, p.subBold)
        y += 13f
        c.drawText("Current Ruling Planets: ${kpExt.currentRulingPlanets.distinctRulers.joinToString(", ") { it.name }}", MARGIN, y, p.bodyRed)
        y += 14f
        y = drawWrappedText(c, kpExt.predictionSummary, MARGIN, y, CONTENT_WIDTH, p.body) + 8f

        c.drawText("KP 4-Step Significator Matrix (Self / Star / Sub / Sub-Star)", MARGIN, y, p.sectionRed)
        y += 13f
        kpExt.fourStepRows.forEachIndexed { idx, row ->
            if (idx % 2 == 0) c.drawRect(MARGIN, y - 8f, PAGE_WIDTH - MARGIN, y + 5f, p.bgCard)
            c.drawText(row.planet.name, MARGIN + 4f, y + 1f, p.subBold)
            c.drawText("1:${row.step1SelfHouses.joinToString(",")} | 2:${row.step2StarLordHouses.take(3).joinToString(",")}", MARGIN + 72f, y + 1f, p.small)
            c.drawText("3:${row.step3SubLordHouses.take(3).joinToString(",")} | 4:${row.step4SubStarHouses.take(3).joinToString(",")}", MARGIN + 230f, y + 1f, p.small)
            c.drawText("6-Fold: ${row.sixFoldSummary.take(20)}", MARGIN + 395f, y + 1f, p.bodyRed)
            y += 15f
        }

        y += 10f
        c.drawText("KP Life-Area Scorecards & Timing Guidance (12 Core Areas)", MARGIN, y, p.sectionRed)
        y += 14f
        kpExt.scorecards.forEachIndexed { idx, sc ->
            if (idx % 2 == 0) c.drawRect(MARGIN, y - 8f, PAGE_WIDTH - MARGIN, y + 5f, p.bgCard)
            c.drawText("${sc.area} (C${sc.primaryCusp})", MARGIN + 4f, y + 1f, p.subBold)
            c.drawText("SL:${sc.cuspSubLord.name.take(3)}", MARGIN + 135f, y + 1f, p.body)
            c.drawText("${sc.scorePct}% (${sc.status})", MARGIN + 195f, y + 1f, if (sc.scorePct >= 75) p.bodyGreen else p.bodyRed)
            c.drawText(sc.timingHint.take(34), MARGIN + 355f, y + 1f, p.small)
            y += 15f
        }
    }

    // 9 & 10. Special Lagnas & Classical Vedic Yogas (Modules 9 & 10)
    fun drawModule9And10LagnasAndYogasPage(
        c: Canvas, p: Paints, astrologer: AstrologerAccount,
        result: KundaliResult, bundle: ComprehensiveReportBundle,
        pageNo: Int, totalPages: Int
    ) {
        var y = drawPageChrome(c, p, astrologer, "9 & 10. Special Lagnas & Classical Vedic Yogas", result.input.name, pageNo, totalPages)
        c.drawText("9. Special Lagnas & Sensitive Points (BL, HL, GL, VGL, VL, SL, PL, IL, BB)", MARGIN, y, p.sectionRed)
        y += 14f
        bundle.specialLagnas.forEachIndexed { idx, sl ->
            if (idx % 2 == 0) c.drawRect(MARGIN, y - 8f, PAGE_WIDTH - MARGIN, y + 5f, p.bgCard)
            c.drawText("${sl.code} — ${sl.name}", MARGIN + 4f, y + 1f, p.subBold)
            c.drawText("${sl.rashi.name} ${sl.dmsFormatted}", MARGIN + 205f, y + 1f, p.body)
            c.drawText("H${sl.houseFromLagna}", MARGIN + 355f, y + 1f, p.bodyRed)
            c.drawText(sl.nakshatraName, MARGIN + 400f, y + 1f, p.body)
            y += 16f
        }

        y += 12f
        c.drawText("10. Rule-Verified Vedic Yogas from D1 Birth Chart", MARGIN, y, p.sectionRed)
        y += 14f
        bundle.vedicYogas.take(9).forEachIndexed { idx, yoga ->
            if (y < 765f) {
                if (idx % 2 == 0) c.drawRect(MARGIN, y - 8f, PAGE_WIDTH - MARGIN, y + 19f, p.bgCard)
                c.drawText("${yoga.name} [${yoga.category} • ${yoga.strength}]", MARGIN + 4f, y + 1f, p.subBold)
                y += 11f
                y = drawWrappedText(c, "${yoga.condition} -> ${yoga.effect}", MARGIN + 8f, y, CONTENT_WIDTH - 10f, p.small, 11f) + 4f
            }
        }
    }

    // 11. All 20 Divisional Charts Matrix (D1..D60 incl D6, D8, D28, D50) (Module 11)
    fun drawModule11All20VargasMatrixPage(
        c: Canvas, p: Paints, astrologer: AstrologerAccount,
        result: KundaliResult, bundle: ComprehensiveReportBundle,
        pageNo: Int, totalPages: Int
    ) {
        var y = drawPageChrome(c, p, astrologer, "11. Complete 20 Divisional Charts Matrix (D1 to D60)", result.input.name, pageNo, totalPages)
        c.drawText("20 Harmonic Charts: D1, D2, D3, D4, D6, D7, D8, D9, D10, D12, D16, D20, D24, D27, D28, D30, D40, D45, D50, D60", MARGIN, y, p.subBold)
        y += 16f
        c.drawRect(MARGIN, y, PAGE_WIDTH - MARGIN, y + 18f, p.bgHeaderBlue)
        c.drawText("Div", MARGIN + 4f, y + 12f, p.hdrWhite)
        c.drawText("Asc", MARGIN + 42f, y + 12f, p.hdrWhite)
        PlanetId.entries.forEachIndexed { idx, pl ->
            c.drawText(AstroStrings.planetShort(pl, AppLanguage.EN), MARGIN + 88f + idx * 48f, y + 12f, p.hdrWhite)
        }
        y += 24f
        bundle.all20DivisionalCharts.forEachIndexed { idx, v ->
            if (idx % 2 == 0) c.drawRect(MARGIN, y - 9f, PAGE_WIDTH - MARGIN, y + 6f, p.bgCard)
            c.drawText(v.code, MARGIN + 4f, y + 1f, p.subBold)
            c.drawText(v.lagnaRashi.name.take(3), MARGIN + 42f, y + 1f, p.bodyRed)
            PlanetId.entries.forEachIndexed { pIdx, pl ->
                val r = v.planetRashis[pl]
                c.drawText(r?.name?.take(3) ?: "-", MARGIN + 88f + pIdx * 48f, y + 1f, p.body)
            }
            y += 18f
        }
    }
}

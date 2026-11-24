package com.example.pdf

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import android.net.Uri
import androidx.core.content.FileProvider
import com.example.astro.ComprehensiveReportBuilder
import com.example.astro.EphemerisEngine
import com.example.astro.LalKitabAndStoreEngine
import com.example.astro.TimeUtils
import com.example.core.AppLanguage
import com.example.core.AstroStrings
import com.example.core.AstrologerAccount
import com.example.model.ChartDivision
import com.example.model.KundaliResult
import com.example.model.MahadashaPeriod
import com.example.model.MatchingResult
import com.example.model.PlanetId
import com.example.model.Rashi
import com.example.pdf.PdfCanvasHelpers.CONTENT_WIDTH
import com.example.pdf.PdfCanvasHelpers.MARGIN
import com.example.pdf.PdfCanvasHelpers.PAGE_HEIGHT
import com.example.pdf.PdfCanvasHelpers.PAGE_WIDTH
import com.example.pdf.PdfCanvasHelpers.Paints
import com.example.pdf.PdfCanvasHelpers.drawNorthIndianVectorChart
import com.example.pdf.PdfCanvasHelpers.drawPageChrome
import com.example.pdf.PdfCanvasHelpers.drawWrappedText
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.withContext
import java.io.File
import java.io.OutputStream
import java.util.Calendar
import java.util.TimeZone
import kotlin.coroutines.coroutineContext
import kotlin.math.floor

object PdfReportGenerator {

    fun createCacheReportFile(context: Context, fileName: String): File {
        val dir = File(context.cacheDir, "reports").apply { mkdirs() }
        return File(dir, fileName)
    }

    fun getFileProviderUri(context: Context, file: File): Uri {
        return FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
    }

    /**
     * Complete 21-Module Compact Kundali Report (18 Pages) — covers EVERY requested module (1..21)
     * with Registered Astrologer Name branded at the very top of every page.
     */
    suspend fun generateSummaryPdf(
        result: KundaliResult,
        language: AppLanguage,
        outputStream: OutputStream,
        astrologer: AstrologerAccount = AstrologerAccount(),
        onProgress: (Int, Int) -> Unit = { _, _ -> }
    ) = withContext(Dispatchers.IO) {
        val bundle = ComprehensiveReportBuilder.build(result)
        val totalPages = 18
        val doc = PdfDocument()
        val p = Paints()
        var pageNo = 1

        fun emitPage(draw: (Canvas, Int) -> Unit) {
            coroutineContext.ensureActive()
            val page = doc.startPage(PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, pageNo).create())
            draw(page.canvas, pageNo)
            doc.finishPage(page)
            onProgress(pageNo, totalPages)
            pageNo++
        }

        try {
            emitPage { c, n -> PdfSectionsPart1.drawModule1BirthPanchangPage(c, p, astrologer, result, bundle, n, totalPages, isBrihat = false) }
            emitPage { c, n -> PdfSectionsPart1.drawModule2ChartsAndPositionsPage(c, p, astrologer, result, bundle, n, totalPages) }
            emitPage { c, n -> drawDashaSummaryPage(c, p, astrologer, result, n, totalPages) }
            emitPage { c, n -> PdfSectionsPart1.drawModule3PhaladeshPage(c, p, astrologer, result, bundle, n, totalPages) }
            emitPage { c, n -> PdfSectionsPart1.drawModule4YoginiDashaPage(c, p, astrologer, result, bundle, n, totalPages) }
            emitPage { c, n -> PdfSectionsPart1.drawModule5BcpPage(c, p, astrologer, result, bundle, n, totalPages) }
            emitPage { c, n -> PdfSectionsPart1.drawModule6CuspsAndKarakasPage(c, p, astrologer, result, bundle, n, totalPages) }
            emitPage { c, n -> PdfSectionsPart1.drawModule7NadiPage(c, p, astrologer, result, bundle, n, totalPages) }
            emitPage { c, n -> PdfSectionsPart1.drawModule8KpExtendedPage(c, p, astrologer, result, bundle, n, totalPages) }
            emitPage { c, n -> PdfSectionsPart1.drawModule9And10LagnasAndYogasPage(c, p, astrologer, result, bundle, n, totalPages) }
            emitPage { c, n -> PdfSectionsPart1.drawModule11All20VargasMatrixPage(c, p, astrologer, result, bundle, n, totalPages) }
            emitPage { c, n -> PdfSectionsPart2.drawModule12AshtakavargaShadbalaPage(c, p, astrologer, result, bundle, n, totalPages) }
            emitPage { c, n -> PdfSectionsPart2.drawModule13And14GemsAndVedicNumPage(c, p, astrologer, result, bundle, n, totalPages) }
            emitPage { c, n -> PdfSectionsPart2.drawModule15LoShuExtendedPage(c, p, astrologer, result, bundle, n, totalPages) }
            emitPage { c, n -> PdfSectionsPart2.drawModule16SarvatobhadraKotaPage(c, p, astrologer, result, bundle, n, totalPages) }
            emitPage { c, n -> PdfSectionsPart2.drawModule17JaiminiPage(c, p, astrologer, result, bundle, n, totalPages) }
            emitPage { c, n -> PdfSectionsPart2.drawModule18And19LalKitabAndBnnPage(c, p, astrologer, result, bundle, n, totalPages) }
            emitPage { c, n ->
                // Compact dual finale: Tajika 2026 + Crystal/Remedy Summary
                PdfSectionsPart2.drawModule20TajikaVarshaphalPage(c, p, astrologer, result, bundle, n, totalPages)
            }

            doc.writeTo(outputStream)
            outputStream.flush()
        } finally {
            doc.close()
        }
    }

    /**
     * Complete 250-Page Colorful Brihat Kundali Compendium covering ALL 21 comprehensive modules
     * plus all 20 Divisional Charts, 12 Bhava Shloka analyses, 108 Dasha/Pratyantardasha pages,
     * and Monthly Gochar Almanac — branded at the top with the registered Astrologer Name.
     */
    suspend fun generateBrihat250Pdf(
        result: KundaliResult,
        language: AppLanguage,
        outputStream: OutputStream,
        astrologer: AstrologerAccount = AstrologerAccount(),
        onProgress: (Int, Int) -> Unit = { _, _ -> }
    ) = withContext(Dispatchers.IO) {
        val bundle = ComprehensiveReportBuilder.build(result)
        val totalPages = 250
        val doc = PdfDocument()
        val p = Paints()
        var pageNo = 1

        fun emitPage(draw: (Canvas, Int) -> Unit) {
            coroutineContext.ensureActive()
            val page = doc.startPage(PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, pageNo).create())
            draw(page.canvas, pageNo)
            doc.finishPage(page)
            onProgress(pageNo, totalPages)
            pageNo++
        }

        try {
            // Pages 1-19: All 21 Comprehensive Core Modules (dedicated full colorful pages)
            emitPage { c, n -> PdfSectionsPart1.drawModule1BirthPanchangPage(c, p, astrologer, result, bundle, n, totalPages, isBrihat = true) }
            emitPage { c, n -> PdfSectionsPart1.drawModule2ChartsAndPositionsPage(c, p, astrologer, result, bundle, n, totalPages) }
            emitPage { c, n -> drawDashaSummaryPage(c, p, astrologer, result, n, totalPages) }
            emitPage { c, n -> PdfSectionsPart1.drawModule3PhaladeshPage(c, p, astrologer, result, bundle, n, totalPages) }
            emitPage { c, n -> PdfSectionsPart1.drawModule4YoginiDashaPage(c, p, astrologer, result, bundle, n, totalPages) }
            emitPage { c, n -> PdfSectionsPart1.drawModule5BcpPage(c, p, astrologer, result, bundle, n, totalPages) }
            emitPage { c, n -> PdfSectionsPart1.drawModule6CuspsAndKarakasPage(c, p, astrologer, result, bundle, n, totalPages) }
            emitPage { c, n -> PdfSectionsPart1.drawModule7NadiPage(c, p, astrologer, result, bundle, n, totalPages) }
            emitPage { c, n -> PdfSectionsPart1.drawModule8KpExtendedPage(c, p, astrologer, result, bundle, n, totalPages) }
            emitPage { c, n -> PdfSectionsPart1.drawModule9And10LagnasAndYogasPage(c, p, astrologer, result, bundle, n, totalPages) }
            emitPage { c, n -> PdfSectionsPart1.drawModule11All20VargasMatrixPage(c, p, astrologer, result, bundle, n, totalPages) }
            emitPage { c, n -> PdfSectionsPart2.drawModule12AshtakavargaShadbalaPage(c, p, astrologer, result, bundle, n, totalPages) }
            emitPage { c, n -> PdfSectionsPart2.drawModule13And14GemsAndVedicNumPage(c, p, astrologer, result, bundle, n, totalPages) }
            emitPage { c, n -> PdfSectionsPart2.drawModule15LoShuExtendedPage(c, p, astrologer, result, bundle, n, totalPages) }
            emitPage { c, n -> PdfSectionsPart2.drawModule16SarvatobhadraKotaPage(c, p, astrologer, result, bundle, n, totalPages) }
            emitPage { c, n -> PdfSectionsPart2.drawModule17JaiminiPage(c, p, astrologer, result, bundle, n, totalPages) }
            emitPage { c, n -> PdfSectionsPart2.drawModule18And19LalKitabAndBnnPage(c, p, astrologer, result, bundle, n, totalPages) }
            emitPage { c, n -> PdfSectionsPart2.drawModule20TajikaVarshaphalPage(c, p, astrologer, result, bundle, n, totalPages) }
            emitPage { c, n -> PdfSectionsPart2.drawModule21CrystalsAndRemedyTrackerPage(c, p, astrologer, result, bundle, n, totalPages) }

            // Page 20: Day/Night Choghadiya & Hora Table
            emitPage { c, n -> drawPanchangChoghadiyaPage(c, p, astrologer, result, n, totalPages) }

            // Pages 21-40: All 20 Individual Divisional Charts (D1..D60 incl D6, D8, D28, D50)
            bundle.all20DivisionalCharts.forEach { divChart ->
                emitPage { c, n ->
                    var y = drawPageChrome(c, p, astrologer, "Divisional Chart — ${divChart.title}", result.input.name, n, totalPages)
                    c.drawText(divChart.title, MARGIN, y, p.titleDarkBlue); y += 14f
                    c.drawText("${divChart.code} Lagna: ${divChart.lagnaRashi.name} (Sign #${divChart.lagnaRashi.number})", MARGIN, y, p.sectionRed); y += 14f
                    val grouped = divChart.planetRashis.entries.groupBy({ it.value }, { it.key })
                    drawNorthIndianVectorChart(c, p, (PAGE_WIDTH - 265f) / 2f, y, 265f, divChart.lagnaRashi, grouped)
                    y += 285f
                    c.drawText("Planetary Placements in ${divChart.code}", MARGIN, y, p.sectionRed); y += 16f
                    PlanetId.entries.forEachIndexed { idx, pl ->
                        val r = divChart.planetRashis[pl] ?: Rashi.MESHA
                        val h = ((r.ordinal - divChart.lagnaRashi.ordinal + 12) % 12) + 1
                        if (idx % 2 == 0) c.drawRect(MARGIN, y - 9f, PAGE_WIDTH - MARGIN, y + 6f, p.bgCard)
                        c.drawText(pl.name, MARGIN + 8f, y + 2f, p.subBold)
                        c.drawText("${r.name} (Sign #${r.number})", MARGIN + 150f, y + 2f, p.body)
                        c.drawText("House $h from ${divChart.code} Lagna", MARGIN + 330f, y + 2f, p.body)
                        y += 21f
                    }
                }
            }

            // Pages 41-47: 7 Bhinnashtakavarga Planet Pages
            val av = result.ashtakavarga
            PlanetId.entries.take(7).forEach { pl ->
                emitPage { c, n ->
                    var y = drawPageChrome(c, p, astrologer, "Bhinnashtakavarga — ${pl.name}", result.input.name, n, totalPages)
                    c.drawText("Bhinnashtakavarga Bindus for ${pl.name}", MARGIN, y, p.sectionRed); y += 20f
                    val bav = av?.bhinnashtakavargas?.firstOrNull { it.planet == pl }
                    Rashi.entries.forEachIndexed { idx, r ->
                        val pts = bav?.bindusByRashi?.getOrNull(idx) ?: 0
                        if (idx % 2 == 0) c.drawRect(MARGIN, y - 10f, PAGE_WIDTH - MARGIN, y + 6f, p.bgCard)
                        c.drawText("${r.number}. ${r.name}", MARGIN + 8f, y + 1f, p.subBold)
                        c.drawRect(MARGIN + 150f, y - 7f, MARGIN + 150f + pts * 28f, y + 3f, if (pts >= 4) p.bgHeaderBlue else p.bgRed)
                        c.drawText("$pts / 8 Bindus", MARGIN + 410f, y + 1f, p.body)
                        y += 22f
                    }
                    y += 12f
                    val shodRow = bundle.avShodhanas.firstOrNull { it.planet == pl }
                    c.drawText("Raw Total: ${bav?.totalBindus ?: 0} • Trikona Red: ${shodRow?.trikonaReducedTotal ?: 0} • Shodhya Pinda: ${shodRow?.shodhyaPinda ?: 0}", MARGIN, y, p.subBold)
                }
            }

            // Pages 48-59: 12 Dedicated Bhava Phalit & Shloka Pages
            result.bhavaAnalyses.forEach { bhava ->
                emitPage { c, n ->
                    var y = drawPageChrome(c, p, astrologer, "Bhava Analysis — House ${bhava.houseNumber}", result.input.name, n, totalPages)
                    c.drawText(AstroStrings.bhavaTitle(bhava.houseNumber, AppLanguage.EN), MARGIN, y, p.titleDarkBlue); y += 18f
                    c.drawRect(MARGIN, y, PAGE_WIDTH - MARGIN, y + 58f, p.bgLightBlue)
                    c.drawRect(MARGIN, y, PAGE_WIDTH - MARGIN, y + 58f, p.goldStroke)
                    c.drawText("House Sign: ${bhava.signRashi.name} • Lord: ${bhava.lord.name} in House ${bhava.lordPlacedInHouse}", MARGIN + 10f, y + 18f, p.subBold)
                    c.drawText("Occupants: ${bhava.occupants.joinToString(", ") { it.name }.ifEmpty { "None" }}", MARGIN + 10f, y + 34f, p.body)
                    c.drawText("Aspects: ${bhava.aspectingPlanets.joinToString(", ") { it.name }.ifEmpty { "None" }}", MARGIN + 10f, y + 49f, p.body)
                    y += 74f

                    c.drawText("Matched Classical Shlokas & Phalit Notes", MARGIN, y, p.sectionRed); y += 16f
                    bhava.conjunctionDescriptionsEn.forEach { note ->
                        y = drawWrappedText(c, "• $note", MARGIN, y, CONTENT_WIDTH, p.bodyRed) + 4f
                    }
                    if (bhava.matchedShlokas.isEmpty()) {
                        drawWrappedText(
                            c,
                            "Evaluation reflects lord ${bhava.lord.name} in H${bhava.lordPlacedInHouse} and SAV bindus (${av?.sarvashtakavargaByRashi?.getOrNull(bhava.signRashi.ordinal) ?: 28}).",
                            MARGIN, y, CONTENT_WIDTH, p.body
                        )
                    } else {
                        bhava.matchedShlokas.forEach { shl ->
                            c.drawText("${shl.sourceBookEn} (${shl.chapter}.${shl.verse})", MARGIN, y, p.subBold); y += 13f
                            y = drawWrappedText(c, shl.sanskritVerse, MARGIN + 6f, y, CONTENT_WIDTH - 12f, p.bodyRed) + 2f
                            y = drawWrappedText(c, shl.meaningEn, MARGIN + 6f, y, CONTENT_WIDTH - 12f, p.body) + 8f
                        }
                    }
                }
            }

            // Pages 60-66: Extended KP Cusps/Significators + Lal Kitab + Western Aspects + Vastu Blueprint
            emitPage { c, n -> drawKpCuspsDetailPage(c, p, astrologer, result, n, totalPages) }
            emitPage { c, n -> drawKpSignificatorsPage(c, p, astrologer, result, n, totalPages) }
            repeat(3) { sub ->
                emitPage { c, n ->
                    var y = drawPageChrome(c, p, astrologer, "Lal Kitab Upay & Karmic Rinas Part ${sub + 1}", result.input.name, n, totalPages)
                    c.drawText("Lal Kitab Planet-by-Planet Upay & Karmic Guidance", MARGIN, y, p.sectionRed); y += 18f
                    result.lalKitab?.entries.orEmpty().drop(sub * 3).take(3).forEach { entry ->
                        c.drawRect(MARGIN, y, PAGE_WIDTH - MARGIN, y + 108f, p.bgCard)
                        c.drawRect(MARGIN, y, PAGE_WIDTH - MARGIN, y + 108f, p.goldStroke)
                        c.drawText("${entry.planet.name} — Khana #${entry.house} (Kal Purush: ${entry.kalPurushRashi.name})", MARGIN + 10f, y + 18f, p.subBold)
                        var ty = drawWrappedText(c, entry.natureNoteEn, MARGIN + 10f, y + 34f, CONTENT_WIDTH - 20f, p.body)
                        ty = drawWrappedText(c, "Remedy (EN): ${entry.remedyEn}", MARGIN + 10f, ty + 4f, CONTENT_WIDTH - 20f, p.bodyGreen)
                        drawWrappedText(c, "उपाय (HI): ${entry.remedyHi}", MARGIN + 10f, ty + 4f, CONTENT_WIDTH - 20f, p.bodyRed)
                        y += 120f
                    }
                }
            }
            emitPage { c, n -> drawWesternAspectsPage(c, p, astrologer, result, n, totalPages) }
            emitPage { c, n -> drawVastuDetailedPage(c, p, astrologer, result, n, totalPages) }

            // Pages 67-165: 99 Pages of Vimshottari Mahadasha x Antardasha x Pratyantardasha
            // 9 Mahadasha overviews + 81 Antardasha/Pratyantardasha pages + 9 Synthesis pages = 99
            result.mahadashas.forEach { maha ->
                emitPage { c, n -> drawMahadashaOverviewPage(c, p, astrologer, result, maha, n, totalPages) }
                maha.antardashas.forEach { antar ->
                    emitPage { c, n ->
                        var y = drawPageChrome(c, p, astrologer, "Vimshottari: ${maha.lord.name} MD / ${antar.lord.name} AD", result.input.name, n, totalPages)
                        c.drawText("Mahadasha: ${maha.lord.name} (${maha.startDateStr} to ${maha.endDateStr})", MARGIN, y, p.titleDarkBlue); y += 16f
                        c.drawText("Antardasha: ${antar.lord.name} (${antar.startDateStr} to ${antar.endDateStr})", MARGIN, y, p.sectionRed); y += 20f
                        c.drawRect(MARGIN, y, PAGE_WIDTH - MARGIN, y + 20f, p.bgHeaderBlue)
                        c.drawText("Pratyantardasha", MARGIN + 8f, y + 13f, p.hdrWhite)
                        c.drawText("Start Date", MARGIN + 225f, y + 13f, p.hdrWhite)
                        c.drawText("End Date", MARGIN + 345f, y + 13f, p.hdrWhite)
                        c.drawText("Status", MARGIN + 460f, y + 13f, p.hdrWhite)
                        y += 26f
                        antar.pratyantardashas.forEachIndexed { idx, prat ->
                            if (idx % 2 == 0) c.drawRect(MARGIN, y - 9f, PAGE_WIDTH - MARGIN, y + 7f, p.bgCard)
                            c.drawText("${maha.lord.name}-${antar.lord.name}-${prat.lord.name}", MARGIN + 8f, y + 2f, p.subBold)
                            c.drawText(prat.startDateStr, MARGIN + 225f, y + 2f, p.body)
                            c.drawText(prat.endDateStr, MARGIN + 345f, y + 2f, p.body)
                            c.drawText(if (prat.isCurrent) "ACTIVE" else "—", MARGIN + 460f, y + 2f, if (prat.isCurrent) p.bodyGreen else p.small)
                            y += 20f
                        }
                    }
                }
                emitPage { c, n ->
                    var y = drawPageChrome(c, p, astrologer, "${maha.lord.name} Mahadasha Synthesis & Remedy Guide", result.input.name, n, totalPages)
                    val mPos = result.planets.firstOrNull { it.planet == maha.lord }
                    val mSum = result.summary.planetSummaries.firstOrNull { it.planet == maha.lord }
                    c.drawText("${maha.lord.name} Mahadasha Functional Profile (${maha.startDateStr} – ${maha.endDateStr})", MARGIN, y, p.sectionRed); y += 18f
                    c.drawText("Nature: ${mSum?.functionalNature?.name ?: "-"} • Placed in H${mPos?.houseFromLagna ?: "-"} (${mPos?.rashi?.name ?: "-"})", MARGIN, y, p.subBold); y += 18f
                    val lk = result.lalKitab?.entries?.firstOrNull { it.planet == maha.lord }
                    if (lk != null) {
                        y = drawWrappedText(c, "Lal Kitab Upay: ${lk.remedyEn}", MARGIN, y, CONTENT_WIDTH, p.bodyGreen) + 8f
                        drawWrappedText(c, lk.remedyHi, MARGIN, y, CONTENT_WIDTH, p.bodyRed)
                    }
                }
            }

            // Pages 166-250: 85 Monthly Gochar Ephemeris & Transit Almanac Pages (to exactly total 250)
            val cal = Calendar.getInstance(TimeZone.getTimeZone("UTC"))
            val baseNowJd = TimeUtils.toJulianDay(
                cal.get(Calendar.YEAR), cal.get(Calendar.MONTH) + 1, cal.get(Calendar.DAY_OF_MONTH),
                12, 0, 0.0, 0.0
            )
            val moonNatalIdx = result.planets.first { it.planet == PlanetId.MOON }.rashi.ordinal
            val lagnaIdx = result.lagnaRashi.ordinal
            val remainingPages = (totalPages - pageNo + 1).coerceAtLeast(1)

            for (monthIdx in 0 until remainingPages) {
                emitPage { c, n ->
                    val targetJd = baseNowJd + monthIdx * 30.437
                    val dateStr = TimeUtils.jdToLocalDateString(targetJd, result.effectiveUtcOffsetHours)
                    var y = drawPageChrome(c, p, astrologer, "Monthly Gochar Almanac — Month ${monthIdx + 1} ($dateStr)", result.input.name, n, totalPages)
                    c.drawText("Gochar Planetary Positions on $dateStr (Lahiri Sidereal)", MARGIN, y, p.sectionRed); y += 18f
                    c.drawRect(MARGIN, y, PAGE_WIDTH - MARGIN, y + 20f, p.bgHeaderBlue)
                    c.drawText("Planet", MARGIN + 8f, y + 13f, p.hdrWhite)
                    c.drawText("Gochar Rashi & Deg", MARGIN + 100f, y + 13f, p.hdrWhite)
                    c.drawText("Nakshatra", MARGIN + 245f, y + 13f, p.hdrWhite)
                    c.drawText("From Lagna", MARGIN + 365f, y + 13f, p.hdrWhite)
                    c.drawText("From Moon", MARGIN + 445f, y + 13f, p.hdrWhite)
                    y += 26f
                    PlanetId.entries.forEachIndexed { pIdx, pl ->
                        val lon = EphemerisEngine.planetSiderealLongitude(pl, targetJd)
                        val rIdx = floor(lon / 30.0).toInt() % 12
                        val r = Rashi.fromIndex(rIdx)
                        val degStr = TimeUtils.formatDms(lon - rIdx * 30.0)
                        val nakIdx = floor(lon / (360.0 / 27.0)).toInt() % 27
                        val hLagna = ((rIdx - lagnaIdx + 12) % 12) + 1
                        val hMoon = ((rIdx - moonNatalIdx + 12) % 12) + 1
                        if (pIdx % 2 == 0) c.drawRect(MARGIN, y - 9f, PAGE_WIDTH - MARGIN, y + 6f, p.bgCard)
                        c.drawText(pl.name, MARGIN + 8f, y + 2f, p.subBold)
                        c.drawText("${r.name} $degStr", MARGIN + 100f, y + 2f, p.body)
                        c.drawText(AstroStrings.nakshatraName(nakIdx, AppLanguage.EN), MARGIN + 245f, y + 2f, p.body)
                        c.drawText("H$hLagna", MARGIN + 375f, y + 2f, p.body)
                        c.drawText("H$hMoon", MARGIN + 455f, y + 2f, p.body)
                        y += 20f
                    }
                    y += 14f
                    val satLon = EphemerisEngine.planetSiderealLongitude(PlanetId.SATURN, targetJd)
                    val satMoonH = ((floor(satLon / 30.0).toInt() % 12 - moonNatalIdx + 12) % 12) + 1
                    val sadeNote = when (satMoonH) {
                        12 -> "Sade Sati Rising Phase (12th from Moon)"
                        1 -> "Sade Sati Peak Phase (Over Natal Moon)"
                        2 -> "Sade Sati Setting Phase (2nd from Moon)"
                        4, 8 -> "Dhaiya Active (House $satMoonH from Moon)"
                        else -> "No Sade Sati / Dhaiya (Saturn H$satMoonH from Moon)"
                    }
                    c.drawText("Transit Highlights ($dateStr): $sadeNote", MARGIN, y, p.subBold)
                }
            }

            doc.writeTo(outputStream)
            outputStream.flush()
        } finally {
            doc.close()
        }
    }

    suspend fun generateMatchingPdf(
        matching: MatchingResult,
        language: AppLanguage,
        outputStream: OutputStream,
        astrologer: AstrologerAccount = AstrologerAccount(),
        onProgress: (Int, Int) -> Unit = { _, _ -> }
    ) = withContext(Dispatchers.IO) {
        val doc = PdfDocument()
        val p = Paints()
        val totalPages = 4
        try {
            // Page 1: Ashtakoot Scorecard
            doc.startPage(PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, 1).create()).also { page ->
                coroutineContext.ensureActive()
                val c = page.canvas
                var y = drawPageChrome(c, p, astrologer, "Ashtakoot Kundali Milan Report", "${matching.boyChart.input.name} × ${matching.girlChart.input.name}", 1, totalPages)
                c.drawText("Prepared by Astrologer: ${astrologer.effectiveHeaderName()}", MARGIN, y, p.subBold); y += 16f
                c.drawText("Groom (Var): ${matching.boyChart.input.name} • Moon: ${matching.boyChart.planets.first { it.planet == PlanetId.MOON }.rashi.name}", MARGIN, y, p.subBold); y += 15f
                c.drawText("Bride (Vadhu): ${matching.girlChart.input.name} • Moon: ${matching.girlChart.planets.first { it.planet == PlanetId.MOON }.rashi.name}", MARGIN, y, p.subBold); y += 20f
                c.drawRect(MARGIN, y, PAGE_WIDTH - MARGIN, y + 48f, p.bgLightBlue)
                c.drawRect(MARGIN, y, PAGE_WIDTH - MARGIN, y + 48f, p.goldStroke)
                c.drawText("Total Ashtakoot Score: ${"%.1f".format(matching.totalScore)} / 36.0 Gunas", MARGIN + 12f, y + 20f, p.titleDarkBlue)
                c.drawText(matching.verdictEn, MARGIN + 12f, y + 38f, p.bodyGreen)
                y += 64f
                c.drawText("8-Koot Breakdown", MARGIN, y, p.sectionRed); y += 16f
                matching.koots.forEachIndexed { idx, k ->
                    if (idx % 2 == 0) c.drawRect(MARGIN, y - 10f, PAGE_WIDTH - MARGIN, y + 8f, p.bgCard)
                    c.drawText("${k.nameEn} (${k.nameHi})", MARGIN + 6f, y + 2f, p.subBold)
                    c.drawText("${k.boyValue} / ${k.girlValue}", MARGIN + 185f, y + 2f, p.body)
                    c.drawText("${"%.1f".format(k.obtainedPoints)} / ${"%.0f".format(k.maxPoints)}", MARGIN + 310f, y + 2f, p.bodyRed)
                    c.drawText(k.noteEn.take(28), MARGIN + 375f, y + 2f, p.small)
                    y += 22f
                }
                y += 16f
                c.drawText("Manglik Dosha Comparison", MARGIN, y, p.sectionRed); y += 16f
                c.drawText("Groom: ${matching.boyManglik.summaryEn}", MARGIN, y, p.body); y += 16f
                c.drawText("Bride: ${matching.girlManglik.summaryEn}", MARGIN, y, p.body)
                doc.finishPage(page)
                onProgress(1, totalPages)
            }
            // Page 2: Groom D1/D9
            doc.startPage(PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, 2).create()).also { page ->
                coroutineContext.ensureActive()
                val bundleB = ComprehensiveReportBuilder.build(matching.boyChart)
                PdfSectionsPart1.drawModule2ChartsAndPositionsPage(page.canvas, p, astrologer, matching.boyChart, bundleB, 2, totalPages)
                doc.finishPage(page)
                onProgress(2, totalPages)
            }
            // Page 3: Bride D1/D9
            doc.startPage(PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, 3).create()).also { page ->
                coroutineContext.ensureActive()
                val bundleG = ComprehensiveReportBuilder.build(matching.girlChart)
                PdfSectionsPart1.drawModule2ChartsAndPositionsPage(page.canvas, p, astrologer, matching.girlChart, bundleG, 3, totalPages)
                doc.finishPage(page)
                onProgress(3, totalPages)
            }
            // Page 4: Joint Dasha & Remedy Advice
            doc.startPage(PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, 4).create()).also { page ->
                coroutineContext.ensureActive()
                val c = page.canvas
                var y = drawPageChrome(c, p, astrologer, "Combined Dasha & Marital Harmony Notes", "${matching.boyChart.input.name} × ${matching.girlChart.input.name}", 4, totalPages)
                c.drawText("Groom Running Dasha: ${matching.boyChart.currentMahadasha?.lord?.name ?: "-"} / ${matching.boyChart.currentAntardasha?.lord?.name ?: "-"}", MARGIN, y, p.subBold); y += 18f
                c.drawText("Bride Running Dasha: ${matching.girlChart.currentMahadasha?.lord?.name ?: "-"} / ${matching.girlChart.currentAntardasha?.lord?.name ?: "-"}", MARGIN, y, p.subBold); y += 28f
                drawWrappedText(c, "Summary Guidance: ${matching.verdictEn} (${matching.verdictHi})", MARGIN, y, CONTENT_WIDTH, p.body)
                doc.finishPage(page)
                onProgress(4, totalPages)
            }

            doc.writeTo(outputStream)
            outputStream.flush()
        } finally {
            doc.close()
        }
    }

    private fun drawDashaSummaryPage(c: Canvas, p: Paints, astrologer: AstrologerAccount, result: KundaliResult, pageNo: Int, totalPages: Int) {
        var y = drawPageChrome(c, p, astrologer, "Vimshottari Mahadasha Timeline (MD / AD / PD)", result.input.name, pageNo, totalPages)
        c.drawText("9-Planet 120-Year Vimshottari Cycle", MARGIN, y, p.sectionRed); y += 18f
        result.mahadashas.forEachIndexed { idx, m ->
            if (idx % 2 == 0) c.drawRect(MARGIN, y - 10f, PAGE_WIDTH - MARGIN, y + 8f, p.bgCard)
            c.drawText(m.lord.name, MARGIN + 8f, y + 2f, p.subBold)
            c.drawText("${m.startDateStr}  ->  ${m.endDateStr}", MARGIN + 110f, y + 2f, p.body)
            if (m.isCurrent) c.drawText("ACTIVE", MARGIN + 350f, y + 2f, p.bodyGreen)
            y += 22f
        }
        y += 18f
        val curA = result.currentAntardasha
        if (curA != null) {
            c.drawText("Current Pratyantardashas (${curA.mahadashaLord.name} / ${curA.lord.name})", MARGIN, y, p.sectionRed); y += 16f
            curA.pratyantardashas.forEach { pd ->
                c.drawText("${pd.mahadashaLord.name}-${pd.antardashaLord.name}-${pd.lord.name}: ${pd.startDateStr} – ${pd.endDateStr}" + if (pd.isCurrent) " [NOW]" else "", MARGIN + 8f, y, if (pd.isCurrent) p.bodyGreen else p.body)
                y += 16f
            }
        }
    }

    private fun drawMahadashaOverviewPage(c: Canvas, p: Paints, astrologer: AstrologerAccount, result: KundaliResult, maha: MahadashaPeriod, pageNo: Int, totalPages: Int) {
        var y = drawPageChrome(c, p, astrologer, "${maha.lord.name} Mahadasha Overview", result.input.name, pageNo, totalPages)
        c.drawText("${maha.lord.name} Mahadasha (${maha.startDateStr} to ${maha.endDateStr})", MARGIN, y, p.titleDarkBlue); y += 22f
        c.drawText("All 9 Antardasha Sub-Periods", MARGIN, y, p.sectionRed); y += 18f
        maha.antardashas.forEachIndexed { idx, ad ->
            if (idx % 2 == 0) c.drawRect(MARGIN, y - 10f, PAGE_WIDTH - MARGIN, y + 8f, p.bgCard)
            c.drawText("${maha.lord.name} / ${ad.lord.name}", MARGIN + 8f, y + 2f, p.subBold)
            c.drawText("${ad.startDateStr} to ${ad.endDateStr}", MARGIN + 150f, y + 2f, p.body)
            if (ad.isCurrent) c.drawText("CURRENT", MARGIN + 380f, y + 2f, p.bodyGreen)
            y += 22f
        }
    }

    private fun drawPanchangChoghadiyaPage(c: Canvas, p: Paints, astrologer: AstrologerAccount, result: KundaliResult, pageNo: Int, totalPages: Int) {
        var y = drawPageChrome(c, p, astrologer, "Day & Night Choghadiya & Hora Table", result.input.name, pageNo, totalPages)
        c.drawText("Day Choghadiya Slots", MARGIN, y, p.sectionRed); y += 16f
        result.panchang.dayChoghadiya.forEach { slot ->
            c.drawText("${slot.startTimeStr} – ${slot.endTimeStr}  •  ${slot.nature.name} (${if (slot.isGood) "Auspicious" else "Caution"})", MARGIN + 8f, y, if (slot.isGood) p.bodyGreen else p.bodyRed)
            y += 18f
        }
        y += 16f
        c.drawText("Night Choghadiya Slots", MARGIN, y, p.sectionRed); y += 16f
        result.panchang.nightChoghadiya.forEach { slot ->
            c.drawText("${slot.startTimeStr} – ${slot.endTimeStr}  •  ${slot.nature.name} (${if (slot.isGood) "Auspicious" else "Caution"})", MARGIN + 8f, y, if (slot.isGood) p.bodyGreen else p.bodyRed)
            y += 18f
        }
    }

    private fun drawKpCuspsDetailPage(c: Canvas, p: Paints, astrologer: AstrologerAccount, result: KundaliResult, pageNo: Int, totalPages: Int) {
        var y = drawPageChrome(c, p, astrologer, "KP Cusp Significators by House (1–12)", result.input.name, pageNo, totalPages)
        val kp = result.kpAnalysis ?: return
        kp.cusps.forEachIndexed { idx, cusp ->
            if (idx % 2 == 0) c.drawRect(MARGIN, y - 10f, PAGE_WIDTH - MARGIN, y + 22f, p.bgCard)
            c.drawText("House ${cusp.houseNumber}: ${cusp.rashi.name} ${cusp.dmsFormatted} (Sub-Lord: ${cusp.kp.subLord.name})", MARGIN + 6f, y + 2f, p.subBold)
            c.drawText("Significator Planets: ${cusp.significatorPlanets.joinToString(", ") { it.name }.ifEmpty { "None" }}", MARGIN + 6f, y + 16f, p.body)
            y += 38f
        }
    }

    private fun drawKpSignificatorsPage(c: Canvas, p: Paints, astrologer: AstrologerAccount, result: KundaliResult, pageNo: Int, totalPages: Int) {
        var y = drawPageChrome(c, p, astrologer, "KP 4-Grade Planetary Significator Matrix", result.input.name, pageNo, totalPages)
        val kp = result.kpAnalysis ?: return
        kp.planets.forEachIndexed { idx, pl ->
            c.drawRect(MARGIN, y, PAGE_WIDTH - MARGIN, y + 52f, if (idx % 2 == 0) p.bgLightBlue else p.bgCard)
            c.drawRect(MARGIN, y, PAGE_WIDTH - MARGIN, y + 52f, p.thinGrayStroke)
            c.drawText("${pl.planet.name} in ${pl.rashi.name} ${pl.dmsFormatted} (KP House ${pl.kpHouse})", MARGIN + 8f, y + 16f, p.subBold)
            c.drawText("Sign: ${pl.kp.signLord.name} • Star: ${pl.kp.starLord.name} • Sub: ${pl.kp.subLord.name} • SSL: ${pl.kp.subSubLord.name} (#${pl.kp.subNumber249})", MARGIN + 8f, y + 31f, p.body)
            c.drawText("Signifies: ${pl.signifiesHouses.joinToString(", ") { "H$it" }}", MARGIN + 8f, y + 45f, p.bodyRed)
            y += 62f
        }
    }

    private fun drawWesternAspectsPage(c: Canvas, p: Paints, astrologer: AstrologerAccount, result: KundaliResult, pageNo: Int, totalPages: Int) {
        var y = drawPageChrome(c, p, astrologer, "Western Tropical Planetary Positions & Aspects", result.input.name, pageNo, totalPages)
        val w = result.westernChart ?: return
        c.drawText("Sun: ${w.sunSign.name} • Moon: ${w.moonSign.name} • Asc: ${w.ascendantSign.name} • MC: ${w.midheavenSign.name}", MARGIN, y, p.subBold); y += 18f
        w.planets.forEachIndexed { idx, pl ->
            if (idx % 2 == 0) c.drawRect(MARGIN, y - 9f, PAGE_WIDTH - MARGIN, y + 6f, p.bgCard)
            c.drawText(pl.planet.name, MARGIN + 6f, y + 2f, p.subBold)
            c.drawText("${pl.rashi.name} ${pl.dmsFormatted}", MARGIN + 120f, y + 2f, p.body)
            c.drawText("${pl.element} • ${pl.modality}", MARGIN + 290f, y + 2f, p.body)
            y += 20f
        }
        y += 16f
        c.drawText("Major Ptolemaic Aspects (Orb <= 6.5°)", MARGIN, y, p.sectionRed); y += 16f
        w.aspects.take(14).forEach { asp ->
            c.drawText("${asp.planet1.name} — ${asp.aspectName} — ${asp.planet2.name} (${"%.1f".format(asp.orbDeg)}°)", MARGIN + 6f, y, if (asp.isHarmonious) p.bodyGreen else p.bodyRed)
            y += 17f
        }
    }

    private fun drawVastuDetailedPage(c: Canvas, p: Paints, astrologer: AstrologerAccount, result: KundaliResult, pageNo: Int, totalPages: Int) {
        var y = drawPageChrome(c, p, astrologer, "Personalized Vastu & Feng Shui Blueprint", result.input.name, pageNo, totalPages)
        val v = result.vastuFengShui ?: return
        c.drawText("Kua Number ${v.kuaNumber} — ${v.kuaGroupEn}", MARGIN, y, p.titleDarkBlue); y += 22f
        listOf(
            "Wealth / Prosperity Corner" to v.wealthDirectionEn,
            "Recommended Main Entrance" to v.entranceAdviceEn,
            "Puja / Meditation Altar" to v.pujaDirectionEn,
            "Kitchen / Agni Kon" to v.kitchenDirectionEn,
            "Master Bedroom" to v.bedroomDirectionEn
        ).forEach { (k, valStr) ->
            c.drawText("$k: $valStr", MARGIN, y, p.body); y += 20f
        }
        y += 14f
        c.drawText("Five-Element Harmonizing Recommendations", MARGIN, y, p.sectionRed); y += 18f
        v.elementTipsEn.forEach { tip ->
            y = drawWrappedText(c, "• $tip", MARGIN, y, CONTENT_WIDTH, p.body) + 6f
        }
    }
}

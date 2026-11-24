package com.example.pdf

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Typeface
import com.example.core.AppLanguage
import com.example.core.AstroStrings
import com.example.core.AstrologerAccount
import com.example.model.PlanetId
import com.example.model.Rashi

internal object PdfCanvasHelpers {
    const val PAGE_WIDTH = 595
    const val PAGE_HEIGHT = 842
    const val MARGIN = 34f
    const val CONTENT_WIDTH = PAGE_WIDTH - MARGIN * 2

    val darkBlueColor = Color.rgb(16, 42, 107)      // #102A6B
    val redColor = Color.rgb(198, 40, 40)           // #C62828
    val deepRedColor = Color.rgb(140, 29, 24)
    val goldColor = Color.rgb(197, 147, 40)         // #C59328
    val lightBlueBg = Color.rgb(225, 240, 255)      // #E1F0FF
    val lighterBlueBg = Color.rgb(242, 248, 255)
    val greenColor = Color.rgb(27, 122, 67)
    val textDark = Color.rgb(28, 35, 50)
    val textMuted = Color.rgb(85, 98, 118)

    class Paints {
        val bgLightBlue = Paint().apply { color = lightBlueBg; style = Paint.Style.FILL }
        val bgCard = Paint().apply { color = lighterBlueBg; style = Paint.Style.FILL }
        val bgHeaderBlue = Paint().apply { color = darkBlueColor; style = Paint.Style.FILL }
        val bgGoldBanner = Paint().apply { color = Color.rgb(255, 248, 226); style = Paint.Style.FILL }
        val bgRed = Paint().apply { color = redColor; style = Paint.Style.FILL }
        val bgGreen = Paint().apply { color = greenColor; style = Paint.Style.FILL }
        val goldStroke = Paint().apply { color = goldColor; style = Paint.Style.STROKE; strokeWidth = 1.5f }
        val blueStroke = Paint().apply { color = darkBlueColor; style = Paint.Style.STROKE; strokeWidth = 1.1f }
        val thinGrayStroke = Paint().apply { color = Color.rgb(196, 214, 232); style = Paint.Style.STROKE; strokeWidth = 0.7f }

        val titleWhite = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.WHITE; textSize = 15.5f; typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }
        val astroTopGold = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.rgb(255, 224, 130); textSize = 10.5f; typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }
        val titleDarkBlue = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = darkBlueColor; textSize = 15.5f; typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }
        val sectionRed = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = redColor; textSize = 12.2f; typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }
        val subBold = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = darkBlueColor; textSize = 9.8f; typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }
        val hdrWhite = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.WHITE; textSize = 8.6f; typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }
        val body = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = textDark; textSize = 9.0f
        }
        val bodyRed = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = deepRedColor; textSize = 9.0f; typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }
        val bodyGreen = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = greenColor; textSize = 9.0f; typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }
        val small = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = textMuted; textSize = 7.9f
        }
        val chartText = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = redColor; textSize = 8.2f; typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            textAlign = Paint.Align.CENTER
        }
        val chartNum = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = darkBlueColor; textSize = 7.4f; typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            textAlign = Paint.Align.CENTER
        }
    }

    /**
     * Draws consistent page chrome with the registered Astrologer's Name prominently at the VERY TOP
     * of every generated PDF page.
     */
    fun drawPageChrome(
        canvas: Canvas,
        p: Paints,
        astrologer: AstrologerAccount,
        pageTitle: String,
        chartName: String,
        pageNumber: Int,
        totalPages: Int
    ): Float {
        canvas.drawRect(16f, 16f, PAGE_WIDTH - 16f, PAGE_HEIGHT - 16f, p.goldStroke)
        canvas.drawRect(20f, 20f, PAGE_WIDTH - 20f, PAGE_HEIGHT - 20f, p.thinGrayStroke)

        // Top Dark Blue Bar — ASTROLOGER NAME AT THE VERY TOP
        canvas.drawRect(20f, 20f, PAGE_WIDTH - 20f, 70f, p.bgHeaderBlue)
        val astroHeader = buildString {
            append("Astrologer: ${astrologer.effectiveHeaderName()}")
            if (astrologer.mobileNumber.isNotBlank()) append(" • ${astrologer.mobileNumber}")
        }
        canvas.drawText(astroHeader.take(66), MARGIN, 35f, p.astroTopGold)
        canvas.drawText("SHRI SAMPURNA ASTROLOGY", MARGIN, 51f, p.titleWhite)
        canvas.drawText(
            pageTitle.take(68),
            MARGIN,
            64f,
            Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.rgb(214, 236, 255); textSize = 8.8f }
        )

        // Footer
        canvas.drawLine(MARGIN, PAGE_HEIGHT - 34f, PAGE_WIDTH - MARGIN, PAGE_HEIGHT - 34f, p.goldStroke)
        canvas.drawText(
            "${chartName.take(32)} • Helplines: +91 7275547797 / +91 9451140566",
            MARGIN,
            PAGE_HEIGHT - 22f,
            p.small
        )
        val pageLabel = "Page $pageNumber / $totalPages"
        canvas.drawText(pageLabel, PAGE_WIDTH - MARGIN - p.small.measureText(pageLabel), PAGE_HEIGHT - 22f, p.small)
        return 84f
    }

    fun drawWrappedText(
        canvas: Canvas,
        text: String,
        x: Float,
        startY: Float,
        maxWidth: Float,
        paint: Paint,
        lineHeight: Float = 12.2f
    ): Float {
        var y = startY
        val words = text.split(Regex("\\s+"))
        var currentLine = StringBuilder()
        for (word in words) {
            val test = if (currentLine.isEmpty()) word else "$currentLine $word"
            if (paint.measureText(test) > maxWidth && currentLine.isNotEmpty()) {
                canvas.drawText(currentLine.toString(), x, y, paint)
                y += lineHeight
                currentLine = StringBuilder(word)
            } else {
                currentLine = StringBuilder(test)
            }
        }
        if (currentLine.isNotEmpty()) {
            canvas.drawText(currentLine.toString(), x, y, paint)
            y += lineHeight
        }
        return y
    }

    fun drawNorthIndianVectorChart(
        canvas: Canvas,
        p: Paints,
        left: Float,
        top: Float,
        size: Float,
        lagnaRashi: Rashi,
        planetsByRashi: Map<Rashi, List<PlanetId>>
    ) {
        val right = left + size
        val bottom = top + size
        val midX = left + size / 2f
        val midY = top + size / 2f

        val rect = RectF(left, top, right, bottom)
        canvas.drawRect(rect, p.bgLightBlue)
        canvas.drawRect(rect, p.goldStroke)

        canvas.drawLine(left, top, right, bottom, p.goldStroke)
        canvas.drawLine(right, top, left, bottom, p.goldStroke)
        canvas.drawLine(midX, top, right, midY, p.goldStroke)
        canvas.drawLine(right, midY, midX, bottom, p.goldStroke)
        canvas.drawLine(midX, bottom, left, midY, p.goldStroke)
        canvas.drawLine(left, midY, midX, top, p.goldStroke)

        val centers = listOf(
            midX to top + size * 0.25f,
            left + size * 0.25f to top + size * 0.12f,
            left + size * 0.12f to top + size * 0.25f,
            left + size * 0.25f to midY,
            left + size * 0.12f to top + size * 0.75f,
            left + size * 0.25f to bottom - size * 0.12f,
            midX to top + size * 0.75f,
            right - size * 0.25f to bottom - size * 0.12f,
            right - size * 0.12f to top + size * 0.75f,
            right - size * 0.25f to midY,
            right - size * 0.12f to top + size * 0.25f,
            right - size * 0.25f to top + size * 0.12f
        )

        for (h in 1..12) {
            val rashi = Rashi.fromIndex(lagnaRashi.ordinal + h - 1)
            val (cx, cy) = centers[h - 1]
            canvas.drawText(rashi.number.toString(), cx, cy - 5f, p.chartNum)
            val occ = planetsByRashi[rashi].orEmpty().joinToString(" ") {
                AstroStrings.planetShort(it, AppLanguage.EN)
            }
            if (occ.isNotEmpty()) {
                canvas.drawText(occ.take(14), cx, cy + 6f, p.chartText)
            }
        }
    }

    fun drawHouseBasedNorthChart(
        canvas: Canvas,
        p: Paints,
        left: Float,
        top: Float,
        size: Float,
        lagnaRashi: Rashi,
        planetsByHouse: Map<Int, List<PlanetId>>
    ) {
        val byRashi = planetsByHouse.entries.associate { (h, list) ->
            Rashi.fromIndex(lagnaRashi.ordinal + h - 1) to list
        }
        drawNorthIndianVectorChart(canvas, p, left, top, size, lagnaRashi, byRashi)
    }

    fun draw3x3NumberGrid(
        canvas: Canvas,
        p: Paints,
        startX: Float,
        startY: Float,
        cellW: Float,
        cellH: Float,
        layoutRows: List<List<Int>>,
        counts: Map<Int, Int>
    ) {
        layoutRows.forEachIndexed { rIdx, rowVals ->
            rowVals.forEachIndexed { cIdx, digit ->
                val x = startX + cIdx * cellW
                val top = startY + rIdx * cellH
                canvas.drawRect(x, top, x + cellW, top + cellH, p.bgLightBlue)
                canvas.drawRect(x, top, x + cellW, top + cellH, p.goldStroke)
                val cnt = counts[digit] ?: 0
                val display = if (cnt > 0) digit.toString().repeat(cnt.coerceAtMost(5)) else "—"
                canvas.drawText("Num $digit", x + 8f, top + 16f, p.small)
                canvas.drawText(display, x + 22f, top + cellH * 0.68f, p.titleDarkBlue)
            }
        }
    }
}

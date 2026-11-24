package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.core.AppLanguage
import com.example.core.AstroStrings
import com.example.model.ChartDivision
import com.example.model.ChartStyle
import com.example.model.KundaliResult
import com.example.model.PlanetId
import com.example.model.Rashi
import com.example.ui.theme.TempleGold

data class ChartDisplayData(
    val title: String,
    val baseRashi: Rashi,
    val planetsByHouse: Map<Int, List<Pair<PlanetId, Boolean>>>
)

fun buildChartDisplayData(
    result: KundaliResult,
    division: ChartDivision,
    lang: AppLanguage
): ChartDisplayData {
    val moonRashi = result.planets.first { it.planet == PlanetId.MOON }.rashi
    val baseRashi = when (division) {
        ChartDivision.LAGNA_D1 -> result.lagnaRashi
        ChartDivision.CHANDRA -> moonRashi
        else -> com.example.astro.ShodashvargaEngine.calculateVargaRashi(result.lagnaLongitude, division)
    }

    val grouped = (1..12).associateWith { mutableListOf<Pair<PlanetId, Boolean>>() }
    result.planets.forEach { p ->
        val targetRashi = when (division) {
            ChartDivision.LAGNA_D1, ChartDivision.CHANDRA -> p.rashi
            ChartDivision.NAVAMSA_D9 -> p.navamsaRashi
            else -> com.example.astro.ShodashvargaEngine.calculateVargaRashi(p.siderealLongitude, division)
        }
        val house = ((targetRashi.ordinal - baseRashi.ordinal + 12) % 12) + 1
        grouped.getValue(house).add(p.planet to p.isRetrograde)
    }

    val title = AstroStrings.divisionTitle(division, lang)
    return ChartDisplayData(title, baseRashi, grouped)
}

@Composable
fun KundaliChartView(
    result: KundaliResult,
    style: ChartStyle,
    division: ChartDivision,
    language: AppLanguage,
    selectedHouse: Int?,
    onHouseSelected: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val data = buildChartDisplayData(result, division, language)
    when (style) {
        ChartStyle.NORTH_INDIAN -> NorthIndianChart(
            data = data,
            language = language,
            selectedHouse = selectedHouse,
            onHouseSelected = onHouseSelected,
            modifier = modifier
        )
        ChartStyle.SOUTH_INDIAN -> SouthIndianChart(
            data = data,
            language = language,
            selectedHouse = selectedHouse,
            onHouseSelected = onHouseSelected,
            modifier = modifier
        )
    }
}

@Composable
private fun NorthIndianChart(
    data: ChartDisplayData,
    language: AppLanguage,
    selectedHouse: Int?,
    onHouseSelected: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val goldColor = MaterialTheme.colorScheme.outline
    val highlightColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
    val bgColor = MaterialTheme.colorScheme.surface
    val signColor = MaterialTheme.colorScheme.secondary
    val planetColor = MaterialTheme.colorScheme.primary

    // Center coordinates (xFrac, yFrac) for the 12 houses in North Indian diamond layout
    val houseCenters = listOf(
        0.50f to 0.25f, // 1: Top Diamond
        0.25f to 0.10f, // 2: Top-Left Triangle
        0.10f to 0.25f, // 3: Left-Top Triangle
        0.25f to 0.50f, // 4: Left Diamond
        0.10f to 0.75f, // 5: Left-Bottom Triangle
        0.25f to 0.90f, // 6: Bottom-Left Triangle
        0.50f to 0.75f, // 7: Bottom Diamond
        0.75f to 0.90f, // 8: Bottom-Right Triangle
        0.90f to 0.75f, // 9: Right-Bottom Triangle
        0.75f to 0.50f, // 10: Right Diamond
        0.90f to 0.25f, // 11: Right-Top Triangle
        0.75f to 0.10f  // 12: Top-Right Triangle
    )

    BoxWithConstraints(
        modifier = modifier
            .widthIn(max = 420.dp)
            .fillMaxWidth()
            .aspectRatio(1f)
            .background(bgColor, RoundedCornerShape(12.dp))
            .testTag("north_indian_chart")
    ) {
        val chartSize = maxWidth
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height
            val stroke = 2.dp.toPx()

            // Optional selected house polygon highlight
            if (selectedHouse != null && selectedHouse in 1..12) {
                val path = housePolygonPath(selectedHouse, w, h)
                drawPath(path = path, color = highlightColor)
            }

            // Outer rectangle
            drawRect(
                color = goldColor,
                style = androidx.compose.ui.graphics.drawscope.Stroke(width = stroke * 1.3f)
            )
            // Diagonals
            drawLine(goldColor, Offset(0f, 0f), Offset(w, h), strokeWidth = stroke)
            drawLine(goldColor, Offset(w, 0f), Offset(0f, h), strokeWidth = stroke)
            // Inner Diamond
            drawLine(goldColor, Offset(w / 2f, 0f), Offset(w, h / 2f), strokeWidth = stroke)
            drawLine(goldColor, Offset(w, h / 2f), Offset(w / 2f, h), strokeWidth = stroke)
            drawLine(goldColor, Offset(w / 2f, h), Offset(0f, h / 2f), strokeWidth = stroke)
            drawLine(goldColor, Offset(0f, h / 2f), Offset(w / 2f, 0f), strokeWidth = stroke)
        }

        // Overlay labels for each of the 12 houses
        for (house in 1..12) {
            val (cx, cy) = houseCenters[house - 1]
            val rashi = Rashi.fromIndex(data.baseRashi.ordinal + house - 1)
            val occupants = data.planetsByHouse[house].orEmpty()
            val cellSize = chartSize * 0.23f

            Box(
                modifier = Modifier
                    .size(cellSize)
                    .offset(
                        x = chartSize * cx - cellSize / 2f,
                        y = chartSize * cy - cellSize / 2f
                    )
                    .clickable { onHouseSelected(house) },
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = rashi.number.toString(),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = signColor,
                        lineHeight = 12.sp
                    )
                    if (house == 1) {
                        Text(
                            text = AstroStrings.lagnaLabel(language),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.tertiary,
                            lineHeight = 11.sp
                        )
                    }
                    if (occupants.isNotEmpty()) {
                        val line = occupants.joinToString(" ") { (p, retro) ->
                            val abbr = AstroStrings.planetShort(p, language)
                            if (retro && p != PlanetId.RAHU && p != PlanetId.KETU) "$abbr*" else abbr
                        }
                        Text(
                            text = line,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = planetColor,
                            textAlign = TextAlign.Center,
                            lineHeight = 13.sp
                        )
                    }
                }
            }
        }
    }
}

private fun housePolygonPath(house: Int, w: Float, h: Float): Path {
    val p = Path()
    when (house) {
        1 -> {
            p.moveTo(w * 0.5f, 0f); p.lineTo(w * 0.75f, h * 0.25f)
            p.lineTo(w * 0.5f, h * 0.5f); p.lineTo(w * 0.25f, h * 0.25f)
        }
        2 -> { p.moveTo(0f, 0f); p.lineTo(w * 0.5f, 0f); p.lineTo(w * 0.25f, h * 0.25f) }
        3 -> { p.moveTo(0f, 0f); p.lineTo(w * 0.25f, h * 0.25f); p.lineTo(0f, h * 0.5f) }
        4 -> {
            p.moveTo(0f, h * 0.5f); p.lineTo(w * 0.25f, h * 0.25f)
            p.lineTo(w * 0.5f, h * 0.5f); p.lineTo(w * 0.25f, h * 0.75f)
        }
        5 -> { p.moveTo(0f, h * 0.5f); p.lineTo(w * 0.25f, h * 0.75f); p.lineTo(0f, h) }
        6 -> { p.moveTo(0f, h); p.lineTo(w * 0.25f, h * 0.75f); p.lineTo(w * 0.5f, h) }
        7 -> {
            p.moveTo(w * 0.5f, h * 0.5f); p.lineTo(w * 0.75f, h * 0.75f)
            p.lineTo(w * 0.5f, h); p.lineTo(w * 0.25f, h * 0.75f)
        }
        8 -> { p.moveTo(w * 0.5f, h); p.lineTo(w * 0.75f, h * 0.75f); p.lineTo(w, h) }
        9 -> { p.moveTo(w, h); p.lineTo(w * 0.75f, h * 0.75f); p.lineTo(w, h * 0.5f) }
        10 -> {
            p.moveTo(w * 0.5f, h * 0.5f); p.lineTo(w * 0.75f, h * 0.25f)
            p.lineTo(w, h * 0.5f); p.lineTo(w * 0.75f, h * 0.75f)
        }
        11 -> { p.moveTo(w, 0f); p.lineTo(w, h * 0.5f); p.lineTo(w * 0.75f, h * 0.25f) }
        12 -> { p.moveTo(w * 0.5f, 0f); p.lineTo(w, 0f); p.lineTo(w * 0.75f, h * 0.25f) }
    }
    p.close()
    return p
}

@Composable
private fun SouthIndianChart(
    data: ChartDisplayData,
    language: AppLanguage,
    selectedHouse: Int?,
    onHouseSelected: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    // Fixed sign layout in 4x4 grid (row, col) -> Rashi ordinal (0=Aries..11=Pisces)
    val gridRashis: Array<Array<Rashi?>> = arrayOf(
        arrayOf(Rashi.MEENA, Rashi.MESHA, Rashi.VRISHABHA, Rashi.MITHUNA),
        arrayOf(Rashi.KUMBHA, null, null, Rashi.KARKA),
        arrayOf(Rashi.MAKARA, null, null, Rashi.SIMHA),
        arrayOf(Rashi.DHANU, Rashi.VRISCHIKA, Rashi.TULA, Rashi.KANYA)
    )

    val borderCol = MaterialTheme.colorScheme.outline
    Box(
        modifier = modifier
            .widthIn(max = 420.dp)
            .fillMaxWidth()
            .aspectRatio(1f)
            .border(1.5.dp, borderCol, RoundedCornerShape(10.dp))
            .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(10.dp))
            .testTag("south_indian_chart")
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            for (r in 0 until 4) {
                Row(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                ) {
                    if (r == 1 || r == 2) {
                        // Left cell
                        SouthCell(
                            rashi = gridRashis[r][0]!!,
                            data = data,
                            language = language,
                            selectedHouse = selectedHouse,
                            onHouseSelected = onHouseSelected,
                            borderCol = borderCol,
                            modifier = Modifier.weight(1f).fillMaxSize()
                        )
                        // Center merged area (shown only in row 1 conceptually, empty container split cleanly)
                        Box(
                            modifier = Modifier
                                .weight(2f)
                                .fillMaxSize()
                                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)),
                            contentAlignment = Alignment.Center
                        ) {
                            if (r == 1) {
                                Text(
                                    text = data.title,
                                    style = MaterialTheme.typography.titleSmall,
                                    color = MaterialTheme.colorScheme.primary,
                                    fontWeight = FontWeight.Bold,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.padding(top = 24.dp, start = 8.dp, end = 8.dp)
                                )
                            } else {
                                Text(
                                    text = AstroStrings.rashiName(data.baseRashi, language),
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = TempleGold,
                                    fontWeight = FontWeight.SemiBold,
                                    modifier = Modifier.padding(bottom = 24.dp)
                                )
                            }
                        }
                        // Right cell
                        SouthCell(
                            rashi = gridRashis[r][3]!!,
                            data = data,
                            language = language,
                            selectedHouse = selectedHouse,
                            onHouseSelected = onHouseSelected,
                            borderCol = borderCol,
                            modifier = Modifier.weight(1f).fillMaxSize()
                        )
                    } else {
                        for (c in 0 until 4) {
                            SouthCell(
                                rashi = gridRashis[r][c]!!,
                                data = data,
                                language = language,
                                selectedHouse = selectedHouse,
                                onHouseSelected = onHouseSelected,
                                borderCol = borderCol,
                                modifier = Modifier.weight(1f).fillMaxSize()
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SouthCell(
    rashi: Rashi,
    data: ChartDisplayData,
    language: AppLanguage,
    selectedHouse: Int?,
    onHouseSelected: (Int) -> Unit,
    borderCol: Color,
    modifier: Modifier = Modifier
) {
    val house = ((rashi.ordinal - data.baseRashi.ordinal + 12) % 12) + 1
    val occupants = data.planetsByHouse[house].orEmpty()
    val isSelected = selectedHouse == house
    val bg = if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.12f) else Color.Transparent

    Box(
        modifier = modifier
            .border(0.75.dp, borderCol.copy(alpha = 0.7f))
            .background(bg)
            .clickable { onHouseSelected(house) }
            .padding(4.dp)
    ) {
        if (house == 1) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                drawLine(
                    color = borderCol,
                    start = Offset(0f, size.height * 0.35f),
                    end = Offset(size.width * 0.35f, 0f),
                    strokeWidth = 1.5.dp.toPx()
                )
            }
        }
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "${rashi.number}",
                    fontSize = 10.sp,
                    color = MaterialTheme.colorScheme.secondary,
                    fontWeight = FontWeight.Bold
                )
                if (house == 1) {
                    Text(
                        text = AstroStrings.lagnaLabel(language),
                        fontSize = 9.sp,
                        color = MaterialTheme.colorScheme.tertiary,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            Text(
                text = occupants.joinToString(" ") { (p, retro) ->
                    val s = AstroStrings.planetShort(p, language)
                    if (retro && p != PlanetId.RAHU && p != PlanetId.KETU) "$s*" else s
                },
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                textAlign = TextAlign.Center,
                lineHeight = 13.sp
            )
            Text(
                text = "H$house",
                fontSize = 9.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

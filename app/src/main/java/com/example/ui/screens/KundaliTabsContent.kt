package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.MenuBook
import androidx.compose.material.icons.outlined.ExpandLess
import androidx.compose.material.icons.outlined.ExpandMore
import androidx.compose.material.icons.outlined.Verified
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.core.AppLanguage
import com.example.core.AstroStrings
import com.example.core.tr
import com.example.model.ChartDivision
import com.example.model.ChartStyle
import com.example.model.FunctionalNature
import com.example.model.KundaliResult
import com.example.model.PlanetId
import com.example.model.Rashi
import com.example.model.SadeSatiPhase
import com.example.ui.components.DignityBadge
import com.example.ui.components.GoldBorderedCard
import com.example.ui.components.KundaliChartView
import com.example.ui.components.SectionTitle
import com.example.ui.components.StatusPill
import com.example.ui.theme.AuspiciousGreen
import com.example.ui.theme.AuspiciousGreenBg
import com.example.ui.theme.CautionAmber
import com.example.ui.theme.CautionAmberBg
import com.example.ui.theme.DarkBrandBlue

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ChartAndPlanetsTab(
    result: KundaliResult,
    language: AppLanguage,
    chartStyle: ChartStyle,
    division: ChartDivision,
    onChartStyleChange: (ChartStyle) -> Unit,
    onDivisionChange: (ChartDivision) -> Unit
) {
    var selectedHouse by remember { mutableStateOf<Int?>(1) }
    val allDivisions = remember {
        listOf(ChartDivision.LAGNA_D1, ChartDivision.CHANDRA) +
            ChartDivision.shodashvargaOrder.filter { it != ChartDivision.LAGNA_D1 }
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        item {
            Column(
                modifier = Modifier.widthIn(max = 680.dp).fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                GoldBorderedCard(containerColor = MaterialTheme.colorScheme.surfaceVariant) {
                    Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(
                            text = result.input.name,
                            style = MaterialTheme.typography.titleLarge,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "${"%04d-%02d-%02d".format(result.input.year, result.input.month, result.input.day)} " +
                                "${"%02d:%02d".format(result.input.hour, result.input.minute)} (UTC%+.1f) • ${AstroStrings.placeName(result.input.place, language)}".format(result.effectiveUtcOffsetHours),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "${AstroStrings.lagnaLabel(language)}: ${AstroStrings.rashiName(result.lagnaRashi, language)} ${result.lagnaDmsFormatted} • " +
                                "${AstroStrings.nakshatraName(result.lagnaNakshatraIndex, language)} (${result.lagnaPada}) • " +
                                "Lahiri ${"%.2f°".format(result.ayanamsaDeg)}",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }

                // North / South Toggle
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    FilterChip(
                        selected = chartStyle == ChartStyle.NORTH_INDIAN,
                        onClick = { onChartStyleChange(ChartStyle.NORTH_INDIAN) },
                        label = { Text(tr(language, "उत्तर भारतीय", "North Indian", "北印盘")) },
                        modifier = Modifier.testTag("toggle_north_chart")
                    )
                    FilterChip(
                        selected = chartStyle == ChartStyle.SOUTH_INDIAN,
                        onClick = { onChartStyleChange(ChartStyle.SOUTH_INDIAN) },
                        label = { Text(tr(language, "दक्षिण भारतीय", "South Indian", "南印盘")) },
                        modifier = Modifier.testTag("toggle_south_chart")
                    )
                }

                // Scrollable 16 Shodashvarga + Moon Divisions Selector
                Text(
                    text = "${tr(language, "षोडशवर्ग कुंडली (16 वर्ग):", "Shodashvarga Charts (16):", "十六分盘 (D1–D60):")} ${AstroStrings.divisionTitle(division, language)}",
                    style = MaterialTheme.typography.labelLarge,
                    color = DarkBrandBlue,
                    fontWeight = FontWeight.Bold
                )
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(allDivisions, key = { it.name }) { div ->
                        val tag = when (div) {
                            ChartDivision.LAGNA_D1 -> "toggle_d1"
                            ChartDivision.CHANDRA -> "toggle_chandra"
                            ChartDivision.NAVAMSA_D9 -> "toggle_d9"
                            else -> "toggle_${div.code.lowercase()}"
                        }
                        FilterChip(
                            selected = division == div,
                            onClick = { onDivisionChange(div) },
                            label = {
                                Text(
                                    if (div == ChartDivision.CHANDRA) {
                                        tr(language, "चंद्र", "Moon", "月亮")
                                    } else {
                                        div.code
                                    }
                                )
                            },
                            modifier = Modifier.testTag(tag)
                        )
                    }
                }

                Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    KundaliChartView(
                        result = result,
                        style = chartStyle,
                        division = division,
                        language = language,
                        selectedHouse = selectedHouse,
                        onHouseSelected = { selectedHouse = it }
                    )
                }

                selectedHouse?.let { h ->
                    val bhava = result.bhavaAnalyses.getOrNull(h - 1)
                    if (bhava != null) {
                        Surface(
                            color = MaterialTheme.colorScheme.secondaryContainer,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            val occ = if (bhava.occupants.isEmpty()) "—" else bhava.occupants.joinToString(", ") {
                                AstroStrings.planetName(it, language)
                            }
                            Text(
                                text = "${AstroStrings.bhavaTitle(h, language)} • ${AstroStrings.rashiName(bhava.signRashi, language)} • " +
                                    "${AstroStrings.planetName(bhava.lord, language)} → H${bhava.lordPlacedInHouse} • [$occ]",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSecondaryContainer,
                                fontWeight = FontWeight.SemiBold,
                                modifier = Modifier.padding(12.dp)
                            )
                        }
                    }
                }

                SectionTitle(
                    title = tr(language, "ग्रह स्थिति तालिका", "Planetary Positions", "行星位置表")
                )
            }
        }

        items(result.planets, key = { it.planet.name }) { p ->
            GoldBorderedCard(modifier = Modifier.widthIn(max = 680.dp)) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1.2f)) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text(
                                text = AstroStrings.planetName(p.planet, language),
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Bold
                            )
                            if (p.isRetrograde) {
                                StatusPill(text = tr(language, "वक्री", "R", "逆"), isPositive = false)
                            }
                            if (p.isCombust) {
                                StatusPill(text = tr(language, "अस्त", "C", "焦"), isPositive = false)
                            }
                        }
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "${AstroStrings.rashiName(p.rashi, language)} ${p.dmsFormatted} (D9: ${AstroStrings.rashiName(p.navamsaRashi, language)})",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "${AstroStrings.nakshatraName(p.nakshatraIndex, language)} (${p.nakshatraPada}) • " +
                                "${tr(language, "भाव", "House", "宫")} ${p.houseFromLagna}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    DignityBadge(dignity = p.dignity, language = language)
                }
            }
        }

        // Complete Shodashvarga Rashi Matrix
        if (result.shodashvarga.isNotEmpty()) {
            item {
                Column(
                    modifier = Modifier.widthIn(max = 680.dp).fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    SectionTitle(
                        title = tr(language, "षोडशवर्ग तालिका (D1–D60)", "Shodashvarga Matrix (D1–D60)", "十六分盘总表")
                    )
                    GoldBorderedCard {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState())
                                .padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Text("Varga", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, color = DarkBrandBlue, modifier = Modifier.width(46.dp))
                                Text(AstroStrings.lagnaLabel(language), style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary, modifier = Modifier.width(56.dp))
                                PlanetId.entries.forEach { pl ->
                                    Text(AstroStrings.planetShort(pl, language), style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, color = DarkBrandBlue, modifier = Modifier.width(54.dp))
                                }
                            }
                            HorizontalDivider()
                            result.shodashvarga.forEach { row ->
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    modifier = Modifier
                                        .clickable { onDivisionChange(row.division) }
                                        .padding(vertical = 2.dp)
                                ) {
                                    Text(row.division.code, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, color = DarkBrandBlue, modifier = Modifier.width(46.dp))
                                    Text(AstroStrings.rashiName(row.lagnaRashi, language).take(5), style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.primary, modifier = Modifier.width(56.dp))
                                    PlanetId.entries.forEach { pl ->
                                        val r = row.planetRashis[pl]
                                        Text(
                                            text = r?.let { AstroStrings.rashiName(it, language).take(5) } ?: "-",
                                            style = MaterialTheme.typography.bodySmall,
                                            modifier = Modifier.width(54.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun DashaTab(
    result: KundaliResult,
    language: AppLanguage
) {
    var expandedMahaIdx by remember {
        mutableStateOf(result.mahadashas.indexOfFirst { it.isCurrent }.coerceAtLeast(0))
    }
    var expandedAntarKey by remember {
        val cm = result.currentMahadasha
        val ca = result.currentAntardasha
        mutableStateOf(if (cm != null && ca != null) "${cm.lord.name}_${ca.lord.name}" else "")
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        item {
            Column(
                modifier = Modifier.widthIn(max = 680.dp).fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                SectionTitle(
                    title = tr(
                        language,
                        "विंशोत्तरी महादशा, अंतर्दशा एवं प्रत्यंतर्दशा",
                        "Vimshottari Mahadasha, Antardasha & Pratyantardasha",
                        "维姆肖塔里大运・小运・微运"
                    )
                )
                val cm = result.currentMahadasha
                val ca = result.currentAntardasha
                val cp = result.currentPratyantardasha
                if (cm != null && ca != null) {
                    GoldBorderedCard(containerColor = MaterialTheme.colorScheme.primaryContainer) {
                        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text(
                                text = tr(language, "वर्तमान दशा", "Active Running Period", "当前运行运势"),
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Bold
                            )
                            val activeChain = buildString {
                                append(AstroStrings.planetName(cm.lord, language))
                                append(" → ")
                                append(AstroStrings.planetName(ca.lord, language))
                                if (cp != null) {
                                    append(" → ")
                                    append(AstroStrings.planetName(cp.lord, language))
                                }
                            }
                            Text(
                                text = activeChain,
                                style = MaterialTheme.typography.headlineSmall,
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = if (cp != null) {
                                    "${tr(language, "प्रत्यंतर", "Pratyantar", "微运")}: ${cp.startDateStr} – ${cp.endDateStr}"
                                } else {
                                    "${ca.startDateStr} – ${ca.endDateStr}"
                                },
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }
                }
            }
        }

        items(result.mahadashas.indices.toList()) { idx ->
            val maha = result.mahadashas[idx]
            val isExpanded = expandedMahaIdx == idx
            GoldBorderedCard(
                modifier = Modifier.widthIn(max = 680.dp),
                containerColor = if (maha.isCurrent) MaterialTheme.colorScheme.secondaryContainer else MaterialTheme.colorScheme.surface
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { expandedMahaIdx = if (isExpanded) -1 else idx }
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Text(
                                    text = AstroStrings.planetName(maha.lord, language),
                                    style = MaterialTheme.typography.titleMedium,
                                    color = MaterialTheme.colorScheme.primary,
                                    fontWeight = FontWeight.Bold
                                )
                                if (maha.isCurrent) {
                                    StatusPill(
                                        text = tr(language, "वर्तमान", "Current", "当前"),
                                        isPositive = true
                                    )
                                }
                            }
                            Text(
                                text = "${maha.startDateStr} → ${maha.endDateStr}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Icon(
                            imageVector = if (isExpanded) Icons.Outlined.ExpandLess else Icons.Outlined.ExpandMore,
                            contentDescription = "Expand Antardasha"
                        )
                    }

                    AnimatedVisibility(visible = isExpanded) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 14.dp)
                                .padding(bottom = 12.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            HorizontalDivider()
                            maha.antardashas.forEach { antar ->
                                val antarKey = "${maha.lord.name}_${antar.lord.name}"
                                val antarExpanded = expandedAntarKey == antarKey
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(
                                            if (antar.isCurrent) AuspiciousGreenBg else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
                                        )
                                        .clickable {
                                            expandedAntarKey = if (antarExpanded) "" else antarKey
                                        }
                                        .padding(horizontal = 10.dp, vertical = 8.dp),
                                    verticalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "${AstroStrings.planetName(maha.lord, language)} / ${AstroStrings.planetName(antar.lord, language)}",
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = if (antar.isCurrent) FontWeight.Bold else FontWeight.Medium,
                                            color = if (antar.isCurrent) AuspiciousGreen else MaterialTheme.colorScheme.onSurface
                                        )
                                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                            Text(
                                                text = "${antar.startDateStr} – ${antar.endDateStr}",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                            Icon(
                                                imageVector = if (antarExpanded) Icons.Outlined.ExpandLess else Icons.Outlined.ExpandMore,
                                                contentDescription = "Expand Pratyantardasha"
                                            )
                                        }
                                    }

                                    AnimatedVisibility(visible = antarExpanded && antar.pratyantardashas.isNotEmpty()) {
                                        Column(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(top = 6.dp, start = 8.dp),
                                            verticalArrangement = Arrangement.spacedBy(3.dp)
                                        ) {
                                            antar.pratyantardashas.forEach { pd ->
                                                Row(
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .clip(RoundedCornerShape(6.dp))
                                                        .background(
                                                            if (pd.isCurrent) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface.copy(alpha = 0.7f)
                                                        )
                                                        .padding(horizontal = 8.dp, vertical = 4.dp),
                                                    horizontalArrangement = Arrangement.SpaceBetween
                                                ) {
                                                    Text(
                                                        text = "${AstroStrings.planetShort(maha.lord, language)}-${AstroStrings.planetShort(antar.lord, language)}-${AstroStrings.planetName(pd.lord, language)}",
                                                        style = MaterialTheme.typography.labelSmall,
                                                        fontWeight = if (pd.isCurrent) FontWeight.Bold else FontWeight.Normal,
                                                        color = if (pd.isCurrent) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                                                    )
                                                    Text(
                                                        text = "${pd.startDateStr} – ${pd.endDateStr}",
                                                        style = MaterialTheme.typography.labelSmall,
                                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun PhalitTab(
    result: KundaliResult,
    language: AppLanguage
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        item {
            Column(
                modifier = Modifier.widthIn(max = 680.dp).fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                SectionTitle(
                    title = tr(language, "द्वादश भाव फलादेश एवं श्लोक मिलान", "12-House Phalit & Classical Shlokas", "十二宫分析与经典颂文匹配")
                )
                Text(
                    text = tr(
                        language,
                        "केवल कुंडली से प्राप्त वास्तविक ग्रह-स्थितियों और सत्यापित शास्त्रीय श्लोकों पर आधारित। कोई फल-वाक्य अपनी ओर से नहीं गढ़ा गया है।",
                        "Derived strictly from chart conditions and verified classical verses; no synthetic predictions are fabricated.",
                        "严格基于星盘实际行星条件与已验证经典颂文，不虚构任何预测文本。"
                    ),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        items(result.bhavaAnalyses, key = { it.houseNumber }) { bhava ->
            GoldBorderedCard(modifier = Modifier.widthIn(max = 680.dp)) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "${AstroStrings.bhavaTitle(bhava.houseNumber, language)} • ${AstroStrings.rashiName(bhava.signRashi, language)}",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )

                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        StatusPill(
                            text = tr(
                                language,
                                "भावेश: ${AstroStrings.planetName(bhava.lord, language)} → ${bhava.lordPlacedInHouse}म भाव",
                                "Lord: ${AstroStrings.planetName(bhava.lord, language)} → H${bhava.lordPlacedInHouse}",
                                "宫主: ${AstroStrings.planetName(bhava.lord, language)} → 第${bhava.lordPlacedInHouse}宫"
                            ),
                            isPositive = bhava.lordPlacedInHouse !in listOf(6, 8, 12)
                        )
                        if (bhava.occupants.isNotEmpty()) {
                            StatusPill(
                                text = tr(
                                    language,
                                    "विद्यमान: ${bhava.occupants.joinToString(", ") { AstroStrings.planetName(it, language) }}",
                                    "Occupants: ${bhava.occupants.joinToString(", ") { AstroStrings.planetName(it, language) }}",
                                    "落入: ${bhava.occupants.joinToString(", ") { AstroStrings.planetName(it, language) }}"
                                ),
                                isPositive = true
                            )
                        }
                        if (bhava.aspectingPlanets.isNotEmpty()) {
                            StatusPill(
                                text = tr(
                                    language,
                                    "दृष्टि: ${bhava.aspectingPlanets.joinToString(", ") { AstroStrings.planetName(it, language) }}",
                                    "Aspects: ${bhava.aspectingPlanets.joinToString(", ") { AstroStrings.planetName(it, language) }}",
                                    "相位: ${bhava.aspectingPlanets.joinToString(", ") { AstroStrings.planetName(it, language) }}"
                                ),
                                isPositive = true
                            )
                        }
                    }

                    val conjList = when (language) {
                        AppLanguage.ZH -> bhava.conjunctionDescriptionsZh
                        AppLanguage.EN -> bhava.conjunctionDescriptionsEn
                        else -> bhava.conjunctionDescriptionsHi
                    }
                    conjList.forEach { desc ->
                        Text(
                            text = desc,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.secondary,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    if (bhava.matchedShlokas.isEmpty()) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                Icons.AutoMirrored.Outlined.MenuBook,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(2.dp)
                            )
                            Text(
                                text = tr(
                                    language,
                                    "इस विशिष्ट स्थिति का सत्यापित श्लोक अभी संकलित डेटाबेस में उपलब्ध नहीं है।",
                                    "No verified shloka matched for this specific combination in the bundled database yet.",
                                    "内置经文库暂未收录此特定组合的已验证颂文。"
                                ),
                                style = MaterialTheme.typography.bodySmall,
                                fontStyle = FontStyle.Italic,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    } else {
                        bhava.matchedShlokas.forEach { shloka ->
                            Surface(
                                color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.65f),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(
                                    modifier = Modifier.padding(12.dp),
                                    verticalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Icon(Icons.Outlined.Verified, contentDescription = "Verified", tint = AuspiciousGreen)
                                        Text(
                                            text = "${
                                                when (language) {
                                                    AppLanguage.ZH -> shloka.sourceBookZh
                                                    AppLanguage.EN -> shloka.sourceBookEn
                                                    else -> shloka.sourceBookHi
                                                }
                                            } (${shloka.chapter}.${shloka.verse})",
                                            style = MaterialTheme.typography.labelMedium,
                                            color = MaterialTheme.colorScheme.primary,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                    Text(
                                        text = shloka.sanskritVerse,
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.SemiBold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = when (language) {
                                            AppLanguage.ZH -> shloka.meaningZh
                                            AppLanguage.EN -> shloka.meaningEn
                                            else -> shloka.meaningHi
                                        },
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SummaryAndBalaTab(
    result: KundaliResult,
    language: AppLanguage
) {
    val summary = result.summary
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        item {
            Column(
                modifier = Modifier.widthIn(max = 680.dp).fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                SectionTitle(
                    title = tr(language, "कुंडली सारांश (लग्नेश, मारकेश, योगकारक)", "Kundali Summary", "星盘摘要")
                )
                GoldBorderedCard {
                    Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = "${AstroStrings.lagnaLabel(language)}: ${AstroStrings.rashiName(summary.lagnaRashi, language)} • " +
                                "${tr(language, "लग्नेश", "Lagnesh", "命主星")}: ${AstroStrings.planetName(summary.lagnesh, language)}",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "${tr(language, "मारकेश", "Marakesh (2nd/7th Lords)", "杀星主")}: " +
                                summary.marakeshPlanets.joinToString(", ") { AstroStrings.planetName(it, language) },
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Text(
                            text = "${tr(language, "सबसे बली ग्रह", "Strongest Planet", "最强行星")}: " +
                                "${AstroStrings.planetName(summary.strongestPlanet, language)} • " +
                                "${tr(language, "दुर्बल ग्रह", "Weakest Planet", "最弱行星")}: " +
                                AstroStrings.planetName(summary.weakestPlanet, language),
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Text(
                            text = "${tr(language, "बली भाव", "Strongest House", "最强宫位")}: H${summary.strongestHouse} • " +
                                "${tr(language, "दुर्बल भाव", "Weakest House", "最弱宫位")}: H${summary.weakestHouse} • " +
                                "${tr(language, "श्लोक मिलान", "Shloka Matches", "匹配经文")}: ${summary.matchedConditionsCount}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                GoldBorderedCard {
                    Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = tr(language, "ग्रहों का कार्यात्मक फल-स्वभाव", "Functional Nature of Planets", "行星功能吉凶属性"),
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        summary.planetSummaries.forEach { item ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "${AstroStrings.planetName(item.planet, language)} (${item.housesOwned.joinToString(",") { "H$it" }} → H${item.placedInHouse})",
                                    style = MaterialTheme.typography.bodyMedium
                                )
                                StatusPill(
                                    text = AstroStrings.functionalNatureLabel(item.functionalNature, language),
                                    isPositive = item.functionalNature != FunctionalNature.MALEFIC
                                )
                            }
                        }
                    }
                }

                SectionTitle(
                    title = tr(language, "आंशिक षड्बल (Shadbala)", "Partial Shadbala Strength", "六力强度 (Shadbala)")
                )
            }
        }

        items(result.shadbala, key = { it.planet.name }) { sb ->
            GoldBorderedCard(modifier = Modifier.widthIn(max = 680.dp)) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = AstroStrings.planetName(sb.planet, language),
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "${"%.1f".format(sb.totalScore)} pts",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                    LinearProgressIndicator(
                        progress = { sb.normalizedStrength },
                        modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(4.dp)),
                        color = MaterialTheme.colorScheme.primary,
                        trackColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                    Text(
                        text = "Sthana ${"%.0f".format(sb.sthanaBala)} • Dig ${"%.0f".format(sb.digBala)} • Kendra ${"%.0f".format(sb.kendradiBala)} • Nais ${"%.0f".format(sb.naisargikaBala)} • Paksha ${"%.0f".format(sb.pakshaBala)}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        // Ashtakavarga (Sarvashtakavarga + Bhinnashtakavarga)
        result.ashtakavarga?.let { av ->
            item {
                Column(
                    modifier = Modifier.widthIn(max = 680.dp).fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    SectionTitle(
                        title = tr(
                            language,
                            "सर्वाष्टकवर्ग (${av.totalSarvaBindus} बिंदु) एवं भिन्नाष्टकवर्ग",
                            "Sarvashtakavarga (${av.totalSarvaBindus} Bindus) & Bhinnashtakavarga",
                            "八分点强度表 (${av.totalSarvaBindus})"
                        )
                    )
                    GoldBorderedCard {
                        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Rashi.entries.forEachIndexed { idx, rashi ->
                                val pts = av.sarvashtakavargaByRashi.getOrElse(idx) { 28 }
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Text(
                                        text = AstroStrings.rashiName(rashi, language),
                                        style = MaterialTheme.typography.labelMedium,
                                        modifier = Modifier.width(84.dp)
                                    )
                                    LinearProgressIndicator(
                                        progress = { (pts / 48f).coerceIn(0f, 1f) },
                                        modifier = Modifier.weight(1f).height(8.dp).clip(RoundedCornerShape(4.dp)),
                                        color = if (pts >= 28) AuspiciousGreen else MaterialTheme.colorScheme.primary,
                                        trackColor = MaterialTheme.colorScheme.surfaceVariant
                                    )
                                    Text(
                                        text = "$pts",
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = if (pts >= 28) AuspiciousGreen else MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.width(28.dp)
                                    )
                                }
                            }
                        }
                    }

                    GoldBorderedCard {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState())
                                .padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = tr(language, "भिन्नाष्टकवर्ग तालिका (12 राशियाँ)", "Bhinnashtakavarga Table (12 Signs)", "行星八分点矩阵"),
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = DarkBrandBlue
                            )
                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                Text("Planet", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, modifier = Modifier.width(62.dp))
                                Rashi.entries.forEach { r ->
                                    Text(r.number.toString(), style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, modifier = Modifier.width(24.dp))
                                }
                                Text("Tot", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, modifier = Modifier.width(30.dp))
                            }
                            HorizontalDivider()
                            av.bhinnashtakavargas.forEach { bav ->
                                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    Text(AstroStrings.planetShort(bav.planet, language), style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, color = DarkBrandBlue, modifier = Modifier.width(62.dp))
                                    bav.bindusByRashi.forEach { b ->
                                        Text("$b", style = MaterialTheme.typography.bodySmall, color = if (b >= 4) AuspiciousGreen else MaterialTheme.colorScheme.onSurface, modifier = Modifier.width(24.dp))
                                    }
                                    Text("${bav.totalBindus}", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary, modifier = Modifier.width(30.dp))
                                }
                            }
                        }
                    }
                }
            }
        }

        item {
            Column(
                modifier = Modifier.widthIn(max = 680.dp).fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                SectionTitle(
                    title = tr(language, "भाव-बल (Bhava Bala)", "Bhava Bala (House Strength)", "宫位力量 (Bhava Bala)")
                )
                GoldBorderedCard {
                    Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        result.bhavaBala.forEach { bb ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Text("H${bb.houseNumber}", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, modifier = Modifier.width(34.dp))
                                LinearProgressIndicator(
                                    progress = { bb.normalizedStrength },
                                    modifier = Modifier.weight(1f).height(8.dp).clip(RoundedCornerShape(4.dp)),
                                    color = MaterialTheme.colorScheme.secondary,
                                    trackColor = MaterialTheme.colorScheme.surfaceVariant
                                )
                                Text("${"%.0f".format(bb.totalScore)}", style = MaterialTheme.typography.labelSmall, modifier = Modifier.width(36.dp))
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun GocharAndRashifalTab(
    result: KundaliResult,
    language: AppLanguage
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        item {
            Column(
                modifier = Modifier.widthIn(max = 680.dp).fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                SectionTitle(
                    title = tr(language, "3-दिवसीय चंद्र-बल एवं तारा-बल", "3-Day Chandra & Tara Bala", "三日月亮力与星宿力")
                )
                result.threeDayHoroscope.forEach { day ->
                    val dayTitle = when (day.dayOffset) {
                        0 -> tr(language, "आज", "Today", "今天")
                        1 -> tr(language, "कल", "Tomorrow", "明天")
                        else -> tr(language, "परसों", "Day After", "后天")
                    }
                    GoldBorderedCard {
                        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "$dayTitle (${day.dateStr})",
                                    style = MaterialTheme.typography.titleSmall,
                                    color = MaterialTheme.colorScheme.primary,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "${AstroStrings.rashiName(day.moonRashi, language)} • ${AstroStrings.nakshatraName(day.moonNakshatraIndex, language)}",
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                StatusPill(
                                    text = "${tr(language, "चंद्र बल", "Chandra", "月力")}: H${day.chandraHouseFromBirthMoon}",
                                    isPositive = day.isChandraBalaGood
                                )
                                StatusPill(
                                    text = "${tr(language, "तारा", "Tara", "星宿")}: ${AstroStrings.taraName(day.taraBalaNumber, language)}",
                                    isPositive = day.isTaraBalaGood
                                )
                            }
                        }
                    }
                }

                val isSadeSatiActive = result.sadeSatiPhase != SadeSatiPhase.NONE
                GoldBorderedCard(
                    containerColor = if (isSadeSatiActive) CautionAmberBg else AuspiciousGreenBg
                ) {
                    Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(
                            text = tr(language, "साढ़े साती / ढैया स्थिति", "Sade Sati & Dhaiya Status", "土星行运状态"),
                            style = MaterialTheme.typography.labelLarge,
                            color = if (isSadeSatiActive) CautionAmber else AuspiciousGreen,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = AstroStrings.sadeSatiLabel(result.sadeSatiPhase, language),
                            style = MaterialTheme.typography.bodyMedium,
                            color = if (isSadeSatiActive) CautionAmber else AuspiciousGreen
                        )
                    }
                }

                SectionTitle(
                    title = tr(language, "आज का गोचर (${result.gocharDateStr})", "Today's Gochar Transits (${result.gocharDateStr})", "今日行星行运 (${result.gocharDateStr})")
                )
            }
        }

        items(result.gocharPlanets, key = { "g_${it.planet.name}" }) { gp ->
            GoldBorderedCard(modifier = Modifier.widthIn(max = 680.dp)) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "${AstroStrings.planetName(gp.planet, language)} • ${AstroStrings.rashiName(gp.rashi, language)} ${gp.dmsFormatted}",
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "${tr(language, "लग्न से", "From Lagna", "命宫起")}: H${gp.houseFromLagna} • " +
                                "${tr(language, "चंद्र से", "From Moon", "月宫起")}: H${gp.houseFromMoon}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    DignityBadge(dignity = gp.dignity, language = language)
                }
            }
        }

        item {
            Column(
                modifier = Modifier.widthIn(max = 680.dp).fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                SectionTitle(
                    title = tr(language, "आगामी राशि परिवर्तन (30 दिन व 1 वर्ष)", "Upcoming Sign Changes (30 Days & 1 Year)", "未来换座时刻表（30天及1年）")
                )
                GoldBorderedCard {
                    Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        val list = result.upcomingTransits1Year.take(15)
                        if (list.isEmpty()) {
                            Text(text = "—", style = MaterialTheme.typography.bodyMedium)
                        } else {
                            list.forEach { t ->
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = "${AstroStrings.planetName(t.planet, language)}: ${AstroStrings.rashiName(t.fromRashi, language)} → ${AstroStrings.rashiName(t.toRashi, language)}",
                                        style = MaterialTheme.typography.bodyMedium,
                                        modifier = Modifier.weight(1f)
                                    )
                                    Text(
                                        text = "${t.changeDateStr} (+${t.daysFromNow}d)",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = if (t.daysFromNow <= 30) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun PanchangTab(
    result: KundaliResult,
    language: AppLanguage
) {
    val p = result.panchang
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        item {
            Column(
                modifier = Modifier.widthIn(max = 680.dp).fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                SectionTitle(
                    title = tr(language, "दैनिक पंचांग एवं मुहूर्त", "Daily Panchang & Muhurat", "每日印度历与吉时")
                )
                GoldBorderedCard {
                    Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        PanchangRow(tr(language, "वार", "Vaar (Weekday)", "星期"), AstroStrings.weekdayName(p.weekdayIndex, language))
                        PanchangRow(tr(language, "तिथि", "Tithi", "阴历日"), AstroStrings.tithiName(p.tithiNumber, language))
                        PanchangRow(tr(language, "नक्षत्र", "Nakshatra", "星宿"), AstroStrings.nakshatraName(p.nakshatraIndex, language))
                        PanchangRow(tr(language, "योग", "Yoga", "瑜伽"), AstroStrings.yogaName(p.yogaIndex, language))
                        PanchangRow(tr(language, "करण", "Karana", "半日"), AstroStrings.karanaName(p.karanaIndex, language))
                        PanchangRow(tr(language, "सूर्योदय / सूर्यास्त", "Sunrise / Sunset", "日出 / 日落"), "${p.sunriseStr}  /  ${p.sunsetStr}")
                        PanchangRow(tr(language, "राहुकाल", "Rahukaal", "罗睺凶时"), p.rahukaalStr)
                        PanchangRow(tr(language, "अभिजीत मुहूर्त", "Abhijit Muhurat", "阿比吉特吉时"), p.abhijitStr)
                        PanchangRow(tr(language, "होरा स्वामी", "Current Hora", "当前时主星"), AstroStrings.planetName(p.currentHoraLord, language))
                    }
                }

                SectionTitle(title = tr(language, "दिन के चौघड़िया", "Day Choghadiya", "白昼时段"))
                GoldBorderedCard {
                    Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        p.dayChoghadiya.forEach { slot ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("${slot.startTimeStr} – ${slot.endTimeStr}", style = MaterialTheme.typography.bodyMedium)
                                StatusPill(text = AstroStrings.choghadiyaName(slot.nature, language), isPositive = slot.isGood)
                            }
                        }
                    }
                }

                SectionTitle(title = tr(language, "रात्रि के चौघड़िया", "Night Choghadiya", "夜间时段"))
                GoldBorderedCard {
                    Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        p.nightChoghadiya.forEach { slot ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("${slot.startTimeStr} – ${slot.endTimeStr}", style = MaterialTheme.typography.bodyMedium)
                                StatusPill(text = AstroStrings.choghadiyaName(slot.nature, language), isPositive = slot.isGood)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun PanchangRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(text = value, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.primary)
    }
}

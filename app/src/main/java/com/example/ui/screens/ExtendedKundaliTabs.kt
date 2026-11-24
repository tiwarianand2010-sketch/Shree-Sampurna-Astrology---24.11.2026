package com.example.ui.screens

import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AccountCircle
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.Download
import androidx.compose.material.icons.outlined.PictureAsPdf
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.core.AppLanguage
import com.example.core.AstroStrings
import com.example.core.AstrologerAccount
import com.example.core.tr
import com.example.model.KundaliResult
import com.example.ui.PdfExportMode
import com.example.ui.PdfExportState
import com.example.ui.components.GoldBorderedCard
import com.example.ui.components.SectionTitle
import com.example.ui.components.StatusPill
import com.example.ui.theme.AuspiciousGreen
import com.example.ui.theme.AuspiciousGreenBg
import com.example.ui.theme.CautionAmber
import com.example.ui.theme.CautionAmberBg
import com.example.ui.theme.DarkBrandBlue

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun KpPaddhatiTab(
    result: KundaliResult,
    language: AppLanguage
) {
    val kp = result.kpAnalysis
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (kp == null) {
            item { Text("KP data unavailable", style = MaterialTheme.typography.bodyMedium) }
            return@LazyColumn
        }

        item {
            Column(
                modifier = Modifier.widthIn(max = 680.dp).fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                SectionTitle(
                    title = tr(language, "कृष्णमूर्ति पद्धति (KP System)", "Krishnamurti Paddhati (KP System)", "KP 占星体系")
                )
                GoldBorderedCard(containerColor = MaterialTheme.colorScheme.secondaryContainer) {
                    Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(
                            text = "KP Ayanamsa: ${"%.4f°".format(kp.kpAyanamsaDeg)}" +
                                (kp.prashnaNumber?.let { " • ${tr(language, "प्रश्न संख्या", "Horary #", "占卜数")} $it/249" } ?: ""),
                            style = MaterialTheme.typography.titleSmall,
                            color = DarkBrandBlue,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "${tr(language, "शासक ग्रह (Ruling Planets)", "Ruling Planets", "支配星")}: " +
                                kp.rulingPlanets.distinctRulers.joinToString(", ") { AstroStrings.planetName(it, language) },
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = "Day: ${AstroStrings.planetShort(kp.rulingPlanets.dayLord, language)} • " +
                                "Mo(Sign/Star): ${AstroStrings.planetShort(kp.rulingPlanets.moonSignLord, language)}/${AstroStrings.planetShort(kp.rulingPlanets.moonStarLord, language)} • " +
                                "Lg(Sign/Star): ${AstroStrings.planetShort(kp.rulingPlanets.lagnaSignLord, language)}/${AstroStrings.planetShort(kp.rulingPlanets.lagnaStarLord, language)}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                SectionTitle(
                    title = tr(language, "12 भाव संधि (Placidus Cusps & Sub-Lords)", "12 Placidus Cusps & Sub-Lords", "12 宫头与副主星")
                )
            }
        }

        items(kp.cusps, key = { "kp_c_${it.houseNumber}" }) { cusp ->
            GoldBorderedCard(modifier = Modifier.widthIn(max = 680.dp)) {
                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "${tr(language, "भाव", "Cusp", "宫")} ${cusp.houseNumber} • ${AstroStrings.rashiName(cusp.rashi, language)} ${cusp.dmsFormatted}",
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold
                        )
                        StatusPill(text = "#${cusp.kp.subNumber249}", isPositive = true)
                    }
                    Text(
                        text = "Sign: ${AstroStrings.planetShort(cusp.kp.signLord, language)} • " +
                            "Star: ${AstroStrings.planetShort(cusp.kp.starLord, language)} • " +
                            "Sub: ${AstroStrings.planetName(cusp.kp.subLord, language)} • " +
                            "Sub-Sub: ${AstroStrings.planetShort(cusp.kp.subSubLord, language)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = DarkBrandBlue,
                        fontWeight = FontWeight.Medium
                    )
                    if (cusp.significatorPlanets.isNotEmpty()) {
                        Text(
                            text = "${tr(language, "कारक ग्रह", "Significators", "指示星")}: ${cusp.significatorPlanets.joinToString(", ") { AstroStrings.planetName(it, language) }}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        item {
            Column(modifier = Modifier.widthIn(max = 680.dp).fillMaxWidth()) {
                SectionTitle(
                    title = tr(language, "KP ग्रह कारक तालिका", "KP Planets & 4-Grade Significators", "KP 行星宫位指示表")
                )
            }
        }

        items(kp.planets, key = { "kp_p_${it.planet.name}" }) { pl ->
            GoldBorderedCard(modifier = Modifier.widthIn(max = 680.dp)) {
                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "${AstroStrings.planetName(pl.planet, language)} (KP H${pl.kpHouse})",
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "${AstroStrings.rashiName(pl.rashi, language)} ${pl.dmsFormatted}",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                    Text(
                        text = "Star: ${AstroStrings.planetName(pl.kp.starLord, language)} • Sub: ${AstroStrings.planetName(pl.kp.subLord, language)} (#${pl.kp.subNumber249})",
                        style = MaterialTheme.typography.bodySmall,
                        color = DarkBrandBlue
                    )
                    Text(
                        text = "${tr(language, "सूचित भाव", "Signifies", "指示宫位")}: ${pl.signifiesHouses.joinToString(", ") { "H$it" }}",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.SemiBold,
                        color = AuspiciousGreen
                    )
                }
            }
        }
    }
}

@Composable
fun LalKitabTab(
    result: KundaliResult,
    language: AppLanguage
) {
    val lk = result.lalKitab
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (lk == null) {
            item { Text("Lal Kitab data unavailable") }
            return@LazyColumn
        }

        item {
            Column(modifier = Modifier.widthIn(max = 680.dp).fillMaxWidth()) {
                SectionTitle(
                    title = tr(language, "लाल किताब ऋण एवं दोष परीक्षण", "Lal Kitab Karmic Rinas (Debts)", "红皮书因果业债检验")
                )
            }
        }

        items(lk.rinas, key = { it.code }) { rina ->
            GoldBorderedCard(
                modifier = Modifier.widthIn(max = 680.dp),
                containerColor = if (rina.isPresent) CautionAmberBg else AuspiciousGreenBg
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (language == AppLanguage.EN) rina.titleEn else rina.titleHi,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = if (rina.isPresent) CautionAmber else AuspiciousGreen
                        )
                        StatusPill(
                            text = if (rina.isPresent) tr(language, "योग विद्यमान", "Active", "存在") else tr(language, "मुक्त", "Clear", "无碍"),
                            isPositive = !rina.isPresent
                        )
                    }
                    Text(
                        text = if (language == AppLanguage.EN) rina.reasonEn else rina.reasonHi,
                        style = MaterialTheme.typography.bodySmall
                    )
                    if (rina.isPresent) {
                        Text(
                            text = "${tr(language, "उपाय", "Remedy", "化解")}: ${if (language == AppLanguage.EN) rina.remedyEn else rina.remedyHi}",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }

        item {
            Column(modifier = Modifier.widthIn(max = 680.dp).fillMaxWidth()) {
                SectionTitle(
                    title = tr(language, "लाल किताब ग्रह खाना एवं अचूक उपाय", "Lal Kitab Khana & Remedies", "红皮书宫位与开运化解")
                )
            }
        }

        items(lk.entries, key = { "lk_${it.planet.name}" }) { entry ->
            GoldBorderedCard(modifier = Modifier.widthIn(max = 680.dp)) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "${AstroStrings.planetName(entry.planet, language)} — ${tr(language, "खाना", "Khana", "宫位")} #${entry.house}",
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold
                        )
                        if (entry.isBlind) {
                            StatusPill(text = tr(language, "अंधा/संवेदनशील", "Dormant", "敏感"), isPositive = false)
                        }
                    }
                    Text(
                        text = if (language == AppLanguage.EN) entry.natureNoteEn else entry.natureNoteHi,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "${tr(language, "उपाय", "Upay", "建议")}: ${if (language == AppLanguage.EN) entry.remedyEn else entry.remedyHi}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = AuspiciousGreen,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun WesternNumerologyVastuTab(
    result: KundaliResult,
    language: AppLanguage
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        result.westernChart?.let { w ->
            item {
                Column(modifier = Modifier.widthIn(max = 680.dp).fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    SectionTitle(
                        title = tr(language, "पाश्चात्य ट्रॉपिकल कुंडली (Western Tropical)", "Western Tropical Astrology", "西方回归黄道星盘")
                    )
                    GoldBorderedCard {
                        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text(
                                text = "Sun: ${AstroStrings.rashiName(w.sunSign, language)} • Moon: ${AstroStrings.rashiName(w.moonSign, language)} • Asc: ${AstroStrings.rashiName(w.ascendantSign, language)} • MC: ${AstroStrings.rashiName(w.midheavenSign, language)}",
                                style = MaterialTheme.typography.titleSmall,
                                color = DarkBrandBlue,
                                fontWeight = FontWeight.Bold
                            )
                            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                StatusPill("Fire: ${w.fireCount}", isPositive = w.fireCount >= 2)
                                StatusPill("Earth: ${w.earthCount}", isPositive = w.earthCount >= 2)
                                StatusPill("Air: ${w.airCount}", isPositive = w.airCount >= 2)
                                StatusPill("Water: ${w.waterCount}", isPositive = w.waterCount >= 2)
                            }
                            w.aspects.take(8).forEach { asp ->
                                Text(
                                    text = "${AstroStrings.planetName(asp.planet1, language)} — ${asp.aspectName} — ${AstroStrings.planetName(asp.planet2, language)} (${"%.1f".format(asp.orbDeg)}°)",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = if (asp.isHarmonious) AuspiciousGreen else MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }
                }
            }
        }

        result.numerology?.let { n ->
            item {
                Column(modifier = Modifier.widthIn(max = 680.dp).fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    SectionTitle(
                        title = tr(language, "अंकशास्त्र एवं लो-शू ग्रिड (Numerology & Lo Shu)", "Numerology & Lo Shu Grid", "数字命理与洛书九宫格")
                    )
                    GoldBorderedCard {
                        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            Text(
                                text = "${tr(language, "मूलांक", "Mulank", "主命数")}: ${n.mulank} • ${tr(language, "भाग्यांक", "Bhagyank", "天赋数")}: ${n.bhagyank} • Kua: ${n.kuaNumber} • Chaldean: ${n.chaldeanNameNumber}",
                                style = MaterialTheme.typography.titleSmall,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Bold
                            )
                            val gridRows = listOf(listOf(4, 9, 2), listOf(3, 5, 7), listOf(8, 1, 6))
                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                verticalArrangement = Arrangement.spacedBy(4.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                gridRows.forEach { rowDigits ->
                                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                        rowDigits.forEach { digit ->
                                            val count = n.loShuCounts[digit] ?: 0
                                            Box(
                                                modifier = Modifier
                                                    .size(72.dp)
                                                    .background(
                                                        if (count > 0) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant,
                                                        RoundedCornerShape(10.dp)
                                                    )
                                                    .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(10.dp)),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Text(
                                                    text = if (count > 0) digit.toString().repeat(count.coerceAtMost(4)) else "($digit)",
                                                    style = MaterialTheme.typography.titleMedium,
                                                    fontWeight = if (count > 0) FontWeight.Bold else FontWeight.Normal,
                                                    color = if (count > 0) DarkBrandBlue else MaterialTheme.colorScheme.onSurfaceVariant,
                                                    textAlign = TextAlign.Center
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                            Text(
                                text = "${tr(language, "शुभ रंग", "Lucky Colors", "幸运色")}: ${if (language == AppLanguage.EN) n.luckyColorsEn else n.luckyColorsHi}",
                                style = MaterialTheme.typography.bodySmall
                            )
                            Text(
                                text = "${tr(language, "शुभ दिन", "Lucky Days", "幸运日")}: ${if (language == AppLanguage.EN) n.luckyDaysEn else n.luckyDaysHi}",
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                }
            }
        }

        result.vastuFengShui?.let { v ->
            item {
                Column(modifier = Modifier.widthIn(max = 680.dp).fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    SectionTitle(
                        title = tr(language, "वास्तु शास्त्र एवं फ़ेंग शुई मार्गदर्शन", "Vastu & Feng Shui Compass", "印度风水与八宅方位")
                    )
                    GoldBorderedCard {
                        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text(
                                text = if (language == AppLanguage.EN) v.kuaGroupEn else v.kuaGroupHi,
                                style = MaterialTheme.typography.titleSmall,
                                color = DarkBrandBlue,
                                fontWeight = FontWeight.Bold
                            )
                            Text("• ${if (language == AppLanguage.EN) v.wealthDirectionEn else v.wealthDirectionHi}", style = MaterialTheme.typography.bodySmall)
                            Text("• ${if (language == AppLanguage.EN) v.entranceAdviceEn else v.entranceAdviceHi}", style = MaterialTheme.typography.bodySmall)
                            Text("• ${if (language == AppLanguage.EN) v.pujaDirectionEn else v.pujaDirectionHi}", style = MaterialTheme.typography.bodySmall)
                            Text("• ${if (language == AppLanguage.EN) v.kitchenDirectionEn else v.kitchenDirectionHi}", style = MaterialTheme.typography.bodySmall)
                            Text("• ${if (language == AppLanguage.EN) v.bedroomDirectionEn else v.bedroomDirectionHi}", style = MaterialTheme.typography.bodySmall)
                            val tips = if (language == AppLanguage.EN) v.elementTipsEn else v.elementTipsHi
                            tips.forEach { tip ->
                                Text("✓ $tip", style = MaterialTheme.typography.bodySmall, color = AuspiciousGreen, fontWeight = FontWeight.Medium)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun PdfReportsTab(
    result: KundaliResult,
    language: AppLanguage,
    pdfExportState: PdfExportState,
    astrologerAccount: AstrologerAccount = AstrologerAccount(),
    onOpenAccount: () -> Unit = {},
    onGenerateSummary: () -> Unit,
    onGenerateBrihat250: () -> Unit,
    onSaveToDevice: (Uri, String) -> Unit,
    onSharePdf: (Uri, String) -> Unit,
    onOpenAiChat: () -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        item {
            Column(
                modifier = Modifier.widthIn(max = 680.dp).fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                SectionTitle(
                    title = tr(
                        language,
                        "रंगीन PDF एवं 250-पृष्ठीय बृहत् कुंडली",
                        "Colorful PDF & 250-Page Brihat Kundali",
                        "彩色 PDF 与 250页详批命书"
                    )
                )

                // Registered Astrologer Name banner (shown at top of every PDF page)
                GoldBorderedCard(containerColor = MaterialTheme.colorScheme.secondaryContainer) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.AccountCircle,
                            contentDescription = null,
                            tint = DarkBrandBlue,
                            modifier = Modifier.size(32.dp)
                        )
                        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                            Text(
                                text = tr(
                                    language,
                                    "PDF के सबसे ऊपर दिखने वाला ज्योतिषी नाम:",
                                    "Top-of-PDF Astrologer Header:",
                                    "PDF顶部占星师姓名："
                                ),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = astrologerAccount.effectiveHeaderName(),
                                style = MaterialTheme.typography.titleSmall,
                                color = DarkBrandBlue,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        OutlinedButton(
                            onClick = onOpenAccount,
                            modifier = Modifier.testTag("pdf_edit_astrologer_button")
                        ) {
                            Text(
                                text = tr(language, "बदलें / खाता", "Edit / Account", "修改账户"),
                                style = MaterialTheme.typography.labelMedium
                            )
                        }
                    }
                }

                // Brihat 250-Page & Complete 21-Module Report Card
                GoldBorderedCard {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text(
                            text = tr(
                                language,
                                "संपूर्ण 21-मॉड्यूल बृहत् कुंडली (250 रंगीन पृष्ठ PDF)",
                                "Complete 21-Module Brihat Kundali (250 Colorful Pages PDF)",
                                "完整21模块大命书 (250页彩色PDF)"
                            ),
                            style = MaterialTheme.typography.titleMedium,
                            color = DarkBrandBlue,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = tr(
                                language,
                                "इसमें जन्म पंचांग, 20 वर्ग चार्ट (D1-D60), विंशोत्तरी व योगिनी दशा, फलादेश, भृगु चक्र (BCP), KP 4-स्टेप, नाड़ी, जैमिनी, सर्वतोभद्र व कोट चक्र, अष्टकवर्ग शोधन, षड्बल, रत्न-रुद्राक्ष, वैदिक/लो शू अंकशास्त्र, लाल किताब वर्षफल, भृगु नंदी नाड़ी, ताजिक वर्षफल 2026 और 40-दिन उपाय ट्रैकर शामिल हैं।",
                                "Includes Birth Panchang, all 20 Varga charts (D1–D60), Vimshottari & Yogini Dasha, Phaladesh, Bhrigu Chakra (BCP), KP 4-Step, Nadi, Jaimini, Sarvatobhadra & Kota Chakra, Ashtakavarga Shodhana, Shadbala, Gems, Vedic & Lo Shu Numerology, Lal Kitab Varshphal, BNN, Tajika 2026 & 40-Day Remedy Tracker.",
                                "包含出生历法、20种分盘(D1-D60)、大运、瑜伽运、BCP、KP四步法、纳迪、杰米尼、八分点、六力、宝石、数理/洛书、红皮书年运、BNN、Tajika 2026及40天开运追踪。"
                            ),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Button(
                            onClick = onGenerateBrihat250,
                            enabled = pdfExportState !is PdfExportState.Generating,
                            colors = ButtonDefaults.buttonColors(containerColor = DarkBrandBlue, contentColor = Color.White),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth().height(50.dp).testTag("generate_brihat_pdf_button")
                        ) {
                            Icon(Icons.Outlined.PictureAsPdf, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                tr(
                                    language,
                                    "250-पृष्ठीय बृहत् कुंडली PDF बनाएँ",
                                    "Generate 250-Page Brihat Kundali PDF",
                                    "生成250页大命书PDF"
                                ),
                                fontWeight = FontWeight.Bold
                            )
                        }

                        OutlinedButton(
                            onClick = onGenerateSummary,
                            enabled = pdfExportState !is PdfExportState.Generating,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth().height(48.dp).testTag("generate_summary_pdf_button")
                        ) {
                            Icon(Icons.Outlined.PictureAsPdf, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                tr(
                                    language,
                                    "संपूर्ण 21-मॉड्यूल 18-पृष्ठ PDF बनाएँ",
                                    "Generate 18-Page Complete 21-Module PDF",
                                    "生成18页全21模块PDF"
                                )
                            )
                        }
                    }
                }

                when (pdfExportState) {
                    is PdfExportState.Generating -> {
                        GoldBorderedCard(containerColor = MaterialTheme.colorScheme.secondaryContainer) {
                            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Text(
                                    text = "${tr(language, "PDF तैयार हो रही है...", "Generating PDF...", "正在生成PDF...")} (${pdfExportState.currentPage} / ${pdfExportState.totalPages})",
                                    style = MaterialTheme.typography.titleSmall,
                                    color = DarkBrandBlue,
                                    fontWeight = FontWeight.Bold
                                )
                                val progress = if (pdfExportState.totalPages > 0) {
                                    pdfExportState.currentPage.toFloat() / pdfExportState.totalPages.toFloat()
                                } else 0f
                                LinearProgressIndicator(
                                    progress = { progress },
                                    modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(4.dp))
                                )
                            }
                        }
                    }
                    is PdfExportState.Ready -> {
                        GoldBorderedCard(containerColor = AuspiciousGreenBg) {
                            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                Text(
                                    text = "✓ ${pdfExportState.fileName} (${pdfExportState.totalPages} pages)",
                                    style = MaterialTheme.typography.titleSmall,
                                    color = AuspiciousGreen,
                                    fontWeight = FontWeight.Bold
                                )
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                                    Button(
                                        onClick = { onSaveToDevice(pdfExportState.uri, pdfExportState.fileName) },
                                        modifier = Modifier.weight(1f).height(48.dp).testTag("save_pdf_button")
                                    ) {
                                        Icon(Icons.Outlined.Download, contentDescription = null, modifier = Modifier.size(18.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(tr(language, "सेव करें", "Save PDF", "保存"))
                                    }
                                    OutlinedButton(
                                        onClick = { onSharePdf(pdfExportState.uri, pdfExportState.fileName) },
                                        modifier = Modifier.weight(1f).height(48.dp).testTag("share_pdf_button")
                                    ) {
                                        Icon(Icons.Outlined.Share, contentDescription = null, modifier = Modifier.size(18.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(tr(language, "शेयर / खोलें", "Open / Share", "分享"))
                                    }
                                }
                            }
                        }
                    }
                    is PdfExportState.Error -> {
                        Surface(color = CautionAmberBg, shape = RoundedCornerShape(12.dp), modifier = Modifier.fillMaxWidth()) {
                            Text(
                                text = pdfExportState.message,
                                color = CautionAmber,
                                modifier = Modifier.padding(12.dp),
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                    PdfExportState.Idle -> Unit
                }

                // AI Astrologer shortcut
                GoldBorderedCard {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = tr(
                                language,
                                "AI ज्योतिषी से अपनी कुंडली पर सीधी बातचीत",
                                "Chat with AI Astrologer About This Chart",
                                "与 AI 占星师探讨此星盘"
                            ),
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold
                        )
                        Button(
                            onClick = onOpenAiChat,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth().height(48.dp)
                        ) {
                            Icon(Icons.Outlined.AutoAwesome, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(tr(language, "AI ज्योतिष चैट खोलें", "Open AI Astrologer Chat", "打开 AI 占星对话"))
                        }
                    }
                }
            }
        }
    }
}

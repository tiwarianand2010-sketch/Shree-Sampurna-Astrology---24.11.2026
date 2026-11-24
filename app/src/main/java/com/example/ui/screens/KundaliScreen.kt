package com.example.ui.screens

import android.net.Uri
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.AutoStories
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Timeline
import androidx.compose.material.icons.outlined.Assessment
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.AutoStories
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.CloudUpload
import androidx.compose.material.icons.outlined.GridView
import androidx.compose.material.icons.outlined.Public
import androidx.compose.material.icons.outlined.Timeline
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.core.AppLanguage
import com.example.core.AstrologerAccount
import com.example.core.tr
import com.example.model.ChartDivision
import com.example.model.ChartStyle
import com.example.ui.KundaliUiState
import com.example.ui.PdfExportState
import com.example.ui.components.LanguageAndSupportActions
import com.example.ui.theme.DarkBrandBlue

private data class KundaliTabSpec(
    val index: Int,
    val labelHi: String,
    val labelEn: String,
    val labelZh: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
    val tag: String
)

private val primaryBottomTabs = listOf(
    KundaliTabSpec(0, "कुंडली", "Chart", "星盘", Icons.Filled.GridView, Icons.Outlined.GridView, "tab_chart"),
    KundaliTabSpec(1, "दशा", "Dasha", "大运", Icons.Filled.Timeline, Icons.Outlined.Timeline, "tab_dasha"),
    KundaliTabSpec(2, "फलादेश", "Phalit", "断语", Icons.Filled.AutoStories, Icons.Outlined.AutoStories, "tab_phalit"),
    KundaliTabSpec(3, "बल/वर्ग", "Bala", "力量", Icons.Filled.Assessment, Icons.Outlined.Assessment, "tab_bala"),
    KundaliTabSpec(4, "गोचर", "Gochar", "行运", Icons.Filled.Public, Icons.Outlined.Public, "tab_gochar"),
    KundaliTabSpec(5, "पंचांग", "Panchang", "历法", Icons.Filled.CalendarMonth, Icons.Outlined.CalendarMonth, "tab_panchang")
)

private data class SecondaryModuleChip(
    val tabIndex: Int,
    val labelHi: String,
    val labelEn: String,
    val labelZh: String,
    val tag: String
)

private val advancedModuleChips = listOf(
    SecondaryModuleChip(0, "षोडशवर्ग", "Shodashvarga", "十六分盘", "chip_shodashvarga"),
    SecondaryModuleChip(6, "KP पद्धति", "KP System", "KP体系", "chip_kp"),
    SecondaryModuleChip(7, "लाल किताब", "Lal Kitab", "红皮书", "chip_lal_kitab"),
    SecondaryModuleChip(8, "पश्चिमी/अंक/वास्तु", "West/Num/Vastu", "西方/数理/风水", "chip_west_vastu"),
    SecondaryModuleChip(9, "250p PDF रिपोर्ट", "250p PDF", "250页PDF", "chip_pdf_reports")
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun KundaliScreen(
    language: AppLanguage,
    kundaliState: KundaliUiState,
    chartStyle: ChartStyle,
    chartDivision: ChartDivision,
    selectedTab: Int,
    pdfExportState: PdfExportState,
    astrologerAccount: AstrologerAccount = AstrologerAccount(),
    onSelectTab: (Int) -> Unit,
    onChartStyleChange: (ChartStyle) -> Unit,
    onDivisionChange: (ChartDivision) -> Unit,
    onLanguageSelected: (AppLanguage) -> Unit,
    onHelpClick: () -> Unit,
    onOpenAiChat: () -> Unit,
    onOpenCloudCharts: () -> Unit,
    onOpenAccount: () -> Unit = {},
    onGenerateSummaryPdf: () -> Unit,
    onGenerateBrihat250Pdf: () -> Unit,
    onSavePdfToDevice: (Uri, String) -> Unit,
    onSharePdf: (Uri, String) -> Unit,
    onBack: () -> Unit
) {
    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val useRail = maxWidth >= 600.dp

        Scaffold(
            containerColor = MaterialTheme.colorScheme.background,
            topBar = {
                TopAppBar(
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    ),
                    navigationIcon = {
                        IconButton(
                            onClick = onBack,
                            modifier = Modifier
                                .minimumInteractiveComponentSize()
                                .testTag("kundali_back_button")
                        ) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                        }
                    },
                    title = {
                        val title = when (kundaliState) {
                            is KundaliUiState.Ready -> kundaliState.result.input.name
                            else -> tr(language, "कुंडली विश्लेषण", "Kundali Workspace", "星盘分析")
                        }
                        Text(
                            text = title,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            color = DarkBrandBlue,
                            fontWeight = FontWeight.Bold
                        )
                    },
                    actions = {
                        IconButton(
                            onClick = onOpenAiChat,
                            modifier = Modifier.minimumInteractiveComponentSize().testTag("kundali_ai_chat_button")
                        ) {
                            Icon(
                                Icons.Outlined.AutoAwesome,
                                contentDescription = tr(language, "AI ज्योतिषी", "AI Astrologer", "AI占星师"),
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                        IconButton(
                            onClick = onOpenCloudCharts,
                            modifier = Modifier.minimumInteractiveComponentSize().testTag("kundali_cloud_save_button")
                        ) {
                            Icon(
                                Icons.Outlined.CloudUpload,
                                contentDescription = tr(language, "क्लाउड में सहेजें", "Cloud Charts", "云端保存"),
                                tint = DarkBrandBlue
                            )
                        }
                        LanguageAndSupportActions(
                            language = language,
                            onLanguageSelected = onLanguageSelected,
                            onHelpClick = onHelpClick
                        )
                    }
                )
            },
            bottomBar = {
                if (!useRail) {
                    NavigationBar(containerColor = MaterialTheme.colorScheme.surface) {
                        primaryBottomTabs.forEach { tab ->
                            val selected = selectedTab == tab.index
                            val label = tr(language, tab.labelHi, tab.labelEn, tab.labelZh)
                            NavigationBarItem(
                                selected = selected,
                                onClick = { onSelectTab(tab.index) },
                                icon = {
                                    Icon(
                                        imageVector = if (selected) tab.selectedIcon else tab.unselectedIcon,
                                        contentDescription = label
                                    )
                                },
                                label = {
                                    Text(text = label, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                },
                                modifier = Modifier.testTag(tab.tag)
                            )
                        }
                    }
                }
            }
        ) { innerPadding ->
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                if (useRail) {
                    NavigationRail(containerColor = MaterialTheme.colorScheme.surface) {
                        primaryBottomTabs.forEach { tab ->
                            val selected = selectedTab == tab.index
                            val label = tr(language, tab.labelHi, tab.labelEn, tab.labelZh)
                            NavigationRailItem(
                                selected = selected,
                                onClick = { onSelectTab(tab.index) },
                                icon = {
                                    Icon(
                                        imageVector = if (selected) tab.selectedIcon else tab.unselectedIcon,
                                        contentDescription = label
                                    )
                                },
                                label = { Text(label, maxLines = 1) },
                                modifier = Modifier.testTag(tab.tag)
                            )
                        }
                    }
                }

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxSize()
                ) {
                    // Quick horizontal selector strip for advanced astrological modules & 250p PDF
                    Surface(
                        color = MaterialTheme.colorScheme.surface,
                        tonalElevation = 1.dp,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        LazyRow(
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(advancedModuleChips, key = { it.tag }) { chip ->
                                FilterChip(
                                    selected = selectedTab == chip.tabIndex,
                                    onClick = { onSelectTab(chip.tabIndex) },
                                    label = {
                                        Text(tr(language, chip.labelHi, chip.labelEn, chip.labelZh))
                                    },
                                    modifier = Modifier.testTag(chip.tag)
                                )
                            }
                        }
                    }

                    Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
                        when (kundaliState) {
                            is KundaliUiState.Idle,
                            is KundaliUiState.Calculating -> {
                                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                            }
                            is KundaliUiState.Ready -> {
                                when (selectedTab) {
                                    0 -> ChartAndPlanetsTab(
                                        result = kundaliState.result,
                                        language = language,
                                        chartStyle = chartStyle,
                                        division = chartDivision,
                                        onChartStyleChange = onChartStyleChange,
                                        onDivisionChange = onDivisionChange
                                    )
                                    1 -> DashaTab(kundaliState.result, language)
                                    2 -> PhalitTab(kundaliState.result, language)
                                    3 -> SummaryAndBalaTab(kundaliState.result, language)
                                    4 -> GocharAndRashifalTab(kundaliState.result, language)
                                    5 -> PanchangTab(kundaliState.result, language)
                                    6 -> KpPaddhatiTab(kundaliState.result, language)
                                    7 -> LalKitabTab(kundaliState.result, language)
                                    8 -> WesternNumerologyVastuTab(kundaliState.result, language)
                                    9 -> PdfReportsTab(
                                        result = kundaliState.result,
                                        language = language,
                                        pdfExportState = pdfExportState,
                                        astrologerAccount = astrologerAccount,
                                        onOpenAccount = onOpenAccount,
                                        onGenerateSummary = onGenerateSummaryPdf,
                                        onGenerateBrihat250 = onGenerateBrihat250Pdf,
                                        onSaveToDevice = onSavePdfToDevice,
                                        onSharePdf = onSharePdf,
                                        onOpenAiChat = onOpenAiChat
                                    )
                                    else -> ChartAndPlanetsTab(
                                        result = kundaliState.result,
                                        language = language,
                                        chartStyle = chartStyle,
                                        division = chartDivision,
                                        onChartStyleChange = onChartStyleChange,
                                        onDivisionChange = onDivisionChange
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

package com.example.ui.screens

import android.net.Uri
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.Download
import androidx.compose.material.icons.outlined.Favorite
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.PictureAsPdf
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material.icons.outlined.SwapVert
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.core.AppLanguage
import com.example.core.AstroStrings
import com.example.core.tr
import com.example.data.PlacesRepository
import com.example.model.MatchingResult
import com.example.ui.BirthFormState
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

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun MatchingScreen(
    language: AppLanguage,
    boyForm: BirthFormState,
    girlForm: BirthFormState,
    matchingResult: MatchingResult?,
    isCalculating: Boolean,
    pdfExportState: PdfExportState,
    onUpdateBoy: ((BirthFormState) -> BirthFormState) -> Unit,
    onUpdateGirl: ((BirthFormState) -> BirthFormState) -> Unit,
    onCopyMainToBoy: () -> Unit,
    onSwapForms: () -> Unit,
    onCalculateMatching: () -> Unit,
    onGenerateMatchingPdf: () -> Unit,
    onSavePdfToDevice: (Uri, String) -> Unit,
    onSharePdf: (Uri, String) -> Unit,
    onBack: () -> Unit
) {
    var pickingForBoy by remember { mutableStateOf<Boolean?>(null) }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface),
                navigationIcon = {
                    IconButton(onClick = onBack, modifier = Modifier.minimumInteractiveComponentSize().testTag("matching_back_button")) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                title = {
                    Text(
                        text = tr(language, "कुंडली मिलान (अष्टकूट एवं मांगलिक)", "Kundali Matching (Ashtakoot Milan)", "合婚配对 (36分)"),
                        color = DarkBrandBlue,
                        fontWeight = FontWeight.Bold
                    )
                }
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            item {
                Column(modifier = Modifier.widthIn(max = 680.dp).fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        OutlinedButton(onClick = onCopyMainToBoy, modifier = Modifier.height(44.dp)) {
                            Text(tr(language, "मुख्य कुंडली वर में भरें", "Use Active Chart as Groom", "载入当前盘(男)"))
                        }
                        OutlinedButton(onClick = onSwapForms, modifier = Modifier.height(44.dp)) {
                            Icon(Icons.Outlined.SwapVert, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(tr(language, "वर-वधू बदलें", "Swap Groom & Bride", "互换双方"))
                        }
                    }

                    PartnerBirthFormCard(
                        title = tr(language, "वर विवरण (Groom)", "Groom Birth Details", "男方出生信息"),
                        form = boyForm,
                        language = language,
                        onUpdate = onUpdateBoy,
                        onPickPlace = { pickingForBoy = true }
                    )

                    PartnerBirthFormCard(
                        title = tr(language, "वधू विवरण (Bride)", "Bride Birth Details", "女方出生信息"),
                        form = girlForm,
                        language = language,
                        onUpdate = onUpdateGirl,
                        onPickPlace = { pickingForBoy = false }
                    )

                    Button(
                        onClick = onCalculateMatching,
                        colors = ButtonDefaults.buttonColors(containerColor = DarkBrandBlue, contentColor = Color.White),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.fillMaxWidth().height(50.dp).testTag("calculate_matching_button")
                    ) {
                        if (isCalculating) {
                            CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color.White, strokeWidth = 2.dp)
                        } else {
                            Icon(Icons.Outlined.Favorite, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(tr(language, "गुण मिलान देखें (36 गुण)", "Calculate 36-Guna Match", "计算合婚评分"), fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            if (matchingResult != null) {
                item {
                    Column(modifier = Modifier.widthIn(max = 680.dp).fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        val good = matchingResult.totalScore >= 18.0
                        GoldBorderedCard(containerColor = if (good) AuspiciousGreenBg else CautionAmberBg) {
                            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Text(
                                    text = "${"%.1f".format(matchingResult.totalScore)} / 36.0 ${tr(language, "गुण", "Gunas", "分")}",
                                    style = MaterialTheme.typography.headlineMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = if (good) AuspiciousGreen else CautionAmber
                                )
                                LinearProgressIndicator(
                                    progress = { (matchingResult.totalScore / 36.0).toFloat().coerceIn(0f, 1f) },
                                    modifier = Modifier.fillMaxWidth().height(10.dp).clip(RoundedCornerShape(5.dp)),
                                    color = if (good) AuspiciousGreen else CautionAmber
                                )
                                Text(
                                    text = if (language == AppLanguage.EN) matchingResult.verdictEn else matchingResult.verdictHi,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    color = if (good) AuspiciousGreen else CautionAmber
                                )
                            }
                        }

                        // Manglik cards
                        GoldBorderedCard {
                            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                SectionTitle(title = tr(language, "मांगलिक दोष विश्लेषण", "Manglik Dosha Comparison", "火星煞 (Manglik) 检验"))
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                    Text(tr(language, "वर", "Groom", "男方"), fontWeight = FontWeight.Bold, modifier = Modifier.width(64.dp))
                                    StatusPill(
                                        text = if (language == AppLanguage.EN) matchingResult.boyManglik.summaryEn else matchingResult.boyManglik.summaryHi,
                                        isPositive = !matchingResult.boyManglik.isManglik || matchingResult.boyManglik.isCancelled
                                    )
                                }
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                    Text(tr(language, "वधू", "Bride", "女方"), fontWeight = FontWeight.Bold, modifier = Modifier.width(64.dp))
                                    StatusPill(
                                        text = if (language == AppLanguage.EN) matchingResult.girlManglik.summaryEn else matchingResult.girlManglik.summaryHi,
                                        isPositive = !matchingResult.girlManglik.isManglik || matchingResult.girlManglik.isCancelled
                                    )
                                }
                            }
                        }

                        // Matching PDF download
                        OutlinedButton(
                            onClick = onGenerateMatchingPdf,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth().height(48.dp).testTag("export_matching_pdf_button")
                        ) {
                            Icon(Icons.Outlined.PictureAsPdf, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(tr(language, "कुंडली मिलान PDF डाउनलोड करें", "Download Matching PDF", "下载合婚PDF"))
                        }

                        if (pdfExportState is PdfExportState.Ready && pdfExportState.mode == PdfExportMode.MATCHING) {
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                                Button(
                                    onClick = { onSavePdfToDevice(pdfExportState.uri, pdfExportState.fileName) },
                                    modifier = Modifier.weight(1f).height(46.dp)
                                ) {
                                    Icon(Icons.Outlined.Download, contentDescription = null, modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(tr(language, "सेव PDF", "Save PDF", "保存"))
                                }
                                OutlinedButton(
                                    onClick = { onSharePdf(pdfExportState.uri, pdfExportState.fileName) },
                                    modifier = Modifier.weight(1f).height(46.dp)
                                ) {
                                    Icon(Icons.Outlined.Share, contentDescription = null, modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(tr(language, "शेयर PDF", "Share PDF", "分享"))
                                }
                            }
                        }

                        SectionTitle(title = tr(language, "अष्टकूट तालिका (8 कूट)", "Ashtakoot Breakdown (8 Koots)", "八重配对分项"))
                    }
                }

                items(matchingResult.koots, key = { it.nameEn }) { k ->
                    GoldBorderedCard(modifier = Modifier.widthIn(max = 680.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "${if (language == AppLanguage.EN) k.nameEn else k.nameHi} (${k.boyValue} / ${k.girlValue})",
                                    style = MaterialTheme.typography.titleSmall,
                                    color = MaterialTheme.colorScheme.primary,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = if (language == AppLanguage.EN) k.noteEn else k.noteHi,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            StatusPill(
                                text = "${"%.1f".format(k.obtainedPoints)} / ${"%.0f".format(k.maxPoints)}",
                                isPositive = k.obtainedPoints >= k.maxPoints * 0.5
                            )
                        }
                    }
                }
            }
        }
    }

    val currentTargetIsBoy = pickingForBoy
    if (currentTargetIsBoy != null) {
        val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        var searchQuery by remember { mutableStateOf("") }
        val filtered = remember(searchQuery) { PlacesRepository.search(searchQuery) }
        ModalBottomSheet(onDismissRequest = { pickingForBoy = null }, sheetState = sheetState) {
            Column(modifier = Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = tr(language, "स्थान चुनें", "Select Place", "选择地点"),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    leadingIcon = { Icon(Icons.Outlined.Search, contentDescription = null) },
                    placeholder = { Text(tr(language, "शहर खोजें...", "Search city...", "搜索城市...")) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                LazyColumn(modifier = Modifier.fillMaxWidth().height(340.dp)) {
                    items(filtered, key = { it.id }) { place ->
                        ListItem(
                            headlineContent = { Text(AstroStrings.placeName(place, language), fontWeight = FontWeight.SemiBold) },
                            supportingContent = { Text("Lat %.3f°, Lon %.3f° • UTC%+.1f".format(place.latitude, place.longitude, place.baseUtcOffsetHours)) },
                            leadingContent = { Icon(Icons.Outlined.LocationOn, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
                            modifier = Modifier.clickable {
                                if (currentTargetIsBoy) onUpdateBoy { it.copy(selectedPlace = place) }
                                else onUpdateGirl { it.copy(selectedPlace = place) }
                                pickingForBoy = null
                            }
                        )
                        HorizontalDivider()
                    }
                }
            }
        }
    }
}

@Composable
private fun PartnerBirthFormCard(
    title: String,
    form: BirthFormState,
    language: AppLanguage,
    onUpdate: ((BirthFormState) -> BirthFormState) -> Unit,
    onPickPlace: () -> Unit
) {
    GoldBorderedCard {
        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            SectionTitle(title = title)
            OutlinedTextField(
                value = form.name,
                onValueChange = { v -> onUpdate { it.copy(name = v) } },
                label = { Text(tr(language, "नाम", "Name", "姓名")) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = form.day,
                    onValueChange = { v -> onUpdate { it.copy(day = v.filter { c -> c.isDigit() }.take(2)) } },
                    label = { Text(tr(language, "दिन", "Day", "日")) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.weight(1f)
                )
                OutlinedTextField(
                    value = form.month,
                    onValueChange = { v -> onUpdate { it.copy(month = v.filter { c -> c.isDigit() }.take(2)) } },
                    label = { Text(tr(language, "माह", "Mon", "月")) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.weight(1f)
                )
                OutlinedTextField(
                    value = form.year,
                    onValueChange = { v -> onUpdate { it.copy(year = v.filter { c -> c.isDigit() }.take(4)) } },
                    label = { Text(tr(language, "वर्ष", "Year", "年")) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.weight(1.2f)
                )
                OutlinedTextField(
                    value = form.hour,
                    onValueChange = { v -> onUpdate { it.copy(hour = v.filter { c -> c.isDigit() }.take(2)) } },
                    label = { Text(tr(language, "घं", "Hr", "时")) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.weight(0.9f)
                )
                OutlinedTextField(
                    value = form.minute,
                    onValueChange = { v -> onUpdate { it.copy(minute = v.filter { c -> c.isDigit() }.take(2)) } },
                    label = { Text(tr(language, "मि", "Min", "分")) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.weight(0.9f)
                )
            }
            OutlinedButton(onClick = onPickPlace, modifier = Modifier.fillMaxWidth().height(48.dp)) {
                Icon(Icons.Outlined.LocationOn, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text(AstroStrings.placeName(form.selectedPlace, language), modifier = Modifier.weight(1f))
            }
            if (form.validationError != null) {
                Text(form.validationError, color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}

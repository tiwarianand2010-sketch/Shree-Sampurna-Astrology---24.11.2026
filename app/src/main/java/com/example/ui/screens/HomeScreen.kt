package com.example.ui.screens

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AccountCircle
import androidx.compose.material.icons.outlined.AddLocationAlt
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.CloudUpload
import androidx.compose.material.icons.outlined.Diamond
import androidx.compose.material.icons.outlined.Favorite
import androidx.compose.material.icons.outlined.GpsFixed
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.PictureAsPdf
import androidx.compose.material.icons.outlined.Psychology
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.core.AppLanguage
import com.example.core.AstroStrings
import com.example.core.AstrologerAccount
import com.example.core.LocationHelper
import com.example.core.ResolvedGpsLocation
import com.example.core.tr
import com.example.data.PlacesRepository
import com.example.model.Place
import com.example.ui.BirthFormState
import com.example.ui.components.CelestialHeroHeader
import com.example.ui.components.GoldBorderedCard
import com.example.ui.components.LanguageAndSupportActions
import com.example.ui.components.SectionTitle
import com.example.ui.theme.DarkBrandBlue
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun HomeScreen(
    language: AppLanguage,
    formState: BirthFormState,
    hasCalculatedChart: Boolean,
    onLanguageSelected: (AppLanguage) -> Unit,
    onHelpClick: () -> Unit,
    onNameChange: (String) -> Unit,
    onDayChange: (String) -> Unit,
    onMonthChange: (String) -> Unit,
    onYearChange: (String) -> Unit,
    onHourChange: (String) -> Unit,
    onMinuteChange: (String) -> Unit,
    onCustomOffsetChange: (String) -> Unit,
    onPrashnaNumberChange: (String) -> Unit,
    onPlaceQueryChange: (String) -> Unit,
    onPlaceSelected: (Place) -> Unit,
    onAddCustomPlace: (name: String, lat: Double, lon: Double, utcOffset: Double) -> Unit,
    onApplyCurrentPreset: () -> Unit,
    onApplyIndependencePreset: () -> Unit,
    onApplyPrashnaGps: (gps: ResolvedGpsLocation?, prashnaNum: Int) -> Unit,
    onCalculateClick: () -> Unit,
    onOpenExistingChart: () -> Unit,
    onOpenMatching: () -> Unit,
    onOpenAiChat: () -> Unit,
    onOpenCloudCharts: () -> Unit,
    onOpenStore: () -> Unit,
    onOpenBrihatPdf: () -> Unit,
    astrologerAccount: AstrologerAccount = AstrologerAccount(),
    onOpenAccount: () -> Unit = {}
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    var showPlacePicker by remember { mutableStateOf(false) }
    var isFetchingGps by remember { mutableStateOf(false) }

    fun triggerGpsPrashnaFetch() {
        val prashnaNum = (formState.prashnaNumberText.toIntOrNull()?.takeIf { it in 1..249 })
            ?: ((System.currentTimeMillis() % 249).toInt() + 1)
        isFetchingGps = true
        scope.launch {
            val locResult = LocationHelper.fetchCurrentLocation(context)
            isFetchingGps = false
            locResult.fold(
                onSuccess = { gps ->
                    onApplyPrashnaGps(gps, prashnaNum)
                },
                onFailure = { err ->
                    onApplyPrashnaGps(null, prashnaNum)
                    snackbarHostState.showSnackbar(
                        err.localizedMessage ?: tr(
                            language,
                            "GPS उपलब्ध नहीं है, चयनित शहर से प्रश्न कुंडली बनाई गई।",
                            "GPS unavailable; Prashna generated using selected city.",
                            "GPS不可用，已使用所选城市生成占卜盘。"
                        )
                    )
                }
            )
        }
    }

    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            triggerGpsPrashnaFetch()
        } else {
            scope.launch {
                snackbarHostState.showSnackbar(
                    tr(
                        language,
                        "GPS स्थान अनुमति अस्वीकृत — आप सूची से अपना शहर चुन सकते हैं।",
                        "Location permission denied. You can choose your city manually from the list.",
                        "位置权限未授予，可从列表手动选择城市。"
                    )
                )
            }
        }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.img_astro_emblem),
                            contentDescription = null,
                            modifier = Modifier
                                .size(34.dp)
                                .clip(CircleShape),
                            contentScale = ContentScale.Crop
                        )
                        Text(
                            text = AstroStrings.appTitle(language),
                            style = MaterialTheme.typography.titleLarge,
                            color = DarkBrandBlue,
                            fontWeight = FontWeight.Bold
                        )
                    }
                },
                actions = {
                    androidx.compose.material3.IconButton(
                        onClick = onOpenAccount,
                        modifier = Modifier
                            .minimumInteractiveComponentSize()
                            .testTag("topbar_account_button")
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.AccountCircle,
                            contentDescription = tr(language, "ज्योतिषी खाता", "Astrologer Account", "占星师账户"),
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
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Column(
                modifier = Modifier
                    .widthIn(max = 640.dp)
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                CelestialHeroHeader(language = language, compact = true)

                // Slim Quick Preset & GPS Prashna Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    SlimPresetChipButton(
                        icon = Icons.Outlined.History,
                        label = tr(language, "1947 कुंडली", "1947 Chart", "1947 盘"),
                        onClick = onApplyIndependencePreset,
                        modifier = Modifier.weight(1f).testTag("preset_independence_chip")
                    )
                    SlimPresetChipButton(
                        icon = Icons.Outlined.Schedule,
                        label = tr(language, "वर्तमान समय", "Now", "当前时刻"),
                        onClick = onApplyCurrentPreset,
                        modifier = Modifier.weight(0.85f).testTag("preset_now_chip")
                    )
                    SlimPresetChipButton(
                        icon = Icons.Outlined.GpsFixed,
                        label = tr(language, "GPS प्रश्न", "GPS Prashna", "GPS 占卜"),
                        isLoading = isFetchingGps,
                        onClick = {
                            if (LocationHelper.hasCoarseLocationPermission(context)) {
                                triggerGpsPrashnaFetch()
                            } else {
                                locationPermissionLauncher.launch(Manifest.permission.ACCESS_COARSE_LOCATION)
                            }
                        },
                        modifier = Modifier.weight(1f).testTag("gps_prashna_button")
                    )
                }

                // Compact 2x3 Feature Hub Grid (slim & uniform buttons)
                GoldBorderedCard {
                    Column(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 10.dp, vertical = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        SectionTitle(
                            title = tr(
                                language,
                                "ज्योतिष सेवाएँ (10 भाषाएँ)",
                                "Astrology Suite (10 Languages)",
                                "占星功能中心 (10种语言)"
                            )
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            SlimHubButton(
                                icon = Icons.Outlined.Favorite,
                                label = tr(language, "कुंडली मिलान", "Matching", "合婚配对"),
                                onClick = onOpenMatching,
                                modifier = Modifier.weight(1f).testTag("nav_matching_button")
                            )
                            SlimHubButton(
                                icon = Icons.Outlined.Psychology,
                                label = tr(language, "AI ज्योतिषी", "AI Chat", "AI 占星"),
                                onClick = onOpenAiChat,
                                modifier = Modifier.weight(1f).testTag("nav_ai_chat_button")
                            )
                            SlimHubButton(
                                icon = Icons.Outlined.CloudUpload,
                                label = tr(language, "क्लाउड संग्रह", "Cloud Save", "云端库"),
                                onClick = onOpenCloudCharts,
                                modifier = Modifier.weight(1f).testTag("nav_cloud_button")
                            )
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            SlimHubButton(
                                icon = Icons.Outlined.Diamond,
                                label = tr(language, "रत्न-रुद्राक्ष", "Gems/Yantra", "宝石法印"),
                                onClick = onOpenStore,
                                modifier = Modifier.weight(1f).testTag("nav_store_button")
                            )
                            SlimHubButton(
                                icon = Icons.Outlined.PictureAsPdf,
                                label = tr(language, "250p PDF", "250p PDF", "250页PDF"),
                                onClick = onOpenBrihatPdf,
                                modifier = Modifier.weight(1f).testTag("nav_pdf_button")
                            )
                            val accLabel = if (astrologerAccount.isConfigured) {
                                astrologerAccount.effectiveHeaderName().take(14)
                            } else {
                                tr(language, "ज्योतिषी खाता", "Account", "占星账户")
                            }
                            SlimHubButton(
                                icon = Icons.Outlined.AccountCircle,
                                label = accLabel,
                                onClick = onOpenAccount,
                                modifier = Modifier.weight(1f).testTag("nav_account_button")
                            )
                        }
                    }
                }

                // Compact Birth & Prashna Form
                GoldBorderedCard {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 10.dp, vertical = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        SectionTitle(
                            title = tr(language, "जन्म विवरण", "Birth Details", "出生信息")
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            OutlinedTextField(
                                value = formState.name,
                                onValueChange = onNameChange,
                                label = {
                                    Text(
                                        tr(language, "नाम / शीर्षक", "Name", "姓名"),
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                },
                                singleLine = true,
                                modifier = Modifier
                                    .weight(1.65f)
                                    .testTag("input_name")
                            )
                            OutlinedTextField(
                                value = formState.prashnaNumberText,
                                onValueChange = onPrashnaNumberChange,
                                label = {
                                    Text(
                                        tr(language, "KP प्रश्न (1-249)", "KP (1-249)", "KP数"),
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                singleLine = true,
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("input_prashna_number")
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            OutlinedTextField(
                                value = formState.day,
                                onValueChange = onDayChange,
                                label = { Text(tr(language, "दिन", "Day", "日")) },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                singleLine = true,
                                modifier = Modifier.weight(1f).testTag("input_day")
                            )
                            OutlinedTextField(
                                value = formState.month,
                                onValueChange = onMonthChange,
                                label = { Text(tr(language, "माह", "Month", "月")) },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                singleLine = true,
                                modifier = Modifier.weight(1f).testTag("input_month")
                            )
                            OutlinedTextField(
                                value = formState.year,
                                onValueChange = onYearChange,
                                label = { Text(tr(language, "वर्ष", "Year", "年")) },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                singleLine = true,
                                modifier = Modifier.weight(1.3f).testTag("input_year")
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            OutlinedTextField(
                                value = formState.hour,
                                onValueChange = onHourChange,
                                label = { Text(tr(language, "घंटा", "Hour", "时")) },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                singleLine = true,
                                modifier = Modifier.weight(1f).testTag("input_hour")
                            )
                            OutlinedTextField(
                                value = formState.minute,
                                onValueChange = onMinuteChange,
                                label = { Text(tr(language, "मिनट", "Min", "分")) },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                singleLine = true,
                                modifier = Modifier.weight(1f).testTag("input_minute")
                            )
                            OutlinedTextField(
                                value = formState.customUtcOffsetText,
                                onValueChange = onCustomOffsetChange,
                                label = { Text(tr(language, "UTC±", "UTC±", "UTC±")) },
                                placeholder = { Text("${formState.selectedPlace.baseUtcOffsetHours}") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                singleLine = true,
                                modifier = Modifier.weight(1f).testTag("input_utc_offset")
                            )
                        }

                        // Slim Place selector button
                        OutlinedButton(
                            onClick = { showPlacePicker = true },
                            shape = RoundedCornerShape(10.dp),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(38.dp)
                                .testTag("place_picker_button")
                        ) {
                            Icon(Icons.Outlined.LocationOn, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = AstroStrings.placeName(formState.selectedPlace, language),
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.weight(1f)
                            )
                            Text(
                                text = "%.2f°,%.2f° (%+.1f)".format(
                                    formState.selectedPlace.latitude,
                                    formState.selectedPlace.longitude,
                                    formState.selectedPlace.baseUtcOffsetHours
                                ),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1
                            )
                        }

                        if (formState.validationError != null) {
                            Text(
                                text = formState.validationError,
                                color = MaterialTheme.colorScheme.primary,
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Medium
                            )
                        }

                        // Slim primary Generate Kundali (+ View Active Kundali side-by-side when ready)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = onCalculateClick,
                                shape = RoundedCornerShape(10.dp),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 0.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = DarkBrandBlue,
                                    contentColor = Color.White
                                ),
                                modifier = Modifier
                                    .weight(1.25f)
                                    .height(40.dp)
                                    .testTag("generate_kundali_button")
                            ) {
                                Icon(Icons.Outlined.AutoAwesome, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = tr(language, "कुंडली बनाएँ", "Generate Kundali", "生成星盘"),
                                    style = MaterialTheme.typography.labelLarge,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }

                            if (hasCalculatedChart) {
                                OutlinedButton(
                                    onClick = onOpenExistingChart,
                                    shape = RoundedCornerShape(10.dp),
                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp),
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(40.dp)
                                        .testTag("open_kundali_button")
                                ) {
                                    Text(
                                        text = tr(language, "कुंडली देखें →", "View Chart →", "查看星盘 →"),
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.SemiBold,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showPlacePicker) {
        val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        var showCustomForm by remember { mutableStateOf(false) }
        var customName by remember { mutableStateOf("") }
        var customLat by remember { mutableStateOf("28.6139") }
        var customLon by remember { mutableStateOf("77.2090") }
        var customUtc by remember { mutableStateOf("5.5") }
        var customError by remember { mutableStateOf<String?>(null) }

        val filtered = remember(formState.placeSearchQuery, showCustomForm) {
            PlacesRepository.search(formState.placeSearchQuery)
        }
        val totalCount = PlacesRepository.allPlaces.size

        ModalBottomSheet(
            onDismissRequest = { showPlacePicker = false },
            sheetState = sheetState
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .padding(bottom = 24.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = tr(
                            language,
                            "जन्म स्थान चुनें ($totalCount स्थान)",
                            "Select Birth Place ($totalCount)",
                            "选择出生地点 ($totalCount)"
                        ),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    AssistChip(
                        onClick = { showCustomForm = !showCustomForm },
                        leadingIcon = {
                            Icon(Icons.Outlined.AddLocationAlt, contentDescription = null, modifier = Modifier.size(18.dp))
                        },
                        label = {
                            Text(
                                if (showCustomForm) tr(language, "सूची देखें", "Show List", "查看列表")
                                else tr(language, "+ नया स्थान", "+ Custom", "+ 自定义")
                            )
                        },
                        modifier = Modifier.testTag("toggle_custom_place_button")
                    )
                }

                if (showCustomForm) {
                    GoldBorderedCard {
                        Column(
                            modifier = Modifier.fillMaxWidth().padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedTextField(
                                value = customName,
                                onValueChange = { customName = it; customError = null },
                                label = { Text(tr(language, "स्थान का नाम", "Place Name", "地点名称")) },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth().testTag("custom_place_name")
                            )
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                OutlinedTextField(
                                    value = customLat,
                                    onValueChange = { customLat = it; customError = null },
                                    label = { Text("Lat") },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                    singleLine = true,
                                    modifier = Modifier.weight(1f).testTag("custom_place_lat")
                                )
                                OutlinedTextField(
                                    value = customLon,
                                    onValueChange = { customLon = it; customError = null },
                                    label = { Text("Lon") },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                    singleLine = true,
                                    modifier = Modifier.weight(1f).testTag("custom_place_lon")
                                )
                                OutlinedTextField(
                                    value = customUtc,
                                    onValueChange = { customUtc = it; customError = null },
                                    label = { Text("UTC") },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                    singleLine = true,
                                    modifier = Modifier.weight(0.8f).testTag("custom_place_utc")
                                )
                            }
                            if (customError != null) {
                                Text(
                                    text = customError!!,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                            Button(
                                onClick = {
                                    val lat = customLat.trim().toDoubleOrNull()
                                    val lon = customLon.trim().toDoubleOrNull()
                                    val utc = customUtc.trim().toDoubleOrNull()
                                    if (customName.isBlank() || lat == null || lat !in -66.0..66.0 ||
                                        lon == null || lon !in -180.0..180.0 || utc == null || utc !in -12.0..14.0
                                    ) {
                                        customError = tr(
                                            language,
                                            "कृपया मान्य नाम, अक्षांश (-66..66), देशांतर (-180..180) और UTC भरें।",
                                            "Enter valid name, Lat (-66..66), Lon (-180..180), and UTC.",
                                            "请输入有效的名称、纬度、经度和UTC时差。"
                                        )
                                    } else {
                                        onAddCustomPlace(customName.trim(), lat, lon, utc)
                                        showCustomForm = false
                                        showPlacePicker = false
                                    }
                                },
                                modifier = Modifier.fillMaxWidth().height(48.dp).testTag("save_custom_place_button")
                            ) {
                                Text(tr(language, "स्थान जोड़ें और चुनें", "Add & Select Place", "添加并选择地点"))
                            }
                        }
                    }
                } else {
                    OutlinedTextField(
                        value = formState.placeSearchQuery,
                        onValueChange = onPlaceQueryChange,
                        leadingIcon = { Icon(Icons.Outlined.Search, contentDescription = null) },
                        placeholder = {
                            Text(
                                tr(
                                    language,
                                    "शहर / जिला खोजें (Agra, Varanasi, Adilabad...)",
                                    "Search district or city...",
                                    "搜索城市或地区..."
                                )
                            )
                        },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().testTag("place_search_input")
                    )
                    LazyColumn(
                        modifier = Modifier.fillMaxWidth().height(360.dp),
                        contentPadding = PaddingValues(vertical = 4.dp)
                    ) {
                        items(filtered, key = { it.id }) { place ->
                            ListItem(
                                headlineContent = {
                                    Text(
                                        text = AstroStrings.placeName(place, language),
                                        fontWeight = if (place.id == formState.selectedPlace.id) FontWeight.Bold else FontWeight.Medium
                                    )
                                },
                                supportingContent = {
                                    Text("Lat %.4f°, Lon %.4f° • UTC%+.1f".format(place.latitude, place.longitude, place.baseUtcOffsetHours))
                                },
                                leadingContent = {
                                    Icon(Icons.Outlined.LocationOn, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                                },
                                modifier = Modifier
                                    .clickable {
                                        onPlaceSelected(place)
                                        showPlacePicker = false
                                    }
                                    .testTag("place_item_${place.id}")
                            )
                            HorizontalDivider()
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SlimPresetChipButton(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isLoading: Boolean = false
) {
    OutlinedButton(
        onClick = onClick,
        shape = RoundedCornerShape(8.dp),
        contentPadding = PaddingValues(horizontal = 6.dp, vertical = 0.dp),
        modifier = modifier.height(30.dp)
    ) {
        if (isLoading) {
            CircularProgressIndicator(modifier = Modifier.size(13.dp), strokeWidth = 2.dp)
        } else {
            Icon(icon, contentDescription = null, modifier = Modifier.size(14.dp))
        }
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = label,
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun SlimHubButton(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    OutlinedButton(
        onClick = onClick,
        shape = RoundedCornerShape(8.dp),
        contentPadding = PaddingValues(horizontal = 5.dp, vertical = 0.dp),
        modifier = modifier.height(32.dp)
    ) {
        Icon(icon, contentDescription = null, modifier = Modifier.size(13.dp))
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = label,
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

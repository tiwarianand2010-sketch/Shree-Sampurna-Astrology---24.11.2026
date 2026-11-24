package com.example

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.firebase.authStateFlow
import com.example.firebase.configureAppCheck
import com.example.ui.KundaliUiState
import com.example.ui.MainViewModel
import com.example.ui.PdfExportMode
import com.example.ui.screens.AccountScreen
import com.example.ui.screens.AiAstrologerScreen
import com.example.ui.screens.CloudChartsScreen
import com.example.ui.screens.HelpScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.KundaliScreen
import com.example.ui.screens.MatchingScreen
import com.example.ui.screens.StoreScreen
import com.example.ui.theme.SampurnaAstroTheme
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.auth
import kotlinx.coroutines.flow.flowOf

object AppRoutes {
    const val HOME = "home"
    const val KUNDALI = "kundali"
    const val HELP = "help"
    const val MATCHING = "matching"
    const val AI_CHAT = "ai_chat"
    const val STORE = "store"
    const val CLOUD_CHARTS = "cloud_charts"
    const val ACCOUNT = "account"
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Configure Firebase App Check BEFORE initializing any Compose UI or Firebase ViewModels
        configureAppCheck(this, intent)
        enableEdgeToEdge()
        setContent {
            SampurnaAstroApp()
        }
    }
}

@Composable
fun SampurnaAstroApp(viewModel: MainViewModel = viewModel()) {
    val context = LocalContext.current
    val language by viewModel.language.collectAsStateWithLifecycle()
    val chartStyle by viewModel.chartStyle.collectAsStateWithLifecycle()
    val chartDivision by viewModel.chartDivision.collectAsStateWithLifecycle()
    val formState by viewModel.formState.collectAsStateWithLifecycle()
    val kundaliState by viewModel.kundaliState.collectAsStateWithLifecycle()
    val selectedTab by viewModel.selectedKundaliTab.collectAsStateWithLifecycle()

    val boyForm by viewModel.boyForm.collectAsStateWithLifecycle()
    val girlForm by viewModel.girlForm.collectAsStateWithLifecycle()
    val matchingResult by viewModel.matchingResult.collectAsStateWithLifecycle()
    val isMatchingCalculating by viewModel.isMatchingCalculating.collectAsStateWithLifecycle()

    val selectedStoreCategory by viewModel.selectedStoreCategory.collectAsStateWithLifecycle()
    val cartIds by viewModel.inquiryCartIds.collectAsStateWithLifecycle()
    val astrologerAccount by viewModel.astrologerAccount.collectAsStateWithLifecycle()
    val pdfExportState by viewModel.pdfExportState.collectAsStateWithLifecycle()

    val authFlow = remember {
        try {
            Firebase.auth.authStateFlow()
        } catch (_: Exception) {
            flowOf<FirebaseUser?>(null)
        }
    }
    val initialUser = remember {
        try {
            Firebase.auth.currentUser
        } catch (_: Exception) {
            null
        }
    }
    val currentUser by authFlow.collectAsStateWithLifecycle(initialValue = initialUser)

    var pendingSourcePdfUri by remember { mutableStateOf<Uri?>(null) }
    val createDocLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/pdf")
    ) { targetUri ->
        val src = pendingSourcePdfUri
        if (targetUri != null && src != null) {
            viewModel.saveCachedPdfToUri(src, targetUri) { ok, msg ->
                Toast.makeText(context, msg, if (ok) Toast.LENGTH_SHORT else Toast.LENGTH_LONG).show()
            }
        }
    }

    fun launchSavePdf(sourceUri: Uri, fileName: String) {
        pendingSourcePdfUri = sourceUri
        createDocLauncher.launch(fileName)
    }

    fun launchSharePdf(sourceUri: Uri, fileName: String) {
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "application/pdf"
            putExtra(Intent.EXTRA_STREAM, sourceUri)
            putExtra(Intent.EXTRA_SUBJECT, fileName)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(shareIntent, fileName))
    }

    val activeChart = (kundaliState as? KundaliUiState.Ready)?.result
    val navController = rememberNavController()

    SampurnaAstroTheme {
        NavHost(
            navController = navController,
            startDestination = AppRoutes.HOME
        ) {
            composable(AppRoutes.HOME) {
                HomeScreen(
                    language = language,
                    formState = formState,
                    hasCalculatedChart = kundaliState is KundaliUiState.Ready,
                    onLanguageSelected = viewModel::setLanguage,
                    onHelpClick = { navController.navigate(AppRoutes.HELP) },
                    onNameChange = viewModel::updateName,
                    onDayChange = viewModel::updateDay,
                    onMonthChange = viewModel::updateMonth,
                    onYearChange = viewModel::updateYear,
                    onHourChange = viewModel::updateHour,
                    onMinuteChange = viewModel::updateMinute,
                    onCustomOffsetChange = viewModel::updateCustomOffset,
                    onPrashnaNumberChange = viewModel::updatePrashnaNumber,
                    onPlaceQueryChange = viewModel::updatePlaceQuery,
                    onPlaceSelected = viewModel::selectPlace,
                    onAddCustomPlace = viewModel::addAndSelectCustomPlace,
                    onApplyCurrentPreset = viewModel::applyCurrentMomentPreset,
                    onApplyIndependencePreset = viewModel::applyIndependencePreset,
                    onApplyPrashnaGps = { gps, pNum ->
                        viewModel.applyPrashnaFromGps(gps, pNum) {
                            viewModel.setSelectedKundaliTab(6) // open KP tab directly on Prashna
                            navController.navigate(AppRoutes.KUNDALI)
                        }
                    },
                    onCalculateClick = {
                        viewModel.calculateKundali {
                            navController.navigate(AppRoutes.KUNDALI)
                        }
                    },
                    onOpenExistingChart = {
                        navController.navigate(AppRoutes.KUNDALI)
                    },
                    onOpenMatching = { navController.navigate(AppRoutes.MATCHING) },
                    onOpenAiChat = { navController.navigate(AppRoutes.AI_CHAT) },
                    onOpenCloudCharts = { navController.navigate(AppRoutes.CLOUD_CHARTS) },
                    onOpenStore = { navController.navigate(AppRoutes.STORE) },
                    onOpenBrihatPdf = {
                        viewModel.setSelectedKundaliTab(9)
                        navController.navigate(AppRoutes.KUNDALI)
                    },
                    astrologerAccount = astrologerAccount,
                    onOpenAccount = { navController.navigate(AppRoutes.ACCOUNT) }
                )
            }

            composable(AppRoutes.KUNDALI) {
                KundaliScreen(
                    language = language,
                    kundaliState = kundaliState,
                    chartStyle = chartStyle,
                    chartDivision = chartDivision,
                    selectedTab = selectedTab,
                    pdfExportState = pdfExportState,
                    astrologerAccount = astrologerAccount,
                    onSelectTab = viewModel::setSelectedKundaliTab,
                    onChartStyleChange = viewModel::setChartStyle,
                    onDivisionChange = viewModel::setChartDivision,
                    onLanguageSelected = viewModel::setLanguage,
                    onHelpClick = { navController.navigate(AppRoutes.HELP) },
                    onOpenAiChat = { navController.navigate(AppRoutes.AI_CHAT) },
                    onOpenCloudCharts = { navController.navigate(AppRoutes.CLOUD_CHARTS) },
                    onOpenAccount = { navController.navigate(AppRoutes.ACCOUNT) },
                    onGenerateSummaryPdf = { viewModel.generatePdfToCache(PdfExportMode.SUMMARY) },
                    onGenerateBrihat250Pdf = { viewModel.generatePdfToCache(PdfExportMode.BRIHAT_250) },
                    onSavePdfToDevice = ::launchSavePdf,
                    onSharePdf = ::launchSharePdf,
                    onBack = { navController.popBackStack() }
                )
            }

            composable(AppRoutes.MATCHING) {
                MatchingScreen(
                    language = language,
                    boyForm = boyForm,
                    girlForm = girlForm,
                    matchingResult = matchingResult,
                    isCalculating = isMatchingCalculating,
                    pdfExportState = pdfExportState,
                    onUpdateBoy = viewModel::updateBoyForm,
                    onUpdateGirl = viewModel::updateGirlForm,
                    onCopyMainToBoy = viewModel::copyMainFormToBoy,
                    onSwapForms = viewModel::swapMatchingForms,
                    onCalculateMatching = viewModel::calculateMatching,
                    onGenerateMatchingPdf = { viewModel.generatePdfToCache(PdfExportMode.MATCHING) },
                    onSavePdfToDevice = ::launchSavePdf,
                    onSharePdf = ::launchSharePdf,
                    onBack = { navController.popBackStack() }
                )
            }

            composable(AppRoutes.AI_CHAT) {
                AiAstrologerScreen(
                    result = activeChart,
                    language = language,
                    onBack = { navController.popBackStack() }
                )
            }

            composable(AppRoutes.STORE) {
                StoreScreen(
                    language = language,
                    activeChart = activeChart,
                    selectedCategory = selectedStoreCategory,
                    cartIds = cartIds,
                    onSelectCategory = viewModel::setStoreCategory,
                    onToggleCart = viewModel::toggleCartItem,
                    onBack = { navController.popBackStack() }
                )
            }

            composable(AppRoutes.CLOUD_CHARTS) {
                CloudChartsScreen(
                    language = language,
                    currentUser = currentUser,
                    activeChart = activeChart,
                    onLoadSavedKundali = { saved ->
                        viewModel.loadSavedKundali(saved) {
                            viewModel.setSelectedKundaliTab(0)
                            navController.navigate(AppRoutes.KUNDALI)
                        }
                    },
                    onBack = { navController.popBackStack() }
                )
            }

            composable(AppRoutes.ACCOUNT) {
                AccountScreen(
                    language = language,
                    currentUser = currentUser,
                    astrologerAccount = astrologerAccount,
                    onSaveMobileAccount = viewModel::saveMobileAccount,
                    onSaveGoogleProfileName = viewModel::saveGoogleAccountProfile,
                    onClearAccount = viewModel::clearAstrologerAccount,
                    onBack = { navController.popBackStack() }
                )
            }

            composable(AppRoutes.HELP) {
                HelpScreen(
                    language = language,
                    onBack = { navController.popBackStack() }
                )
            }
        }
    }
}

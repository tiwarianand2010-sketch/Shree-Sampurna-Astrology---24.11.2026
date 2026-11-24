package com.example.ui

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.astro.VedicCalculator
import com.example.astro.WesternNumerologyVastuEngine
import com.example.core.AccountMethod
import com.example.core.AppLanguage
import com.example.core.AstrologerAccount
import com.example.core.PreferencesRepository
import com.example.core.ResolvedGpsLocation
import com.example.core.tr
import com.example.data.PlacesRepository
import com.example.firebase.SavedKundali
import com.example.model.BirthInput
import com.example.model.ChartDivision
import com.example.model.ChartStyle
import com.example.model.KundaliResult
import com.example.model.MatchingResult
import com.example.model.Place
import com.example.model.StoreCategory
import com.example.pdf.PdfReportGenerator
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Calendar

data class BirthFormState(
    val name: String = "भारत स्वतंत्र कुंडली",
    val year: String = "1947",
    val month: String = "8",
    val day: String = "15",
    val hour: String = "0",
    val minute: String = "0",
    val selectedPlace: Place = PlacesRepository.defaultPlace,
    val customUtcOffsetText: String = "",
    val placeSearchQuery: String = "",
    val prashnaNumberText: String = "",
    val validationError: String? = null
)

sealed interface KundaliUiState {
    data object Idle : KundaliUiState
    data object Calculating : KundaliUiState
    data class Ready(val result: KundaliResult) : KundaliUiState
}

enum class PdfExportMode {
    SUMMARY,
    BRIHAT_250,
    MATCHING
}

sealed interface PdfExportState {
    data object Idle : PdfExportState
    data class Generating(val currentPage: Int, val totalPages: Int, val mode: PdfExportMode) : PdfExportState
    data class Ready(val uri: Uri, val fileName: String, val totalPages: Int, val mode: PdfExportMode) : PdfExportState
    data class Error(val message: String) : PdfExportState
}

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val prefs = PreferencesRepository(application.applicationContext)

    private val _language = MutableStateFlow(AppLanguage.HI)
    val language: StateFlow<AppLanguage> = _language.asStateFlow()

    private val _chartStyle = MutableStateFlow(ChartStyle.NORTH_INDIAN)
    val chartStyle: StateFlow<ChartStyle> = _chartStyle.asStateFlow()

    private var userChangedLanguage = false
    private var userChangedChartStyle = false

    private val _formState = MutableStateFlow(BirthFormState())
    val formState: StateFlow<BirthFormState> = _formState.asStateFlow()

    private val _kundaliState = MutableStateFlow<KundaliUiState>(KundaliUiState.Idle)
    val kundaliState: StateFlow<KundaliUiState> = _kundaliState.asStateFlow()

    private val _chartDivision = MutableStateFlow(ChartDivision.LAGNA_D1)
    val chartDivision: StateFlow<ChartDivision> = _chartDivision.asStateFlow()

    private val _selectedKundaliTab = MutableStateFlow(0)
    val selectedKundaliTab: StateFlow<Int> = _selectedKundaliTab.asStateFlow()

    // Kundali Matching (Bride & Groom)
    private val _boyForm = MutableStateFlow(
        BirthFormState(
            name = "वर (Groom)",
            year = "1995",
            month = "5",
            day = "14",
            hour = "9",
            minute = "30"
        )
    )
    val boyForm: StateFlow<BirthFormState> = _boyForm.asStateFlow()

    private val _girlForm = MutableStateFlow(
        BirthFormState(
            name = "वधू (Bride)",
            year = "1997",
            month = "11",
            day = "22",
            hour = "16",
            minute = "15"
        )
    )
    val girlForm: StateFlow<BirthFormState> = _girlForm.asStateFlow()

    private val _matchingResult = MutableStateFlow<MatchingResult?>(null)
    val matchingResult: StateFlow<MatchingResult?> = _matchingResult.asStateFlow()

    private val _isMatchingCalculating = MutableStateFlow(false)
    val isMatchingCalculating: StateFlow<Boolean> = _isMatchingCalculating.asStateFlow()

    // Remedial Store Catalog
    private val _selectedStoreCategory = MutableStateFlow<StoreCategory?>(null)
    val selectedStoreCategory: StateFlow<StoreCategory?> = _selectedStoreCategory.asStateFlow()

    private val _inquiryCartIds = MutableStateFlow<Set<String>>(emptySet())
    val inquiryCartIds: StateFlow<Set<String>> = _inquiryCartIds.asStateFlow()

    private val _astrologerAccount = MutableStateFlow(AstrologerAccount())
    val astrologerAccount: StateFlow<AstrologerAccount> = _astrologerAccount.asStateFlow()

    // PDF Export
    private val _pdfExportState = MutableStateFlow<PdfExportState>(PdfExportState.Idle)
    val pdfExportState: StateFlow<PdfExportState> = _pdfExportState.asStateFlow()

    init {
        viewModelScope.launch {
            prefs.languageFlow.collect { saved ->
                if (!userChangedLanguage) _language.value = saved
            }
        }
        viewModelScope.launch {
            prefs.chartStyleFlow.collect { saved ->
                if (!userChangedChartStyle) _chartStyle.value = saved
            }
        }
        viewModelScope.launch {
            prefs.astrologerAccountFlow.collect { saved ->
                _astrologerAccount.value = saved
            }
        }
        calculateKundali()
        calculateMatching()
    }

    fun saveMobileAccount(name: String, mobile: String, studio: String) {
        val updated = AstrologerAccount(
            astrologerName = name.trim(),
            mobileNumber = mobile.trim(),
            studioName = studio.trim().ifBlank { "Shri Sampurna Astrology" },
            accountMethod = AccountMethod.MOBILE
        )
        _astrologerAccount.value = updated
        viewModelScope.launch { prefs.saveAstrologerAccount(updated) }
    }

    fun saveGoogleAccountProfile(name: String, mobile: String, studio: String, email: String) {
        val updated = AstrologerAccount(
            astrologerName = name.trim(),
            mobileNumber = mobile.trim(),
            studioName = studio.trim().ifBlank { "Shri Sampurna Astrology" },
            email = email.trim(),
            accountMethod = AccountMethod.GOOGLE
        )
        _astrologerAccount.value = updated
        viewModelScope.launch { prefs.saveAstrologerAccount(updated) }
    }

    fun clearAstrologerAccount() {
        _astrologerAccount.value = AstrologerAccount()
        viewModelScope.launch { prefs.clearAstrologerAccount() }
    }

    fun setLanguage(lang: AppLanguage) {
        userChangedLanguage = true
        _language.value = lang
        viewModelScope.launch { prefs.setLanguage(lang) }
    }

    fun setChartStyle(style: ChartStyle) {
        userChangedChartStyle = true
        _chartStyle.value = style
        viewModelScope.launch { prefs.setChartStyle(style) }
    }

    fun setChartDivision(division: ChartDivision) {
        _chartDivision.value = division
    }

    fun setSelectedKundaliTab(index: Int) {
        _selectedKundaliTab.value = index
    }

    fun updateName(v: String) = _formState.update { it.copy(name = v, validationError = null) }
    fun updateYear(v: String) = _formState.update { it.copy(year = v.filter { c -> c.isDigit() }.take(4), validationError = null) }
    fun updateMonth(v: String) = _formState.update { it.copy(month = v.filter { c -> c.isDigit() }.take(2), validationError = null) }
    fun updateDay(v: String) = _formState.update { it.copy(day = v.filter { c -> c.isDigit() }.take(2), validationError = null) }
    fun updateHour(v: String) = _formState.update { it.copy(hour = v.filter { c -> c.isDigit() }.take(2), validationError = null) }
    fun updateMinute(v: String) = _formState.update { it.copy(minute = v.filter { c -> c.isDigit() }.take(2), validationError = null) }
    fun updateCustomOffset(v: String) = _formState.update { it.copy(customUtcOffsetText = v, validationError = null) }
    fun updatePlaceQuery(v: String) = _formState.update { it.copy(placeSearchQuery = v) }
    fun updatePrashnaNumber(v: String) = _formState.update { it.copy(prashnaNumberText = v.filter { c -> c.isDigit() }.take(3), validationError = null) }

    fun selectPlace(place: Place) {
        _formState.update { it.copy(selectedPlace = place, placeSearchQuery = "") }
    }

    fun addAndSelectCustomPlace(name: String, latitude: Double, longitude: Double, utcOffsetHours: Double) {
        val added = PlacesRepository.addCustomPlace(name, latitude, longitude, utcOffsetHours)
        _formState.update { it.copy(selectedPlace = added, placeSearchQuery = "") }
    }

    fun applyCurrentMomentPreset() {
        val cal = Calendar.getInstance()
        val lang = language.value
        val presetName = tr(lang, "वर्तमान गोचर कुंडली", "Current Moment Chart", "当前天象盘")
        _formState.update {
            it.copy(
                name = presetName,
                year = cal.get(Calendar.YEAR).toString(),
                month = (cal.get(Calendar.MONTH) + 1).toString(),
                day = cal.get(Calendar.DAY_OF_MONTH).toString(),
                hour = cal.get(Calendar.HOUR_OF_DAY).toString(),
                minute = cal.get(Calendar.MINUTE).toString(),
                customUtcOffsetText = "",
                validationError = null
            )
        }
    }

    fun applyIndependencePreset() {
        val lang = language.value
        val presetName = tr(lang, "भारत स्वतंत्र कुंडली", "India Independence Chart", "印度独立星盘")
        _formState.update {
            it.copy(
                name = presetName,
                year = "1947",
                month = "8",
                day = "15",
                hour = "0",
                minute = "0",
                selectedPlace = PlacesRepository.defaultPlace,
                customUtcOffsetText = "",
                prashnaNumberText = "",
                validationError = null
            )
        }
    }

    fun applyPrashnaFromGps(gps: ResolvedGpsLocation?, prashnaNum: Int, onDone: () -> Unit) {
        val cal = Calendar.getInstance()
        val place = if (gps != null) {
            PlacesRepository.addCustomPlace(
                name = "GPS (${"%.3f".format(gps.latitude)}, ${"%.3f".format(gps.longitude)})",
                latitude = gps.latitude,
                longitude = gps.longitude,
                utcOffsetHours = gps.utcOffsetHours
            )
        } else {
            _formState.value.selectedPlace
        }
        val title = tr(language.value, "प्रश्न कुंडली #$prashnaNum", "Prashna Chart #$prashnaNum", "占卜盘 #$prashnaNum")
        _formState.update {
            it.copy(
                name = title,
                year = cal.get(Calendar.YEAR).toString(),
                month = (cal.get(Calendar.MONTH) + 1).toString(),
                day = cal.get(Calendar.DAY_OF_MONTH).toString(),
                hour = cal.get(Calendar.HOUR_OF_DAY).toString(),
                minute = cal.get(Calendar.MINUTE).toString(),
                selectedPlace = place,
                customUtcOffsetText = "",
                prashnaNumberText = prashnaNum.toString(),
                validationError = null
            )
        }
        calculateKundali(onDone)
    }

    fun loadSavedKundali(saved: SavedKundali, onLoaded: () -> Unit) {
        val input = saved.toBirthInput()
        _formState.update {
            it.copy(
                name = input.name,
                year = input.year.toString(),
                month = input.month.toString(),
                day = input.day.toString(),
                hour = input.hour.toString(),
                minute = input.minute.toString(),
                selectedPlace = input.place,
                customUtcOffsetText = input.customUtcOffsetHours?.toString().orEmpty(),
                prashnaNumberText = "",
                validationError = null
            )
        }
        calculateKundali(onLoaded)
    }

    fun calculateKundali(onSuccess: (() -> Unit)? = null) {
        val form = _formState.value
        val y = form.year.toIntOrNull()
        val m = form.month.toIntOrNull()
        val d = form.day.toIntOrNull()
        val h = form.hour.toIntOrNull()
        val min = form.minute.toIntOrNull()
        val customOffset = form.customUtcOffsetText.trim().takeIf { it.isNotEmpty() }?.toDoubleOrNull()
        val prashnaNum = form.prashnaNumberText.trim().takeIf { it.isNotEmpty() }?.toIntOrNull()

        if (y == null || y !in 1800..2100 ||
            m == null || m !in 1..12 ||
            d == null || d !in 1..31 ||
            h == null || h !in 0..23 ||
            min == null || min !in 0..59 ||
            (form.customUtcOffsetText.isNotBlank() && (customOffset == null || customOffset !in -12.0..14.0)) ||
            (form.prashnaNumberText.isNotBlank() && (prashnaNum == null || prashnaNum !in 1..249))
        ) {
            val err = tr(
                language.value,
                "कृपया मान्य दिनांक (1800-2100), समय, UTC अंतर और प्रश्न संख्या (1-249) दर्ज करें।",
                "Enter valid date (1800–2100), time, UTC offset, and Prashna number (1–249).",
                "请输入有效的日期（1800–2100）、时间、UTC时差和占卜数（1–249）。"
            )
            _formState.update { it.copy(validationError = err) }
            return
        }

        val input = BirthInput(
            name = form.name.ifBlank { "Kundali" },
            year = y,
            month = m,
            day = d,
            hour = h,
            minute = min,
            place = form.selectedPlace,
            customUtcOffsetHours = customOffset
        )

        _kundaliState.value = KundaliUiState.Calculating
        viewModelScope.launch {
            val result = withContext(Dispatchers.Default) {
                VedicCalculator.calculateKundali(input, prashnaNumber = prashnaNum)
            }
            _kundaliState.value = KundaliUiState.Ready(result)
            onSuccess?.invoke()
        }
    }

    // --- Matching Operations ---
    fun updateBoyForm(transform: (BirthFormState) -> BirthFormState) {
        _boyForm.update { transform(it).copy(validationError = null) }
    }

    fun updateGirlForm(transform: (BirthFormState) -> BirthFormState) {
        _girlForm.update { transform(it).copy(validationError = null) }
    }

    fun copyMainFormToBoy() {
        _boyForm.value = _formState.value.copy(validationError = null)
        calculateMatching()
    }

    fun copyMainFormToGirl() {
        _girlForm.value = _formState.value.copy(validationError = null)
        calculateMatching()
    }

    fun swapMatchingForms() {
        val oldBoy = _boyForm.value
        _boyForm.value = _girlForm.value
        _girlForm.value = oldBoy
        calculateMatching()
    }

    private fun parseBirthInputOrNull(form: BirthFormState): BirthInput? {
        val y = form.year.toIntOrNull() ?: return null
        val m = form.month.toIntOrNull() ?: return null
        val d = form.day.toIntOrNull() ?: return null
        val h = form.hour.toIntOrNull() ?: return null
        val min = form.minute.toIntOrNull() ?: return null
        if (y !in 1800..2100 || m !in 1..12 || d !in 1..31 || h !in 0..23 || min !in 0..59) return null
        val customOffset = form.customUtcOffsetText.trim().takeIf { it.isNotEmpty() }?.toDoubleOrNull()
        return BirthInput(
            name = form.name.ifBlank { "Partner" },
            year = y,
            month = m,
            day = d,
            hour = h,
            minute = min,
            place = form.selectedPlace,
            customUtcOffsetHours = customOffset
        )
    }

    fun calculateMatching() {
        val boyIn = parseBirthInputOrNull(_boyForm.value)
        val girlIn = parseBirthInputOrNull(_girlForm.value)
        if (boyIn == null) {
            _boyForm.update { it.copy(validationError = tr(language.value, "मान्य जन्म विवरण भरें", "Enter valid birth details")) }
            return
        }
        if (girlIn == null) {
            _girlForm.update { it.copy(validationError = tr(language.value, "मान्य जन्म विवरण भरें", "Enter valid birth details")) }
            return
        }
        _isMatchingCalculating.value = true
        viewModelScope.launch {
            val res = withContext(Dispatchers.Default) {
                val bChart = VedicCalculator.calculateKundali(boyIn)
                val gChart = VedicCalculator.calculateKundali(girlIn)
                WesternNumerologyVastuEngine.calculateMatching(bChart, gChart)
            }
            _matchingResult.value = res
            _isMatchingCalculating.value = false
        }
    }

    // --- Store Catalog ---
    fun setStoreCategory(cat: StoreCategory?) {
        _selectedStoreCategory.value = cat
    }

    fun toggleCartItem(itemId: String) {
        _inquiryCartIds.update { set ->
            if (itemId in set) set - itemId else set + itemId
        }
    }

    // --- PDF Reports ---
    fun resetPdfState() {
        _pdfExportState.value = PdfExportState.Idle
    }

    fun generatePdfToCache(mode: PdfExportMode) {
        val ctx = getApplication<Application>().applicationContext
        val lang = _language.value
        val activeChart = (_kundaliState.value as? KundaliUiState.Ready)?.result
        val matchRes = _matchingResult.value

        if (mode != PdfExportMode.MATCHING && activeChart == null) return
        if (mode == PdfExportMode.MATCHING && matchRes == null) return

        val safeName = (activeChart?.input?.name ?: "Matching")
            .replace(Regex("[^a-zA-Z0-9_-]"), "_")
            .ifBlank { "Kundali" }
        val fileName = when (mode) {
            PdfExportMode.SUMMARY -> "${safeName}_Kundali_Summary.pdf"
            PdfExportMode.BRIHAT_250 -> "${safeName}_Brihat_Kundali_250p.pdf"
            PdfExportMode.MATCHING -> "Kundali_Milan_Ashtakoot.pdf"
        }
        val total = when (mode) {
            PdfExportMode.SUMMARY -> 18
            PdfExportMode.BRIHAT_250 -> 250
            PdfExportMode.MATCHING -> 4
        }
        val astroAccount = _astrologerAccount.value

        _pdfExportState.value = PdfExportState.Generating(0, total, mode)
        viewModelScope.launch {
            try {
                val file = PdfReportGenerator.createCacheReportFile(ctx, fileName)
                file.outputStream().use { out ->
                    when (mode) {
                        PdfExportMode.SUMMARY -> PdfReportGenerator.generateSummaryPdf(activeChart!!, lang, out, astroAccount) { cur, tot ->
                            _pdfExportState.value = PdfExportState.Generating(cur, tot, mode)
                        }
                        PdfExportMode.BRIHAT_250 -> PdfReportGenerator.generateBrihat250Pdf(activeChart!!, lang, out, astroAccount) { cur, tot ->
                            _pdfExportState.value = PdfExportState.Generating(cur, tot, mode)
                        }
                        PdfExportMode.MATCHING -> PdfReportGenerator.generateMatchingPdf(matchRes!!, lang, out, astroAccount) { cur, tot ->
                            _pdfExportState.value = PdfExportState.Generating(cur, tot, mode)
                        }
                    }
                }
                val uri = PdfReportGenerator.getFileProviderUri(ctx, file)
                _pdfExportState.value = PdfExportState.Ready(uri, fileName, total, mode)
            } catch (e: Exception) {
                _pdfExportState.value = PdfExportState.Error(e.localizedMessage ?: "PDF generation failed.")
            }
        }
    }

    fun saveCachedPdfToUri(sourceUri: Uri, targetSafUri: Uri, onComplete: (Boolean, String) -> Unit) {
        val ctx = getApplication<Application>().applicationContext
        viewModelScope.launch(Dispatchers.IO) {
            val result = try {
                ctx.contentResolver.openInputStream(sourceUri)?.use { input ->
                    ctx.contentResolver.openOutputStream(targetSafUri)?.use { output ->
                        input.copyTo(output)
                        output.flush()
                    }
                }
                true to "PDF saved to device Downloads/Documents."
            } catch (e: Exception) {
                false to (e.localizedMessage ?: "Failed to save PDF file.")
            }
            withContext(Dispatchers.Main) {
                onComplete(result.first, result.second)
            }
        }
    }
}

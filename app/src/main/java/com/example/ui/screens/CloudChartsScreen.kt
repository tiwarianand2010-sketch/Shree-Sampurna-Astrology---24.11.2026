package com.example.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.outlined.Login
import androidx.compose.material.icons.automirrored.outlined.Logout
import androidx.compose.material.icons.outlined.CloudUpload
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.FolderOpen
import androidx.compose.material.icons.outlined.Login
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.credentials.CredentialManager
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.core.AppLanguage
import com.example.core.tr
import com.example.firebase.AuthManager
import com.example.firebase.CloudChartsRepository
import com.example.firebase.CloudChartsViewModel
import com.example.firebase.SavedKundali
import com.example.model.KundaliResult
import com.example.ui.components.GoldBorderedCard
import com.example.ui.components.SectionTitle
import com.example.ui.theme.AuspiciousGreen
import com.example.ui.theme.AuspiciousGreenBg
import com.example.ui.theme.CautionAmber
import com.example.ui.theme.CautionAmberBg
import com.example.ui.theme.DarkBrandBlue
import com.google.firebase.auth.FirebaseUser

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CloudChartsScreen(
    language: AppLanguage,
    currentUser: FirebaseUser?,
    activeChart: KundaliResult?,
    onLoadSavedKundali: (SavedKundali) -> Unit,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val credentialManager = remember(context) { CredentialManager.create(context) }
    var authError by remember { mutableStateOf<String?>(null) }
    var isSigningIn by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        if (currentUser == null) {
            AuthManager.attemptAutoSignIn(
                context = context,
                credentialManager = credentialManager,
                onAuthSuccess = { authError = null },
                onUnauthenticated = {},
                scope = scope
            )
        }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface),
                navigationIcon = {
                    IconButton(onClick = onBack, modifier = Modifier.minimumInteractiveComponentSize().testTag("cloud_back_button")) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                title = {
                    Text(
                        text = tr(language, "क्लाउड कुंडली संग्रह", "Cloud Kundali Vault", "云端星盘档案库"),
                        color = DarkBrandBlue,
                        fontWeight = FontWeight.Bold
                    )
                },
                actions = {
                    if (currentUser != null) {
                        IconButton(
                            onClick = {
                                AuthManager.signOut(credentialManager, scope)
                            },
                            modifier = Modifier.minimumInteractiveComponentSize().testTag("cloud_sign_out_button")
                        ) {
                            Icon(Icons.AutoMirrored.Outlined.Logout, contentDescription = "Sign Out", tint = MaterialTheme.colorScheme.primary)
                        }
                    }
                }
            )
        }
    ) { innerPadding ->
        if (currentUser == null) {
            // Unauthenticated state — NO Firestore listeners or ViewModels mounted!
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                GoldBorderedCard(modifier = Modifier.widthIn(max = 520.dp)) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = tr(
                                language,
                                "हज़ारों कुंडलियाँ क्लाउड में सहेजें",
                                "Save Thousands of Kundalis in Cloud",
                                "云端安全保存数千份星盘"
                            ),
                            style = MaterialTheme.typography.titleLarge,
                            color = DarkBrandBlue,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = tr(
                                language,
                                "अपनी जन्म व प्रश्न कुंडलियों को सुरक्षित क्लाउड सिंक के लिए Google से साइन इन करें।",
                                "Sign in with Google to securely store, search, and access up to thousands of saved birth charts across sessions.",
                                "请使用 Google 账号登录，以在云端安全存储和检索大量星盘档案。"
                            ),
                            style = MaterialTheme.typography.bodyMedium
                        )
                        if (authError != null) {
                            Surface(color = CautionAmberBg, shape = RoundedCornerShape(10.dp), modifier = Modifier.fillMaxWidth()) {
                                Text(authError!!, color = CautionAmber, style = MaterialTheme.typography.bodySmall, modifier = Modifier.padding(10.dp))
                            }
                        }
                        Button(
                            onClick = {
                                isSigningIn = true
                                authError = null
                                AuthManager.onGoogleSignInClicked(
                                    context = context,
                                    credentialManager = credentialManager,
                                    onAuthSuccess = {
                                        isSigningIn = false
                                        authError = null
                                    },
                                    onAuthError = { err ->
                                        isSigningIn = false
                                        authError = err
                                    },
                                    scope = scope,
                                    onAuthCancelled = {
                                        isSigningIn = false
                                    }
                                )
                            },
                            enabled = !isSigningIn,
                            colors = ButtonDefaults.buttonColors(containerColor = DarkBrandBlue, contentColor = Color.White),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth().height(50.dp).testTag("google_sign_in_button")
                        ) {
                            if (isSigningIn) {
                                CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color.White, strokeWidth = 2.dp)
                            } else {
                                Icon(Icons.AutoMirrored.Outlined.Login, contentDescription = null)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(tr(language, "Google से साइन इन करें", "Sign in with Google", "使用 Google 登录"), fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        } else {
            val uid = currentUser.uid
            key(uid) {
                val repo = remember(context, uid) { CloudChartsRepository(context) }
                val cloudViewModel: CloudChartsViewModel = viewModel(
                    key = uid,
                    factory = CloudChartsViewModel.Factory(uid, repo)
                )
                CloudVaultBody(
                    language = language,
                    userEmail = currentUser.email ?: currentUser.displayName ?: "Signed In",
                    activeChart = activeChart,
                    cloudViewModel = cloudViewModel,
                    onLoadSavedKundali = onLoadSavedKundali,
                    modifier = Modifier.fillMaxSize().padding(innerPadding)
                )
            }
        }
    }
}

@Composable
private fun CloudVaultBody(
    language: AppLanguage,
    userEmail: String,
    activeChart: KundaliResult?,
    cloudViewModel: CloudChartsViewModel,
    onLoadSavedKundali: (SavedKundali) -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by cloudViewModel.uiState.collectAsStateWithLifecycle()
    var notesInput by remember { mutableStateOf("") }
    var editingItem by remember { mutableStateOf<SavedKundali?>(null) }
    var editName by remember { mutableStateOf("") }
    var editNotes by remember { mutableStateOf("") }

    LazyColumn(
        modifier = modifier,
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        item {
            Column(modifier = Modifier.widthIn(max = 680.dp).fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "${tr(language, "खाता", "Account", "账号")}: $userEmail • ${uiState.allCharts.size} ${tr(language, "सहेजी गई कुंडलियाँ", "Saved Charts", "已存星盘")}",
                    style = MaterialTheme.typography.labelLarge,
                    color = DarkBrandBlue
                )

                if (uiState.statusMessage != null) {
                    Surface(color = AuspiciousGreenBg, shape = RoundedCornerShape(10.dp), modifier = Modifier.fillMaxWidth()) {
                        Text(uiState.statusMessage!!, color = AuspiciousGreen, modifier = Modifier.padding(10.dp), style = MaterialTheme.typography.bodySmall)
                    }
                }
                if (uiState.errorMessage != null) {
                    Surface(color = CautionAmberBg, shape = RoundedCornerShape(10.dp), modifier = Modifier.fillMaxWidth()) {
                        Text(uiState.errorMessage!!, color = CautionAmber, modifier = Modifier.padding(10.dp), style = MaterialTheme.typography.bodySmall)
                    }
                }

                if (activeChart != null) {
                    GoldBorderedCard {
                        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            SectionTitle(title = "${tr(language, "वर्तमान कुंडली सहेजें", "Save Active Chart", "保存当前星盘")}: ${activeChart.input.name}")
                            OutlinedTextField(
                                value = notesInput,
                                onValueChange = { notesInput = it },
                                label = { Text(tr(language, "टिप्पणी / नोट्स (वैकल्पिक)", "Notes (optional)", "备注（可选）")) },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth()
                            )
                            Button(
                                onClick = {
                                    cloudViewModel.saveCurrentChart(activeChart, notesInput)
                                    notesInput = ""
                                },
                                enabled = !uiState.isSaving,
                                colors = ButtonDefaults.buttonColors(containerColor = DarkBrandBlue, contentColor = Color.White),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.fillMaxWidth().height(48.dp).testTag("cloud_save_active_button")
                            ) {
                                Icon(Icons.Outlined.CloudUpload, contentDescription = null)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(tr(language, "क्लाउड में सुरक्षित सहेजें", "Save Chart to Cloud", "保存到云端"), fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }

                OutlinedTextField(
                    value = uiState.searchQuery,
                    onValueChange = cloudViewModel::updateSearchQuery,
                    leadingIcon = { Icon(Icons.Outlined.Search, contentDescription = null) },
                    placeholder = { Text(tr(language, "नाम, स्थान, राशि या नोट्स से खोजें...", "Search saved Kundalis...", "搜索已存星盘...")) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("cloud_search_input")
                )
            }
        }

        if (uiState.isLoading) {
            item { CircularProgressIndicator() }
        } else if (uiState.filteredCharts.isEmpty()) {
            item {
                Text(
                    text = tr(language, "कोई सहेजी गई कुंडली नहीं मिली। ऊपर बटन दबाकर पहली कुंडली सहेजें।", "No saved Kundalis found. Save your first chart above.", "暂无云端星盘。"),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            items(uiState.filteredCharts, key = { it.id }) { item ->
                GoldBorderedCard(modifier = Modifier.widthIn(max = 680.dp)) {
                    Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(
                            text = item.chartName,
                            style = MaterialTheme.typography.titleMedium,
                            color = DarkBrandBlue,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "${"%04d-%02d-%02d".format(item.birthYear, item.birthMonth, item.birthDay)} " +
                                "${"%02d:%02d".format(item.birthHour, item.birthMinute)} • ${item.placeName} • Lagna: ${item.lagnaRashi} • Moon: ${item.moonRashi}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        if (item.notes.isNotBlank()) {
                            Text(text = item.notes, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                            OutlinedButton(
                                onClick = { onLoadSavedKundali(item) },
                                modifier = Modifier.weight(1f).height(44.dp)
                            ) {
                                Icon(Icons.Outlined.FolderOpen, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(tr(language, "खोलें", "Open", "打开"))
                            }
                            IconButton(
                                onClick = {
                                    editingItem = item
                                    editName = item.chartName
                                    editNotes = item.notes
                                },
                                modifier = Modifier.minimumInteractiveComponentSize()
                            ) {
                                Icon(Icons.Outlined.Edit, contentDescription = "Edit")
                            }
                            IconButton(
                                onClick = { cloudViewModel.deleteChart(item.id) },
                                modifier = Modifier.minimumInteractiveComponentSize()
                            ) {
                                Icon(Icons.Outlined.DeleteOutline, contentDescription = "Delete", tint = MaterialTheme.colorScheme.primary)
                            }
                        }
                    }
                }
            }
        }
    }

    val toEdit = editingItem
    if (toEdit != null) {
        AlertDialog(
            onDismissRequest = { editingItem = null },
            title = { Text(tr(language, "कुंडली संपादित करें", "Edit Chart Notes", "编辑备注")) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(value = editName, onValueChange = { editName = it }, label = { Text("Name") }, singleLine = true)
                    OutlinedTextField(value = editNotes, onValueChange = { editNotes = it }, label = { Text("Notes") })
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    cloudViewModel.updateChartNotes(toEdit.id, editName, editNotes)
                    editingItem = null
                }) { Text(tr(language, "सहेजें", "Save", "保存")) }
            },
            dismissButton = {
                TextButton(onClick = { editingItem = null }) { Text(tr(language, "रद्द करें", "Cancel", "取消")) }
            }
        )
    }
}

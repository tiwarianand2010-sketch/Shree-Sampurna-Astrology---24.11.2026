package com.example.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.outlined.Login
import androidx.compose.material.icons.automirrored.outlined.Logout
import androidx.compose.material.icons.outlined.AccountCircle
import androidx.compose.material.icons.outlined.Badge
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.PhoneAndroid
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
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.credentials.CredentialManager
import com.example.R
import com.example.core.AccountMethod
import com.example.core.AppLanguage
import com.example.core.AstrologerAccount
import com.example.core.tr
import com.example.firebase.AuthManager
import com.example.ui.components.GoldBorderedCard
import com.example.ui.components.SectionTitle
import com.example.ui.theme.AuspiciousGreen
import com.example.ui.theme.AuspiciousGreenBg
import com.example.ui.theme.CautionAmber
import com.example.ui.theme.CautionAmberBg
import com.example.ui.theme.DarkBrandBlue
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AccountScreen(
    language: AppLanguage,
    currentUser: FirebaseUser?,
    astrologerAccount: AstrologerAccount,
    onSaveMobileAccount: (name: String, mobile: String, studio: String) -> Unit,
    onSaveGoogleProfileName: (name: String, mobile: String, studio: String, email: String) -> Unit,
    onClearAccount: () -> Unit,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    val credentialManager = remember(context) { CredentialManager.create(context) }

    var nameInput by remember(astrologerAccount.astrologerName) {
        mutableStateOf(
            astrologerAccount.astrologerName.ifBlank {
                currentUser?.displayName.orEmpty()
            }
        )
    }
    var mobileInput by remember(astrologerAccount.mobileNumber) {
        mutableStateOf(astrologerAccount.mobileNumber)
    }
    var studioInput by remember(astrologerAccount.studioName) {
        mutableStateOf(astrologerAccount.studioName.ifBlank { "Shri Sampurna Astrology" })
    }
    var formError by remember { mutableStateOf<String?>(null) }
    var authError by remember { mutableStateOf<String?>(null) }
    var isSigningIn by remember { mutableStateOf(false) }

    LaunchedEffect(currentUser?.uid) {
        if (currentUser != null && astrologerAccount.accountMethod != AccountMethod.GOOGLE) {
            val gName = nameInput.ifBlank { currentUser.displayName ?: currentUser.email ?: "Acharya" }
            onSaveGoogleProfileName(gName, mobileInput, studioInput, currentUser.email.orEmpty())
        }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface),
                navigationIcon = {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier.minimumInteractiveComponentSize().testTag("account_back_button")
                    ) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                title = {
                    Text(
                        text = tr(
                            language,
                            "ज्योतिषी खाता (PDF हेडर सेटअप)",
                            "Astrologer Account (PDF Header)",
                            "占星师账户设置"
                        ),
                        color = DarkBrandBlue,
                        fontWeight = FontWeight.Bold
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
                    .widthIn(max = 620.dp)
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Active PDF Header Preview Card
                GoldBorderedCard(
                    containerColor = if (astrologerAccount.isConfigured || currentUser != null) {
                        AuspiciousGreenBg
                    } else {
                        MaterialTheme.colorScheme.surface
                    }
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.img_astro_emblem),
                            contentDescription = null,
                            modifier = Modifier.size(54.dp).clip(CircleShape),
                            contentScale = ContentScale.Crop
                        )
                        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text(
                                text = tr(
                                    language,
                                    "PDF रिपोर्ट के सबसे ऊपर दिखने वाला नाम:",
                                    "Displayed at the Top of Every PDF Report:",
                                    "每份PDF报告顶部显示的名称："
                                ),
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = astrologerAccount.effectiveHeaderName(),
                                style = MaterialTheme.typography.titleMedium,
                                color = DarkBrandBlue,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.testTag("active_astrologer_header_name")
                            )
                            if (astrologerAccount.mobileNumber.isNotBlank()) {
                                Text(
                                    text = "Mob: ${astrologerAccount.mobileNumber} • ${astrologerAccount.studioName}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = AuspiciousGreen,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                        if (astrologerAccount.isConfigured || currentUser != null) {
                            Icon(Icons.Outlined.CheckCircle, contentDescription = null, tint = AuspiciousGreen)
                        }
                    }
                }

                // Option 1: Account via Mobile Number + Astrologer Name
                GoldBorderedCard {
                    Column(
                        modifier = Modifier.fillMaxWidth().padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        SectionTitle(
                            title = tr(
                                language,
                                "मोबाइल नंबर से ज्योतिषी खाता बनाएँ",
                                "Create Account with Mobile Number",
                                "使用手机号创建占星师账户"
                            )
                        )
                        Text(
                            text = tr(
                                language,
                                "अपना नाम, मोबाइल नंबर और केंद्र का नाम भरें — हर PDF रिपोर्ट के सबसे ऊपर आपका नाम छपेगा।",
                                "Enter your Astrologer Name & Mobile Number. Your name will appear prominently at the top of every generated PDF report.",
                                "输入占星师姓名和手机号，您的姓名将显示在每份PDF报告的最顶部。"
                            ),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        OutlinedTextField(
                            value = nameInput,
                            onValueChange = { nameInput = it; formError = null },
                            leadingIcon = { Icon(Icons.Outlined.Badge, contentDescription = null) },
                            label = {
                                Text(tr(language, "ज्योतिषी का नाम (Astrologer Name)", "Astrologer Full Name", "占星师姓名"))
                            },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth().testTag("account_name_input")
                        )
                        OutlinedTextField(
                            value = mobileInput,
                            onValueChange = { mobileInput = it; formError = null },
                            leadingIcon = { Icon(Icons.Outlined.PhoneAndroid, contentDescription = null) },
                            label = {
                                Text(tr(language, "मोबाइल नंबर (+91 ...)", "Mobile Number", "手机号码"))
                            },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth().testTag("account_mobile_input")
                        )
                        OutlinedTextField(
                            value = studioInput,
                            onValueChange = { studioInput = it },
                            leadingIcon = { Icon(Icons.Outlined.AccountCircle, contentDescription = null) },
                            label = {
                                Text(tr(language, "ज्योतिष केंद्र / संस्था (वैकल्पिक)", "Astrology Kendra / Studio (Optional)", "工作室名称（可选）"))
                            },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth().testTag("account_studio_input")
                        )

                        if (formError != null) {
                            Text(
                                text = formError!!,
                                color = MaterialTheme.colorScheme.primary,
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Medium
                            )
                        }

                        Button(
                            onClick = {
                                val trimmedName = nameInput.trim()
                                val digits = mobileInput.filter { it.isDigit() }
                                if (trimmedName.length < 2) {
                                    formError = tr(
                                        language,
                                        "कृपया ज्योतिषी का पूरा नाम दर्ज करें।",
                                        "Please enter the astrologer's name.",
                                        "请输入占星师姓名。"
                                    )
                                } else if (digits.length < 10) {
                                    formError = tr(
                                        language,
                                        "कृपया कम से कम 10 अंकों का मान्य मोबाइल नंबर दर्ज करें।",
                                        "Please enter a valid 10+ digit mobile number.",
                                        "请输入有效的10位以上手机号码。"
                                    )
                                } else {
                                    if (currentUser != null) {
                                        onSaveGoogleProfileName(trimmedName, mobileInput.trim(), studioInput.trim(), currentUser.email.orEmpty())
                                    } else {
                                        onSaveMobileAccount(trimmedName, mobileInput.trim(), studioInput.trim())
                                    }
                                    scope.launch {
                                        snackbarHostState.showSnackbar(
                                            tr(
                                                language,
                                                "ज्योतिषी खाता सुरक्षित — अब हर PDF पर आपका नाम दिखेगा!",
                                                "Astrologer account saved — your name now appears at the top of every PDF!",
                                                "占星师账户已保存 — 所有PDF顶部都将显示您的姓名！"
                                            )
                                        )
                                    }
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = DarkBrandBlue, contentColor = Color.White),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth().height(50.dp).testTag("save_mobile_account_button")
                        ) {
                            Text(
                                text = tr(language, "मोबाइल से खाता सहेजें", "Save Mobile Astrologer Account", "保存手机号账户"),
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                // Option 2: Account via Google Sign-In
                GoldBorderedCard {
                    Column(
                        modifier = Modifier.fillMaxWidth().padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        SectionTitle(
                            title = tr(
                                language,
                                "Google से खाता बनाएँ / साइन इन करें",
                                "Create / Sign in with Google Account",
                                "使用 Google 账号创建 / 登录"
                            )
                        )
                        Text(
                            text = tr(
                                language,
                                "Google खाते से जुड़ने पर आपका नाम PDF पर दिखेगा और क्लाउड कुंडली संग्रह भी सिंक होगा।",
                                "Connecting with Google sets your astrologer identity and unlocks Cloud Kundali Sync across devices.",
                                "绑定 Google 账号即可设置占星师身份并解锁云端星盘同步。"
                            ),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        if (authError != null) {
                            Surface(color = CautionAmberBg, shape = RoundedCornerShape(10.dp), modifier = Modifier.fillMaxWidth()) {
                                Text(authError!!, color = CautionAmber, style = MaterialTheme.typography.bodySmall, modifier = Modifier.padding(10.dp))
                            }
                        }

                        if (currentUser == null) {
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
                                        onAuthCancelled = { isSigningIn = false }
                                    )
                                },
                                enabled = !isSigningIn,
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary, contentColor = Color.White),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.fillMaxWidth().height(50.dp).testTag("account_google_signin_button")
                            ) {
                                if (isSigningIn) {
                                    CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color.White, strokeWidth = 2.dp)
                                } else {
                                    Icon(Icons.AutoMirrored.Outlined.Login, contentDescription = null)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(tr(language, "Google से खाता बनाएँ", "Continue with Google", "通过 Google 登录"), fontWeight = FontWeight.Bold)
                                }
                            }
                        } else {
                            Text(
                                text = "Google Linked: ${currentUser.displayName ?: currentUser.email ?: "Active"} (${currentUser.email.orEmpty()})",
                                style = MaterialTheme.typography.bodyMedium,
                                color = AuspiciousGreen,
                                fontWeight = FontWeight.SemiBold
                            )
                            OutlinedButton(
                                onClick = {
                                    AuthManager.signOut(credentialManager, scope) {
                                        onClearAccount()
                                    }
                                },
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.fillMaxWidth().height(48.dp).testTag("account_sign_out_button")
                            ) {
                                Icon(Icons.AutoMirrored.Outlined.Logout, contentDescription = null)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(tr(language, "साइन आउट करें", "Sign Out", "退出登录"))
                            }
                        }
                    }
                }
            }
        }
    }
}

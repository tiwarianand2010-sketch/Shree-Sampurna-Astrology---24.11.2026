package com.example.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material3.AssistChip
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.core.AppLanguage
import com.example.core.AstroStrings
import com.example.core.tr
import com.example.firebase.AiAstrologerSpecialty
import com.example.model.KundaliResult
import com.example.ui.AiChatViewModel
import com.example.ui.AiStreamState
import com.example.ui.components.GoldBorderedCard
import com.example.ui.theme.CautionAmber
import com.example.ui.theme.CautionAmberBg
import com.example.ui.theme.DarkBrandBlue

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AiAstrologerScreen(
    result: KundaliResult?,
    language: AppLanguage,
    onBack: () -> Unit,
    aiViewModel: AiChatViewModel = viewModel()
) {
    val uiState by aiViewModel.uiState.collectAsStateWithLifecycle()
    var questionInput by remember { mutableStateOf("") }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface),
                navigationIcon = {
                    IconButton(onClick = onBack, modifier = Modifier.minimumInteractiveComponentSize().testTag("ai_back_button")) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                title = {
                    Text(
                        text = tr(language, "AI ज्योतिष परामर्श", "AI Astrologer Consultation", "AI 占星师咨询"),
                        color = DarkBrandBlue,
                        fontWeight = FontWeight.Bold
                    )
                },
                actions = {
                    IconButton(onClick = { aiViewModel.clearConversation() }, modifier = Modifier.minimumInteractiveComponentSize()) {
                        Icon(Icons.Outlined.DeleteOutline, contentDescription = "Clear Chat")
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 12.dp, vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Column(modifier = Modifier.widthIn(max = 680.dp).fillMaxSize()) {
                // 4 AI Astrologer Personas (Vedic, Western, Numerology, Vastu/Feng Shui)
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                    items(AiAstrologerSpecialty.entries) { spec ->
                        FilterChip(
                            selected = uiState.specialty == spec,
                            onClick = { aiViewModel.selectSpecialty(spec) },
                            label = {
                                Text(if (language == AppLanguage.EN) spec.titleEn else spec.titleHi)
                            },
                            modifier = Modifier.testTag("ai_spec_${spec.name.lowercase()}")
                        )
                    }
                }

                if (result != null) {
                    Text(
                        text = "${tr(language, "सक्रिय कुंडली", "Active Chart", "当前星盘")}: ${result.input.name} • ${AstroStrings.rashiName(result.lagnaRashi, language)}",
                        style = MaterialTheme.typography.labelMedium,
                        color = DarkBrandBlue,
                        modifier = Modifier.padding(vertical = 4.dp)
                    )
                }

                // Quick prompts tailored to selected persona
                val suggestions = when (uiState.specialty) {
                    AiAstrologerSpecialty.VEDIC -> listOf(
                        tr(language, "वर्तमान महादशा-अंतरदशा का फल बताएं", "Analyze my current Dasha & Career prospects"),
                        tr(language, "KP और अष्टकवर्ग से विवाह व धन योग देखें", "Check marriage & wealth houses via KP & Ashtakavarga")
                    )
                    AiAstrologerSpecialty.WESTERN -> listOf(
                        tr(language, "मेरे ट्रॉपिकल सूर्य-चंद्र और मुख्य पहलुओं का अर्थ", "Interpret my Tropical Sun, Moon & major aspects"),
                        tr(language, "तत्व संतुलन (अग्नि/पृथ्वी/वायु/जल) पर सलाह", "Advice based on my Fire/Earth/Air/Water balance")
                    )
                    AiAstrologerSpecialty.NUMEROLOGY -> listOf(
                        tr(language, "मूलांक, भाग्यांक और लो-शू की लुप्त संख्याओं के उपाय", "Remedies for missing Lo Shu numbers & Mulank"),
                        tr(language, "नाम की चाल्डियन संख्या का प्रभाव बताएं", "How does my Chaldean name number affect luck?")
                    )
                    AiAstrologerSpecialty.VASTU_FENG_SHUI -> listOf(
                        tr(language, "मेरी कुआ संख्या के अनुसार मुख्य द्वार व पूजा दिशा", "Best entrance & puja direction per my Kua number"),
                        tr(language, "घर/कार्यालय के लिए वास्तु व फ़ेंग शुई सुधार", "Top Vastu & Feng Shui corrections for prosperity")
                    )
                }

                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(bottom = 8.dp)) {
                    items(suggestions) { prompt ->
                        AssistChip(
                            onClick = {
                                if (result != null) aiViewModel.sendQuestion(result, language, prompt)
                            },
                            leadingIcon = { Icon(Icons.Outlined.AutoAwesome, contentDescription = null, modifier = Modifier.size(16.dp)) },
                            label = { Text(prompt, maxLines = 1) }
                        )
                    }
                }

                // Chat thread
                LazyColumn(
                    modifier = Modifier.weight(1f).fillMaxWidth(),
                    contentPadding = PaddingValues(vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    if (uiState.messages.isEmpty() && uiState.streamState is AiStreamState.Idle) {
                        item {
                            GoldBorderedCard {
                                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                    Text(
                                        text = if (language == AppLanguage.EN) uiState.specialty.titleEn else uiState.specialty.titleHi,
                                        style = MaterialTheme.typography.titleMedium,
                                        color = DarkBrandBlue,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = tr(
                                            language,
                                            "आपकी पूरी कुंडली, केपी, अष्टकवर्ग, दशा और वास्तु डेटा AI ज्योतिषी को उपलब्ध है। नीचे अपना प्रश्न पूछें।",
                                            "Your full chart, Dasha, KP, Ashtakavarga, Numerology, and Vastu metrics are loaded. Ask any question below.",
                                            "您的完整星盘、大运、KP与风水参数已载入，请在下方输入问题。"
                                        ),
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                }
                            }
                        }
                    }

                    items(uiState.messages, key = { it.id }) { msg ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = if (msg.isUser) Arrangement.End else Arrangement.Start
                        ) {
                            Surface(
                                color = if (msg.isUser) DarkBrandBlue else MaterialTheme.colorScheme.surfaceVariant,
                                shape = RoundedCornerShape(14.dp),
                                modifier = Modifier.widthIn(max = 520.dp)
                            ) {
                                Text(
                                    text = msg.text,
                                    color = if (msg.isUser) Color.White else MaterialTheme.colorScheme.onSurface,
                                    style = MaterialTheme.typography.bodyMedium,
                                    modifier = Modifier.padding(12.dp)
                                )
                            }
                        }
                    }

                    when (val stream = uiState.streamState) {
                        is AiStreamState.Streaming -> {
                            item {
                                Surface(
                                    color = MaterialTheme.colorScheme.surfaceVariant,
                                    shape = RoundedCornerShape(14.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(
                                        text = stream.partialText.ifBlank { tr(language, "कुंडली का विश्लेषण किया जा रहा है...", "Reading your chart...", "正在解读星盘...") },
                                        style = MaterialTheme.typography.bodyMedium,
                                        modifier = Modifier.padding(12.dp)
                                    )
                                }
                            }
                        }
                        is AiStreamState.Error -> {
                            item {
                                Surface(
                                    color = CautionAmberBg,
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(
                                        text = stream.message,
                                        color = CautionAmber,
                                        style = MaterialTheme.typography.bodySmall,
                                        modifier = Modifier.padding(12.dp)
                                    )
                                }
                            }
                        }
                        AiStreamState.Idle -> Unit
                    }
                }

                // Input row
                Row(
                    modifier = Modifier.fillMaxWidth().padding(top = 6.dp, bottom = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = questionInput,
                        onValueChange = { questionInput = it },
                        placeholder = { Text(tr(language, "अपना ज्योतिष प्रश्न लिखें...", "Ask about career, marriage, remedies...", "输入您的占星问题...")) },
                        singleLine = true,
                        modifier = Modifier.weight(1f).testTag("ai_question_input")
                    )
                    IconButton(
                        onClick = {
                            if (result != null && questionInput.isNotBlank()) {
                                val q = questionInput
                                questionInput = ""
                                aiViewModel.sendQuestion(result, language, q)
                            }
                        },
                        enabled = result != null && questionInput.isNotBlank() && uiState.streamState !is AiStreamState.Streaming,
                        modifier = Modifier
                            .size(48.dp)
                            .background(DarkBrandBlue, RoundedCornerShape(12.dp))
                            .testTag("ai_send_button")
                    ) {
                        Icon(Icons.AutoMirrored.Filled.Send, contentDescription = "Send", tint = Color.White)
                    }
                }
            }
        }
    }
}

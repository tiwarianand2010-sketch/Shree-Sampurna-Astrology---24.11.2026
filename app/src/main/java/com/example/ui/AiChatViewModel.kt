package com.example.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.core.AppLanguage
import com.example.firebase.AiAstrologerRepository
import com.example.firebase.AiAstrologerSpecialty
import com.example.firebase.AiChatMessage
import com.example.model.KundaliResult
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

sealed interface AiStreamState {
    data object Idle : AiStreamState
    data class Streaming(val partialText: String) : AiStreamState
    data class Error(val message: String) : AiStreamState
}

data class AiChatUiState(
    val specialty: AiAstrologerSpecialty = AiAstrologerSpecialty.VEDIC,
    val messages: List<AiChatMessage> = emptyList(),
    val streamState: AiStreamState = AiStreamState.Idle
)

class AiChatViewModel(
    private val repository: AiAstrologerRepository = AiAstrologerRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(AiChatUiState())
    val uiState: StateFlow<AiChatUiState> = _uiState.asStateFlow()

    private var activeJob: Job? = null

    fun selectSpecialty(specialty: AiAstrologerSpecialty) {
        _uiState.update { it.copy(specialty = specialty, streamState = AiStreamState.Idle) }
    }

    fun clearConversation() {
        activeJob?.cancel()
        _uiState.update { it.copy(messages = emptyList(), streamState = AiStreamState.Idle) }
    }

    fun sendQuestion(result: KundaliResult, language: AppLanguage, question: String) {
        val cleanQ = question.trim()
        if (cleanQ.isEmpty()) return
        activeJob?.cancel()

        val currentSpec = _uiState.value.specialty
        val priorHistory = _uiState.value.messages
        val userMsg = AiChatMessage(
            id = "u_${System.currentTimeMillis()}",
            isUser = true,
            text = cleanQ,
            specialty = currentSpec
        )
        _uiState.update {
            it.copy(
                messages = it.messages + userMsg,
                streamState = AiStreamState.Streaming("")
            )
        }

        activeJob = viewModelScope.launch {
            var latestReply = ""
            try {
                repository.streamChatReply(
                    result = result,
                    specialty = currentSpec,
                    language = language,
                    priorHistory = priorHistory,
                    userQuestion = cleanQ
                ).collect { accumulated ->
                    latestReply = accumulated
                    _uiState.update { it.copy(streamState = AiStreamState.Streaming(accumulated)) }
                }

                if (latestReply.isNotBlank()) {
                    val botMsg = AiChatMessage(
                        id = "a_${System.currentTimeMillis()}",
                        isUser = false,
                        text = latestReply,
                        specialty = currentSpec
                    )
                    _uiState.update {
                        it.copy(
                            messages = it.messages + botMsg,
                            streamState = AiStreamState.Idle
                        )
                    }
                } else {
                    _uiState.update {
                        it.copy(streamState = AiStreamState.Error("No response received from AI astrologer."))
                    }
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        streamState = AiStreamState.Error(
                            e.localizedMessage ?: "AI consultation request failed. Please check network and retry."
                        )
                    )
                }
            }
        }
    }
}

package com.example.firebase

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.model.KundaliResult
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class CloudChartsUiState(
    val isLoading: Boolean = true,
    val isSaving: Boolean = false,
    val searchQuery: String = "",
    val allCharts: List<SavedKundali> = emptyList(),
    val filteredCharts: List<SavedKundali> = emptyList(),
    val errorMessage: String? = null,
    val statusMessage: String? = null
)

class CloudChartsViewModel(
    private val userId: String,
    private val repository: CloudChartsRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(CloudChartsUiState())
    val uiState: StateFlow<CloudChartsUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            repository.observeSavedCharts(userId, limitCount = 2000L)
                .catch { e ->
                    if (e is CancellationException) throw e
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = e.localizedMessage ?: "Failed to load saved Kundalis."
                        )
                    }
                }
                .collect { charts ->
                    _uiState.update { state ->
                        state.copy(
                            isLoading = false,
                            allCharts = charts,
                            filteredCharts = applyFilter(charts, state.searchQuery),
                            errorMessage = null
                        )
                    }
                }
        }
    }

    private fun applyFilter(list: List<SavedKundali>, query: String): List<SavedKundali> {
        val q = query.trim().lowercase()
        if (q.isEmpty()) return list
        return list.filter { item ->
            item.chartName.lowercase().contains(q) ||
                item.placeName.lowercase().contains(q) ||
                item.notes.lowercase().contains(q) ||
                item.lagnaRashi.lowercase().contains(q) ||
                item.moonRashi.lowercase().contains(q)
        }
    }

    fun updateSearchQuery(query: String) {
        _uiState.update { state ->
            state.copy(
                searchQuery = query,
                filteredCharts = applyFilter(state.allCharts, query)
            )
        }
    }

    fun saveCurrentChart(result: KundaliResult, notes: String = "") {
        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, errorMessage = null, statusMessage = null) }
            val outcome = repository.saveChart(userId, result, notes)
            outcome.fold(
                onSuccess = {
                    _uiState.update {
                        it.copy(
                            isSaving = false,
                            statusMessage = "Saved \"${result.input.name}\" to cloud (${it.allCharts.size + 1})."
                        )
                    }
                },
                onFailure = { e ->
                    _uiState.update {
                        it.copy(
                            isSaving = false,
                            errorMessage = e.localizedMessage ?: "Unable to save Kundali to cloud."
                        )
                    }
                }
            )
        }
    }

    fun updateChartNotes(kundaliId: String, chartName: String, notes: String) {
        viewModelScope.launch {
            val outcome = repository.updateNotes(userId, kundaliId, chartName, notes)
            outcome.fold(
                onSuccess = {
                    _uiState.update { it.copy(statusMessage = "Updated \"$chartName\".", errorMessage = null) }
                },
                onFailure = { e ->
                    _uiState.update { it.copy(errorMessage = e.localizedMessage ?: "Failed to update notes.") }
                }
            )
        }
    }

    fun deleteChart(kundaliId: String) {
        viewModelScope.launch {
            val outcome = repository.deleteChart(userId, kundaliId)
            outcome.fold(
                onSuccess = {
                    _uiState.update { it.copy(statusMessage = "Chart deleted from cloud.", errorMessage = null) }
                },
                onFailure = { e ->
                    _uiState.update { it.copy(errorMessage = e.localizedMessage ?: "Failed to delete chart.") }
                }
            )
        }
    }

    fun clearBanner() {
        _uiState.update { it.copy(errorMessage = null, statusMessage = null) }
    }

    class Factory(
        private val userId: String,
        private val repository: CloudChartsRepository
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return CloudChartsViewModel(userId, repository) as T
        }
    }
}

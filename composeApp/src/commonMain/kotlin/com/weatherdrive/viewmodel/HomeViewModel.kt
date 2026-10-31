package com.weatherdrive.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.weatherdrive.model.CategoryNode
import com.weatherdrive.model.YearNode
import com.weatherdrive.repository.ShowRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

sealed class UiState {
    data object Loading : UiState()
    data class Success(val treeNodes: List<YearNode>) : UiState()
    data class Error(val message: String) : UiState()
}

class HomeViewModel(private val repository: ShowRepository) : ViewModel() {
    private val _uiState = MutableStateFlow<UiState>(UiState.Loading)
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    private val inFlightYears = mutableSetOf<String>()

    init {
        refresh()
    }

    fun refresh() {
        viewModelScope.launch {
            _uiState.value = UiState.Loading
            inFlightYears.clear()
            _uiState.value = try {
                UiState.Success(
                    repository.fetchYears()
                        .map { YearNode(year = it.name, count = it.count) }
                        .sortedByDescending { it.year }
                )
            } catch (e: Exception) {
                UiState.Error(e.message ?: "Unknown error")
            }
        }
    }

    /** Loads the recordings of [year] if not already loaded or loading. Safe to call repeatedly. */
    fun loadYear(year: String) {
        val loaded = (_uiState.value as? UiState.Success)?.treeNodes
            ?.firstOrNull { it.year == year }
            ?.children != null
        if (loaded || !inFlightYears.add(year)) return

        viewModelScope.launch {
            val result = try {
                repository.getYear(year).categories
                    .map { CategoryNode(category = it.category, children = it.shows.sortedBy { s -> s.title }) }
                    .sortedBy { it.category }
            } catch (e: Exception) {
                null
            }
            inFlightYears.remove(year)
            updateYear(year) { it.copy(children = result, loadFailed = result == null) }
        }
    }

    private fun updateYear(year: String, transform: (YearNode) -> YearNode) {
        _uiState.update { state ->
            if (state !is UiState.Success) state
            else state.copy(treeNodes = state.treeNodes.map { if (it.year == year) transform(it) else it })
        }
    }
}

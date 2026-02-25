package com.weatherdrive.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.weatherdrive.model.CategoryNode
import com.weatherdrive.model.Show
import com.weatherdrive.model.YearNode
import com.weatherdrive.repository.ShowRepository
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

sealed class UiState {
    data object Loading : UiState()
    data class Success(val treeNodes: List<YearNode>) : UiState()
    data class Error(val message: String) : UiState()
}

class HomeViewModel(private val repository: ShowRepository) : ViewModel() {
    private val refreshTrigger = MutableSharedFlow<Unit>(replay = 1).also {
        it.tryEmit(Unit)
    }

    val uiState: StateFlow<UiState> = refreshTrigger
        .flatMapLatest {
            flow {
                emit(UiState.Loading)
                try {
                    val shows = repository.fetchShows()
                    emit(UiState.Success(buildTree(shows)))
                } catch (e: Exception) {
                    emit(UiState.Error(e.message ?: "Unknown error"))
                }
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), UiState.Loading)

    fun refresh() {
        viewModelScope.launch { refreshTrigger.emit(Unit) }
    }

    private fun buildTree(shows: List<Show>): List<YearNode> {
        return shows
            .groupBy { it.year }
            .entries
            .sortedByDescending { it.key }
            .map { (year, yearShows) ->
                val categories = yearShows
                    .groupBy { it.category }
                    .entries
                    .sortedBy { it.key }
                    .map { (category, catShows) ->
                        CategoryNode(
                            category = category,
                            children = catShows.sortedBy { it.title }
                        )
                    }
                YearNode(year = year, children = categories)
            }
    }
}

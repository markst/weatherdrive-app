package com.weatherdrive.viewmodel

import com.weatherdrive.model.CategoryNode
import com.weatherdrive.model.Show
import com.weatherdrive.model.YearNode
import com.weatherdrive.network.WeatherdriveApi
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
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

class HomeViewModel {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private val api = WeatherdriveApi()

    private val refreshTrigger = MutableSharedFlow<Unit>(replay = 1).also {
        it.tryEmit(Unit)
    }

    val uiState: StateFlow<UiState> = refreshTrigger
        .flatMapLatest {
            flow {
                emit(UiState.Loading)
                try {
                    val shows = api.fetchShows()
                    emit(UiState.Success(buildTree(shows)))
                } catch (e: Exception) {
                    emit(UiState.Error(e.message ?: "Unknown error"))
                }
            }
        }
        .stateIn(scope, SharingStarted.WhileSubscribed(5_000), UiState.Loading)

    fun refresh() {
        scope.launch { refreshTrigger.emit(Unit) }
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

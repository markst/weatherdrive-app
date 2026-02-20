package com.weatherdrive.viewmodel

import com.weatherdrive.model.CategoryNode
import com.weatherdrive.model.YearNode
import com.weatherdrive.network.WeatherdriveApi
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class HomeViewModel {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private val api = WeatherdriveApi()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _treeNodes = MutableStateFlow<List<YearNode>>(emptyList())
    val treeNodes: StateFlow<List<YearNode>> = _treeNodes.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    init {
        loadShows()
    }

    fun loadShows() {
        scope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                val shows = api.fetchShows()
                _treeNodes.value = buildTree(shows)
            } catch (e: Exception) {
                _error.value = e.message ?: "Unknown error"
            } finally {
                _isLoading.value = false
            }
        }
    }

    private fun buildTree(shows: List<com.weatherdrive.model.Show>): List<YearNode> {
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

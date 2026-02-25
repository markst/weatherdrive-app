package com.weatherdrive.repository

import com.weatherdrive.model.Show
import com.weatherdrive.network.WeatherdriveApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Repository for managing Show data with caching.
 */
class ShowRepository(private val api: WeatherdriveApi) {
    private val _shows = MutableStateFlow<List<Show>>(emptyList())
    val shows: StateFlow<List<Show>> = _shows.asStateFlow()

    /**
     * Fetch shows from the API and cache them.
     */
    suspend fun fetchShows(): List<Show> {
        val shows = api.fetchShows()
        _shows.value = shows
        return shows
    }

    /**
     * Get a show by ID from the cache.
     * If not found in cache, attempts to fetch from API.
     */
    suspend fun getShowById(id: Long): Show? {
        // First check the cache
        _shows.value.firstOrNull { it.id == id }?.let { return it }

        // If not in cache, fetch all shows
        fetchShows()

        // Try again from the updated cache
        return _shows.value.firstOrNull { it.id == id }
    }

    /**
     * Get cached shows without making a network call.
     */
    fun getCachedShows(): List<Show> = _shows.value
}

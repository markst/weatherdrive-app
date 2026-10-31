package com.weatherdrive.repository

import com.weatherdrive.model.Show
import com.weatherdrive.network.CollectionSummary
import com.weatherdrive.network.WeatherdriveApi
import com.weatherdrive.network.SearchResult
import com.weatherdrive.network.YearRecordings
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/**
 * Repository for archive data. Years and recordings are fetched lazily and cached in memory.
 */
class ShowRepository(private val api: WeatherdriveApi) {
    private val lock = Mutex()
    private val years = mutableMapOf<String, YearRecordings>()
    private val shows = mutableMapOf<String, Show>()

    /** Always hits the network so pull-to-refresh/retry works. */
    suspend fun fetchYears(): List<CollectionSummary> = api.fetchYears().also {
        lock.withLock { years.clear() }
    }

    suspend fun getYear(year: String): YearRecordings {
        lock.withLock { years[year] }?.let { return it }
        return api.fetchYear(year).also { lock.withLock { years[year] = it } }
    }

    suspend fun getShow(slug: String): Show {
        lock.withLock { shows[slug] }?.let { return it }
        return api.fetchRecording(slug).also { lock.withLock { shows[slug] = it } }
    }

    suspend fun search(query: String): List<SearchResult> = api.search(query)
}

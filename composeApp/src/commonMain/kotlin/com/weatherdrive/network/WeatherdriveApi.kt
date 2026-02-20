package com.weatherdrive.network

import com.weatherdrive.model.Show
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.get
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

private const val API_URL = "https://www.flightpathestate.com/api/drvr/2026-02-07"

class WeatherdriveApi {
    private val client = HttpClient {
        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true
                isLenient = true
            })
        }
    }

    suspend fun fetchShows(): List<Show> {
        return try {
            client.get(API_URL).body()
        } catch (e: Exception) {
            emptyList()
        }
    }
}

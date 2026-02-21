package com.weatherdrive.network

import com.weatherdrive.model.Show
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.get
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

private const val BASE_URL = "https://www.flightpathestate.com/api"

@Serializable
data class TokenResponse(
    val success: Boolean = false,
    val date: String = ""
)

class WeatherdriveApi {
    private val client = HttpClient {
        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true
                isLenient = true
            })
        }
    }

    private suspend fun fetchDate(): String {
        return client.get("$BASE_URL/token").body<TokenResponse>().date
    }

    suspend fun fetchShows(): List<Show> {
        val date = fetchDate()
        return client.get("$BASE_URL/drvr/$date").body()
    }
}

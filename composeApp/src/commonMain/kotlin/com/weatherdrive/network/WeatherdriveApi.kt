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
private const val FILE_API_URL = "https://weatherdrive.markturnip.dev"

@Serializable
data class TokenResponse(
    val success: Boolean = false,
    val date: String = ""
)

@Serializable
data class FileCredentials(
    @kotlinx.serialization.SerialName("access_token")
    val accessToken: String = "",
    @kotlinx.serialization.SerialName("expiry_date")
    val expiryDate: Long = 0,
    @kotlinx.serialization.SerialName("token_type")
    val tokenType: String = "",
    @kotlinx.serialization.SerialName("refresh_token")
    val refreshToken: String = ""
)

@Serializable
data class FileAccessResponse(
    val url: String = "",
    val credentials: FileCredentials = FileCredentials()
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

    suspend fun fetchFileAccess(googleDriveId: String): FileAccessResponse {
        return client.get("$FILE_API_URL/file/$googleDriveId").body()
    }
}

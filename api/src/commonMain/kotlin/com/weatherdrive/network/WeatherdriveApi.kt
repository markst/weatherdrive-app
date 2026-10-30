package com.weatherdrive.network

import com.weatherdrive.model.FileItem
import com.weatherdrive.model.Show
import com.weatherdrive.model.ShowDate
import com.weatherdrive.model.ShowSummary
import com.weatherdrive.model.Webpage
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.get
import io.ktor.http.encodeURLPathPart
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

/** See docs/openapi.yaml. */
private const val BASE_URL = "https://www.flightpathestate.com/public/api/drvr"
private const val FILE_API_URL = "https://weatherdrive.markturnip.dev"

@Serializable
data class FileCredentials(
    @SerialName("access_token")
    val accessToken: String = "",
    @SerialName("expiry_date")
    val expiryDate: Long = 0,
    @SerialName("token_type")
    val tokenType: String = "",
    @SerialName("refresh_token")
    val refreshToken: String = ""
)

@Serializable
data class FileAccessResponse(
    val url: String = "",
    val credentials: FileCredentials = FileCredentials()
)

/** A year or category with its recording count (`/year`, `/category`). */
data class CollectionSummary(val name: String, val title: String, val count: Int)

/** Recordings of one year, grouped by category. */
data class YearRecordings(val categories: List<CategoryRecordings>)

data class CategoryRecordings(val category: String, val shows: List<ShowSummary>)

data class SearchResult(val show: ShowSummary, val foundOnField: String)

@Serializable
private data class CollectionListDto(val list: List<CollectionItemDto> = emptyList())

@Serializable
private data class CollectionItemDto(val name: String, val title: String? = null, val count: Int = 0)

@Serializable
private data class RecordingListDto(val collections: List<RecordingCollectionDto> = emptyList())

@Serializable
private data class RecordingCollectionDto(
    val name: String,
    val list: List<RecordingSummaryDto> = emptyList()
)

@Serializable
private data class RecordingSummaryDto(
    val id: Long,
    val slug: String,
    val category: String = "",
    val date: ShowDate? = null,
    val displayTitle: String = ""
) {
    fun toModel() = ShowSummary(id = id, slug = slug, category = category, date = date, title = displayTitle)
}

@Serializable
private data class RecordingResponseDto(val recording: RecordingDto)

@Serializable
private data class SearchResponseDto(val list: List<SearchItemDto> = emptyList())

@Serializable
private data class SearchItemDto(val recording: RecordingSummaryDto, val foundOnField: String = "")

@Serializable
private data class RecordingDto(
    val id: Long,
    val slug: String,
    val title: List<String> = emptyList(),
    val image: String? = null,
    val date: ShowDate? = null,
    val category: String = "",
    val content: String = "",
    val filelist: List<FileDto> = emptyList(),
    val tracklistItems: List<TracklistItemDto> = emptyList(),
    val webpage: Webpage? = null
) {
    fun toModel() = Show(
        id = id,
        slug = slug,
        titles = title,
        thumbnail = image,
        date = date,
        category = category,
        content = content,
        filelist = filelist.map {
            FileItem(title = it.title, googleDriveId = it.googleDriveId, timeInSeconds = it.timeInSeconds)
        },
        tracklisting = tracklistItems.mapNotNull { it.toLine() }.joinToString("\n"),
        webpage = webpage
    )
}

@Serializable
private data class FileDto(
    val title: String = "",
    val googleDriveId: String = "",
    val timeInSeconds: Int = 0
)

/** `type` is `track` (has [raw]) or `section` (has [text]). */
@Serializable
private data class TracklistItemDto(
    val type: String = "",
    val raw: String? = null,
    val text: String? = null
) {
    fun toLine(): String? = when (type) {
        "section" -> text?.let { "## $it" }
        else -> raw
    }
}

class WeatherdriveApi {
    private val json = Json { ignoreUnknownKeys = true }

    private val client = HttpClient {
        install(ContentNegotiation) {
            json(json)
        }
    }

    /** All years with recording counts, newest first. */
    @Throws(Exception::class)
    suspend fun fetchYears(): List<CollectionSummary> =
        client.get("$BASE_URL/year").body<CollectionListDto>().list.map {
            CollectionSummary(name = it.name, title = it.title ?: it.name, count = it.count)
        }

    /** Recordings for [year], grouped by category (the aggregate `All` collection is dropped). */
    @Throws(Exception::class)
    suspend fun fetchYear(year: String): YearRecordings {
        val dto = client.get("$BASE_URL/year/${year.encodeURLPathPart()}").body<RecordingListDto>()
        return YearRecordings(
            dto.collections
                .filter { it.name != "All" }
                .map { CategoryRecordings(it.name, it.list.map(RecordingSummaryDto::toModel)) }
        )
    }

    @Throws(Exception::class)
    suspend fun fetchRecording(slug: String): Show =
        client.get("$BASE_URL/recording/${slug.encodeURLPathPart()}").body<RecordingResponseDto>().recording.toModel()

    /** Full-text search. [SearchResult.foundOnField] is where the first match was found (title, alias, artist, content, tracklisting). */
    @Throws(Exception::class)
    suspend fun search(query: String): List<SearchResult> =
        client.get("$BASE_URL/search/${query.encodeURLPathPart()}").body<SearchResponseDto>().list.map {
            SearchResult(show = it.recording.toModel(), foundOnField = it.foundOnField)
        }

    @Throws(Exception::class)
    suspend fun fetchFileAccess(googleDriveId: String): FileAccessResponse {
        return client.get("$FILE_API_URL/file/$googleDriveId").body()
    }
}

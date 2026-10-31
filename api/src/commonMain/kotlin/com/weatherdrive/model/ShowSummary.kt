package com.weatherdrive.model

/**
 * Lightweight list entry returned by the year/category endpoints.
 * The full [Show] is fetched on demand by [slug].
 */
data class ShowSummary(
    val id: Long,
    val slug: String,
    val category: String,
    val date: ShowDate?,
    val title: String
)

fun Show.toSummary() = ShowSummary(id = id, slug = slug, category = category, date = date, title = title)

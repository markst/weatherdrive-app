package com.weatherdrive.model

import kotlinx.serialization.Serializable

@Serializable
data class ShowDate(
    val year: String = "",
    val month: String = "",
    val day: String = ""
) {
    /**
     * Returns a human-readable date string in day/month/year order, omitting parts that are exactly "00" or blank.
     * For example: year=1991, month=10, day=00 → "10/1991"
     * Falls back to just the year if month and day are absent.
     */
    val formatted: String
        get() = listOf(day, month, year)
            .filter { it.isNotBlank() && it != "00" }
            .joinToString("/")
            .ifEmpty { year }
}

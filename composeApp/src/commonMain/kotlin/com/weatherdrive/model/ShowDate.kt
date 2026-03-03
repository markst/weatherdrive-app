package com.weatherdrive.model

import kotlinx.serialization.Serializable

@Serializable
data class ShowDate(
    val year: String = "",
    val month: String? = null,
    val day: String? = null
) {
    /**
     * Returns a human-readable date string in day/month/year order, omitting parts that are null, blank, or exactly "00".
     * For example: year=1991, month=10, day=null → "10/1991"
     * Falls back to just the year if month and day are absent.
     */
    val formatted: String
        get() = listOfNotNull(day, month, year)
            .filter { it.isNotBlank() && it != "00" }
            .joinToString("/")
            .ifEmpty { year }
}

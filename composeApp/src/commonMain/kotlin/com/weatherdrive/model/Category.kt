package com.weatherdrive.model

enum class Category(val value: String) {
    EVENT("event"),
    CHART("chart"),
    COMPILATION("compilation"),
    MIX("mix"),
    BROADCAST("broadcast");

    val formattedName: String
        get() = value.replaceFirstChar { it.uppercase() }

    companion object {
        fun fromValue(value: String): Category? =
            entries.find { it.value == value.lowercase() }
    }
}

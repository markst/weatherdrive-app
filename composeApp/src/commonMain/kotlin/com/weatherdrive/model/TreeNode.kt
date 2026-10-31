package com.weatherdrive.model

/** A year row. [children] is `null` until the year has been expanded and loaded. */
data class YearNode(
    val year: String,
    val count: Int,
    val children: List<CategoryNode>? = null,
    val loadFailed: Boolean = false
)

data class CategoryNode(
    val category: String,
    val children: List<ShowSummary>
)

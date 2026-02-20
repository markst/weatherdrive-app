package com.weatherdrive.model

data class YearNode(
    val year: String,
    val children: List<CategoryNode>
)

data class CategoryNode(
    val category: String,
    val year: String,
    val children: List<Show>
)

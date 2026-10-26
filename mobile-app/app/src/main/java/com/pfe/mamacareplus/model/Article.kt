package com.pfe.mamacareplus.model

data class Article(
    val id: Long = 0L,
    val title: String = "",
    val summary: String = "",
    val imageUrl: String = "",
    val category: String = ""
)

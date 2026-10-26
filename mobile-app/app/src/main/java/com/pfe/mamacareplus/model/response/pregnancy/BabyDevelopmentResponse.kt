package com.pfe.mamacareplus.model.response.pregnancy

data class BabyDevelopmentResponse(
    val babyDevelopmentId: Int,
    val weekNumber: Int,
    val babySize: String?,
    val babyWeightGrams: Double?,
    val babyLengthCm: Double?,
    val imageUrl: String?,
    val comparisonType: String?,
    val comparisonName: String?,
    val description: String?,
    val advice: String?,
    val createdAt: String?
)

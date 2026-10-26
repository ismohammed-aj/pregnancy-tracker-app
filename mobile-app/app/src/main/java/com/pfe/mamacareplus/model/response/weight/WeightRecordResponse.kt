package com.pfe.mamacareplus.model.response.weight

data class WeightRecordResponse(
    val weightRecordId: Int,
    val pregnancyProfileId: Int,
    val weightKg: Double,
    val bmi: Double?,
    val recordDate: String,
    val notes: String?,
    val createdAt: String?
)
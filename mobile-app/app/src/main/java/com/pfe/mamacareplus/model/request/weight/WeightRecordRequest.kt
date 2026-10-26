package com.pfe.mamacareplus.model.request.weight

data class WeightRecordRequest(
    val pregnancyProfileId: Int,
    val weightKg: Double,
    val bmi: Double? = null,
    val recordDate: String? = null,
    val notes: String? = null
)
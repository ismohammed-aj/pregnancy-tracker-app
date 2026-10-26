package com.pfe.mamacareplus.model.request.profile

data class PregnancyProfileRequest(
    val femmeId: Int,
    val startDate: String,
    val dueDate: String,
    val healthStatus: String?,
    val notes: String?
)
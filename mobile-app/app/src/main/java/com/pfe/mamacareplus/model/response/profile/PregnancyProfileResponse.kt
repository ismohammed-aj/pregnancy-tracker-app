package com.pfe.mamacareplus.model.response.profile

data class PregnancyProfileResponse(
    val pregnancyProfileId: Int,
    val femmeId: Int,
    val username: String?,
    val startDate: String,
    val dueDate: String,
    val currentWeek: Int,
    val currentMonth: Int,
    val remainingDays: Int,
    val progressPercentage: Double,
    val healthStatus: String?,
    val notes: String?,
    val createdAt: String?,
    val updatedAt: String?
)
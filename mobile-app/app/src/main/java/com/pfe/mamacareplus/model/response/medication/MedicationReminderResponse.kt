package com.pfe.mamacareplus.model.response.medication

data class MedicationReminderResponse(
    val id: Int,
    val femmeId: Int,
    val username: String?,
    val medicationName: String,
    val dosage: String,
    val frequency: String,
    val daysOfWeek: String?,
    val reminderTime: String,
    val startDate: String,
    val endDate: String?,
    val notes: String?,
    val active: Boolean,
    val done: Boolean,
    val lastCompletedAt: String?,
    val createdAt: String?,
    val updatedAt: String?
) {
    val title: String
        get() = "Take $medicationName"
}

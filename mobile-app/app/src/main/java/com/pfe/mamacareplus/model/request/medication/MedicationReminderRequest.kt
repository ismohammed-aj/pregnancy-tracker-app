package com.pfe.mamacareplus.model.request.medication

data class MedicationReminderRequest(
    val femmeId: Int? = null,
    val medicationName: String,
    val dosage: String,
    val frequency: String,
    val daysOfWeek: String? = null,
    val reminderTime: String,
    val startDate: String,
    val endDate: String? = null,
    val notes: String? = null,
    val active: Boolean = true,
    val done: Boolean = false
)

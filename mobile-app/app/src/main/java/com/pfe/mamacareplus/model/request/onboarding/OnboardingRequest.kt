package com.pfe.mamacareplus.model.request.onboarding

data class OnboardingRequest(
    val birthYear: Int?,
    val heightCm: Int?,
    val weightKg: Double?,
    val currentStatus: String?,
    val lastPeriodDate: String? = null,
    val dueDate: String? = null,
    val firstPregnancy: Boolean? = null,
    val medicalFollowUp: String? = null,
    val previousPregnancyProblem: String? = null,
    val previousPregnancyProblemTypes: List<String> = emptyList(),
    val currentWarningSymptoms: List<String> = emptyList(),
    val chronicDiseases: List<String> = emptyList(),
    val takesMedication: Boolean? = null,
    val medicationName: String? = null,
    val medicationTime: String? = null,
    val folicAcid: String? = null,
    val smokingStatus: String? = null,
    val remindersEnabled: Boolean? = null,
    val preferredReminderTime: String? = null,
    val wantsAppointmentReminder: String? = null,
    val nextAppointmentDateTime: String? = null
)

package com.pfe.mamacareplus.model.request.appointment

data class AppointmentRequest(
    val femmeId: Int,
    val title: String,
    val appointmentDate: String,
    val location: String? = null,
    val doctorName: String? = null,
    val notes: String? = null,
    val status: String? = "pending"
)
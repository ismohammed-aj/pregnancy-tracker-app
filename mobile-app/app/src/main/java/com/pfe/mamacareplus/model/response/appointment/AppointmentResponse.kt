package com.pfe.mamacareplus.model.response.appointment

data class AppointmentResponse(
    val appointmentId: Int,
    val femmeId: Int,
    val username: String?,
    val title: String,
    val appointmentDate: String,
    val location: String?,
    val doctorName: String?,
    val notes: String?,
    val status: String,
    val createdAt: String?
)
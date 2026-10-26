package com.pfe.mamacareplus.model

data class Appointment(
    val id: Long = 0L,
    val doctorName: String = "",
    val appointmentDate: String = "",
    val location: String = "",
    val status: String = ""
)

package com.pfe.mamacareplus.model

data class PregnancyProfile(
    val id: Long = 0L,
    val motherId: Long = 0L,
    val gestationWeek: Int = 0,
    val expectedDueDate: String = "",
    val notes: String = ""
)

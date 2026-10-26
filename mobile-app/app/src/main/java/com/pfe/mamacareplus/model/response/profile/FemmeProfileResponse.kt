package com.pfe.mamacareplus.model.response.profile

data class FemmeProfileResponse(
    val femmeId: Int,
    val userId: Int,
    val email: String,
    val username: String,
    val birthDate: String?,
    val avatar: String?,
    val createdAt: String?,
    val updatedAt: String?
)
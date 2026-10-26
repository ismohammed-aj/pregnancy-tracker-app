package com.pfe.mamacareplus.model.response.auth

data class AuthResponse(
    val token: String,
    val userId: Int,
    val email: String,
    val role: String,
    val femmeId: Int?
)
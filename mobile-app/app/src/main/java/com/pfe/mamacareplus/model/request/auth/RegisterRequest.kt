package com.pfe.mamacareplus.model.request.auth

data class RegisterRequest(
    val email: String,
    val password: String,
    val role: String = "FEMME",
    val username: String,
    val birthDate: String?,
    val avatar: String? = null
)

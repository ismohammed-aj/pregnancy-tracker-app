package com.pfe.mamacareplus.model.request.auth

data class LoginRequest(
    val email: String,
    val password: String
)
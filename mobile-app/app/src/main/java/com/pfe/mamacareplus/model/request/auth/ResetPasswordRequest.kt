package com.pfe.mamacareplus.model.request.auth

data class ResetPasswordRequest(
    val email: String,
    val code: String,
    val newPassword: String
)

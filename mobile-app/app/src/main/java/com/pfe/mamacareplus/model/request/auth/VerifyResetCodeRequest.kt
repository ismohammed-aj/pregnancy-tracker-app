package com.pfe.mamacareplus.model.request.auth

data class VerifyResetCodeRequest(
    val email: String,
    val code: String
)

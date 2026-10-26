package com.pfe.mamacareplus.model.request.profile

data class ChangePasswordRequest(
    val currentPassword: String,
    val newPassword: String,
    val confirmPassword: String
)

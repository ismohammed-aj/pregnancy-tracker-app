package com.pfe.mamacareplus.api.auth

import com.pfe.mamacareplus.model.request.auth.LoginRequest
import com.pfe.mamacareplus.model.request.auth.RegisterRequest
import com.pfe.mamacareplus.model.request.auth.ForgotPasswordSendCodeRequest
import com.pfe.mamacareplus.model.request.auth.ResetPasswordRequest
import com.pfe.mamacareplus.model.request.auth.VerifyResetCodeRequest
import com.pfe.mamacareplus.model.response.auth.AuthResponse
import com.pfe.mamacareplus.model.response.auth.MessageResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface AuthApiService {

    @POST("auth/register")
    suspend fun register(
        @Body request: RegisterRequest
    ): Response<AuthResponse>

    @POST("auth/login")
    suspend fun login(
        @Body request: LoginRequest
    ): Response<AuthResponse>

    @POST("api/auth/forgot-password/send-code")
    suspend fun sendPasswordResetCode(
        @Body request: ForgotPasswordSendCodeRequest
    ): Response<MessageResponse>

    @POST("api/auth/forgot-password/verify-code")
    suspend fun verifyPasswordResetCode(
        @Body request: VerifyResetCodeRequest
    ): Response<MessageResponse>

    @POST("api/auth/forgot-password/reset-password")
    suspend fun resetPassword(
        @Body request: ResetPasswordRequest
    ): Response<MessageResponse>
}

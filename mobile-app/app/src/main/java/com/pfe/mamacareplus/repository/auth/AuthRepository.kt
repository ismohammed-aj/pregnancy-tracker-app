package com.pfe.mamacareplus.repository.auth

import android.content.Context
import com.pfe.mamacareplus.api.ApiClient
import com.pfe.mamacareplus.api.auth.AuthApiService
import com.pfe.mamacareplus.model.request.auth.ForgotPasswordSendCodeRequest
import com.pfe.mamacareplus.model.request.auth.LoginRequest
import com.pfe.mamacareplus.model.request.auth.RegisterRequest
import com.pfe.mamacareplus.model.request.auth.ResetPasswordRequest
import com.pfe.mamacareplus.model.request.auth.VerifyResetCodeRequest
import com.pfe.mamacareplus.model.response.auth.AuthResponse
import com.pfe.mamacareplus.model.response.auth.MessageResponse
import com.pfe.mamacareplus.utils.session.TokenManager
import org.json.JSONObject
import retrofit2.Response

class AuthRepository(context: Context) {

    private val tokenManager = TokenManager(context)

    private val authApiService: AuthApiService =
        ApiClient.create(context, includeAuth = false).create(AuthApiService::class.java)

    suspend fun login(email: String, password: String): Result<AuthResponse> {
        return try {
            val response = authApiService.login(
                LoginRequest(email = email, password = password)
            )

            if (response.isSuccessful && response.body() != null) {
                val authResponse = response.body()!!

                tokenManager.saveSession(
                    token = authResponse.token,
                    userId = authResponse.userId,
                    email = authResponse.email,
                    role = authResponse.role,
                    femmeId = authResponse.femmeId
                )

                Result.success(authResponse)
            } else {
                Result.failure(buildHttpError("Connexion", response))
            }

        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun register(
        email: String,
        password: String,
        username: String,
        birthDate: String?,
        avatar: String? = null
    ): Result<AuthResponse> {
        return try {
            val response = authApiService.register(
                RegisterRequest(
                    email = email,
                    password = password,
                    role = "FEMME",
                    username = username,
                    birthDate = birthDate,
                    avatar = avatar
                )
            )

            if (response.isSuccessful && response.body() != null) {
                val authResponse = response.body()!!

                Result.success(authResponse)
            } else {
                Result.failure(buildHttpError("Inscription", response))
            }

        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun sendPasswordResetCode(email: String): Result<String> {
        return try {
            val response = authApiService.sendPasswordResetCode(
                ForgotPasswordSendCodeRequest(email = email)
            )

            if (response.isSuccessful) {
                Result.success(readMessage(response, "Code envoyé par email."))
            } else {
                Result.failure(buildPasswordResetHttpError("Impossible d'envoyer le code.", response))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun verifyPasswordResetCode(email: String, code: String): Result<String> {
        return try {
            val response = authApiService.verifyPasswordResetCode(
                VerifyResetCodeRequest(email = email, code = code)
            )

            if (response.isSuccessful) {
                Result.success(readMessage(response, "Code vérifié."))
            } else {
                Result.failure(buildPasswordResetHttpError("Code incorrect ou expiré.", response))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun resetPassword(email: String, code: String, newPassword: String): Result<String> {
        return try {
            val response = authApiService.resetPassword(
                ResetPasswordRequest(
                    email = email,
                    code = code,
                    newPassword = newPassword
                )
            )

            if (response.isSuccessful) {
                Result.success(readMessage(response, "Mot de passe modifié avec succès"))
            } else {
                Result.failure(buildPasswordResetHttpError("Impossible de modifier le mot de passe.", response))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun logout() {
        tokenManager.clearSession()
    }

    fun isLoggedIn(): Boolean {
        return tokenManager.isLoggedIn()
    }

    private fun buildHttpError(action: String, response: Response<*>): Exception {
        val errorBody = runCatching { response.errorBody()?.string() }.getOrNull()
        val serverMessage = errorBody?.let { extractServerMessage(it) }
        val fallback = when (response.code()) {
            400 -> "$action impossible : vérifiez les informations."
            401, 403 -> "$action impossible : accès refusé."
            409 -> "$action impossible : cet email existe déjà."
            500 -> "$action impossible : erreur serveur. Vérifiez que le backend et la base de données fonctionnent."
            else -> "$action impossible (${response.code()})."
        }

        return Exception(serverMessage ?: fallback)
    }

    private fun buildPasswordResetHttpError(defaultMessage: String, response: Response<*>): Exception {
        val errorBody = runCatching { response.errorBody()?.string() }.getOrNull()
        val serverMessage = errorBody?.let { extractServerMessage(it) }
        return Exception(serverMessage ?: defaultMessage)
    }

    private fun readMessage(response: Response<MessageResponse>, fallback: String): String {
        return response.body()?.message?.takeIf { it.isNotBlank() } ?: fallback
    }

    private fun extractServerMessage(body: String): String? {
        val trimmed = body.trim()
        if (trimmed.isBlank()) return null

        if (trimmed.contains("Email already exists", ignoreCase = true)) {
            return "Cet email existe déjà. Essayez de vous connecter."
        }

        if (trimmed.contains("Invalid email or password", ignoreCase = true)) {
            return "Email ou mot de passe invalide."
        }

        return runCatching {
            val json = JSONObject(trimmed)
            json.optString("message")
                .ifBlank { json.optString("error") }
                .takeIf { it.isNotBlank() }
        }.getOrNull()
    }
}

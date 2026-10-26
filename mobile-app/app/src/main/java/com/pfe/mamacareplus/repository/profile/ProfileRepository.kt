package com.pfe.mamacareplus.repository.profile

import android.content.Context
import android.net.Uri
import android.webkit.MimeTypeMap
import com.pfe.mamacareplus.api.ApiClient
import com.pfe.mamacareplus.api.profile.ProfileApiService
import com.pfe.mamacareplus.model.request.profile.ChangePasswordRequest
import com.pfe.mamacareplus.model.request.profile.FemmeProfileRequest
import com.pfe.mamacareplus.model.response.profile.FemmeProfileResponse
import com.pfe.mamacareplus.utils.session.TokenManager
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject

class ProfileRepository(context: Context) {

    private val appContext = context.applicationContext
    private val tokenManager = TokenManager(context)

    private val api = ApiClient
        .create(context)
        .create(ProfileApiService::class.java)

    suspend fun getProfile(): Result<FemmeProfileResponse> {

        return try {

            val femmeId = resolveFemmeId()

            val response = api.getProfile(femmeId)

            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Impossible de charger le profil"))
            }

        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateProfile(
        username: String,
        birthDate: String?
    ): Result<FemmeProfileResponse> {
        return try {
            val femmeId = resolveFemmeId()

            val response = api.updateProfile(
                femmeId = femmeId,
                request = FemmeProfileRequest(
                    username = username,
                    birthDate = birthDate?.takeIf { it.isNotBlank() }
                )
            )

            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception(readError(response.code(), response.message(), response.errorBody()?.string())))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun uploadAvatar(uri: Uri): Result<FemmeProfileResponse> {
        return try {
            val femmeId = resolveFemmeId()

            val mimeType = appContext.contentResolver.getType(uri) ?: "image/jpeg"
            val bytes = appContext.contentResolver.openInputStream(uri)?.use { it.readBytes() }
                ?: return Result.failure(Exception("Impossible de lire l'image sélectionnée"))

            val extension = MimeTypeMap.getSingleton()
                .getExtensionFromMimeType(mimeType)
                ?.takeIf { it.isNotBlank() }
                ?: "jpg"

            val requestBody = bytes.toRequestBody(mimeType.toMediaTypeOrNull())
            val avatarPart = MultipartBody.Part.createFormData(
                name = "avatar",
                filename = "avatar_${System.currentTimeMillis()}.$extension",
                body = requestBody
            )

            val response = api.uploadAvatar(femmeId, avatarPart)

            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception(readError(response.code(), response.message(), response.errorBody()?.string())))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun changePassword(
        currentPassword: String,
        newPassword: String,
        confirmPassword: String
    ): Result<String> {
        return try {
            if (tokenManager.getToken().isNullOrBlank()) {
                return Result.failure(Exception("Session expirée. Veuillez vous reconnecter."))
            }

            val femmeId = resolveFemmeId()
            val response = api.changePassword(
                femmeId = femmeId,
                request = ChangePasswordRequest(
                    currentPassword = currentPassword,
                    newPassword = newPassword,
                    confirmPassword = confirmPassword
                )
            )

            if (response.isSuccessful) {
                Result.success(response.body()?.message?.takeIf { it.isNotBlank() } ?: "Mot de passe modifié avec succès")
            } else {
                Result.failure(Exception(readPasswordChangeError(response.code(), response.message(), response.errorBody()?.string())))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private suspend fun resolveFemmeId(): Int {
        tokenManager.getFemmeId()?.let { return it }

        val userId = tokenManager.getUserId()
        if (userId <= 0) {
            throw Exception("Aucune utilisatrice connectée")
        }

        val response = api.getProfileByUserId(userId)
        if (response.isSuccessful && response.body() != null) {
            val femmeId = response.body()!!.femmeId
            tokenManager.saveFemmeId(femmeId)
            return femmeId
        }

        throw Exception(readError(response.code(), response.message(), response.errorBody()?.string()))
    }

    private fun readError(code: Int, message: String, body: String?): String {
        val detail = body
            ?.takeIf { it.isNotBlank() }
            ?.let { extractServerMessage(it) }
            ?: message.takeIf { it.isNotBlank() }
        return if (detail.isNullOrBlank()) {
            "Requête impossible ($code)"
        } else {
            "Requête impossible ($code) : $detail"
        }
    }

    private fun readPasswordChangeError(code: Int, message: String, body: String?): String {
        val detail = body
            ?.takeIf { it.isNotBlank() }
            ?.let { extractServerMessage(it) }
            ?: message.takeIf { it.isNotBlank() }

        return when (code) {
            400 -> detail ?: "Vérifiez les champs du mot de passe puis réessayez."
            401, 403 -> "Session expirée. Veuillez vous reconnecter."
            404 -> "Profil introuvable. Veuillez actualiser votre session."
            else -> detail ?: "Impossible de modifier le mot de passe ($code)."
        }
    }

    private fun extractServerMessage(body: String): String {
        val trimmed = body.trim()
        return runCatching {
            val json = JSONObject(trimmed)
            json.optString("message")
                .ifBlank { json.optString("error") }
                .ifBlank { trimmed }
        }.getOrDefault(trimmed)
    }
}

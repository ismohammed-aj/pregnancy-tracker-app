package com.pfe.mamacareplus.repository.profile

import android.content.Context
import com.pfe.mamacareplus.api.ApiClient
import com.pfe.mamacareplus.api.profile.PregnancyApiService
import com.pfe.mamacareplus.model.request.profile.PregnancyProfileRequest
import com.pfe.mamacareplus.model.response.profile.PregnancyProfileResponse
import com.pfe.mamacareplus.utils.session.TokenManager

class PregnancyRepository(context: Context) {

    private val tokenManager = TokenManager(context)

    private val api = ApiClient
        .create(context)
        .create(PregnancyApiService::class.java)

    suspend fun hasPregnancyProfile(): Result<Boolean> {
        return try {
            val femmeId = tokenManager.getFemmeId()
                ?: return Result.failure(Exception("Identifiant utilisatrice introuvable"))

            val response = api.getProfileByFemmeId(femmeId)

            when {
                response.isSuccessful && response.body() != null -> Result.success(true)
                response.code() == 404 -> Result.success(false)
                else -> Result.failure(Exception("Impossible de vérifier le profil de grossesse"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getPregnancyProfile(): Result<PregnancyProfileResponse> {
        return try {
            val femmeId = tokenManager.getFemmeId()
                ?: return Result.failure(Exception("Identifiant utilisatrice introuvable"))

            val response = api.getProfileByFemmeId(femmeId)

            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Profil de grossesse introuvable"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun createPregnancyProfile(
        startDate: String,
        dueDate: String,
        healthStatus: String?,
        notes: String?
    ): Result<PregnancyProfileResponse> {
        return try {
            val femmeId = tokenManager.getFemmeId()
                ?: return Result.failure(Exception("Identifiant utilisatrice introuvable"))

            val response = api.createProfile(
                PregnancyProfileRequest(
                    femmeId = femmeId,
                    startDate = startDate,
                    dueDate = dueDate,
                    healthStatus = healthStatus,
                    notes = notes
                )
            )

            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Impossible de créer le profil de grossesse"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updatePregnancyProfile(
        profileId: Int,
        startDate: String,
        dueDate: String,
        healthStatus: String?,
        notes: String?
    ): Result<PregnancyProfileResponse> {
        return try {
            val femmeId = tokenManager.getFemmeId()
                ?: return Result.failure(Exception("Identifiant utilisatrice introuvable"))

            val response = api.updateProfile(
                profileId = profileId,
                request = PregnancyProfileRequest(
                    femmeId = femmeId,
                    startDate = startDate,
                    dueDate = dueDate,
                    healthStatus = healthStatus,
                    notes = notes
                )
            )

            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Impossible de modifier le profil de grossesse"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

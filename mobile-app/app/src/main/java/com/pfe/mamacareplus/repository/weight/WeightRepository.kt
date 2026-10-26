package com.pfe.mamacareplus.repository.weight

import android.content.Context
import com.pfe.mamacareplus.api.ApiClient
import com.pfe.mamacareplus.api.profile.PregnancyApiService
import com.pfe.mamacareplus.api.weight.WeightApiService
import com.pfe.mamacareplus.model.request.weight.WeightRecordRequest
import com.pfe.mamacareplus.model.response.weight.WeightRecordResponse
import com.pfe.mamacareplus.utils.session.TokenManager

class WeightRepository(context: Context) {

    private val tokenManager = TokenManager(context)

    private val weightApi = ApiClient
        .create(context)
        .create(WeightApiService::class.java)

    private val pregnancyApi = ApiClient
        .create(context)
        .create(PregnancyApiService::class.java)

    private suspend fun getPregnancyProfileId(): Int {
        val femmeId = tokenManager.getFemmeId()
            ?: throw Exception("Identifiant utilisatrice introuvable")

        val response = pregnancyApi.getProfileByFemmeId(femmeId)

        if (response.isSuccessful && response.body() != null) {
            return response.body()!!.pregnancyProfileId
        }

        throw Exception("Profil de grossesse introuvable")
    }

    suspend fun getWeightRecords(): Result<List<WeightRecordResponse>> {
        return try {
            val pregnancyProfileId = getPregnancyProfileId()

            val response = weightApi.getWeightRecordsByPregnancyProfileId(pregnancyProfileId)

            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Impossible de charger les poids"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun createWeightRecord(
        weightKg: Double,
        bmi: Double?,
        recordDate: String?,
        notes: String?
    ): Result<WeightRecordResponse> {
        return try {
            val pregnancyProfileId = getPregnancyProfileId()

            val response = weightApi.createWeightRecord(
                WeightRecordRequest(
                    pregnancyProfileId = pregnancyProfileId,
                    weightKg = weightKg,
                    bmi = bmi,
                    recordDate = recordDate,
                    notes = notes
                )
            )

            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Impossible d'ajouter le poids"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteWeightRecord(weightRecordId: Int): Result<Unit> {
        return try {
            val response = weightApi.deleteWeightRecord(weightRecordId)

            if (response.isSuccessful) {
                Result.success(Unit)
            } else {
                Result.failure(Exception("Impossible de supprimer le poids"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

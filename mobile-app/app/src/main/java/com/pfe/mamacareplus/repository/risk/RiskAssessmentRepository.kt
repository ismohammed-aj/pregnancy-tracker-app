package com.pfe.mamacareplus.repository.risk

import android.content.Context
import com.pfe.mamacareplus.api.ApiClient
import com.pfe.mamacareplus.api.risk.RiskAssessmentApiService
import com.pfe.mamacareplus.model.response.risk.RiskAssessmentResponse
import com.pfe.mamacareplus.utils.session.TokenManager

class RiskAssessmentRepository(context: Context) {

    private val tokenManager = TokenManager(context)

    private val api = ApiClient
        .create(context)
        .create(RiskAssessmentApiService::class.java)

    suspend fun getLatestRiskAssessment(): Result<RiskAssessmentResponse> {
        return try {
            val femmeId = tokenManager.getFemmeId()
                ?: return Result.failure(Exception("Identifiant utilisatrice introuvable"))

            val response = api.getLatestRiskAssessment(femmeId)

            when {
                response.isSuccessful && response.body() != null -> Result.success(response.body()!!)
                response.code() == 404 -> Result.failure(Exception("Aucune évaluation des risques pour le moment"))
                else -> Result.failure(Exception("Impossible de charger l'évaluation des risques"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

package com.pfe.mamacareplus.api.risk

import com.pfe.mamacareplus.model.response.risk.RiskAssessmentResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path

interface RiskAssessmentApiService {

    @GET("api/risk-assessments/femme/{femmeId}/latest")
    suspend fun getLatestRiskAssessment(
        @Path("femmeId") femmeId: Int
    ): Response<RiskAssessmentResponse>
}

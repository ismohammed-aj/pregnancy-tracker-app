package com.pfe.mamacareplus.model.response.risk

data class RiskAssessmentResponse(
    val riskAssessmentId: Int?,
    val femmeId: Int,
    val responseId: Int?,
    val totalScore: Int?,
    val riskLevel: String?,
    val description: String?,
    val recommendation: String?,
    val evaluatedAt: String?
)

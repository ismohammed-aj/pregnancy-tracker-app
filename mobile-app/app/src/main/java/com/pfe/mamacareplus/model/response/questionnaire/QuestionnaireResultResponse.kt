package com.pfe.mamacareplus.model.response.questionnaire

data class QuestionnaireResultResponse(
    val responseId: Int,
    val femmeId: Int,
    val questionnaireId: Int,
    val totalScore: Int,
    val riskLevel: String,
    val riskDescription: String?,
    val recommendation: String?,
    val completedAt: String?,
    val answers: List<AnswerResultResponse>
)

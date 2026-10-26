package com.pfe.mamacareplus.model.response.questionnaire

data class QuestionnaireResponse(
    val questionnaireId: Int,
    val title: String,
    val description: String?,
    val type: String?,
    val level: String?,
    val createdAt: String?,
    val questions: List<QuestionResponse>
)
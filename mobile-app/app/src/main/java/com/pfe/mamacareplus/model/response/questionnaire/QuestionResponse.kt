package com.pfe.mamacareplus.model.response.questionnaire

data class QuestionResponse(
    val questionId: Int,
    val questionnaireId: Int,
    val text: String,
    val type: String,
    val createdAt: String?,
    val options: List<OptionResponse>
)
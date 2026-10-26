package com.pfe.mamacareplus.model.response.questionnaire

data class AnswerResultResponse(
    val id: Int?,
    val answerId: Int?,
    val questionId: Int,
    val questionText: String,
    val optionId: Int?,
    val optionLabel: String?,
    val textAnswer: String?,
    val score: Int?
)

package com.pfe.mamacareplus.model.request.questionnaire

data class AnswerSubmitRequest(
    val questionId: Int,
    val optionId: Int? = null,
    val textAnswer: String? = null,
    val numberAnswer: Double? = null
)

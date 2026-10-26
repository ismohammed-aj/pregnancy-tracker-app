package com.pfe.mamacareplus.model.response.questionnaire

data class OptionResponse(
    val optionId: Int,
    val questionId: Int,
    val label: String,
    val scoreValue: Int?
)
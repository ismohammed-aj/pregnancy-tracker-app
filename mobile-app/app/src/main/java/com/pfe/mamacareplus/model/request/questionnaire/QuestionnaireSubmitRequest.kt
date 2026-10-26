package com.pfe.mamacareplus.model.request.questionnaire

data class QuestionnaireSubmitRequest(
    val femmeId: Int,
    val questionnaireId: Int,
    val answers: List<AnswerSubmitRequest>
)
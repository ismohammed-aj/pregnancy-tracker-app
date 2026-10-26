package com.pfe.mamacareplus.api.questionnaire

import com.pfe.mamacareplus.model.request.questionnaire.QuestionnaireSubmitRequest
import com.pfe.mamacareplus.model.response.questionnaire.QuestionnaireResponse
import com.pfe.mamacareplus.model.response.questionnaire.QuestionnaireResultResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface QuestionnaireApiService {

    @GET("api/questionnaires")
    suspend fun getAllQuestionnaires(): Response<List<QuestionnaireResponse>>

    @POST("questionnaire-responses/submit")
    suspend fun submitQuestionnaire(
        @Body request: QuestionnaireSubmitRequest
    ): Response<QuestionnaireResultResponse>
}
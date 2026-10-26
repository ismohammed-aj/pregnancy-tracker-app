package com.pfe.mamacareplus.repository.questionnaire

import android.content.Context
import com.pfe.mamacareplus.api.ApiClient
import com.pfe.mamacareplus.api.profile.PregnancyApiService
import com.pfe.mamacareplus.api.questionnaire.QuestionnaireApiService
import com.pfe.mamacareplus.model.request.profile.PregnancyProfileRequest
import com.pfe.mamacareplus.model.request.questionnaire.AnswerSubmitRequest
import com.pfe.mamacareplus.model.request.questionnaire.QuestionnaireSubmitRequest
import com.pfe.mamacareplus.model.response.questionnaire.AnswerResultResponse
import com.pfe.mamacareplus.model.response.questionnaire.QuestionnaireResponse
import com.pfe.mamacareplus.model.response.questionnaire.QuestionnaireResultResponse
import com.pfe.mamacareplus.utils.session.TokenManager
import java.text.Normalizer
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException

class QuestionnaireRepository(context: Context) {

    private val tokenManager = TokenManager(context)

    private val api = ApiClient
        .create(context)
        .create(QuestionnaireApiService::class.java)

    private val pregnancyApi = ApiClient
        .create(context)
        .create(PregnancyApiService::class.java)

    suspend fun getQuestionnaires(): Result<List<QuestionnaireResponse>> {
        return try {
            val response = api.getAllQuestionnaires()

            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Impossible de charger les questionnaires"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun submitQuestionnaire(
        questionnaireId: Int,
        answers: List<AnswerSubmitRequest>
    ): Result<QuestionnaireResultResponse> {
        return try {
            val femmeId = tokenManager.getFemmeId()
                ?: return Result.failure(Exception("Identifiant utilisatrice introuvable"))

            val response = api.submitQuestionnaire(
                QuestionnaireSubmitRequest(
                    femmeId = femmeId,
                    questionnaireId = questionnaireId,
                    answers = answers
                )
            )

            if (response.isSuccessful && response.body() != null) {
                val result = response.body()!!
                syncPregnancyProfile(result)
                Result.success(result)
            } else {
                val errorMessage = response.errorBody()?.string()
                    ?.takeIf { it.isNotBlank() }
                    ?: response.message()
                    ?: "Erreur serveur inconnue"

                Result.failure(
                    Exception("Envoi impossible (${response.code()}) : $errorMessage")
                )
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private suspend fun syncPregnancyProfile(result: QuestionnaireResultResponse) {
        val existingResponse = pregnancyApi.getProfileByFemmeId(result.femmeId)
        val existingProfile = existingResponse.body()
        val pregnancyDates = extractPregnancyDates(result.answers)

        val startDate = pregnancyDates?.startDate ?: existingProfile?.startDate ?: return
        val dueDate = pregnancyDates?.dueDate ?: existingProfile?.dueDate ?: return
        val request = PregnancyProfileRequest(
            femmeId = result.femmeId,
            startDate = startDate,
            dueDate = dueDate,
            healthStatus = existingProfile?.healthStatus ?: "Suivi grossesse",
            notes = "Questionnaire ${result.questionnaireId}: dates de grossesse synchronisees"
        )

        if (existingProfile != null) {
            pregnancyApi.updateProfile(existingProfile.pregnancyProfileId, request)
        } else {
            pregnancyApi.createProfile(request)
        }
    }

    private fun extractPregnancyDates(answers: List<AnswerResultResponse>): PregnancyDates? {
        var startDate: LocalDate? = null
        var dueDate: LocalDate? = null

        answers.forEach { answer ->
            val dateAnswer = parseDate(answer.textAnswer) ?: return@forEach
            val normalizedQuestion = normalizeText(answer.questionText)

            when {
                normalizedQuestion.contains("derniere") &&
                    normalizedQuestion.contains("regle") -> startDate = dateAnswer
                normalizedQuestion.contains("date prevue") &&
                    normalizedQuestion.contains("accouchement") -> dueDate = dateAnswer
                normalizedQuestion.contains("dpa") -> dueDate = dateAnswer
            }
        }

        if (startDate == null && dueDate == null) return null

        val resolvedStartDate = startDate ?: dueDate?.minusDays(PREGNANCY_DURATION_DAYS) ?: return null
        val resolvedDueDate = dueDate ?: startDate?.plusDays(PREGNANCY_DURATION_DAYS) ?: return null

        return PregnancyDates(
            startDate = resolvedStartDate.format(DateTimeFormatter.ISO_LOCAL_DATE),
            dueDate = resolvedDueDate.format(DateTimeFormatter.ISO_LOCAL_DATE)
        )
    }

    private fun parseDate(value: String?): LocalDate? {
        val cleanedValue = value?.trim()?.takeIf { it.isNotBlank() } ?: return null
        val formatters = listOf(
            DateTimeFormatter.ISO_LOCAL_DATE,
            DateTimeFormatter.ofPattern("dd/MM/yyyy"),
            DateTimeFormatter.ofPattern("d/M/yyyy"),
            DateTimeFormatter.ofPattern("dd-MM-yyyy"),
            DateTimeFormatter.ofPattern("d-M-yyyy")
        )

        for (formatter in formatters) {
            try {
                return LocalDate.parse(cleanedValue, formatter)
            } catch (_: DateTimeParseException) {
                // Try the next supported date format.
            }
        }

        return null
    }

    private fun normalizeText(value: String): String {
        return Normalizer.normalize(value.lowercase(), Normalizer.Form.NFD)
            .replace("\\p{Mn}+".toRegex(), "")
    }

    private data class PregnancyDates(
        val startDate: String,
        val dueDate: String
    )

    companion object {
        private const val PREGNANCY_DURATION_DAYS = 280L
    }
}

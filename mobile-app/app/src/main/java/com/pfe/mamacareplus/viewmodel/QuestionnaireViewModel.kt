package com.pfe.mamacareplus.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.pfe.mamacareplus.model.request.questionnaire.AnswerSubmitRequest
import com.pfe.mamacareplus.model.request.medication.MedicationReminderRequest
import com.pfe.mamacareplus.model.response.questionnaire.QuestionResponse
import com.pfe.mamacareplus.model.response.questionnaire.QuestionnaireResponse
import com.pfe.mamacareplus.model.response.questionnaire.QuestionnaireResultResponse
import com.pfe.mamacareplus.repository.medication.MedicationReminderRepository
import com.pfe.mamacareplus.repository.questionnaire.QuestionnaireRepository
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class QuestionnaireViewModel(
    application: Application
) : AndroidViewModel(application) {

    private val repository = QuestionnaireRepository(application)
    private val medicationReminderRepository = MedicationReminderRepository(application)

    private val _questionnaireState =
        MutableStateFlow<QuestionnaireUiState>(QuestionnaireUiState.Loading)

    val questionnaireState: StateFlow<QuestionnaireUiState> = _questionnaireState

    private val selectedAnswers = mutableMapOf<Int, Int>()
    private val textAnswers = mutableMapOf<Int, String>()

    fun loadQuestionnaires() {
        _questionnaireState.value = QuestionnaireUiState.Loading

        viewModelScope.launch {
            val result = repository.getQuestionnaires()

            _questionnaireState.value = result.fold(
                onSuccess = { questionnaires ->
                    if (questionnaires.isEmpty()) {
                        QuestionnaireUiState.Empty
                    } else {
                        QuestionnaireUiState.Loaded(questionnaires)
                    }
                },
                onFailure = {
                    QuestionnaireUiState.Error(it.message ?: "Impossible de charger les questionnaires")
                }
            )
        }
    }

    fun selectAnswer(questionId: Int, optionId: Int) {
        selectedAnswers[questionId] = optionId
        textAnswers.remove(questionId)
    }

    fun setTextAnswer(questionId: Int, answer: String) {
        val cleanedAnswer = answer.trim()
        if (cleanedAnswer.isBlank()) {
            textAnswers.remove(questionId)
        } else {
            textAnswers[questionId] = cleanedAnswer
        }
        selectedAnswers.remove(questionId)
    }

    fun submitQuestionnaire(
        questionnaire: QuestionnaireResponse,
        medicationReminderRequest: MedicationReminderRequest? = null
    ) {
        if (questionnaire.questions.any { question -> !hasAnswer(question) }) {
            _questionnaireState.value = QuestionnaireUiState.Error("Veuillez répondre à toutes les questions")
            return
        }

        _questionnaireState.value = QuestionnaireUiState.Submitting

        viewModelScope.launch {
            val answers = questionnaire.questions.map { question ->
                if (isNumberQuestion(question)) {
                    AnswerSubmitRequest(
                        questionId = question.questionId,
                        numberAnswer = parseNumberAnswer(textAnswers[question.questionId])
                    )
                } else if (requiresTextAnswer(question)) {
                    AnswerSubmitRequest(
                        questionId = question.questionId,
                        textAnswer = textAnswers[question.questionId]
                    )
                } else {
                    AnswerSubmitRequest(
                        questionId = question.questionId,
                        optionId = selectedAnswers[question.questionId]
                    )
                }
            }

            val result = repository.submitQuestionnaire(
                questionnaireId = questionnaire.questionnaireId,
                answers = answers
            )

            _questionnaireState.value = result.fold(
                onSuccess = { questionnaireResult ->
                    val medicationResult = medicationReminderRequest?.let {
                        medicationReminderRepository.createReminder(it)
                    }
                    val medicationSaved = medicationResult?.isSuccess == true
                    val medicationMessage = medicationResult?.exceptionOrNull()?.message

                    QuestionnaireUiState.Result(
                        result = questionnaireResult,
                        medicationSaved = medicationSaved,
                        medicationMessage = medicationMessage
                    )
                },
                onFailure = { QuestionnaireUiState.Error(it.message ?: "Envoi impossible") }
            )
        }
    }

    fun clearAnswers() {
        selectedAnswers.clear()
        textAnswers.clear()
    }

    private fun hasAnswer(question: QuestionResponse): Boolean {
        return when {
            isDateQuestion(question) -> isValidDateAnswer(textAnswers[question.questionId])
            isNumberQuestion(question) -> parseNumberAnswer(textAnswers[question.questionId]) != null
            requiresTextAnswer(question) -> !textAnswers[question.questionId].isNullOrBlank()
            else -> selectedAnswers.containsKey(question.questionId)
        }
    }

    private fun requiresTextAnswer(question: QuestionResponse): Boolean {
        val type = question.type.trim().lowercase()
        return type == "text" ||
            type == "date" ||
            type == "number"
    }

    private fun isDateQuestion(question: QuestionResponse): Boolean {
        return question.type.trim().lowercase() == "date"
    }

    private fun isNumberQuestion(question: QuestionResponse): Boolean {
        return question.type.trim().lowercase() == "number"
    }

    private fun parseNumberAnswer(value: String?): Double? {
        return value
            ?.trim()
            ?.replace(',', '.')
            ?.takeIf { it.isNotBlank() }
            ?.toDoubleOrNull()
    }

    private fun isValidDateAnswer(value: String?): Boolean {
        val cleanedValue = value?.trim()?.takeIf { it.isNotBlank() } ?: return false
        val formatters = listOf(
            DateTimeFormatter.ISO_LOCAL_DATE,
            DateTimeFormatter.ofPattern("dd/MM/yyyy"),
            DateTimeFormatter.ofPattern("d/M/yyyy"),
            DateTimeFormatter.ofPattern("dd-MM-yyyy"),
            DateTimeFormatter.ofPattern("d-M-yyyy")
        )

        return formatters.any { formatter ->
            try {
                LocalDate.parse(cleanedValue, formatter)
                true
            } catch (_: DateTimeParseException) {
                false
            }
        }
    }
}

sealed class QuestionnaireUiState {
    object Loading : QuestionnaireUiState()
    object Empty : QuestionnaireUiState()
    object Submitting : QuestionnaireUiState()

    data class Loaded(
        val questionnaires: List<QuestionnaireResponse>
    ) : QuestionnaireUiState()

    data class Result(
        val result: QuestionnaireResultResponse,
        val medicationSaved: Boolean = false,
        val medicationMessage: String? = null
    ) : QuestionnaireUiState()

    data class Error(
        val message: String
    ) : QuestionnaireUiState()
}

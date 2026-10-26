package com.pfe.mamacareplus.model.onboarding

data class OnboardingQuestion(
    val id: String,
    val phase: String,
    val title: String,
    val description: String,
    val type: QuestionType,
    val options: List<String> = emptyList(),
    val fieldName: String,
    val required: Boolean = true,
    val condition: QuestionCondition? = null,
    val unit: String? = null
)

enum class QuestionType {
    SINGLE_CHOICE,
    MULTI_CHOICE,
    TEXT_INPUT,
    NUMBER_INPUT,
    DATE_PICKER,
    TIME_PICKER,
    DATE_TIME_PICKER,
    YEAR_PICKER
}

data class QuestionCondition(
    val fieldName: String,
    val acceptedValues: List<String>
) {
    fun matches(answers: Map<String, Any?>): Boolean {
        val value = answers[fieldName] ?: return false
        return acceptedValues.any { expected ->
            value.toString().equals(expected, ignoreCase = true)
        }
    }
}

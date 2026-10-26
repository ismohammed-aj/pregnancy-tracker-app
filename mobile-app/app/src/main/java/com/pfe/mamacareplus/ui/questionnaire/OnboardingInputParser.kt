package com.pfe.mamacareplus.ui.questionnaire

internal object OnboardingInputParser {
    private const val MIN_HEIGHT_CM = 100
    private const val MAX_HEIGHT_CM = 230
    private const val MIN_WEIGHT_KG = 30.0
    private const val MAX_WEIGHT_KG = 250.0

    fun parseHeightCm(value: Any?): Int? {
        val cleaned = value
            ?.toString()
            ?.trim()
            ?.lowercase()
            ?.replace("cm", "")
            ?.trim()
            ?: return null

        if (cleaned.isBlank()) return null

        return cleaned.toIntOrNull()
    }

    fun parseValidHeightCm(value: Any?): Int? {
        val heightCm = parseHeightCm(value) ?: return null
        return heightCm.takeIf { it in MIN_HEIGHT_CM..MAX_HEIGHT_CM }
    }

    fun isValidHeightCm(value: Any?): Boolean {
        return parseValidHeightCm(value) != null
    }

    fun parseWeightKg(value: Any?): Double? {
        val cleaned = value
            ?.toString()
            ?.trim()
            ?.lowercase()
            ?.replace("kg", "")
            ?.replace(",", ".")
            ?.trim()
            ?: return null

        if (cleaned.isBlank()) return null

        return cleaned.toDoubleOrNull()
    }

    fun isValidWeightKg(value: Any?): Boolean {
        return parseValidWeightKg(value) != null
    }

    fun parseValidWeightKg(value: Any?): Double? {
        val weightKg = parseWeightKg(value) ?: return null
        return weightKg.takeIf { it in MIN_WEIGHT_KG..MAX_WEIGHT_KG }
    }
}

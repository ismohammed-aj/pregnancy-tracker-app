package com.pfe.mamacareplus.ui.questionnaire

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class OnboardingInputParserTest {

    @Test
    fun parseHeightCmAcceptsIntegerCentimetersOnlyInValidRange() {
        assertEquals(165, OnboardingInputParser.parseValidHeightCm("165"))
        assertEquals(170, OnboardingInputParser.parseValidHeightCm("170 cm"))
    }

    @Test
    fun parseHeightCmRejectsBlankAndOutOfRangeValues() {
        assertNull(OnboardingInputParser.parseValidHeightCm(""))
        assertFalse(OnboardingInputParser.isValidHeightCm(""))
        assertFalse(OnboardingInputParser.isValidHeightCm("99"))
        assertFalse(OnboardingInputParser.isValidHeightCm("231"))
    }

    @Test
    fun parseWeightKgAcceptsRequestedValidFormats() {
        assertEquals(60.0, OnboardingInputParser.parseValidWeightKg("60")!!, 0.0)
        assertEquals(60.5, OnboardingInputParser.parseValidWeightKg("60.5")!!, 0.0)
        assertEquals(60.5, OnboardingInputParser.parseValidWeightKg("60,5")!!, 0.0)
        assertEquals(75.0, OnboardingInputParser.parseValidWeightKg("75 kg")!!, 0.0)
    }

    @Test
    fun parseWeightKgRejectsBlankAndOutOfRangeValues() {
        assertNull(OnboardingInputParser.parseValidWeightKg(""))
        assertFalse(OnboardingInputParser.isValidWeightKg(""))
        assertFalse(OnboardingInputParser.isValidWeightKg("20"))
        assertFalse(OnboardingInputParser.isValidWeightKg("300"))
    }

    @Test
    fun parseWeightKgReturnsNumberForJsonPayload() {
        val weightKg = OnboardingInputParser.parseValidWeightKg("60,5")

        assertTrue(weightKg is Double)
        assertEquals(60.5, weightKg!!, 0.0)
    }
}

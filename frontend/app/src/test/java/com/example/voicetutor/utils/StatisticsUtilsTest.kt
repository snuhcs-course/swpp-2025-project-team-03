package com.example.voicetutor.utils

import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized

@RunWith(Parameterized::class)
class StatisticsUtilsTest(
    private val input: List<Boolean>,
    private val expectedActive: Float,
    private val expectedCompleted: Float
) {

    companion object {
        @JvmStatic
        @Parameterized.Parameters(name = "{index}: input={0} -> active={1}, completed={2}")
        fun data(): Collection<Array<Any>> = listOf(
            arrayOf(emptyList<Boolean>(), 0f, 0f),
            arrayOf(listOf(false), 1f, 0f),
            arrayOf(listOf(true), 0f, 1f),
            arrayOf(listOf(true, false), 0.5f, 0.5f),
            arrayOf(listOf(true, true, false, false), 0.5f, 0.5f),
            arrayOf(listOf(true, true, true, false), 0.25f, 0.75f)
        )
    }

    @Test
    fun getActiveAndCompletedStats_variousInputs_returnsExpectedRatios() {
        // Given
        val inputFlags = input

        // When
        val (activeRatio, completedRatio) = StatisticsUtils.getActiveAndCompletedStats(inputFlags)

        // Then
        assertEquals(expectedActive, activeRatio)
        assertEquals(expectedCompleted, completedRatio)
    }
}



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

    @Test
    fun getActiveAndCompletedStats_largeList_handlesCorrectly() {
        // Given - Large list with mixed values
        val largeList = List(1000) { it % 2 == 0 }

        // When
        val (activeRatio, completedRatio) = StatisticsUtils.getActiveAndCompletedStats(largeList)

        // Then
        assertEquals(0.5f, activeRatio, 0.01f)
        assertEquals(0.5f, completedRatio, 0.01f)
    }

    @Test
    fun getActiveAndCompletedStats_allTrue_returnsZeroActive() {
        // Given
        val allCompleted = listOf(true, true, true, true, true)

        // When
        val (activeRatio, completedRatio) = StatisticsUtils.getActiveAndCompletedStats(allCompleted)

        // Then
        assertEquals(0f, activeRatio)
        assertEquals(1f, completedRatio)
    }

    @Test
    fun getActiveAndCompletedStats_allFalse_returnsZeroCompleted() {
        // Given
        val allActive = listOf(false, false, false, false, false)

        // When
        val (activeRatio, completedRatio) = StatisticsUtils.getActiveAndCompletedStats(allActive)

        // Then
        assertEquals(1f, activeRatio)
        assertEquals(0f, completedRatio)
    }

    @Test
    fun getActiveAndCompletedStats_singleTrue_returnsCorrectRatios() {
        // Given
        val singleCompleted = listOf(true)

        // When
        val (activeRatio, completedRatio) = StatisticsUtils.getActiveAndCompletedStats(singleCompleted)

        // Then
        assertEquals(0f, activeRatio)
        assertEquals(1f, completedRatio)
    }

    @Test
    fun getActiveAndCompletedStats_singleFalse_returnsCorrectRatios() {
        // Given
        val singleActive = listOf(false)

        // When
        val (activeRatio, completedRatio) = StatisticsUtils.getActiveAndCompletedStats(singleActive)

        // Then
        assertEquals(1f, activeRatio)
        assertEquals(0f, completedRatio)
    }

    @Test
    fun getActiveAndCompletedStats_unevenDistribution_returnsCorrectRatios() {
        // Given - 3 completed, 1 active
        val unevenList = listOf(true, true, true, false)

        // When
        val (activeRatio, completedRatio) = StatisticsUtils.getActiveAndCompletedStats(unevenList)

        // Then
        assertEquals(0.25f, activeRatio)
        assertEquals(0.75f, completedRatio)
    }

    @Test
    fun getActiveAndCompletedStats_ratiosSumToOne() {
        // Given
        val testList = listOf(true, false, true, false, true, false, true)

        // When
        val (activeRatio, completedRatio) = StatisticsUtils.getActiveAndCompletedStats(testList)

        // Then - Ratios should sum to 1.0 (or close to it)
        assertEquals(1f, activeRatio + completedRatio, 0.001f)
    }
}



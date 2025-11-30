package com.example.voicetutor.ui.components

import org.junit.Assert.*
import org.junit.Test

/**
 * Unit tests for Card component enums.
 */
class CardTest {

    @Test
    fun cardVariant_enumValues_areCorrect() {
        assertEquals(5, CardVariant.entries.size)
        assertTrue(CardVariant.entries.contains(CardVariant.Default))
        assertTrue(CardVariant.entries.contains(CardVariant.Elevated))
        assertTrue(CardVariant.entries.contains(CardVariant.Outlined))
        assertTrue(CardVariant.entries.contains(CardVariant.Gradient))
        assertTrue(CardVariant.entries.contains(CardVariant.Selected))
    }

    @Test
    fun cardVariant_default_isFirst() {
        assertEquals(CardVariant.Default, CardVariant.entries[0])
    }

    @Test
    fun cardVariant_allVariants_haveUniqueNames() {
        val names = CardVariant.entries.map { it.name }
        assertEquals(names.size, names.distinct().size)
    }
}

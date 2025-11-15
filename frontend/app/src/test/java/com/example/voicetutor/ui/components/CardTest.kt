package com.example.voicetutor.ui.components

import org.junit.Test
import org.junit.Assert.*

/**
 * Unit tests for Card component enums.
 */
class CardTest {

    @Test
    fun cardVariant_enumValues_areCorrect() {
        assertEquals(5, CardVariant.values().size)
        assertTrue(CardVariant.values().contains(CardVariant.Default))
        assertTrue(CardVariant.values().contains(CardVariant.Elevated))
        assertTrue(CardVariant.values().contains(CardVariant.Outlined))
        assertTrue(CardVariant.values().contains(CardVariant.Gradient))
        assertTrue(CardVariant.values().contains(CardVariant.Selected))
    }

    @Test
    fun cardVariant_default_isFirst() {
        assertEquals(CardVariant.Default, CardVariant.values()[0])
    }

    @Test
    fun cardVariant_allVariants_haveUniqueNames() {
        val names = CardVariant.values().map { it.name }
        assertEquals(names.size, names.distinct().size)
    }
}


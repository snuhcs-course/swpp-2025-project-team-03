package com.example.voicetutor.ui.components

import org.junit.Assert.*
import org.junit.Test

/**
 * Unit tests for Button component enums and logic.
 */
class ButtonTest {

    @Test
    fun buttonVariant_enumValues_areCorrect() {
        assertEquals(7, ButtonVariant.values().size)
        assertTrue(ButtonVariant.values().contains(ButtonVariant.Primary))
        assertTrue(ButtonVariant.values().contains(ButtonVariant.Secondary))
        assertTrue(ButtonVariant.values().contains(ButtonVariant.Outline))
        assertTrue(ButtonVariant.values().contains(ButtonVariant.Outlined))
        assertTrue(ButtonVariant.values().contains(ButtonVariant.Ghost))
        assertTrue(ButtonVariant.values().contains(ButtonVariant.Gradient))
        assertTrue(ButtonVariant.values().contains(ButtonVariant.Danger))
    }

    @Test
    fun buttonSize_enumValues_areCorrect() {
        assertEquals(3, ButtonSize.values().size)
        assertTrue(ButtonSize.values().contains(ButtonSize.Small))
        assertTrue(ButtonSize.values().contains(ButtonSize.Medium))
        assertTrue(ButtonSize.values().contains(ButtonSize.Large))
    }

    @Test
    fun buttonVariant_primary_isFirst() {
        assertEquals(ButtonVariant.Primary, ButtonVariant.values()[0])
    }

    @Test
    fun buttonSize_medium_isDefault() {
        assertEquals(ButtonSize.Medium, ButtonSize.values()[1])
    }
}

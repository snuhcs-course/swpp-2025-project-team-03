package com.example.voicetutor.ui.components

import org.junit.Assert.*
import org.junit.Test

/**
 * Unit tests for Button component enums and logic.
 */
class ButtonTest {

    @Test
    fun buttonVariant_enumValues_areCorrect() {
        assertEquals(7, ButtonVariant.entries.size)
        assertTrue(ButtonVariant.entries.contains(ButtonVariant.Primary))
        assertTrue(ButtonVariant.entries.contains(ButtonVariant.Secondary))
        assertTrue(ButtonVariant.entries.contains(ButtonVariant.Outline))
        assertTrue(ButtonVariant.entries.contains(ButtonVariant.Outlined))
        assertTrue(ButtonVariant.entries.contains(ButtonVariant.Ghost))
        assertTrue(ButtonVariant.entries.contains(ButtonVariant.Gradient))
        assertTrue(ButtonVariant.entries.contains(ButtonVariant.Danger))
    }

    @Test
    fun buttonSize_enumValues_areCorrect() {
        assertEquals(3, ButtonSize.entries.size)
        assertTrue(ButtonSize.entries.contains(ButtonSize.Small))
        assertTrue(ButtonSize.entries.contains(ButtonSize.Medium))
        assertTrue(ButtonSize.entries.contains(ButtonSize.Large))
    }

    @Test
    fun buttonVariant_primary_isFirst() {
        assertEquals(ButtonVariant.Primary, ButtonVariant.entries[0])
    }

    @Test
    fun buttonSize_medium_isDefault() {
        assertEquals(ButtonSize.Medium, ButtonSize.entries[1])
    }
}

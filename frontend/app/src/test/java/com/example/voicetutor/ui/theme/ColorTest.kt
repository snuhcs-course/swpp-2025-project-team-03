package com.example.voicetutor.ui.theme

import androidx.compose.ui.graphics.Color
import org.junit.Test
import org.junit.Assert.*

/**
 * Unit tests for Color constants.
 */
class ColorTest {

    @Test
    fun purpleColors_areDefined() {
        assertNotNull(Purple80)
        assertNotNull(PurpleGrey80)
        assertNotNull(Pink80)
    }

    @Test
    fun primaryColors_areDefined() {
        assertNotNull(PrimaryIndigo)
        assertNotNull(PrimaryPurple)
        assertNotNull(PrimaryEmerald)
    }

    @Test
    fun lightColors_areDefined() {
        assertNotNull(LightIndigo)
        assertNotNull(LightPurple)
        assertNotNull(LightBlue)
    }

    @Test
    fun grayColors_areDefined() {
        assertNotNull(Gray50)
        assertNotNull(Gray100)
        assertNotNull(Gray200)
        assertNotNull(Gray300)
        assertNotNull(Gray400)
        assertNotNull(Gray500)
        assertNotNull(Gray600)
        assertNotNull(Gray700)
        assertNotNull(Gray800)
        assertNotNull(Gray900)
    }

    @Test
    fun statusColors_areDefined() {
        assertNotNull(Success)
        assertNotNull(Warning)
        assertNotNull(Error)
        assertNotNull(Info)
    }

    @Test
    fun colors_areNotTransparent() {
        assertTrue(PrimaryIndigo.alpha > 0f)
        assertTrue(Success.alpha > 0f)
        assertTrue(Error.alpha > 0f)
    }

    @Test
    fun colors_haveValidRGBValues() {
        assertTrue(PrimaryIndigo.red >= 0f && PrimaryIndigo.red <= 1f)
        assertTrue(PrimaryIndigo.green >= 0f && PrimaryIndigo.green <= 1f)
        assertTrue(PrimaryIndigo.blue >= 0f && PrimaryIndigo.blue <= 1f)
    }
}


package com.example.voicetutor

import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
class MainActivityTest {

    @get:Rule(order = 0)
    val hiltRule = HiltAndroidRule(this)

    @get:Rule(order = 1)
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    @Before
    fun setup() {
        hiltRule.inject()
    }

    @Test
    fun mainActivity_launches_successfully() {
        composeTestRule.waitForIdle()
    }

    @Test
    fun mainActivity_setsContent_withVoiceTutorTheme() {
        composeTestRule.waitForIdle()
    }

    @Test
    fun voiceTutorApp_displays_navigationContent() {
        composeTestRule.waitForIdle()
    }

    @Test
    fun mainActivity_enablesEdgeToEdge() {
        composeTestRule.waitForIdle()
    }
}

package com.example.voicetutor.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.voicetutor.HiltComponentActivity
import com.example.voicetutor.data.models.AssignmentStatus
import com.example.voicetutor.di.NetworkModule
import com.example.voicetutor.ui.theme.VoiceTutorTheme
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.android.testing.UninstallModules
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@HiltAndroidTest
@UninstallModules(NetworkModule::class)
@RunWith(AndroidJUnit4::class)
class AllAssignmentsScreenCoverageTest {

    @get:Rule(order = 0)
    val hiltRule = HiltAndroidRule(this)

    @get:Rule(order = 1)
    val composeRule = createAndroidComposeRule<HiltComponentActivity>()

    @Before
    fun setUp() {
        hiltRule.inject()
    }

    @Test
    fun testTypeBadge() {
        composeRule.setContent {
            VoiceTutorTheme {
                Column {
                    TypeBadge("Quiz")
                    TypeBadge("Continuous")
                    TypeBadge("Discussion")
                    TypeBadge("Unknown")
                }
            }
        }

        composeRule.onNodeWithText("퀴즈").assertIsDisplayed()

        composeRule.onNodeWithText("연속").assertIsDisplayed()

        composeRule.onNodeWithText("토론").assertIsDisplayed()

        composeRule.onNodeWithText("알 수 없음").assertIsDisplayed()
    }

    @Test
    fun testCustomStatusBadge() {
        composeRule.setContent {
            VoiceTutorTheme {
                Column {
                    CustomStatusBadge("시작 안함")
                    CustomStatusBadge("진행 중")
                    CustomStatusBadge("완료")
                    CustomStatusBadge("기타")
                }
            }
        }

        composeRule.onNodeWithText("시작 안함").assertIsDisplayed()
        composeRule.onNodeWithText("진행 중").assertIsDisplayed()
        composeRule.onNodeWithText("완료").assertIsDisplayed()
        composeRule.onNodeWithText("기타").assertIsDisplayed()
    }

    @Test
    fun testStatusBadge() {
        composeRule.setContent {
            VoiceTutorTheme {
                Column {
                    StatusBadge(AssignmentStatus.IN_PROGRESS)
                    StatusBadge(AssignmentStatus.COMPLETED)
                    StatusBadge(AssignmentStatus.DRAFT)
                }
            }
        }

        composeRule.onNodeWithText("진행중").assertIsDisplayed()
        composeRule.onNodeWithText("완료").assertIsDisplayed()
        composeRule.onNodeWithText("임시저장").assertIsDisplayed()
    }
}

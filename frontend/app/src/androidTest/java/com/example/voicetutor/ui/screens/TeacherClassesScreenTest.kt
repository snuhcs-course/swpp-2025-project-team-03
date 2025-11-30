package com.example.voicetutor.ui.screens

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithText
import androidx.lifecycle.ViewModelProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.voicetutor.HiltComponentActivity
import com.example.voicetutor.data.network.ApiService
import com.example.voicetutor.data.network.FakeApiService
import com.example.voicetutor.di.NetworkModule
import com.example.voicetutor.ui.theme.VoiceTutorTheme
import com.example.voicetutor.ui.viewmodel.AssignmentViewModel
import com.example.voicetutor.ui.viewmodel.AuthViewModel
import com.example.voicetutor.ui.viewmodel.ClassViewModel
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.android.testing.UninstallModules
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import javax.inject.Inject

@HiltAndroidTest
@UninstallModules(NetworkModule::class)
@RunWith(AndroidJUnit4::class)
class TeacherClassesScreenTest {

    @get:Rule(order = 0)
    val hiltRule = HiltAndroidRule(this)

    @get:Rule(order = 1)
    val composeRule = createAndroidComposeRule<HiltComponentActivity>()

    @Inject
    lateinit var apiService: ApiService

    private val fakeApi: FakeApiService
        get() = apiService as FakeApiService

    @Before
    fun setUp() {
        hiltRule.inject()
        fakeApi.apply {
            shouldFailGetAllAssignments = false
            shouldFailDashboardStats = false
            shouldFailPersonalAssignments = false
        }
    }

    private fun waitForText(text: String, timeoutMillis: Long = 10_000) {
        composeRule.waitUntil(timeoutMillis = timeoutMillis) {
            composeRule.onAllNodesWithText(text, substring = true, useUnmergedTree = true)
                .fetchSemanticsNodes()
                .isNotEmpty()
        }
    }

    @Test
    fun teacherClassesScreen_displaysClassCardsAndActions() {
        composeRule.setContent {
            VoiceTutorTheme {
                TeacherClassesScreen()
            }
        }

        val classViewModel = ViewModelProvider(composeRule.activity)[ClassViewModel::class.java]
        val assignmentViewModel = ViewModelProvider(composeRule.activity)[AssignmentViewModel::class.java]
        val authViewModel = ViewModelProvider(composeRule.activity)[AuthViewModel::class.java]

        composeRule.runOnIdle {
            authViewModel.login("teacher@voicetutor.com", "teacher123")
        }

        composeRule.waitUntil(timeoutMillis = 10_000) {
            authViewModel.currentUser.value != null &&
                classViewModel.classes.value.isNotEmpty() &&
                assignmentViewModel.assignments.value.isNotEmpty()
        }

        waitForText("수업 관리")
        composeRule.onNodeWithText("수업 목록", useUnmergedTree = true).assertIsDisplayed()
        composeRule.onNodeWithText("수업 생성", useUnmergedTree = true).assertIsDisplayed()
        waitForText("수학 A반")
        composeRule.onNodeWithText("수학 A반", useUnmergedTree = true).assertIsDisplayed()
    }
}

package com.example.voicetutor.ui.screens

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.voicetutor.HiltComponentActivity
import com.example.voicetutor.data.models.Student
import com.example.voicetutor.data.models.UserRole
import com.example.voicetutor.data.network.ApiService
import com.example.voicetutor.data.network.FakeApiService
import com.example.voicetutor.di.NetworkModule
import com.example.voicetutor.ui.theme.VoiceTutorTheme
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
class TeacherStudentsScreenCoverageTest {

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

        fakeApi.classStudentsResponse = listOf(
            Student(id = 1, name = "Student1", email = "s1@test.com", role = UserRole.STUDENT),
            Student(id = 2, name = "Student2", email = "s2@test.com", role = UserRole.STUDENT),
        )
    }

    @Test
    fun testDeleteStudentDialog() {
        composeRule.setContent {
            VoiceTutorTheme {
                TeacherStudentsScreen(classId = 1, teacherId = "2")
            }
        }

        composeRule.onNodeWithText("학생 삭제").performClick()
        composeRule.waitForIdle()

        composeRule.onAllNodes(isToggleable()).onFirst().performClick()
        composeRule.waitForIdle()

        composeRule.onAllNodesWithText("삭제")
            .filter(hasClickAction())
            .onFirst()
            .performClick()

        composeRule.waitForIdle()

        composeRule.onNodeWithText("학생 제거").assertIsDisplayed()
        composeRule.onNodeWithText("선택한 1명의 학생을 이 반에서 제거하시겠습니까?", substring = true).assertIsDisplayed()

        composeRule.onNodeWithText("제거").performClick()
        composeRule.waitForIdle()
    }
}

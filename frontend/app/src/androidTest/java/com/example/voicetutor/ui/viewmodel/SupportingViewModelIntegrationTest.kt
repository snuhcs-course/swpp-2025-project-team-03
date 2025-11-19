package com.example.voicetutor.ui.viewmodel

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.voicetutor.data.models.ClassStudentsStatistics
import com.example.voicetutor.data.models.EnrollmentData
import com.example.voicetutor.data.network.ApiResponse
import com.example.voicetutor.data.network.ApiService
import com.example.voicetutor.data.network.FakeApiService
import com.example.voicetutor.data.repository.ClassRepository
import com.example.voicetutor.data.repository.DashboardRepository
import com.example.voicetutor.data.repository.ReportRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import retrofit2.Response

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(AndroidJUnit4::class)
class SupportingViewModelIntegrationTest {

    private lateinit var dispatcher: TestDispatcher
    private lateinit var reportViewModel: ReportViewModel
    private lateinit var dashboardViewModel: DashboardViewModel
    private lateinit var classViewModel: ClassViewModel
    private lateinit var apiService: FakeApiService

    @Before
    fun setUp() {
        dispatcher = StandardTestDispatcher()
        Dispatchers.setMain(dispatcher)
        apiService = FakeApiService()
        reportViewModel = ReportViewModel(ReportRepository(apiService))
        dashboardViewModel = DashboardViewModel(DashboardRepository(apiService))
        classViewModel = ClassViewModel(ClassRepository(apiService))
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun loadCurriculumReport_populatesData() = runTest(dispatcher) {
        reportViewModel.loadCurriculumReport(classId = 1, studentId = 1)
        advanceUntilIdle()

        assertNotNull(reportViewModel.curriculumReport.value)
        assertEquals(null, reportViewModel.error.value)
    }

    @Test
    fun loadDashboardStats_populatesData() = runTest(dispatcher) {
        dashboardViewModel.loadDashboardData(teacherId = "2")
        advanceUntilIdle()

        assertNotNull(dashboardViewModel.dashboardStats.value)
        assertEquals(null, dashboardViewModel.error.value)
    }

    @Test
    fun classViewModel_loadsAndUpdatesStudents() = runTest(dispatcher) {
        classViewModel.loadClasses(teacherId = "2")
        advanceUntilIdle()
        assertTrue(classViewModel.classes.value.isNotEmpty())

        classViewModel.loadClassById(1)
        advanceUntilIdle()
        assertNotNull(classViewModel.currentClass.value)

        classViewModel.loadClassStudents(1)
        advanceUntilIdle()
        assertTrue(classViewModel.classStudents.value.isNotEmpty())

        var statisticsResult: Result<ClassStudentsStatistics>? = null
        classViewModel.loadClassStudentsStatistics(1) { result ->
            statisticsResult = result
        }
        advanceUntilIdle()
        assertTrue(statisticsResult?.isSuccess == true)

        classViewModel.enrollStudentToClass(classId = 1, studentId = 3)
        advanceUntilIdle()
        assertEquals(null, classViewModel.error.value)

        classViewModel.removeStudentFromClass(classId = 1, studentId = 3)
        advanceUntilIdle()
        assertEquals(null, classViewModel.error.value)
    }

    @Test
    fun failingLoadClasses_setsError() = runTest(dispatcher) {
        val failingClassViewModel = ClassViewModel(
            ClassRepository(
                object : ApiService by FakeApiService() {
                    override suspend fun getClasses(teacherId: String): Response<ApiResponse<List<com.example.voicetutor.data.models.ClassData>>> {
                        return Response.success(
                            ApiResponse(
                                success = false,
                                data = null,
                                message = null,
                                error = "class error",
                            ),
                        )
                    }

                    override suspend fun enrollStudentToClass(
                        id: Int,
                        studentId: Int,
                    ): Response<ApiResponse<EnrollmentData>> {
                        return Response.success(
                            ApiResponse(
                                success = false,
                                data = null,
                                message = null,
                                error = "enroll error",
                            ),
                        )
                    }

                    override suspend fun removeStudentFromClass(
                        id: Int,
                        student_id: Int,
                    ): Response<ApiResponse<Unit>> {
                        return Response.success(
                            ApiResponse(
                                success = false,
                                data = null,
                                message = null,
                                error = "remove error",
                            ),
                        )
                    }
                },
            ),
        )

        failingClassViewModel.loadClasses("2")
        advanceUntilIdle()
        assertEquals("class error", failingClassViewModel.error.value)

        failingClassViewModel.enrollStudentToClass(classId = 1, studentId = 3)
        advanceUntilIdle()
        assertEquals("enroll error", failingClassViewModel.error.value)

        failingClassViewModel.removeStudentFromClass(classId = 1, studentId = 3)
        advanceUntilIdle()
        assertEquals("remove error", failingClassViewModel.error.value)
    }

    @Test
    fun loadDashboardStats_failure_setsError() = runTest(dispatcher) {
        apiService.shouldFailDashboardStats = true
        apiService.dashboardStatsErrorMessage = "대시보드 실패"

        dashboardViewModel.loadDashboardData(teacherId = "2")
        advanceUntilIdle()

        assertEquals("대시보드 실패", dashboardViewModel.error.value)
    }

    @Test
    fun loadCurriculumReport_failure_setsError() = runTest(dispatcher) {
        apiService.shouldFailCurriculumReport = true
        apiService.curriculumReportErrorMessage = "리포트 실패"

        reportViewModel.loadCurriculumReport(classId = 1, studentId = 1)
        advanceUntilIdle()

        assertEquals("리포트 실패", reportViewModel.error.value)
    }
}

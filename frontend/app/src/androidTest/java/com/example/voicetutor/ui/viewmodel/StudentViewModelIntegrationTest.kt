package com.example.voicetutor.ui.viewmodel

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.voicetutor.data.network.ApiResponse
import com.example.voicetutor.data.network.ApiService
import com.example.voicetutor.data.network.FakeApiService
import com.example.voicetutor.data.repository.StudentRepository
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
class StudentViewModelIntegrationTest {

    private lateinit var dispatcher: TestDispatcher
    private lateinit var viewModel: StudentViewModel
    private lateinit var apiService: FakeApiService

    @Before
    fun setUp() {
        dispatcher = StandardTestDispatcher()
        Dispatchers.setMain(dispatcher)
        apiService = FakeApiService()
        viewModel = StudentViewModel(StudentRepository(apiService))
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun loadAllStudents_updatesState() = runTest(dispatcher) {
        viewModel.loadAllStudents(teacherId = "2")
        advanceUntilIdle()

        assertTrue(viewModel.students.value.isNotEmpty())
        assertEquals(null, viewModel.error.value)
    }

    @Test
    fun loadStudentById_setsCurrentStudent() = runTest(dispatcher) {
        viewModel.loadStudentById(1)
        advanceUntilIdle()

        assertNotNull(viewModel.currentStudent.value)
        assertEquals(null, viewModel.error.value)
    }


    @Test
    fun failingLoadAllStudents_setsError() = runTest(dispatcher) {
        val failingViewModel = StudentViewModel(
            StudentRepository(object : ApiService by FakeApiService() {
                override suspend fun getAllStudents(
                    teacherId: String?,
                    classId: String?,
                ): Response<ApiResponse<List<com.example.voicetutor.data.models.Student>>> {
                    return Response.success(
                        ApiResponse(
                            success = false,
                            data = null,
                            message = null,
                            error = "student error",
                        ),
                    )
                }
            }),
        )

        failingViewModel.loadAllStudents()
        advanceUntilIdle()

        assertEquals("student error", failingViewModel.error.value)
    }
}

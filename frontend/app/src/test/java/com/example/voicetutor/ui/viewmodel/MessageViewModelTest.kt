package com.example.voicetutor.ui.viewmodel

import app.cash.turbine.test
import com.example.voicetutor.data.models.Message
import com.example.voicetutor.data.models.SendMessageResponse
import com.example.voicetutor.data.models.MessageListResponse
import com.example.voicetutor.data.repository.MessageRepository
import com.example.voicetutor.testing.MainDispatcherRule
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.Mockito.times
import org.mockito.junit.MockitoJUnitRunner

@RunWith(MockitoJUnitRunner::class)
class MessageViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Mock
    lateinit var messageRepository: MessageRepository

    private lateinit var viewModel: MessageViewModel

    @Before
    fun setUp() {
        viewModel = MessageViewModel(messageRepository)
    }

    @Test
    fun messages_initialState_emitsEmptyList() = runTest {
        // Given: 새로 생성된 ViewModel
        // When: 초기 상태를 관측하면
        viewModel.messages.test {
            // Then: 첫 방출이 emptyList 여야 한다
            assert(awaitItem().isEmpty())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun sendMessage_success_updatesSendMessageResult() = runTest {
        // Given: 메시지 전송 성공
        val response = SendMessageResponse(
            messageId = "123",
            sentCount = 2,
            failedCount = 0,
            timestamp = System.currentTimeMillis()
        )
        Mockito.`when`(messageRepository.sendMessage(1, listOf(1, 2), "Hello"))
            .thenReturn(Result.success(response))

        // When
        viewModel.sendMessageResult.test {
            assert(awaitItem() == null)
            
            viewModel.sendMessage(teacherId = 1, studentIds = listOf(1, 2), message = "Hello")
            runCurrent()

            // Then
            assert(awaitItem() == response)
            cancelAndIgnoreRemainingEvents()
        }

        Mockito.verify(messageRepository, times(1)).sendMessage(1, listOf(1, 2), "Hello")
    }

    @Test
    fun sendMessage_failure_setsError() = runTest {
        // Given: 메시지 전송 실패
        Mockito.`when`(messageRepository.sendMessage(1, listOf(1), "Hello"))
            .thenReturn(Result.failure(Exception("Network error")))

        // When
        viewModel.error.test {
            awaitItem() // initial null
            
            viewModel.sendMessage(teacherId = 1, studentIds = listOf(1), message = "Hello")
            runCurrent()

            // Then
            val error = awaitItem()
            assert(error?.contains("Network error") == true)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun loadMessages_success_updatesMessages() = runTest {
        // Given: 메시지 목록 반환
        val messages = listOf(
            Message(
                id = "1",
                teacherId = 1,
                teacherName = "Teacher",
                studentId = 2,
                studentName = "Student",
                content = "Hello",
                timestamp = System.currentTimeMillis()
            ),
            Message(
                id = "2",
                teacherId = 1,
                teacherName = "Teacher",
                studentId = 2,
                studentName = "Student",
                content = "World",
                timestamp = System.currentTimeMillis()
            )
        )
        val messageResponse = MessageListResponse(messages = messages, totalCount = 2)
        Mockito.`when`(messageRepository.getMessages(1, null, 50, 0))
            .thenReturn(Result.success(messageResponse))

        // When
        viewModel.messages.test {
            assert(awaitItem().isEmpty())
            
            viewModel.loadMessages(userId = 1)
            runCurrent()

            // Then
            assert(awaitItem() == messages)
            cancelAndIgnoreRemainingEvents()
        }

        Mockito.verify(messageRepository, times(1)).getMessages(1, null, 50, 0)
    }

    @Test
    fun loadMessages_withFilters_success_updatesMessages() = runTest {
        // Given: 필터링된 메시지
        val messages = listOf(
            Message(
                id = "1",
                teacherId = 1,
                teacherName = "Teacher",
                studentId = 2,
                studentName = "Student",
                content = "Filtered",
                timestamp = System.currentTimeMillis()
            )
        )
        val messageResponse = MessageListResponse(messages = messages, totalCount = 1)
        Mockito.`when`(messageRepository.getMessages(1, "NOTIFICATION", 20, 10))
            .thenReturn(Result.success(messageResponse))

        // When
        viewModel.loadMessages(userId = 1, messageType = "NOTIFICATION", limit = 20, offset = 10)
        runCurrent()

        // Then
        viewModel.messages.test {
            assert(awaitItem() == messages)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun isLoading_loadingOperation_setsTrueThenFalse() = runTest {
        // Given
        val messageResponse = MessageListResponse(messages = emptyList(), totalCount = 0)
        Mockito.`when`(messageRepository.getMessages(1, null, 50, 0))
            .thenReturn(Result.success(messageResponse))

        // When
        viewModel.isLoading.test {
            assert(!awaitItem()) // initial false
            
            viewModel.loadMessages(1)
            runCurrent()

            // Then: 로딩 상태 변경 확인
            val states = listOf(awaitItem(), awaitItem())
            assert(states.contains(true))
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun clearSendMessageResult_clearsResult() = runTest {
        // Given: 메시지 전송 성공 후 결과 설정
        val response = SendMessageResponse(
            messageId = "1",
            sentCount = 1,
            failedCount = 0,
            timestamp = System.currentTimeMillis()
        )
        Mockito.`when`(messageRepository.sendMessage(1, listOf(1), "Test"))
            .thenReturn(Result.success(response))

        viewModel.sendMessageResult.test {
            awaitItem()
            viewModel.sendMessage(1, listOf(1), "Test")
            runCurrent()
            assert(awaitItem() != null)
            
            // When: clearSendMessageResult 호출
            viewModel.clearSendMessageResult()
            
            // Then: 결과가 null로 변경
            assert(awaitItem() == null)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun clearError_clearsErrorState() = runTest {
        // Given: 에러 발생
        Mockito.`when`(messageRepository.sendMessage(1, listOf(1), "Test"))
            .thenReturn(Result.failure(Exception("Error")))

        viewModel.error.test {
            awaitItem()
            viewModel.sendMessage(1, listOf(1), "Test")
            runCurrent()
            assert(awaitItem() != null)
            
            // When: clearError 호출
            viewModel.clearError()
            
            // Then: 에러가 null로 변경
            assert(awaitItem() == null)
            cancelAndIgnoreRemainingEvents()
        }
    }
}


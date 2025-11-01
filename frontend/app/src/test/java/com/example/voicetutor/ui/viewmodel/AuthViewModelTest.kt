package com.example.voicetutor.ui.viewmodel

import app.cash.turbine.test
import com.example.voicetutor.data.models.User
import com.example.voicetutor.data.models.UserRole
import com.example.voicetutor.data.repository.AuthRepository
import com.example.voicetutor.testing.MainDispatcherRule
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.Mockito.times
import org.mockito.junit.MockitoJUnitRunner

@RunWith(MockitoJUnitRunner::class)
class AuthViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Mock
    lateinit var authRepository: AuthRepository

    @Test
    fun initialStates_areSane_withoutCrashing() = runTest {
        val vm = AuthViewModel(authRepository)
        vm.currentUser.test { val u = awaitItem(); assert(u == null); cancelAndIgnoreRemainingEvents() }
        vm.isLoggedIn.test { val logged = awaitItem(); assert(!logged); cancelAndIgnoreRemainingEvents() }
        vm.error.test { val e = awaitItem(); assert(e == null); cancelAndIgnoreRemainingEvents() }
    }

    @Test
    fun signup_success_setsAutoFillAndCurrentUser() = runTest {
        // Given
        val vm = AuthViewModel(authRepository)
        val user = User(id = 1, name = "Alice", email = "a@ex.com", role = UserRole.STUDENT)
        Mockito.`when`(authRepository.signup("Alice", "a@ex.com", "pw", UserRole.STUDENT))
            .thenReturn(Result.success(user))

        // When
        vm.signup("Alice", "a@ex.com", "pw", UserRole.STUDENT)
        advanceUntilIdle()
        vm.autoFillCredentials.test { val creds = awaitItem(); assert(creds == ("a@ex.com" to "pw")); cancelAndIgnoreRemainingEvents() }

        // Collect after action: first emission is the latest state
        vm.currentUser.test {
            val current = awaitItem()
            assert(current == user)
            cancelAndIgnoreRemainingEvents()
        }

        Mockito.verify(authRepository, times(1))
            .signup("Alice", "a@ex.com", "pw", UserRole.STUDENT)
    }

    @Test
    fun login_success_updatesCurrentUserAndIsLoggedIn() = runTest {
        // Given
        val vm = AuthViewModel(authRepository)
        val user = User(id = 2, name = "Bob", email = "b@ex.com", role = UserRole.TEACHER)
        Mockito.`when`(authRepository.login("b@ex.com", "pw"))
            .thenReturn(Result.success(user))

        // When
        vm.login("b@ex.com", "pw")
        advanceUntilIdle()

        vm.currentUser.test {
            val current = awaitItem()
            assert(current == user)
            cancelAndIgnoreRemainingEvents()
        }

        vm.isLoggedIn.test {
            val flag = awaitItem()
            assert(flag)
            cancelAndIgnoreRemainingEvents()
        }

        Mockito.verify(authRepository, times(1)).login("b@ex.com", "pw")
    }

    @Test
    fun logout_clearsUserAndIsLoggedIn() = runTest {
        // Given: 로그인 상태
        val vm = AuthViewModel(authRepository)
        val user = User(id = 2, name = "Bob", email = "b@ex.com", role = UserRole.TEACHER)
        Mockito.`when`(authRepository.login("b@ex.com", "pw"))
            .thenReturn(Result.success(user))

        vm.login("b@ex.com", "pw")
        advanceUntilIdle()

        // When: logout 호출
        vm.logout()

        // Then
        vm.currentUser.test {
            assert(awaitItem() == null)
            cancelAndIgnoreRemainingEvents()
        }

        vm.isLoggedIn.test {
            assert(!awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun clearError_clearsErrorState() = runTest {
        // Given: 에러가 발생한 상태
        val vm = AuthViewModel(authRepository)
        Mockito.`when`(authRepository.login("test@ex.com", "wrong"))
            .thenReturn(Result.failure(Exception("Invalid credentials")))

        vm.login("test@ex.com", "wrong")
        advanceUntilIdle()

        vm.error.test {
            assert(awaitItem() != null) // 에러 설정 확인
            
            // When: clearError 호출
            vm.clearError()
            
            // Then: 에러가 null로 변경
            assert(awaitItem() == null)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun clearAutoFillCredentials_clearsAutoFillState() = runTest {
        // Given: 회원가입 후 autoFill 설정됨
        val vm = AuthViewModel(authRepository)
        val user = User(id = 1, name = "Alice", email = "a@ex.com", role = UserRole.STUDENT)
        Mockito.`when`(authRepository.signup("Alice", "a@ex.com", "pw", UserRole.STUDENT))
            .thenReturn(Result.success(user))

        vm.signup("Alice", "a@ex.com", "pw", UserRole.STUDENT)
        advanceUntilIdle()

        vm.autoFillCredentials.test {
            assert(awaitItem() == ("a@ex.com" to "pw")) // 자동 입력 정보 확인
            
            // When: clearAutoFillCredentials 호출
            vm.clearAutoFillCredentials()
            
            // Then: 자동 입력 정보가 null로 변경
            assert(awaitItem() == null)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun signup_failure_setsError() = runTest {
        // Given: 회원가입 실패
        val vm = AuthViewModel(authRepository)
        Mockito.`when`(authRepository.signup("Alice", "a@ex.com", "pw", UserRole.STUDENT))
            .thenReturn(Result.failure(Exception("Email already exists")))

        // When
        vm.error.test {
            awaitItem() // initial null
            
            vm.signup("Alice", "a@ex.com", "pw", UserRole.STUDENT)
            advanceUntilIdle()

            // Then: 에러 메시지 설정
            val error = awaitItem()
            assert(error?.contains("Email already exists") == true)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun login_failure_setsError() = runTest {
        // Given: 로그인 실패
        val vm = AuthViewModel(authRepository)
        Mockito.`when`(authRepository.login("test@ex.com", "wrong"))
            .thenReturn(Result.failure(Exception("Invalid credentials")))

        // When
        vm.error.test {
            awaitItem() // initial null
            
            vm.login("test@ex.com", "wrong")
            advanceUntilIdle()

            // Then: 에러 메시지 설정
            val error = awaitItem()
            assert(error?.contains("Invalid credentials") == true)
            cancelAndIgnoreRemainingEvents()
        }

        // Then: currentUser는 null로 유지
        vm.currentUser.test {
            assert(awaitItem() == null)
            cancelAndIgnoreRemainingEvents()
        }

        vm.isLoggedIn.test {
            assert(!awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun isLoading_loadingOperation_setsTrueThenFalse() = runTest {
        // Given
        val user = User(id = 1, name = "Alice", email = "a@ex.com", role = UserRole.STUDENT)
        Mockito.`when`(authRepository.login("a@ex.com", "pw"))
            .thenReturn(Result.success(user))

        val vm = AuthViewModel(authRepository)

        // When
        vm.isLoading.test {
            assert(!awaitItem()) // initial false
            
            vm.login("a@ex.com", "pw")
            advanceUntilIdle()

            // Then: 로딩 상태 변경 확인
            val states = mutableListOf<Boolean>()
            states.add(awaitItem())
            states.add(awaitItem())
            // 최소 한 번은 true여야 함
            assert(states.any { it })
            cancelAndIgnoreRemainingEvents()
        }
    }
}



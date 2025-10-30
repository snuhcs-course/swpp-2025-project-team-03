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
}



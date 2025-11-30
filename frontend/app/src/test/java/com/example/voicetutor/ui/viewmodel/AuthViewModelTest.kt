package com.example.voicetutor.ui.viewmodel

import app.cash.turbine.test
import com.example.voicetutor.data.models.User
import com.example.voicetutor.data.models.UserRole
import com.example.voicetutor.data.repository.AuthRepository
import com.example.voicetutor.data.repository.LoginException
import com.example.voicetutor.data.repository.SignupException
import com.example.voicetutor.testing.MainDispatcherRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.Mockito.times
import org.mockito.junit.MockitoJUnitRunner

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(MockitoJUnitRunner::class)
class AuthViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule { StandardTestDispatcher() }

    @Mock
    lateinit var authRepository: AuthRepository

    @Test
    fun initialStates_areSane_withoutCrashing() = runTest {
        val vm = AuthViewModel(authRepository)
        vm.currentUser.test {
            val u = awaitItem()
            assert(u == null)
            cancelAndIgnoreRemainingEvents()
        }
        vm.isLoggedIn.test {
            val logged = awaitItem()
            assert(!logged)
            cancelAndIgnoreRemainingEvents()
        }
        vm.error.test {
            val e = awaitItem()
            assert(e == null)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun signup_success_setsAutoFillAndCurrentUser() = runTest {
        val vm = AuthViewModel(authRepository)
        val user = User(id = 1, name = "Alice", email = "a@ex.com", role = UserRole.STUDENT)
        Mockito.`when`(authRepository.signup("Alice", "a@ex.com", "pw", UserRole.STUDENT))
            .thenReturn(Result.success(user))

        vm.signup("Alice", "a@ex.com", "pw", UserRole.STUDENT)
        advanceUntilIdle()
        vm.autoFillCredentials.test {
            val creds = awaitItem()
            assert(creds == ("a@ex.com" to "pw"))
            cancelAndIgnoreRemainingEvents()
        }

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
        val vm = AuthViewModel(authRepository)
        val user = User(id = 2, name = "Bob", email = "b@ex.com", role = UserRole.TEACHER)
        Mockito.`when`(authRepository.login("b@ex.com", "pw"))
            .thenReturn(Result.success(user))

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
        val vm = AuthViewModel(authRepository)
        val user = User(id = 2, name = "Bob", email = "b@ex.com", role = UserRole.TEACHER)
        Mockito.`when`(authRepository.login("b@ex.com", "pw"))
            .thenReturn(Result.success(user))

        vm.login("b@ex.com", "pw")
        advanceUntilIdle()

        vm.logout()

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
        val vm = AuthViewModel(authRepository)
        Mockito.`when`(authRepository.login("test@ex.com", "wrong"))
            .thenReturn(Result.failure(Exception("Invalid credentials")))

        vm.login("test@ex.com", "wrong")
        advanceUntilIdle()

        vm.error.test {
            assert(awaitItem() != null)

            vm.clearError()
            assert(awaitItem() == null)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun clearAutoFillCredentials_clearsAutoFillState() = runTest {
        val vm = AuthViewModel(authRepository)
        val user = User(id = 1, name = "Alice", email = "a@ex.com", role = UserRole.STUDENT)
        Mockito.`when`(authRepository.signup("Alice", "a@ex.com", "pw", UserRole.STUDENT))
            .thenReturn(Result.success(user))

        vm.signup("Alice", "a@ex.com", "pw", UserRole.STUDENT)
        advanceUntilIdle()

        vm.autoFillCredentials.test {
            assert(awaitItem() == ("a@ex.com" to "pw"))

            vm.clearAutoFillCredentials()
            assert(awaitItem() == null)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun signup_failure_setsError() = runTest {
        val vm = AuthViewModel(authRepository)
        Mockito.`when`(authRepository.signup("Alice", "a@ex.com", "pw", UserRole.STUDENT))
            .thenReturn(Result.failure(Exception("Email already exists")))

        vm.error.test {
            awaitItem()

            vm.signup("Alice", "a@ex.com", "pw", UserRole.STUDENT)
            advanceUntilIdle()
            val error = awaitItem()
            assert(error?.contains("Email already exists") == true)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun login_failure_setsError() = runTest {
        val vm = AuthViewModel(authRepository)
        Mockito.`when`(authRepository.login("test@ex.com", "wrong"))
            .thenReturn(Result.failure(Exception("Invalid credentials")))

        vm.error.test {
            awaitItem()

            vm.login("test@ex.com", "wrong")
            advanceUntilIdle()
            val error = awaitItem()
            assert(error?.contains("Invalid credentials") == true)
            cancelAndIgnoreRemainingEvents()
        }

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
        val user = User(id = 1, name = "Alice", email = "a@ex.com", role = UserRole.STUDENT)
        Mockito.`when`(authRepository.login("a@ex.com", "pw"))
            .thenReturn(Result.success(user))

        val vm = AuthViewModel(authRepository)

        vm.isLoading.test {
            assert(!awaitItem())

            vm.login("a@ex.com", "pw")
            advanceUntilIdle()

            val states = mutableListOf<Boolean>()
            states.add(awaitItem())
            states.add(awaitItem())
            assert(states.any { it })
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun getCurrentUser_withNull_returnsNull() = runTest {
        val vm = AuthViewModel(authRepository)

        vm.currentUser.test {
            assert(awaitItem() == null)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun getCurrentUser_withUser_returnsUser() = runTest {
        val vm = AuthViewModel(authRepository)
        val user = User(id = 2, name = "Bob", email = "b@ex.com", role = UserRole.TEACHER)
        Mockito.`when`(authRepository.login("b@ex.com", "pw"))
            .thenReturn(Result.success(user))

        vm.login("b@ex.com", "pw")
        advanceUntilIdle()

        vm.currentUser.test {
            assert(awaitItem() == user)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun getUserName_withNull_returnsDefault() = runTest {
        val vm = AuthViewModel(authRepository)

        vm.currentUser.test {
            val currentUser = awaitItem()
            assert(currentUser == null)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun getUserName_withUser_returnsUserName() = runTest {
        val vm = AuthViewModel(authRepository)
        val user = User(id = 2, name = "Bob", email = "b@ex.com", role = UserRole.TEACHER)
        Mockito.`when`(authRepository.login("b@ex.com", "pw"))
            .thenReturn(Result.success(user))

        vm.login("b@ex.com", "pw")
        advanceUntilIdle()

        vm.currentUser.test {
            val currentUser = awaitItem()
            assert(currentUser?.name == "Bob")
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun getUserRole_withNull_returnsNull() = runTest {
        val vm = AuthViewModel(authRepository)

        vm.currentUser.test {
            val currentUser = awaitItem()
            assert(currentUser?.role == null)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun getUserRole_withUser_returnsUserRole() = runTest {
        val vm = AuthViewModel(authRepository)
        val user = User(id = 2, name = "Bob", email = "b@ex.com", role = UserRole.TEACHER)
        Mockito.`when`(authRepository.login("b@ex.com", "pw"))
            .thenReturn(Result.success(user))

        vm.login("b@ex.com", "pw")
        advanceUntilIdle()

        vm.currentUser.test {
            val currentUser = awaitItem()
            assert(currentUser?.role == UserRole.TEACHER)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun login_success_withAssignments_setsCurrentUser() = runTest {
        val vm = AuthViewModel(authRepository)
        val assignments = listOf(
            com.example.voicetutor.data.models.AssignmentData(
                id = 1,
                title = "Assignment 1",
                description = "desc",
                totalQuestions = 5,
                createdAt = null,

                dueAt = "",
                courseClass = com.example.voicetutor.data.models.CourseClass(
                    id = 1,
                    name = "Class A",
                    description = null,
                    subject = com.example.voicetutor.data.models.Subject(id = 1, name = "Math"),
                    teacherName = "Teacher",

                    studentCount = 0,
                    createdAt = "",
                ),
                materials = null,
                grade = null,
            ),
        )
        val user = User(
            id = 1,
            name = "Alice",
            email = "a@ex.com",
            role = UserRole.STUDENT,
            assignments = assignments,
        )
        Mockito.`when`(authRepository.login("a@ex.com", "pw"))
            .thenReturn(Result.success(user))

        vm.login("a@ex.com", "pw")
        advanceUntilIdle()

        vm.currentUser.test {
            val currentUser = awaitItem()
            assert(currentUser != null)
            assert(currentUser?.assignments == assignments)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun login_success_withoutAssignments_setsCurrentUser() = runTest {
        val vm = AuthViewModel(authRepository)
        val user = User(id = 1, name = "Alice", email = "a@ex.com", role = UserRole.STUDENT, assignments = null)
        Mockito.`when`(authRepository.login("a@ex.com", "pw"))
            .thenReturn(Result.success(user))

        vm.login("a@ex.com", "pw")
        advanceUntilIdle()

        vm.currentUser.test {
            val currentUser = awaitItem()
            assert(currentUser != null)
            assert(currentUser?.assignments == null)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun setError_setsErrorState() = runTest {
        val vm = AuthViewModel(authRepository)

        vm.setSignupInputError(SignupField.EMAIL, "테스트 에러 메시지")

        vm.error.test {
            assert(awaitItem() == "테스트 에러 메시지")
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun setSignupInputError_setsSignupError() = runTest {
        val vm = AuthViewModel(authRepository)

        vm.setSignupInputError(SignupField.EMAIL, "이메일 형식이 올바르지 않습니다")

        vm.signupError.test {
            val error = awaitItem()
            assert(error is SignupError.Input)
            assert((error as SignupError.Input).field == SignupField.EMAIL)
            assert(error.message == "이메일 형식이 올바르지 않습니다")
            cancelAndIgnoreRemainingEvents()
        }
        vm.error.test {
            assert(awaitItem() == "이메일 형식이 올바르지 않습니다")
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun clearSignupError_clearsSignupError() = runTest {
        val vm = AuthViewModel(authRepository)
        vm.setSignupInputError(SignupField.PASSWORD, "비밀번호 오류")

        vm.clearSignupError()

        vm.signupError.test {
            assert(awaitItem() == null)
            cancelAndIgnoreRemainingEvents()
        }
        vm.error.test {
            assert(awaitItem() == null)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun clearFieldError_withMatchingField_clearsError() = runTest {
        val vm = AuthViewModel(authRepository)
        vm.setSignupInputError(SignupField.EMAIL, "이메일 오류")

        vm.clearFieldError(SignupField.EMAIL)
        vm.signupError.test {
            assert(awaitItem() == null)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun clearFieldError_withDifferentField_keepsError() = runTest {
        val vm = AuthViewModel(authRepository)
        vm.setSignupInputError(SignupField.EMAIL, "이메일 오류")

        vm.clearFieldError(SignupField.PASSWORD)
        vm.signupError.test {
            val error = awaitItem()
            assert(error != null)
            assert((error as SignupError.Input).field == SignupField.EMAIL)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun setLoginInputError_setsLoginError() = runTest {
        val vm = AuthViewModel(authRepository)

        vm.setLoginInputError(LoginField.PASSWORD, "비밀번호가 올바르지 않습니다")

        vm.loginError.test {
            val error = awaitItem()
            assert(error is LoginError.Input)
            assert((error as LoginError.Input).field == LoginField.PASSWORD)
            assert(error.message == "비밀번호가 올바르지 않습니다")
            cancelAndIgnoreRemainingEvents()
        }
        vm.error.test {
            assert(awaitItem() == "비밀번호가 올바르지 않습니다")
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun clearLoginError_clearsLoginError() = runTest {
        val vm = AuthViewModel(authRepository)
        vm.setLoginInputError(LoginField.EMAIL, "이메일 오류")

        vm.clearLoginError()

        vm.loginError.test {
            assert(awaitItem() == null)
            cancelAndIgnoreRemainingEvents()
        }
        vm.error.test {
            assert(awaitItem() == null)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun clearLoginFieldError_withMatchingField_clearsError() = runTest {
        val vm = AuthViewModel(authRepository)
        vm.setLoginInputError(LoginField.EMAIL, "이메일 오류")

        vm.clearLoginFieldError(LoginField.EMAIL)

        vm.loginError.test {
            assert(awaitItem() == null)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun clearLoginFieldError_withDifferentField_keepsError() = runTest {
        val vm = AuthViewModel(authRepository)
        vm.setLoginInputError(LoginField.EMAIL, "이메일 오류")

        vm.clearLoginFieldError(LoginField.PASSWORD)

        vm.loginError.test {
            val error = awaitItem()
            assert(error != null)
            assert((error as LoginError.Input).field == LoginField.EMAIL)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun login_withLoginExceptionInvalidCredentials_setsCorrectError() = runTest {
        val vm = AuthViewModel(authRepository)
        Mockito.`when`(authRepository.login("test@ex.com", "wrong"))
            .thenReturn(Result.failure(LoginException.InvalidCredentials("이메일 또는 비밀번호가 올바르지 않습니다")))

        vm.login("test@ex.com", "wrong")
        advanceUntilIdle()

        vm.loginError.test {
            val error = awaitItem()
            assert(error is LoginError.General.InvalidCredentials)
            val generalError = error as LoginError.General.InvalidCredentials
            assert(generalError.message.contains("이메일") || generalError.message.contains("비밀번호"))
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun login_withLoginExceptionAccountNotFound_setsCorrectError() = runTest {
        val vm = AuthViewModel(authRepository)
        Mockito.`when`(authRepository.login("test@ex.com", "pw"))
            .thenReturn(Result.failure(LoginException.AccountNotFound("계정을 찾을 수 없습니다")))

        vm.login("test@ex.com", "pw")
        advanceUntilIdle()

        vm.loginError.test {
            val error = awaitItem()
            assert(error is LoginError.General.AccountNotFound)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun login_withLoginExceptionAccountLocked_setsCorrectError() = runTest {
        val vm = AuthViewModel(authRepository)
        Mockito.`when`(authRepository.login("test@ex.com", "pw"))
            .thenReturn(Result.failure(LoginException.AccountLocked("계정이 잠겨 있습니다")))

        vm.login("test@ex.com", "pw")
        advanceUntilIdle()

        vm.loginError.test {
            val error = awaitItem()
            assert(error is LoginError.General.AccountLocked)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun login_withLoginExceptionServer_setsCorrectError() = runTest {
        val vm = AuthViewModel(authRepository)
        Mockito.`when`(authRepository.login("test@ex.com", "pw"))
            .thenReturn(Result.failure(LoginException.Server("서버 오류")))

        vm.login("test@ex.com", "pw")
        advanceUntilIdle()

        vm.loginError.test {
            val error = awaitItem()
            assert(error is LoginError.General.Server)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun login_withLoginExceptionNetwork_setsCorrectError() = runTest {
        val vm = AuthViewModel(authRepository)
        Mockito.`when`(authRepository.login("test@ex.com", "pw"))
            .thenReturn(Result.failure(LoginException.Network("네트워크 오류")))

        vm.login("test@ex.com", "pw")
        advanceUntilIdle()

        vm.loginError.test {
            val error = awaitItem()
            assert(error is LoginError.General.Network)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun signup_withSignupExceptionDuplicateEmail_setsCorrectError() = runTest {
        val vm = AuthViewModel(authRepository)
        Mockito.`when`(authRepository.signup("Alice", "a@ex.com", "pw", UserRole.STUDENT))
            .thenReturn(Result.failure(SignupException.DuplicateEmail("이미 사용 중인 이메일입니다")))

        vm.signup("Alice", "a@ex.com", "pw", UserRole.STUDENT)
        advanceUntilIdle()

        vm.signupError.test {
            val error = awaitItem()
            assert(error is SignupError.General.DuplicateEmail)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun signup_withSignupExceptionServer_setsCorrectError() = runTest {
        val vm = AuthViewModel(authRepository)
        Mockito.`when`(authRepository.signup("Alice", "a@ex.com", "pw", UserRole.STUDENT))
            .thenReturn(Result.failure(SignupException.Server("서버 오류")))

        vm.signup("Alice", "a@ex.com", "pw", UserRole.STUDENT)
        advanceUntilIdle()

        vm.signupError.test {
            val error = awaitItem()
            assert(error is SignupError.General.Server)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun signup_withSignupExceptionNetwork_setsCorrectError() = runTest {
        val vm = AuthViewModel(authRepository)
        Mockito.`when`(authRepository.signup("Alice", "a@ex.com", "pw", UserRole.STUDENT))
            .thenReturn(Result.failure(SignupException.Network("네트워크 오류")))

        vm.signup("Alice", "a@ex.com", "pw", UserRole.STUDENT)
        advanceUntilIdle()

        vm.signupError.test {
            val error = awaitItem()
            assert(error is SignupError.General.Network)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun logout_clearsAllErrors() = runTest {
        val vm = AuthViewModel(authRepository)
        vm.setSignupInputError(SignupField.EMAIL, "이메일 에러")
        vm.setLoginInputError(LoginField.PASSWORD, "비밀번호 에러")

        vm.logout()

        vm.error.test {
            assert(awaitItem() == null)
            cancelAndIgnoreRemainingEvents()
        }
        vm.signupError.test {
            assert(awaitItem() == null)
            cancelAndIgnoreRemainingEvents()
        }
        vm.loginError.test {
            assert(awaitItem() == null)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun signup_withClassName_handlesCorrectly() = runTest {
        val vm = AuthViewModel(authRepository)
        val user = User(id = 1, name = "Alice", email = "a@ex.com", role = UserRole.STUDENT)
        Mockito.`when`(authRepository.signup("Alice", "a@ex.com", "pw", UserRole.STUDENT))
            .thenReturn(Result.success(user))

        vm.signup("Alice", "a@ex.com", "pw", UserRole.STUDENT)
        advanceUntilIdle()

        vm.currentUser.test {
            assert(awaitItem() == user)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun deleteAccount_success_setsAccountDeletedAndLogsOut() = runTest {
        val vm = AuthViewModel(authRepository)
        val user = User(id = 1, name = "Alice", email = "a@ex.com", role = UserRole.STUDENT)
        Mockito.`when`(authRepository.login("a@ex.com", "pw"))
            .thenReturn(Result.success(user))
        Mockito.`when`(authRepository.deleteAccount())
            .thenReturn(Result.success(Unit))

        vm.login("a@ex.com", "pw")
        advanceUntilIdle()

        vm.deleteAccount()
        advanceUntilIdle()

        vm.accountDeleted.test {
            assert(awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
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
    fun deleteAccount_failure_setsError() = runTest {
        val vm = AuthViewModel(authRepository)
        Mockito.`when`(authRepository.deleteAccount())
            .thenReturn(Result.failure(com.example.voicetutor.data.repository.DeleteAccountException.Unauthorized("권한이 없습니다")))

        vm.deleteAccount()
        advanceUntilIdle()

        vm.error.test {
            val error = awaitItem()
            assert(error != null)
            assert(error?.contains("권한") == true || error?.contains("계정 삭제") == true)
            cancelAndIgnoreRemainingEvents()
        }
        vm.accountDeleted.test {
            assert(!awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun deleteAccount_withServerException_setsError() = runTest {
        val vm = AuthViewModel(authRepository)
        Mockito.`when`(authRepository.deleteAccount())
            .thenReturn(Result.failure(com.example.voicetutor.data.repository.DeleteAccountException.Server("서버 오류")))

        vm.deleteAccount()
        advanceUntilIdle()

        vm.error.test {
            val error = awaitItem()
            assert(error != null)
            assert(error?.contains("서버") == true || error?.contains("오류") == true)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun deleteAccount_withNetworkException_setsError() = runTest {
        val vm = AuthViewModel(authRepository)
        Mockito.`when`(authRepository.deleteAccount())
            .thenReturn(Result.failure(com.example.voicetutor.data.repository.DeleteAccountException.Network("네트워크 오류")))

        vm.deleteAccount()
        advanceUntilIdle()

        vm.error.test {
            val error = awaitItem()
            assert(error != null)
            assert(error?.contains("네트워크") == true || error?.contains("연결") == true)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun deleteAccount_withUnknownException_setsError() = runTest {
        val vm = AuthViewModel(authRepository)
        Mockito.`when`(authRepository.deleteAccount())
            .thenReturn(Result.failure(com.example.voicetutor.data.repository.DeleteAccountException.Unknown("알 수 없는 오류")))

        vm.deleteAccount()
        advanceUntilIdle()

        vm.error.test {
            val error = awaitItem()
            assert(error != null)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun clearAccountDeletedFlag_clearsFlag() = runTest {
        val vm = AuthViewModel(authRepository)
        Mockito.`when`(authRepository.deleteAccount())
            .thenReturn(Result.success(Unit))

        vm.deleteAccount()
        advanceUntilIdle()

        vm.accountDeleted.test {
            assert(awaitItem())

            vm.clearAccountDeletedFlag()

            assert(!awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }
}

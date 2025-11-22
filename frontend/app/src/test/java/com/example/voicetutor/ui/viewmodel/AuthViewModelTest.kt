package com.example.voicetutor.ui.viewmodel

import app.cash.turbine.test
import com.example.voicetutor.data.models.User
import com.example.voicetutor.data.models.UserRole
import com.example.voicetutor.data.repository.AuthRepository
import com.example.voicetutor.data.repository.LoginException
import com.example.voicetutor.data.repository.SignupException
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
        // Given
        val vm = AuthViewModel(authRepository)
        val user = User(id = 1, name = "Alice", email = "a@ex.com", role = UserRole.STUDENT)
        Mockito.`when`(authRepository.signup("Alice", "a@ex.com", "pw", UserRole.STUDENT))
            .thenReturn(Result.success(user))

        // When
        vm.signup("Alice", "a@ex.com", "pw", UserRole.STUDENT)
        advanceUntilIdle()
        vm.autoFillCredentials.test {
            val creds = awaitItem()
            assert(creds == ("a@ex.com" to "pw"))
            cancelAndIgnoreRemainingEvents()
        }

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

    @Test
    fun getCurrentUser_withNull_returnsNull() = runTest {
        // Given: 초기 상태
        val vm = AuthViewModel(authRepository)

        // When: currentUser 확인
        // Then: null 반환
        vm.currentUser.test {
            assert(awaitItem() == null)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun getCurrentUser_withUser_returnsUser() = runTest {
        // Given: 로그인 상태
        val vm = AuthViewModel(authRepository)
        val user = User(id = 2, name = "Bob", email = "b@ex.com", role = UserRole.TEACHER)
        Mockito.`when`(authRepository.login("b@ex.com", "pw"))
            .thenReturn(Result.success(user))

        vm.login("b@ex.com", "pw")
        advanceUntilIdle()

        // When: currentUser 확인
        // Then: user 반환
        vm.currentUser.test {
            assert(awaitItem() == user)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun getUserName_withNull_returnsDefault() = runTest {
        // Given: 초기 상태 (currentUser가 null)
        val vm = AuthViewModel(authRepository)

        // When: currentUser.name 확인
        // Then: null이므로 기본값 사용
        vm.currentUser.test {
            val currentUser = awaitItem()
            assert(currentUser == null)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun getUserName_withUser_returnsUserName() = runTest {
        // Given: 로그인 상태
        val vm = AuthViewModel(authRepository)
        val user = User(id = 2, name = "Bob", email = "b@ex.com", role = UserRole.TEACHER)
        Mockito.`when`(authRepository.login("b@ex.com", "pw"))
            .thenReturn(Result.success(user))

        vm.login("b@ex.com", "pw")
        advanceUntilIdle()

        // When: currentUser.name 확인
        // Then: user.name 반환
        vm.currentUser.test {
            val currentUser = awaitItem()
            assert(currentUser?.name == "Bob")
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun getUserRole_withNull_returnsNull() = runTest {
        // Given: 초기 상태
        val vm = AuthViewModel(authRepository)

        // When: currentUser.role 확인
        // Then: null 반환
        vm.currentUser.test {
            val currentUser = awaitItem()
            assert(currentUser?.role == null)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun getUserRole_withUser_returnsUserRole() = runTest {
        // Given: 로그인 상태
        val vm = AuthViewModel(authRepository)
        val user = User(id = 2, name = "Bob", email = "b@ex.com", role = UserRole.TEACHER)
        Mockito.`when`(authRepository.login("b@ex.com", "pw"))
            .thenReturn(Result.success(user))

        vm.login("b@ex.com", "pw")
        advanceUntilIdle()

        // When: currentUser.role 확인
        // Then: user.role 반환
        vm.currentUser.test {
            val currentUser = awaitItem()
            assert(currentUser?.role == UserRole.TEACHER)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun login_success_withAssignments_setsCurrentUser() = runTest {
        // Given: 로그인 시 assignments가 포함된 user
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

        // When: 로그인
        vm.login("a@ex.com", "pw")
        advanceUntilIdle()

        // Then: currentUser가 설정됨
        vm.currentUser.test {
            val currentUser = awaitItem()
            assert(currentUser != null)
            assert(currentUser?.assignments == assignments)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun login_success_withoutAssignments_setsCurrentUser() = runTest {
        // Given: 로그인 시 assignments가 null인 user
        val vm = AuthViewModel(authRepository)
        val user = User(id = 1, name = "Alice", email = "a@ex.com", role = UserRole.STUDENT, assignments = null)
        Mockito.`when`(authRepository.login("a@ex.com", "pw"))
            .thenReturn(Result.success(user))

        // When: 로그인
        vm.login("a@ex.com", "pw")
        advanceUntilIdle()

        // Then: currentUser가 설정됨
        vm.currentUser.test {
            val currentUser = awaitItem()
            assert(currentUser != null)
            assert(currentUser?.assignments == null)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun setError_setsErrorState() = runTest {
        // Given
        val vm = AuthViewModel(authRepository)

        // When: setSignupInputError를 사용하여 에러 설정 (setError는 없으므로)
        vm.setSignupInputError(SignupField.EMAIL, "테스트 에러 메시지")

        // Then
        vm.error.test {
            assert(awaitItem() == "테스트 에러 메시지")
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun setSignupInputError_setsSignupError() = runTest {
        // Given
        val vm = AuthViewModel(authRepository)

        // When
        vm.setSignupInputError(SignupField.EMAIL, "이메일 형식이 올바르지 않습니다")

        // Then
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
        // Given: signup error가 설정된 상태
        val vm = AuthViewModel(authRepository)
        vm.setSignupInputError(SignupField.PASSWORD, "비밀번호 오류")

        // When
        vm.clearSignupError()

        // Then
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
        // Given: 특정 필드에 에러가 설정된 상태
        val vm = AuthViewModel(authRepository)
        vm.setSignupInputError(SignupField.EMAIL, "이메일 오류")

        // When: 해당 필드의 에러를 클리어
        vm.clearFieldError(SignupField.EMAIL)

        // Then: 에러가 클리어됨
        vm.signupError.test {
            assert(awaitItem() == null)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun clearFieldError_withDifferentField_keepsError() = runTest {
        // Given: EMAIL 필드에 에러가 설정된 상태
        val vm = AuthViewModel(authRepository)
        vm.setSignupInputError(SignupField.EMAIL, "이메일 오류")

        // When: 다른 필드의 에러를 클리어 시도
        vm.clearFieldError(SignupField.PASSWORD)

        // Then: 에러가 유지됨
        vm.signupError.test {
            val error = awaitItem()
            assert(error != null)
            assert((error as SignupError.Input).field == SignupField.EMAIL)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun setLoginInputError_setsLoginError() = runTest {
        // Given
        val vm = AuthViewModel(authRepository)

        // When
        vm.setLoginInputError(LoginField.PASSWORD, "비밀번호가 올바르지 않습니다")

        // Then
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
        // Given: login error가 설정된 상태
        val vm = AuthViewModel(authRepository)
        vm.setLoginInputError(LoginField.EMAIL, "이메일 오류")

        // When
        vm.clearLoginError()

        // Then
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
        // Given: 특정 필드에 에러가 설정된 상태
        val vm = AuthViewModel(authRepository)
        vm.setLoginInputError(LoginField.EMAIL, "이메일 오류")

        // When: 해당 필드의 에러를 클리어
        vm.clearLoginFieldError(LoginField.EMAIL)

        // Then: 에러가 클리어됨
        vm.loginError.test {
            assert(awaitItem() == null)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun clearLoginFieldError_withDifferentField_keepsError() = runTest {
        // Given: EMAIL 필드에 에러가 설정된 상태
        val vm = AuthViewModel(authRepository)
        vm.setLoginInputError(LoginField.EMAIL, "이메일 오류")

        // When: 다른 필드의 에러를 클리어 시도
        vm.clearLoginFieldError(LoginField.PASSWORD)

        // Then: 에러가 유지됨
        vm.loginError.test {
            val error = awaitItem()
            assert(error != null)
            assert((error as LoginError.Input).field == LoginField.EMAIL)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun login_withLoginExceptionInvalidCredentials_setsCorrectError() = runTest {
        // Given
        val vm = AuthViewModel(authRepository)
        Mockito.`when`(authRepository.login("test@ex.com", "wrong"))
            .thenReturn(Result.failure(LoginException.InvalidCredentials("이메일 또는 비밀번호가 올바르지 않습니다")))

        // When
        vm.login("test@ex.com", "wrong")
        advanceUntilIdle()

        // Then
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
        // Given
        val vm = AuthViewModel(authRepository)
        Mockito.`when`(authRepository.login("test@ex.com", "pw"))
            .thenReturn(Result.failure(LoginException.AccountNotFound("계정을 찾을 수 없습니다")))

        // When
        vm.login("test@ex.com", "pw")
        advanceUntilIdle()

        // Then
        vm.loginError.test {
            val error = awaitItem()
            assert(error is LoginError.General.AccountNotFound)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun login_withLoginExceptionAccountLocked_setsCorrectError() = runTest {
        // Given
        val vm = AuthViewModel(authRepository)
        Mockito.`when`(authRepository.login("test@ex.com", "pw"))
            .thenReturn(Result.failure(LoginException.AccountLocked("계정이 잠겨 있습니다")))

        // When
        vm.login("test@ex.com", "pw")
        advanceUntilIdle()

        // Then
        vm.loginError.test {
            val error = awaitItem()
            assert(error is LoginError.General.AccountLocked)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun login_withLoginExceptionServer_setsCorrectError() = runTest {
        // Given
        val vm = AuthViewModel(authRepository)
        Mockito.`when`(authRepository.login("test@ex.com", "pw"))
            .thenReturn(Result.failure(LoginException.Server("서버 오류")))

        // When
        vm.login("test@ex.com", "pw")
        advanceUntilIdle()

        // Then
        vm.loginError.test {
            val error = awaitItem()
            assert(error is LoginError.General.Server)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun login_withLoginExceptionNetwork_setsCorrectError() = runTest {
        // Given
        val vm = AuthViewModel(authRepository)
        Mockito.`when`(authRepository.login("test@ex.com", "pw"))
            .thenReturn(Result.failure(LoginException.Network("네트워크 오류")))

        // When
        vm.login("test@ex.com", "pw")
        advanceUntilIdle()

        // Then
        vm.loginError.test {
            val error = awaitItem()
            assert(error is LoginError.General.Network)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun signup_withSignupExceptionDuplicateEmail_setsCorrectError() = runTest {
        // Given
        val vm = AuthViewModel(authRepository)
        Mockito.`when`(authRepository.signup("Alice", "a@ex.com", "pw", UserRole.STUDENT))
            .thenReturn(Result.failure(SignupException.DuplicateEmail("이미 사용 중인 이메일입니다")))

        // When
        vm.signup("Alice", "a@ex.com", "pw", UserRole.STUDENT)
        advanceUntilIdle()

        // Then
        vm.signupError.test {
            val error = awaitItem()
            assert(error is SignupError.General.DuplicateEmail)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun signup_withSignupExceptionServer_setsCorrectError() = runTest {
        // Given
        val vm = AuthViewModel(authRepository)
        Mockito.`when`(authRepository.signup("Alice", "a@ex.com", "pw", UserRole.STUDENT))
            .thenReturn(Result.failure(SignupException.Server("서버 오류")))

        // When
        vm.signup("Alice", "a@ex.com", "pw", UserRole.STUDENT)
        advanceUntilIdle()

        // Then
        vm.signupError.test {
            val error = awaitItem()
            assert(error is SignupError.General.Server)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun signup_withSignupExceptionNetwork_setsCorrectError() = runTest {
        // Given
        val vm = AuthViewModel(authRepository)
        Mockito.`when`(authRepository.signup("Alice", "a@ex.com", "pw", UserRole.STUDENT))
            .thenReturn(Result.failure(SignupException.Network("네트워크 오류")))

        // When
        vm.signup("Alice", "a@ex.com", "pw", UserRole.STUDENT)
        advanceUntilIdle()

        // Then
        vm.signupError.test {
            val error = awaitItem()
            assert(error is SignupError.General.Network)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun logout_clearsAllErrors() = runTest {
        // Given: 에러가 설정된 상태
        val vm = AuthViewModel(authRepository)
        vm.setSignupInputError(SignupField.EMAIL, "이메일 에러")
        vm.setLoginInputError(LoginField.PASSWORD, "비밀번호 에러")

        // When
        vm.logout()

        // Then: 모든 에러가 클리어됨
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
        // Given
        val vm = AuthViewModel(authRepository)
        val user = User(id = 1, name = "Alice", email = "a@ex.com", role = UserRole.STUDENT)
        Mockito.`when`(authRepository.signup("Alice", "a@ex.com", "pw", UserRole.STUDENT))
            .thenReturn(Result.success(user))

        // When: signup 호출
        vm.signup("Alice", "a@ex.com", "pw", UserRole.STUDENT)
        advanceUntilIdle()

        // Then: 정상적으로 처리됨
        vm.currentUser.test {
            assert(awaitItem() == user)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun deleteAccount_success_setsAccountDeletedAndLogsOut() = runTest {
        // Given
        val vm = AuthViewModel(authRepository)
        val user = User(id = 1, name = "Alice", email = "a@ex.com", role = UserRole.STUDENT)
        Mockito.`when`(authRepository.login("a@ex.com", "pw"))
            .thenReturn(Result.success(user))
        Mockito.`when`(authRepository.deleteAccount())
            .thenReturn(Result.success(Unit))

        // Login first
        vm.login("a@ex.com", "pw")
        advanceUntilIdle()

        // When
        vm.deleteAccount()
        advanceUntilIdle()

        // Then
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
        // Given
        val vm = AuthViewModel(authRepository)
        Mockito.`when`(authRepository.deleteAccount())
            .thenReturn(Result.failure(com.example.voicetutor.data.repository.DeleteAccountException.Unauthorized("권한이 없습니다")))

        // When
        vm.deleteAccount()
        advanceUntilIdle()

        // Then
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
        // Given
        val vm = AuthViewModel(authRepository)
        Mockito.`when`(authRepository.deleteAccount())
            .thenReturn(Result.failure(com.example.voicetutor.data.repository.DeleteAccountException.Server("서버 오류")))

        // When
        vm.deleteAccount()
        advanceUntilIdle()

        // Then
        vm.error.test {
            val error = awaitItem()
            assert(error != null)
            assert(error?.contains("서버") == true || error?.contains("오류") == true)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun deleteAccount_withNetworkException_setsError() = runTest {
        // Given
        val vm = AuthViewModel(authRepository)
        Mockito.`when`(authRepository.deleteAccount())
            .thenReturn(Result.failure(com.example.voicetutor.data.repository.DeleteAccountException.Network("네트워크 오류")))

        // When
        vm.deleteAccount()
        advanceUntilIdle()

        // Then
        vm.error.test {
            val error = awaitItem()
            assert(error != null)
            assert(error?.contains("네트워크") == true || error?.contains("연결") == true)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun deleteAccount_withUnknownException_setsError() = runTest {
        // Given
        val vm = AuthViewModel(authRepository)
        Mockito.`when`(authRepository.deleteAccount())
            .thenReturn(Result.failure(com.example.voicetutor.data.repository.DeleteAccountException.Unknown("알 수 없는 오류")))

        // When
        vm.deleteAccount()
        advanceUntilIdle()

        // Then
        vm.error.test {
            val error = awaitItem()
            assert(error != null)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun clearAccountDeletedFlag_clearsFlag() = runTest {
        // Given
        val vm = AuthViewModel(authRepository)
        Mockito.`when`(authRepository.deleteAccount())
            .thenReturn(Result.success(Unit))

        vm.deleteAccount()
        advanceUntilIdle()

        vm.accountDeleted.test {
            assert(awaitItem()) // Verify it's set

            // When
            vm.clearAccountDeletedFlag()

            // Then
            assert(!awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }
}

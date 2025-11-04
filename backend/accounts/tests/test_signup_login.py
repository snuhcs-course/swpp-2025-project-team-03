from django.contrib.auth import get_user_model
from django.urls import reverse
from rest_framework import status
from rest_framework.test import APITestCase

Account = get_user_model()


class SignupLoginTestCase(APITestCase):
    def setUp(self):
        self.signup_url = reverse("signup")
        self.login_url = reverse("login")
        self.logout_url = reverse("logout")
        self.user_data = {
            "email": "test@example.com",
            "password": "testpass123",
            "name": "테스트유저",
            "role": "STUDENT",
        }

    # (1) 회원가입 성공 테스트
    def test_signup_success(self):
        response = self.client.post(self.signup_url, self.user_data, format="json")
        self.assertEqual(response.status_code, status.HTTP_201_CREATED)
        self.assertIn("token", response.data)
        self.assertTrue(Account.objects.filter(email=self.user_data["email"]).exists())

    # (2) 회원가입 시 중복 이메일로 실패 테스트
    def test_signup_duplicate_email_fails(self):
        Account.objects.create_user(email="test@example.com", password="1234")
        response = self.client.post(self.signup_url, self.user_data, format="json")
        self.assertEqual(response.status_code, status.HTTP_400_BAD_REQUEST)

    # (3) 로그인 성공 테스트
    def test_login_success(self):
        user = Account.objects.create_user(email="test@example.com", password="testpass123", display_name="테스트유저")
        response = self.client.post(
            self.login_url, {"email": "test@example.com", "password": "testpass123"}, format="json"
        )
        self.assertEqual(response.status_code, status.HTTP_200_OK)
        self.assertIn("token", response.data)
        self.assertIn("refresh_token", response.cookies)

    # (4) 로그인 실패 테스트 (비밀번호 틀림)
    def test_login_wrong_password(self):
        Account.objects.create_user(email="test@example.com", password="rightpass")
        response = self.client.post(
            self.login_url, {"email": "test@example.com", "password": "wrongpass"}, format="json"
        )
        self.assertEqual(response.status_code, status.HTTP_401_UNAUTHORIZED)
        self.assertIn("비밀번호가 일치하지 않습니다.", response.data["message"])

    # (5) 로그인 실패 테스트 (존재하지 않는 이메일)
    def test_login_nonexistent_user(self):
        response = self.client.post(self.login_url, {"email": "nouser@example.com", "password": "1234"}, format="json")
        self.assertEqual(response.status_code, status.HTTP_404_NOT_FOUND)
        self.assertIn("해당 이메일의 사용자가 없습니다.", response.data["message"])

    # (6) 로그인 실패 테스트 (email 없음)
    def test_login_missing_email(self):
        response = self.client.post(self.login_url, {"password": "testpass123"}, format="json")
        self.assertEqual(response.status_code, status.HTTP_400_BAD_REQUEST)
        self.assertIn("email/password 필수 입력", response.data["message"])

    # (7) 로그인 실패 테스트 (password 없음)
    def test_login_missing_password(self):
        response = self.client.post(self.login_url, {"email": "test@example.com"}, format="json")
        self.assertEqual(response.status_code, status.HTTP_400_BAD_REQUEST)
        self.assertIn("email/password 필수 입력", response.data["message"])

    # (8) 회원가입 ValidationError 테스트
    def test_signup_validation_error(self):
        invalid_data = {
            "email": "invalid-email",
            "password": "testpass123",
        }
        response = self.client.post(self.signup_url, invalid_data, format="json")
        self.assertEqual(response.status_code, status.HTTP_400_BAD_REQUEST)

    # (9) 회원가입 일반 예외 테스트
    def test_signup_general_exception(self):
        # 잘못된 필드로 예외 발생시키기
        invalid_data = {
            "email": "test@example.com",
            "password": "testpass123",
            "invalid_field": "invalid",
        }
        response = self.client.post(self.signup_url, invalid_data, format="json")
        self.assertIn(response.status_code, [status.HTTP_400_BAD_REQUEST, status.HTTP_500_INTERNAL_SERVER_ERROR])

    # (13) 회원가입 일반 Exception 발생 테스트 (line 68-70 커버)
    def test_signup_unexpected_exception(self):
        """SignupView에서 예상치 못한 Exception 발생 시 처리 테스트"""
        from unittest.mock import Mock, patch

        with patch("accounts.views.SignupRequestSerializer") as mock_serializer_class:
            mock_serializer_instance = Mock()
            mock_serializer_instance.is_valid.side_effect = Exception("Unexpected error")
            mock_serializer_class.return_value = mock_serializer_instance

            response = self.client.post(
                self.signup_url,
                {"email": "test@example.com", "password": "testpass123", "name": "Test", "role": "STUDENT"},
                format="json",
            )

            self.assertEqual(response.status_code, status.HTTP_500_INTERNAL_SERVER_ERROR)
            self.assertIn("회원가입 중 오류가 발생했습니다", response.data["message"])
            self.assertIn("error", response.data)
            self.assertEqual(response.data["error"], "Unexpected error")

    # (10) 로그아웃 성공 테스트
    def test_logout_success(self):
        user = Account.objects.create_user(email="test@example.com", password="testpass123")
        login_response = self.client.post(
            self.login_url, {"email": "test@example.com", "password": "testpass123"}, format="json"
        )
        refresh_token = login_response.cookies.get("refresh_token").value

        self.client.cookies["refresh_token"] = refresh_token
        response = self.client.post(self.logout_url, format="json")
        self.assertEqual(response.status_code, status.HTTP_200_OK)
        self.assertIn("로그아웃 성공", response.data["message"])
        # delete_cookie는 빈 값으로 쿠키를 설정함
        self.assertEqual(response.cookies.get("refresh_token").value, "")

    # (11) 로그아웃 실패 테스트 (refresh_token 없음)
    def test_logout_no_refresh_token(self):
        response = self.client.post(self.logout_url, format="json")
        self.assertEqual(response.status_code, status.HTTP_400_BAD_REQUEST)
        self.assertIn("refresh token이 없습니다", response.data["message"])

    # (12) 로그아웃 실패 테스트 (잘못된 토큰)
    def test_logout_invalid_token(self):
        self.client.cookies["refresh_token"] = "invalid_token"
        response = self.client.post(self.logout_url, format="json")
        self.assertEqual(response.status_code, status.HTTP_200_OK)
        self.assertIn("로그아웃 성공", response.data["message"])

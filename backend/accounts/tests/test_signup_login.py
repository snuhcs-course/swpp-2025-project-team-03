from django.contrib.auth import get_user_model
from django.urls import reverse
from rest_framework import status
from rest_framework.test import APITestCase

Account = get_user_model()


class SignupLoginTestCase(APITestCase):
    def setUp(self):
        self.signup_url = reverse("signup")
        self.login_url = reverse("login")
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

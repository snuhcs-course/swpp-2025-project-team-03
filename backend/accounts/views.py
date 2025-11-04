import logging

from django.contrib.auth import get_user_model
from django.db import IntegrityError
from drf_yasg.utils import swagger_auto_schema
from rest_framework import serializers, status
from rest_framework.response import Response
from rest_framework.views import APIView

# from drf_yasg import openapi
from rest_framework_simplejwt.tokens import RefreshToken

from .request_serializers import LoginRequestSerializer, SignupRequestSerializer
from .serializers import UserResponseSerializer

logger = logging.getLogger(__name__)

Account = get_user_model()


# JWT 토큰을 쿠키에 저장하는 헬퍼 함수
def set_jwt_cookie_response(user, status_code=status.HTTP_200_OK):
    """
    RefreshToken 발급 후, access/refresh 토큰을 httponly cookie로 저장
    """
    token = RefreshToken.for_user(user)
    serialized_user = UserResponseSerializer(user).data

    response = Response(
        {
            "success": True,
            "data": serialized_user,
            "token": str(token.access_token),
            "message": "성공",
            "error": None,
        },
        status=status_code,
    )
    response.set_cookie("refresh_token", str(token), httponly=True, samesite="Strict")
    response.set_cookie("access_token", str(token.access_token), httponly=True, samesite="Strict")
    return response


class SignupView(APIView):
    @swagger_auto_schema(
        operation_id="회원가입",
        operation_description="새 사용자를 등록하고 JWT 토큰을 발급합니다.",
        request_body=SignupRequestSerializer,
        responses={201: "회원가입 성공", 400: "유효하지 않은 요청"},
    )
    def post(self, request):
        try:
            serializer = SignupRequestSerializer(data=request.data)
            if serializer.is_valid(raise_exception=True):
                user = serializer.save()
                return set_jwt_cookie_response(user, status_code=status.HTTP_201_CREATED)
        except IntegrityError:
            return Response(
                {"success": False, "message": "이미 존재하는 이메일입니다."},
                status=status.HTTP_400_BAD_REQUEST,
            )
        except serializers.ValidationError as ve:
            print("Validation Error:", ve.detail)
            return Response(
                {"success": False, "error": ve.detail, "message": "입력값 오류"},
                status=status.HTTP_400_BAD_REQUEST,
            )
        except Exception as e:
            logger.error(f"[SignupView] {e}", exc_info=True)
            return Response(
                {"success": False, "error": str(e), "message": "회원가입 중 오류가 발생했습니다."},
                status=status.HTTP_500_INTERNAL_SERVER_ERROR,
            )


class LoginView(APIView):
    @swagger_auto_schema(
        operation_id="로그인",
        operation_description="이메일과 비밀번호로 로그인 후 JWT 토큰을 쿠키에 저장합니다.",
        request_body=LoginRequestSerializer,
        responses={200: "로그인 성공", 400: "잘못된 요청", 401: "인증 실패"},
    )
    def post(self, request):
        email = request.data.get("email")
        password = request.data.get("password")
        logger.info("로그인 요청 도착함")

        if not email or not password:
            return Response(
                {"success": False, "message": "email/password 필수 입력"},
                status=status.HTTP_400_BAD_REQUEST,
            )

        try:
            user = Account.objects.get(email=email)
            if not user.check_password(password):
                return Response(
                    {"success": False, "message": "비밀번호가 일치하지 않습니다."},
                    status=status.HTTP_401_UNAUTHORIZED,
                )

            logger.info("로그인 성공함")
            return set_jwt_cookie_response(user, status_code=status.HTTP_200_OK)

        except Account.DoesNotExist:
            return Response(
                {"success": False, "message": "해당 이메일의 사용자가 없습니다."},
                status=status.HTTP_404_NOT_FOUND,
            )


class LogoutView(APIView):
    @swagger_auto_schema(
        operation_id="로그아웃",
        operation_description="JWT 토큰을 무효화하고 쿠키를 삭제합니다.",
        responses={200: "로그아웃 성공", 400: "refresh token 없음"},
    )
    def post(self, request):
        refresh_token = request.COOKIES.get("refresh_token")

        if not refresh_token:
            return Response(
                {"success": False, "message": "refresh token이 없습니다."},
                status=status.HTTP_400_BAD_REQUEST,
            )

        try:
            token = RefreshToken(refresh_token)
            token.blacklist()  # 토큰 블랙리스트 등록
        except Exception:
            logger.warning("잘못된 토큰 또는 이미 만료된 토큰")
            pass

        response = Response(
            {"success": True, "message": "로그아웃 성공", "error": None},
            status=status.HTTP_200_OK,
        )
        response.delete_cookie("access_token")
        response.delete_cookie("refresh_token")
        return response

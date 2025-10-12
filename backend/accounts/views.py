from django.shortcuts import render
from rest_framework.views import APIView
from rest_framework.response import Response
from rest_framework import status
from drf_yasg.utils import swagger_auto_schema

class LoginView(APIView):
    @swagger_auto_schema(operation_id="로그인", operation_description="사용자가 로그인합니다.", responses={200: "로그인 성공"})
    def post(self, request):
        return Response({"message": "로그인 성공"}, status=status.HTTP_200_OK)

class SignupView(APIView):
    @swagger_auto_schema(operation_id="회원가입", operation_description="새 사용자를 등록합니다.", responses={201: "회원가입 성공"})
    def post(self, request):
        return Response({"message": "회원가입 성공"}, status=status.HTTP_201_CREATED)

class LogoutView(APIView):
    @swagger_auto_schema(operation_id="로그아웃", operation_description="현재 사용자를 로그아웃합니다.", responses={200: "로그아웃 성공"})
    def post(self, request):
        return Response({"message": "로그아웃 성공"}, status=status.HTTP_200_OK)


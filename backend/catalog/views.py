from django.shortcuts import render
from rest_framework.views import APIView
from rest_framework.response import Response
from rest_framework import status
from drf_yasg.utils import swagger_auto_schema

# 과목 목록 조회
class SubjectListView(APIView):
    @swagger_auto_schema(
        operation_id="과목 목록 조회",
        operation_description="전체 과목(Subject)을 조회합니다.",
        responses={200: "Subject list"}
    )
    def get(self, request):
        return Response({"message": "과목 목록 조회"}, status=status.HTTP_200_OK)


# 주제 목록 조회
class TopicListView(APIView):
    @swagger_auto_schema(
        operation_id="주제 목록 조회",
        operation_description="특정 과목에 속한 주제 목록을 조회합니다.",
        responses={200: "Topic list"}
    )
    def get(self, request):
        return Response({"message": "주제 목록 조회"}, status=status.HTTP_200_OK)

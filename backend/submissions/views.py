from drf_yasg.utils import swagger_auto_schema
from rest_framework import status
from rest_framework.response import Response
from rest_framework.views import APIView


# 개인 과제 조회
class PersonalAssignmentListView(APIView):
    @swagger_auto_schema(
        operation_id="개인 과제 목록 조회",
        operation_description="학생 개인 과제 목록을 조회합니다.",
        responses={200: "PersonalAssignment list"},
    )
    def get(self, request):
        return Response({"message": "개인 과제 목록 조회"}, status=status.HTTP_200_OK)


# 답안 제출
class AnswerSubmitView(APIView):
    @swagger_auto_schema(
        operation_id="답안 제출",
        operation_description="학생이 문제에 대한 답안을 제출합니다.",
        responses={201: "Answer created"},
    )
    def post(self, request):
        return Response({"message": "답안 제출"}, status=status.HTTP_201_CREATED)

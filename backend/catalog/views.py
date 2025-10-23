from drf_yasg.utils import swagger_auto_schema
from rest_framework import status
from rest_framework.response import Response
from rest_framework.views import APIView


# 과목 목록 조회
class SubjectListView(APIView):
    @swagger_auto_schema(
        operation_id="과목 목록 조회",
        operation_description="전체 과목(Subject)을 조회합니다.",
        responses={200: "Subject list"},
    )
    def get(self, request):
        return Response({"message": "과목 목록 조회"}, status=status.HTTP_200_OK)

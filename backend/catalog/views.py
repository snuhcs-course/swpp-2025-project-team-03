from drf_yasg.utils import swagger_auto_schema
from rest_framework import status
from rest_framework.response import Response
from rest_framework.views import APIView

from .models import Subject
from .serializers import SubjectSerializer


class SubjectListView(APIView):
    @swagger_auto_schema(
        operation_id="과목 목록 조회",
        operation_description="현재 Subject 테이블에 등록된 과목의 목록을 조회합니다.",
        responses={200: SubjectSerializer(many=True)},
    )
    def get(self, request):
        try:
            subjects = Subject.objects.all().order_by("name")
            serializer = SubjectSerializer(subjects, many=True)
            return Response(
                {"success": True, "data": serializer.data, "message": "과목 목록 조회 성공"},
                status=status.HTTP_200_OK,
            )
        except Exception as e:
            return Response(
                {"success": False, "data": None, "error": str(e), "message": "과목 목록 조회 중 오류가 발생했습니다."},
                status=status.HTTP_500_INTERNAL_SERVER_ERROR,
            )

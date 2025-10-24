import logging

from drf_yasg.utils import swagger_auto_schema
from rest_framework import status
from rest_framework.response import Response
from rest_framework.views import APIView

from .serializers import CurriculumAnalysisSerializer
from .utils.analyze_achievement import parse_curriculum

logger = logging.getLogger(__name__)


class CurriculumAnalysisView(APIView):
    """성취기준 분석 및 통계량 조회 API"""

    @swagger_auto_schema(
        operation_id="성취기준 분석",
        operation_description="특정 학생과 클래스의 성취기준을 분석하고 통계량을 반환합니다.",
        responses={
            200: CurriculumAnalysisSerializer,
            400: "잘못된 요청",
            404: "학생 또는 클래스를 찾을 수 없음",
            500: "서버 내부 오류",
        },
        tags=["Reports"],
    )
    def get(self, request, class_id, student_id):
        """성취기준 분석 및 통계량 조회"""
        try:
            # URL 파라미터 검증
            if class_id <= 0:
                return Response(
                    {
                        "success": False,
                        "data": None,
                        "message": "클래스 ID는 양수여야 합니다.",
                        "error": "Invalid class_id",
                    },
                    status=status.HTTP_400_BAD_REQUEST,
                )

            if student_id <= 0:
                return Response(
                    {
                        "success": False,
                        "data": None,
                        "message": "학생 ID는 양수여야 합니다.",
                        "error": "Invalid student_id",
                    },
                    status=status.HTTP_400_BAD_REQUEST,
                )

            logger.info(f"성취기준 분석 요청: student_id={student_id}, class_id={class_id}")

            # 성취기준 분석 및 통계량 계산
            statistics = parse_curriculum(student_id, class_id)

            # 응답 데이터 검증
            response_serializer = CurriculumAnalysisSerializer(data=statistics)
            if not response_serializer.is_valid():
                logger.error(f"응답 데이터 검증 실패: {response_serializer.errors}")
                return Response(
                    {
                        "success": False,
                        "data": None,
                        "message": "응답 데이터 생성 중 오류가 발생했습니다.",
                        "error": "Internal server error",
                    },
                    status=status.HTTP_500_INTERNAL_SERVER_ERROR,
                )

            logger.info(f"성취기준 분석 완료: student_id={student_id}, class_id={class_id}")

            return Response(
                {
                    "success": True,
                    "data": response_serializer.validated_data,
                    "message": "성취기준 분석이 완료되었습니다.",
                    "error": None,
                },
                status=status.HTTP_200_OK,
            )

        except Exception as e:
            logger.error(f"성취기준 분석 중 오류 발생: {str(e)}")
            return Response(
                {"success": False, "data": None, "message": "성취기준 분석 중 오류가 발생했습니다.", "error": str(e)},
                status=status.HTTP_500_INTERNAL_SERVER_ERROR,
            )

import logging
import os

from django.conf import settings
from drf_yasg import openapi
from drf_yasg.utils import swagger_auto_schema
from rest_framework import status
from rest_framework.response import Response
from rest_framework.views import APIView

logger = logging.getLogger(__name__)


def create_api_response(
    success=True, data=None, message="성공", error=None, status_code=status.HTTP_200_OK
):  # pragma: no cover
    """API 응답을 생성하는 헬퍼 함수"""
    return Response({"success": success, "data": data, "message": message, "error": error}, status=status_code)


class LogTailView(APIView):  # pragma: no cover
    @swagger_auto_schema(
        operation_id="nohup.out 로그 조회",
        operation_description="nohup.out 파일의 마지막 n줄을 조회합니다.",
        manual_parameters=[
            openapi.Parameter(
                name="n",
                in_=openapi.IN_QUERY,
                description="조회할 줄 수 (양수)",
                type=openapi.TYPE_INTEGER,
                required=True,
            ),
        ],
        responses={
            200: openapi.Response(
                description="성공",
                examples={
                    "application/json": {
                        "success": True,
                        "data": {"lines": ["로그 라인 1", "로그 라인 2"]},
                        "message": "성공",
                        "error": None,
                    }
                },
            ),
            400: openapi.Response(description="잘못된 요청 (n이 양수가 아님)"),
            404: openapi.Response(description="nohup.out 파일을 찾을 수 없음"),
            500: openapi.Response(description="서버 오류"),
        },
    )
    def get(self, request):
        """nohup.out 파일의 마지막 n줄을 반환"""
        try:
            # n 파라미터 가져오기
            n_str = request.query_params.get("n")

            if n_str is None:
                logger.warning("[LogTailView] n 파라미터가 제공되지 않음")
                return create_api_response(
                    success=False,
                    error="n 파라미터가 필요합니다.",
                    message="잘못된 요청입니다.",
                    status_code=status.HTTP_400_BAD_REQUEST,
                )

            # n을 정수로 변환
            try:
                n = int(n_str)
            except ValueError:
                logger.warning(f"[LogTailView] n 파라미터가 정수가 아님: {n_str}")
                return create_api_response(
                    success=False,
                    error="n은 정수여야 합니다.",
                    message="잘못된 요청입니다.",
                    status_code=status.HTTP_400_BAD_REQUEST,
                )

            # n이 양수인지 체크
            if n <= 0:
                logger.warning(f"[LogTailView] n이 양수가 아님: {n}")
                return create_api_response(
                    success=False,
                    error="n은 양수여야 합니다.",
                    message="잘못된 요청입니다.",
                    status_code=status.HTTP_400_BAD_REQUEST,
                )

            # nohup.out 파일 경로
            base_dir = settings.BASE_DIR
            nohup_path = os.path.join(base_dir, "nohup.out")

            # 파일 존재 확인
            if not os.path.exists(nohup_path):
                logger.error(f"[LogTailView] nohup.out 파일을 찾을 수 없음: {nohup_path}")
                return create_api_response(
                    success=False,
                    error="nohup.out 파일을 찾을 수 없습니다.",
                    message="파일을 찾을 수 없습니다.",
                    status_code=status.HTTP_404_NOT_FOUND,
                )

            # 파일의 마지막 n줄 읽기
            with open(nohup_path, "r", encoding="utf-8", errors="ignore") as f:
                lines = f.readlines()
                tail_lines = lines[-n:] if len(lines) >= n else lines
                # 줄바꿈 문자 제거
                tail_lines = [line.rstrip("\n") for line in tail_lines]

            logger.info(f"[LogTailView] nohup.out의 마지막 {n}줄 조회 성공 (실제: {len(tail_lines)}줄)")

            return create_api_response(
                success=True,
                data={"lines": tail_lines, "requested": n, "returned": len(tail_lines)},
                message="로그 조회에 성공했습니다.",
                status_code=status.HTTP_200_OK,
            )

        except Exception as e:
            logger.error(f"[LogTailView] {e}", exc_info=True)
            return create_api_response(
                success=False,
                error=str(e),
                message="로그 조회 중 오류가 발생했습니다.",
                status_code=status.HTTP_500_INTERNAL_SERVER_ERROR,
            )


class HealthView(APIView):
    @swagger_auto_schema(
        operation_id="Health Check",
        operation_description="API 헬스 체크 엔드포인트입니다.",
        responses={
            200: openapi.Response(
                description="성공",
                examples={"application/json": {"message": "Hello, World!"}},
            ),
        },
    )
    def get(self, request):
        """Health check 엔드포인트 - Hello, World! 반환"""
        return Response({"message": "Hello, World!"}, status=status.HTTP_200_OK)

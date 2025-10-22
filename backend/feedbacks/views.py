import logging

from courses.models import CourseClass
from django.contrib.auth import get_user_model
from drf_yasg import openapi
from drf_yasg.utils import swagger_auto_schema
from feedbacks.models import TeacherFeedback
from feedbacks.request_serializers import MessageSendRequestSerializer
from feedbacks.serializers import MessageSerializer
from rest_framework import status
from rest_framework.response import Response
from rest_framework.views import APIView

logger = logging.getLogger(__name__)
Account = get_user_model()


def create_api_response(success=True, data=None, message="성공", error=None, status_code=status.HTTP_200_OK):
    """API 응답을 생성하는 헬퍼 함수"""
    return Response({"success": success, "data": data, "message": message, "error": error}, status=status_code)


class MessageSendView(APIView):
    """
    POST /messages/send
    메시지(피드백) 전송
    """

    @swagger_auto_schema(
        operation_id="메시지 전송",
        operation_description="선생님이 학생에게 피드백 메시지를 전송합니다.",
        request_body=MessageSendRequestSerializer,
        responses={
            201: "Message",
            400: "Invalid input",
            403: "Not a teacher",
            404: "Class or student not found",
        },
    )
    def post(self, request):
        try:
            # 요청 데이터 검증
            serializer = MessageSendRequestSerializer(data=request.data)
            serializer.is_valid(raise_exception=True)
            data = serializer.validated_data

            # 선생님 여부 확인 (인증 구현 후 request.user 사용)
            # TODO: 인증 미들웨어 구현 후 request.user로 변경
            teacher_id = request.data.get("teacher_id")  # 임시로 받음
            if not teacher_id:
                return create_api_response(
                    success=False,
                    error="teacher_id is required",
                    message="teacher_id is required",
                    status_code=status.HTTP_400_BAD_REQUEST,
                )

            try:
                teacher = Account.objects.get(id=teacher_id)
                if teacher.is_student:
                    return create_api_response(
                        success=False,
                        error="Only teachers can send feedback",
                        message="Only teachers can send feedback",
                        status_code=status.HTTP_403_FORBIDDEN,
                    )
            except Account.DoesNotExist:
                return create_api_response(
                    success=False,
                    error="Invalid teacher_id",
                    message="Invalid teacher_id",
                    status_code=status.HTTP_404_NOT_FOUND,
                )

            # 클래스 조회
            try:
                course_class = CourseClass.objects.get(id=data["class_id"])
            except CourseClass.DoesNotExist:
                return create_api_response(
                    success=False,
                    error="Class not found",
                    message="Class not found",
                    status_code=status.HTTP_404_NOT_FOUND,
                )

            # 학생 조회
            try:
                student = Account.objects.get(id=data["student_id"])
            except Account.DoesNotExist:
                return create_api_response(
                    success=False,
                    error="Student not found",
                    message="Student not found",
                    status_code=status.HTTP_404_NOT_FOUND,
                )

            # 피드백 생성
            feedback = TeacherFeedback.objects.create(
                course_class=course_class, student=student, teacher=teacher, content=data["content"]
            )

            logger.info(
                f"[MessageSendView] Teacher {teacher.id} sent feedback to student {student.id} in class {course_class.id}"
            )

            # 응답
            response_serializer = MessageSerializer(feedback)
            return create_api_response(
                data=response_serializer.data, message="메시지 전송 성공", status_code=status.HTTP_201_CREATED
            )

        except Exception as e:
            logger.error(f"[MessageSendView] Error: {e}", exc_info=True)
            return create_api_response(
                success=False,
                error=str(e),
                message="메시지 전송 실패",
                status_code=status.HTTP_500_INTERNAL_SERVER_ERROR,
            )


class ClassMessageListView(APIView):
    """
    GET /messages/{classId}
    특정 클래스의 모든 메시지(피드백) 조회
    """

    @swagger_auto_schema(
        operation_id="클래스 메시지 조회",
        operation_description="특정 클래스의 모든 피드백 메시지를 조회합니다.",
        manual_parameters=[
            openapi.Parameter(
                "classId", openapi.IN_PATH, description="클래스 ID", type=openapi.TYPE_INTEGER, required=True
            )
        ],
        responses={
            200: MessageSerializer(many=True),
            404: "Class not found",
        },
    )
    def get(self, request, classId):
        try:
            # 클래스 존재 여부 확인
            try:
                course_class = CourseClass.objects.get(id=classId)
            except CourseClass.DoesNotExist:
                return create_api_response(
                    success=False,
                    error="Class not found",
                    message="Class not found",
                    status_code=status.HTTP_404_NOT_FOUND,
                )

            # 클래스의 모든 피드백 조회 (최신순)
            feedbacks = (
                TeacherFeedback.objects.filter(course_class=course_class)
                .select_related("teacher", "student", "course_class")
                .order_by("-created_at")
            )

            serializer = MessageSerializer(feedbacks, many=True)

            logger.info(f"[ClassMessageListView] Retrieved {feedbacks.count()} messages for class {classId}")

            return create_api_response(
                data=serializer.data, message="클래스 메시지 조회 성공", status_code=status.HTTP_200_OK
            )

        except Exception as e:
            logger.error(f"[ClassMessageListView] Error: {e}", exc_info=True)
            return create_api_response(
                success=False,
                error=str(e),
                message="클래스 메시지 조회 실패",
                status_code=status.HTTP_500_INTERNAL_SERVER_ERROR,
            )


class MessageListView(APIView):
    """
    GET /messages?userId={userId}&limit={limit}
    특정 사용자의 메시지(피드백) 조회
    """

    @swagger_auto_schema(
        operation_id="사용자 메시지 조회",
        operation_description=(
            "특정 사용자의 피드백 메시지를 조회합니다.\n\n- 학생: 받은 피드백 조회\n- 선생님: 보낸 피드백 조회"
        ),
        manual_parameters=[
            openapi.Parameter(
                "userId", openapi.IN_QUERY, description="사용자 ID", type=openapi.TYPE_INTEGER, required=True
            ),
            openapi.Parameter(
                "limit",
                openapi.IN_QUERY,
                description="조회 개수 (기본값: 50)",
                type=openapi.TYPE_INTEGER,
                required=False,
            ),
        ],
        responses={
            200: MessageSerializer(many=True),
            400: "userId is required",
            404: "User not found",
        },
    )
    def get(self, request):
        try:
            # Query Parameters 추출
            user_id = request.query_params.get("userId")
            limit = request.query_params.get("limit", 50)

            if not user_id:
                return create_api_response(
                    success=False,
                    error="userId is required",
                    message="userId is required",
                    status_code=status.HTTP_400_BAD_REQUEST,
                )

            try:
                limit = int(limit)
                if limit <= 0:
                    limit = 50
            except ValueError:
                limit = 50

            # 사용자 조회
            try:
                user = Account.objects.get(id=user_id)
            except Account.DoesNotExist:
                return create_api_response(
                    success=False,
                    error="User not found",
                    message="User not found",
                    status_code=status.HTTP_404_NOT_FOUND,
                )

            # 역할에 따라 다른 쿼리
            if user.is_student:
                # 학생: 받은 피드백
                feedbacks = (
                    TeacherFeedback.objects.filter(student=user)
                    .select_related("teacher", "student", "course_class")
                    .order_by("-created_at")[:limit]
                )
            else:
                # 선생님: 보낸 피드백
                feedbacks = (
                    TeacherFeedback.objects.filter(teacher=user)
                    .select_related("teacher", "student", "course_class")
                    .order_by("-created_at")[:limit]
                )

            serializer = MessageSerializer(feedbacks, many=True)

            logger.info(f"[MessageListView] Retrieved {len(feedbacks)} messages for user {user_id}")

            return create_api_response(
                data=serializer.data, message="사용자 메시지 조회 성공", status_code=status.HTTP_200_OK
            )

        except Exception as e:
            logger.error(f"[MessageListView] Error: {e}", exc_info=True)
            return create_api_response(
                success=False,
                error=str(e),
                message="사용자 메시지 조회 실패",
                status_code=status.HTTP_500_INTERNAL_SERVER_ERROR,
            )


# 기존의 다른 View들 (Dashboard, Analysis 등)은 그대로 유지
class DashboardStatsView(APIView):
    """대시보드 통계 조회 (추후 구현)"""

    def get(self, request):
        return Response({"message": "Dashboard stats - to be implemented"})


class DashboardRecentActivitiesView(APIView):
    """최근 활동 조회 (추후 구현)"""

    def get(self, request):
        return Response({"message": "최근 활동 조회"}, status=status.HTTP_200_OK)


class AnalysisStudentView(APIView):  # POST /analysis/student
    @swagger_auto_schema(
        operation_id="학생 분석",
        operation_description="학생의 학습 데이터를 분석합니다.",
        responses={200: "Student analysis result"},
    )
    def post(self, request):
        return Response({"message": "학생 분석 결과"}, status=status.HTTP_200_OK)


class AnalysisClassView(APIView):  # POST /analysis/class
    @swagger_auto_schema(
        operation_id="클래스 분석",
        operation_description="클래스의 학습 데이터를 분석합니다.",
        responses={200: "Class analysis result"},
    )
    def post(self, request):
        return Response({"message": "클래스 분석 결과"}, status=status.HTTP_200_OK)


class AnalysisSubjectView(APIView):  # POST /analysis/subject
    @swagger_auto_schema(
        operation_id="과목 분석",
        operation_description="과목의 학습 데이터를 분석합니다.",
        responses={200: "Subject analysis result"},
    )
    def post(self, request):
        return Response({"message": "과목 분석 결과"}, status=status.HTTP_200_OK)


class ProgressReportView(APIView):  # GET /reports/progress
    @swagger_auto_schema(
        operation_id="진도 보고서 조회",
        operation_description="학생의 진도 보고서를 조회합니다.",
        responses={200: "Progress report"},
    )
    def get(self, request):
        return Response({"message": "진도 보고서 조회"}, status=status.HTTP_200_OK)

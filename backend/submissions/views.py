import logging

from django.contrib.auth import get_user_model
from drf_yasg.utils import swagger_auto_schema
from rest_framework import status
from rest_framework.response import Response
from rest_framework.views import APIView

from .models import PersonalAssignment
from .serializers import PersonalAssignmentSerializer

logger = logging.getLogger(__name__)
Account = get_user_model()


def create_api_response(success=True, data=None, message="성공", error=None, status_code=status.HTTP_200_OK):
    """API 응답을 생성하는 헬퍼 함수"""
    return Response({"success": success, "data": data, "message": message, "error": error}, status=status_code)


# 개인 과제 조회
class PersonalAssignmentListView(APIView):
    @swagger_auto_schema(
        operation_id="개인 과제 목록 조회",
        operation_description="학생 개인 과제 목록을 조회합니다. assignment_id 또는 student_id로 필터링 가능합니다.",
        responses={200: PersonalAssignmentSerializer(many=True)},
    )
    def get(self, request):
        """
        개인 과제 목록 조회

        Query Parameters:
            - assignment_id (optional): 과제 ID로 필터링
            - student_id (optional): 학생 ID로 필터링
            - 최소 하나는 필수, 둘 다 있으면 AND 조건으로 필터링
        """
        try:
            assignment_id = request.query_params.get("assignment_id")
            student_id = request.query_params.get("student_id")

            # 둘 다 없으면 에러
            if not assignment_id and not student_id:
                return create_api_response(
                    success=False,
                    error="Missing required parameter",
                    message="assignment_id 또는 student_id 중 하나 이상을 제공해야 합니다.",
                    status_code=status.HTTP_400_BAD_REQUEST,
                )

            # 기본 쿼리셋
            queryset = PersonalAssignment.objects.select_related("student", "assignment").all()

            # assignment_id로 필터링
            if assignment_id:
                queryset = queryset.filter(assignment_id=assignment_id)

            # student_id로 필터링
            if student_id:
                queryset = queryset.filter(student_id=student_id)

            # 직렬화
            serializer = PersonalAssignmentSerializer(queryset, many=True)

            return create_api_response(
                data=serializer.data, message="개인 과제 목록 조회 성공", status_code=status.HTTP_200_OK
            )

        except Exception as e:
            logger.error(f"[PersonalAssignmentListView] {e}", exc_info=True)
            return create_api_response(
                success=False,
                error=str(e),
                message="개인 과제 목록 조회 중 오류가 발생했습니다.",
                status_code=status.HTTP_500_INTERNAL_SERVER_ERROR,
            )


# 퀴즈 조회
class PersonalAssignmentQuestionsView(APIView):
    @swagger_auto_schema(
        operation_id="개인 과제 문제 조회",
        operation_description="학생 개인 과제의 문제 목록을 조회합니다.",
        responses={200: "문제 목록"},
    )
    def get(self, request, id):
        """
        개인 과제 문제 목록 조회

        Path Parameters:
            - id: PersonalAssignment ID
        """
        try:
            # PersonalAssignment 조회
            personal_assignment = PersonalAssignment.objects.get(id=id)

            # 문제 목록 조회 (recalled_num=0인 기본 문제만)
            questions = personal_assignment.questions.filter(recalled_num=0).order_by("number")
            question_data = []
            for q in questions:
                question_data.append(
                    {
                        "id": q.id,
                        "number": q.number,
                        "question": q.content,
                        "answer": q.model_answer,
                        "explanation": q.explanation,
                        "difficulty": q.difficulty,
                    }
                )

            return create_api_response(
                data=question_data, message="개인 과제 문제 목록 조회 성공", status_code=status.HTTP_200_OK
            )

        except PersonalAssignment.DoesNotExist:
            return create_api_response(
                success=False,
                error="PersonalAssignment not found",
                message="해당 개인 과제를 찾을 수 없습니다.",
                status_code=status.HTTP_404_NOT_FOUND,
            )
        except Exception as e:
            logger.error(f"[PersonalAssignmentQuestionsView] {e}", exc_info=True)
            return create_api_response(
                success=False,
                error=str(e),
                message="개인 과제 문제 목록 조회 중 오류가 발생했습니다.",
                status_code=status.HTTP_500_INTERNAL_SERVER_ERROR,
            )


# 답안 제출
class AnswerSubmitView(APIView):
    @swagger_auto_schema(
        operation_id="답안 제출",
        operation_description="학생이 문제에 대한 답안을 제출합니다.",
        responses={201: "Answer created"},
    )
    def post(self, request):
        return Response({"message": "답안 제출"}, status=status.HTTP_201_CREATED)

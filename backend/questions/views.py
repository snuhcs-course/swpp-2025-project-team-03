import tempfile

import boto3
from assignments.models import Assignment, Material
from django.conf import settings
from drf_yasg.utils import swagger_auto_schema
from httpx import NetworkError, TimeoutException
from openai import OpenAIError
from rest_framework import status
from rest_framework.response import Response
from rest_framework.views import APIView
from submissions.models import PersonalAssignment

from .models import Question
from .request_serializers import QuestionCreateRequestSerializer
from .serializers import QuestionCreateSerializer
from .utils.base_question_generator import generate_base_quizzes
from .utils.pdf_to_text import summarize_pdf_from_s3


class QuestionCreateView(APIView):
    """
    POST /questions/generate
    S3의 PDF를 요약하고, 그 내용을 기반으로 자동 문제를 생성합니다.
    """

    @swagger_auto_schema(
        operation_id="PDF 요약 & 기본 질문 생성",
        operation_description=(
            "S3의 PDF를 요약하고 그 내용을 바탕으로 자동으로 질문을 생성합니다.\n\n"
            "- 입력: assignment_id, material_id, total_number\n"
            "- 출력: 요약 결과 + 생성된 질문 리스트"
        ),
        request_body=QuestionCreateRequestSerializer,
        responses={
            200: QuestionCreateSerializer,
            404: "Invalid assignment_id or material_id",
            500: "Summarization or question generation failed",
        },
    )
    def post(self, request):
        serializer = QuestionCreateRequestSerializer(data=request.data)
        serializer.is_valid(raise_exception=True)
        data = serializer.validated_data

        assignment_id = data["assignment_id"]
        material_id = data["material_id"]

        # Assignment, Material 가져오기
        try:
            assignment = Assignment.objects.get(id=assignment_id)
        except Assignment.DoesNotExist:
            return Response({"error": "Invalid assignment_id"}, status=status.HTTP_404_NOT_FOUND)

        try:
            material = Material.objects.get(id=material_id, assignment=assignment)
        except Material.DoesNotExist:
            return Response({"error": "Invalid material_id"}, status=status.HTTP_404_NOT_FOUND)

        try:
            if material.summary:
                summarized_text = material.summary
            else:
                s3_key = material.s3_key
                s3 = boto3.client(
                    "s3",
                    aws_access_key_id=settings.AWS_ACCESS_KEY_ID,
                    aws_secret_access_key=settings.AWS_SECRET_ACCESS_KEY,
                    region_name=settings.AWS_REGION,
                )

                with tempfile.NamedTemporaryFile(suffix=".pdf", delete=False) as tmp:
                    s3.download_fileobj(settings.AWS_STORAGE_BUCKET_NAME, s3_key, tmp)
                    tmp.flush()
                    local_pdf = tmp.name

                try:
                    summarized_text = summarize_pdf_from_s3(local_pdf)
                except TimeoutException:
                    return Response({"error": "OpenAI API timeout 발생"}, status=500)
                except OpenAIError as e:
                    return Response({"error": f"OpenAI API 오류: {e}"}, status=500)
                except NetworkError as e:
                    return Response({"error": f"네트워크 오류: {e}"}, status=500)
                except Exception as e:
                    return Response({"error": f"예상치 못한 오류: {e}"}, status=500)

                material.summary = summarized_text
                material.save()

            quizzes = generate_base_quizzes(summarized_text, n=data["total_number"])

            # 해당 assignment에 연결된 모든 personal assignment들 가져오기
            personal_assignments = PersonalAssignment.objects.filter(assignment=assignment)

            if not personal_assignments.exists():
                return Response(
                    {"error": "No personal assignments found for this assignment"}, status=status.HTTP_400_BAD_REQUEST
                )

            created_questions = []
            total_questions_created = 0

            # 각 personal assignment에 대해 질문 생성
            for personal_assignment in personal_assignments:
                for i, quiz in enumerate(quizzes, 1):
                    q = Question.objects.create(
                        personal_assignment=personal_assignment,
                        number=i,
                        content=quiz.question,
                        recalled_num=0,
                        explanation=quiz.explanation,
                        model_answer=quiz.model_answer,
                        difficulty=quiz.difficulty.lower(),
                    )
                    total_questions_created += 1

                    # 첫 번째 personal assignment의 질문들만 응답에 포함
                    if personal_assignment == personal_assignments.first():
                        created_questions.append(
                            {
                                "id": q.id,
                                "number": q.number,
                                "question": q.content,
                                "answer": q.model_answer,
                                "explanation": q.explanation,
                                "difficulty": q.difficulty,
                            }
                        )

            # 모든 작업이 성공적으로 완료된 후 assignment의 total_questions 업데이트
            assignment.total_questions = data["total_number"]
            assignment.save()

            return Response(
                {
                    "assignment_id": assignment.id,
                    "material_id": material.id,
                    "summary_preview": summarized_text[:100],
                    "questions": created_questions,
                },
                status=status.HTTP_200_OK,
            )

        except Exception as e:
            return Response({"error": str(e)}, status=status.HTTP_500_INTERNAL_SERVER_ERROR)

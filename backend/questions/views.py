import tempfile

import boto3
from assignments.models import Assignment, Material
from django.conf import settings
from drf_yasg.utils import swagger_auto_schema
from questions.models import Question
from questions.serializers import QuestionCreateRequestSerializer
from questions.utils.base_question_generator import generate_base_quizzes
from questions.utils.pdf_to_text import summarize_pdf_from_s3
from rest_framework import status
from rest_framework.response import Response
from rest_framework.views import APIView


class QuestionCreateView(APIView):
    @swagger_auto_schema(
        operation_id="PDF Summarize + Question Generate",
        operation_description="S3의 PDF를 요약하고 그 내용을 바탕으로 자동으로 질문을 생성합니다.",
        request_body=QuestionCreateRequestSerializer,
        responses={200: "Summarization & Question generation complete"},
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

        s3_key = material.s3_key
        s3 = boto3.client(
            "s3",
            aws_access_key_id=settings.AWS_ACCESS_KEY_ID,
            aws_secret_access_key=settings.AWS_SECRET_ACCESS_KEY,
            region_name=settings.AWS_REGION,
        )

        try:
            # --- PDF 다운로드 ---
            with tempfile.NamedTemporaryFile(suffix=".pdf", delete=False) as tmp:
                s3.download_fileobj(settings.AWS_STORAGE_BUCKET_NAME, s3_key, tmp)
                tmp.flush()
                local_pdf = tmp.name

            # --- Vision summarization ---
            summarized_text = summarize_pdf_from_s3(local_pdf)

            # --- Text Material 생성 ---
            text_material = Material.objects.create(
                assignment=assignment,
                kind=Material.Kind.TEXT,
                s3_key=s3_key.replace(".pdf", "_summary.txt"),
                bytes=len(summarized_text.encode("utf-8")),
            )

            # --- Quiz Generation ---
            quizzes = generate_base_quizzes(summarized_text, n=data["total_number"])

            created_questions = []
            for i, quiz in enumerate(quizzes, 1):
                q = Question.objects.create(
                    personal_assignment=None,  # 테스트 목적, 나중에 연동
                    number=i,
                    content=quiz.question,
                    # topic=quiz.topic,
                    explanation=quiz.explanation,
                    model_answer=quiz.model_answer,
                    difficulty=quiz.difficulty,
                )
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

            return Response(
                {
                    "assignment_id": assignment.id,
                    "material_summary_id": text_material.id,
                    "summary_preview": summarized_text[:500],
                    "questions": created_questions,
                },
                status=status.HTTP_200_OK,
            )

        except Exception as e:
            return Response({"error": str(e)}, status=status.HTTP_500_INTERNAL_SERVER_ERROR)

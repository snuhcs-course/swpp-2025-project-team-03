import tempfile

import boto3
from assignments.models import Assignment, Material
from django.conf import settings
from drf_yasg.utils import swagger_auto_schema
from questions.serializers import QuestionCreateRequestSerializer
from questions.utils.pdf_to_text import summarize_pdf_from_s3
from rest_framework import status
from rest_framework.response import Response
from rest_framework.views import APIView


class QuestionCreateView(APIView):  # POST /questions/create/
    @swagger_auto_schema(
        operation_id="PDF Summarize",
        operation_description="S3에 업로드된 PDF를 Vision 기반으로 요약하고 Material(kind='text')로 저장합니다.",
        request_body=QuestionCreateRequestSerializer,
        responses={200: "Summarization complete"},
    )
    def post(self, request):
        serializer = QuestionCreateRequestSerializer(data=request.data)
        serializer.is_valid(raise_exception=True)
        data = serializer.validated_data

        assignment_id = data["assignment_id"]
        material_id = data["material_id"]

        # Assignment & Material 존재 검증
        try:
            assignment = Assignment.objects.get(id=assignment_id)
        except Assignment.DoesNotExist:
            return Response({"error": "Invalid assignment_id"}, status=status.HTTP_404_NOT_FOUND)

        try:
            material = Material.objects.get(id=material_id, assignment=assignment)
        except Material.DoesNotExist:
            return Response({"error": "Invalid material_id for this assignment"}, status=status.HTTP_404_NOT_FOUND)

        s3_key = material.s3_key
        s3 = boto3.client(
            "s3",
            aws_access_key_id=settings.AWS_ACCESS_KEY_ID,
            aws_secret_access_key=settings.AWS_SECRET_ACCESS_KEY,
            region_name=settings.AWS_REGION,
        )

        try:
            with tempfile.NamedTemporaryFile(suffix=".pdf", delete=False) as tmp:
                s3.download_fileobj(settings.AWS_STORAGE_BUCKET_NAME, s3_key, tmp)
                tmp.flush()
                local_pdf = tmp.name

            # Summarize PDF
            summarized_text = summarize_pdf_from_s3(local_pdf)

            # Material 추가 (TEXT)
            text_material = Material.objects.create(
                assignment=assignment,
                kind=Material.Kind.TEXT,
                s3_key=s3_key.replace(".pdf", "_summary.txt"),
                bytes=len(summarized_text.encode("utf-8")),
            )

            return Response(
                {
                    "assignment_id": assignment.id,
                    "source_material_id": material.id,
                    "summary_material_id": text_material.id,
                    "summary_preview": summarized_text[:1000],
                },
                status=status.HTTP_200_OK,
            )

        except Exception as e:
            return Response({"error": str(e)}, status=status.HTTP_500_INTERNAL_SERVER_ERROR)

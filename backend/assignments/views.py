import uuid
from datetime import datetime

import boto3
from courses.models import CourseClass
from dateutil import parser
from django.conf import settings
from django.utils import timezone
from drf_yasg.utils import swagger_auto_schema
from rest_framework import status
from rest_framework.response import Response
from rest_framework.views import APIView

from .models import Assignment, Material
from .serializers import AssignmentCreateSerializer


class AssignmentListView(APIView):  # GET /assignments
    @swagger_auto_schema(
        operation_id="과제 목록 조회",
        operation_description="모든 과제를 조회합니다.",
        responses={200: "Assignment list"},
    )
    def get(self, request):
        return Response({"message": "모든 과제 조회"}, status=status.HTTP_200_OK)


class AssignmentDetailView(APIView):  # GET, PUT, DELETE /assignments/{id}
    @swagger_auto_schema(
        operation_id="과제 상세 조회",
        operation_description="특정 과제의 상세 정보를 조회합니다.",
        responses={200: "Assignment detail"},
    )
    def get(self, request, id):
        return Response({"message": f"{id}번 과제 조회"}, status=status.HTTP_200_OK)

    @swagger_auto_schema(
        operation_id="과제 수정", operation_description="특정 과제를 수정합니다.", responses={200: "Assignment updated"}
    )
    def put(self, request, id):
        return Response({"message": "과제 수정"}, status=status.HTTP_200_OK)

    @swagger_auto_schema(
        operation_id="과제 삭제", operation_description="특정 과제를 삭제합니다.", responses={200: "Assignment deleted"}
    )
    def delete(self, request, id):
        return Response({"message": "과제 삭제"}, status=status.HTTP_200_OK)


class AssignmentCreateView(APIView):  # POST /assignments
    @swagger_auto_schema(
        operation_id="과제 생성",
        operation_description="새로운 과제를 생성하고 PDF 업로드용 presigned URL을 반환합니다.",
        request_body=AssignmentCreateSerializer,
        responses={201: "Assignment created with presigned S3 URL"},
    )
    def post(self, request):
        serializer = AssignmentCreateSerializer(data=request.data)
        serializer.is_valid(raise_exception=True)
        data = serializer.validated_data

        # assignment에 연동될 course를 조회
        try:
            course_class = CourseClass.objects.get(id=data["class_id"])
        except CourseClass.DoesNotExist:
            # TODO: course_class api를 개발한 이후에는 pass를 지우고 밑에 주석을 해제해야합니다!!
            course_class = None
            pass
            # return Response({"error": "Invalid class_id"}, status=status.HTTP_400_BAD_REQUEST)

        # due_at을 timezone-aware로 변환 (유연한 파싱)
        # 예: "2025-10-25T23:59:00+09:00", "2025-10-25 23:59", "2025-10-25T23:59Z" 등 모두 허용
        try:
            raw_due = str(data["due_at"]).strip()  # 혹시 모를 공백 제거
            due_at = parser.parse(raw_due)
            if due_at.tzinfo is None:
                due_at = timezone.make_aware(due_at, timezone.get_current_timezone())
        except Exception as e:
            return Response(
                {"error": "Invalid due_at format (use ISO 8601)", "detail": str(e)},
                status=status.HTTP_400_BAD_REQUEST,
            )

        assignment = Assignment.objects.create(
            course_class=course_class,
            title=data["title"],
            description=data.get("description", ""),
            visible_from=datetime.now(),
            due_at=due_at,
        )

        # S3 presigned URL 생성
        s3_key = f"pdf/{data['class_id']}/{assignment.id}/{uuid.uuid4()}.pdf"

        s3_client = boto3.client(
            "s3",
            aws_access_key_id=settings.AWS_ACCESS_KEY_ID,
            aws_secret_access_key=settings.AWS_SECRET_ACCESS_KEY,
            region_name=settings.AWS_REGION,
            endpoint_url=f"https://s3.{settings.AWS_REGION}.amazonaws.com",
        )

        try:
            presigned_url = s3_client.generate_presigned_url(
                ClientMethod="put_object",
                Params={
                    "Bucket": settings.AWS_STORAGE_BUCKET_NAME,
                    "Key": s3_key,
                    "ContentType": "application/pdf",
                },
                ExpiresIn=3600,
                HttpMethod="PUT",
            )
        except Exception as e:
            assignment.delete()  # 실패 시 rollback
            return Response(
                {"error": f"Failed to generate presigned URL: {str(e)}"}, status=status.HTTP_500_INTERNAL_SERVER_ERROR
            )

        # Material 행 추가
        material = Material.objects.create(
            assignment=assignment,
            kind=Material.Kind.PDF,
            s3_key=s3_key,
            bytes=None,  # 아직 업로드 전
        )

        # Response 반환
        return Response(
            {
                "assignment_id": assignment.id,
                "material_id": material.id,
                "s3_key": s3_key,
                "upload_url": presigned_url,
            },
            status=status.HTTP_201_CREATED,
        )


class AssignmentSubmitView(APIView):  # POST /assignments/{id}/submit
    @swagger_auto_schema(
        operation_id="과제 제출",
        operation_description="학생이 특정 과제를 제출합니다.",
        responses={201: "Assignment submitted"},
    )
    def post(self, request, id):
        return Response({"message": "과제 제출 완료"}, status=status.HTTP_201_CREATED)


class AssignmentResultsView(APIView):  # GET /assignments/{id}/results
    @swagger_auto_schema(
        operation_id="과제 결과 조회",
        operation_description="특정 과제의 제출 결과를 조회합니다.",
        responses={200: "Assignment results"},
    )
    def get(self, request, id):
        return Response({"message": "과제 결과 조회"}, status=status.HTTP_200_OK)


class AssignmentQuestionsView(APIView):  # GET /assignments/{id}/questions
    @swagger_auto_schema(
        operation_id="과제 문제 목록 조회",
        operation_description="특정 과제에 포함된 문제 목록을 조회합니다.",
        responses={200: "Assignment questions"},
    )
    def get(self, request, id):
        return Response({"message": "과제 문제 목록 조회"}, status=status.HTTP_200_OK)


class AssignmentDraftView(APIView):  # POST /assignments/{id}/draft
    @swagger_auto_schema(
        operation_id="과제 초안 저장",
        operation_description="특정 과제의 초안을 저장합니다.",
        responses={200: "Assignment draft saved"},
    )
    def post(self, request, id):
        return Response({"message": "과제 초안 저장"}, status=status.HTTP_200_OK)

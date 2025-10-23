import logging
import uuid
from datetime import datetime

import boto3
from catalog.models import Subject
from courses.models import CourseClass, Enrollment
from dateutil import parser
from django.conf import settings
from django.utils import timezone
from drf_yasg.utils import swagger_auto_schema
from rest_framework import status
from rest_framework.response import Response
from rest_framework.views import APIView
from submissions.models import PersonalAssignment

from .models import Assignment, Material
from .request_serializers import AssignmentCreateRequestSerializer
from .serializers import (
    AssignmentCreateSerializer,
    AssignmentDetailSerializer,
    AssignmentSerializer,
    AssignmentUpdateSerializer,
)

logger = logging.getLogger(__name__)


def create_api_response(success=True, data=None, message="성공", error=None, status_code=status.HTTP_200_OK):
    """API 응답을 생성하는 헬퍼 함수"""
    return Response({"success": success, "data": data, "message": message, "error": error}, status=status_code)


class AssignmentListView(APIView):  # GET /assignments
    @swagger_auto_schema(
        operation_id="과제 목록 조회",
        operation_description="모든 과제를 조회합니다. teacherId, classId로 필터링 가능합니다.",
        responses={200: "Assignment list"},
    )
    def get(self, request):
        try:
            # 기본 쿼리셋
            assignments = Assignment.objects.select_related(
                "course_class", "course_class__subject", "course_class__teacher"
            ).prefetch_related("materials")

            # 필터링
            teacher_id = request.query_params.get("teacherId")
            class_id = request.query_params.get("classId")

            if teacher_id:
                assignments = assignments.filter(course_class__teacher_id=teacher_id)

            if class_id:
                assignments = assignments.filter(course_class_id=class_id)

            serializer = AssignmentSerializer(assignments, many=True)
            return create_api_response(data=serializer.data, message="과제 목록 조회 성공")
        except Exception as e:
            logger.error(f"[AssignmentListView] {e}", exc_info=True)
            return create_api_response(
                success=False,
                error=str(e),
                message="과제 목록 조회 중 오류가 발생했습니다.",
                status_code=status.HTTP_500_INTERNAL_SERVER_ERROR,
            )


class AssignmentDetailView(APIView):  # GET, PUT, DELETE /assignments/{id}
    @swagger_auto_schema(
        operation_id="과제 상세 조회",
        operation_description="특정 과제의 상세 정보를 조회합니다.",
        responses={200: "Assignment detail"},
    )
    def get(self, request, id):
        try:
            assignment = Assignment.objects.select_related("course_class").prefetch_related("materials").get(id=id)
            serializer = AssignmentDetailSerializer(assignment)
            return create_api_response(data=serializer.data, message="과제 상세 조회 성공")

        except Assignment.DoesNotExist:
            return create_api_response(
                success=False,
                error="Assignment not found",
                message="해당 과제를 찾을 수 없습니다.",
                status_code=status.HTTP_404_NOT_FOUND,
            )
        except Exception as e:
            logger.error(f"[AssignmentDetailView] {e}", exc_info=True)
            return create_api_response(
                success=False,
                error=str(e),
                message="과제 상세 조회 중 오류가 발생했습니다.",
                status_code=status.HTTP_500_INTERNAL_SERVER_ERROR,
            )

    @swagger_auto_schema(
        operation_id="과제 수정",
        operation_description="특정 과제를 수정합니다.",
        request_body=AssignmentUpdateSerializer,
        responses={200: "Assignment updated"},
    )
    def put(self, request, id):
        try:
            assignment = Assignment.objects.get(id=id)
            serializer = AssignmentUpdateSerializer(assignment, data=request.data, partial=True)

            if serializer.is_valid():
                for field, value in serializer.validated_data.items():
                    if value is not None:
                        setattr(assignment, field, value)
                assignment.save()
                # 업데이트된 객체를 다시 조회하여 모든 관계를 포함한 데이터 반환
                updated_assignment = (
                    Assignment.objects.select_related("course_class", "course_class__subject", "course_class__teacher")
                    .prefetch_related("materials")
                    .get(id=id)
                )
                response_serializer = AssignmentDetailSerializer(updated_assignment)
                return create_api_response(data=response_serializer.data, message="과제 수정 성공")

            return create_api_response(
                success=False,
                error=serializer.errors,
                message="입력값 오류",
                status_code=status.HTTP_400_BAD_REQUEST,
            )

        except Assignment.DoesNotExist:
            return create_api_response(
                success=False,
                error="Assignment not found",
                message="해당 과제를 찾을 수 없습니다.",
                status_code=status.HTTP_404_NOT_FOUND,
            )

        except Exception as e:
            logger.error(f"[AssignmentDetailView] {e}", exc_info=True)
            return create_api_response(
                success=False,
                error=str(e),
                message="과제 수정 중 오류가 발생했습니다.",
                status_code=status.HTTP_500_INTERNAL_SERVER_ERROR,
            )

    @swagger_auto_schema(
        operation_id="과제 삭제", operation_description="특정 과제를 삭제합니다.", responses={204: "Assignment deleted"}
    )
    def delete(self, request, id):
        try:
            assignment = Assignment.objects.get(id=id)
            assignment.delete()
            return create_api_response(message="과제 삭제 성공")
        except Assignment.DoesNotExist:
            return create_api_response(
                success=False,
                error="Assignment not found",
                message="해당 과제를 찾을 수 없습니다.",
                status_code=status.HTTP_404_NOT_FOUND,
            )
        except Exception as e:
            logger.error(f"[AssignmentDetailView] {e}", exc_info=True)
            return create_api_response(
                success=False,
                error=str(e),
                message="과제 삭제 중 오류가 발생했습니다.",
                status_code=status.HTTP_500_INTERNAL_SERVER_ERROR,
            )


class AssignmentCreateView(APIView):  # POST /assignments
    """
    POST /assignments
    새로운 과제를 생성하고 PDF 업로드용 presigned S3 URL을 반환합니다.
    """

    @swagger_auto_schema(
        operation_id="과제 생성",
        operation_description=(
            "새로운 과제를 생성하고 업로드용 S3 presigned URL을 반환합니다.\n\n"
            "- 요청: class_id, title, due_at, description\n"
            "- 응답: assignment_id, material_id, s3_key, upload_url"
        ),
        request_body=AssignmentCreateRequestSerializer,
        responses={
            201: AssignmentCreateSerializer,
            400: "Invalid input or wrong request format",
            500: "Failed to generate presigned URL",
        },
    )
    def post(self, request):
        serializer = AssignmentCreateRequestSerializer(data=request.data)
        serializer.is_valid(raise_exception=True)
        data = serializer.validated_data

        # assignment에 연동될 course를 조회
        try:
            course_class = CourseClass.objects.get(id=data["class_id"])
        except CourseClass.DoesNotExist:
            course_class = None
            return create_api_response(
                success=False,
                error="Invalid class_id",
                message="Invalid class_id",
                status_code=status.HTTP_400_BAD_REQUEST,
            )

        # assignment에 연동될 subject를 조회
        subject_name = data["subject"].strip()

        # Subject가 존재하면 가져오고, 없으면 새로 생성
        subject, _ = Subject.objects.get_or_create(name=subject_name)

        # due_at을 timezone-aware로 변환 (유연한 파싱)
        # 예: "2025-10-25T23:59:00+09:00", "2025-10-25 23:59", "2025-10-25T23:59Z" 등 모두 허용
        try:
            raw_due = str(data["due_at"]).strip()  # 혹시 모를 공백 제거
            due_at = parser.parse(raw_due)
            if due_at.tzinfo is None:
                due_at = timezone.make_aware(due_at, timezone.get_current_timezone())
        except Exception:
            return create_api_response(
                success=False,
                error="Invalid due_at format (use ISO 8601)",
                message="Invalid due_at format (use ISO 8601)",
                status_code=status.HTTP_400_BAD_REQUEST,
            )

        assignment = Assignment.objects.create(
            course_class=course_class,
            subject=subject,
            title=data["title"],
            description=data.get("description", ""),
            visible_from=datetime.now(),
            due_at=due_at,
        )

        # 여기서 course_class에 등록된 students를 조회하여 PersonalAssignment 생성

        try:
            # course_class에 등록된 학생들 조회 (ENROLLED 상태만)
            enrollments = Enrollment.objects.filter(
                course_class=course_class, status=Enrollment.Status.ENROLLED
            ).select_related("student")

            # 각 학생에 대해 PersonalAssignment 생성
            personal_assignments = [
                PersonalAssignment(
                    student=enrollment.student,
                    assignment=assignment,
                    status=PersonalAssignment.Status.NOT_STARTED,
                    solved_num=0,
                )
                for enrollment in enrollments
            ]

            # bulk_create로 한 번에 생성
            if personal_assignments:
                PersonalAssignment.objects.bulk_create(personal_assignments)
                logger.info(
                    f"[AssignmentCreateView] Created {len(personal_assignments)} PersonalAssignments for assignment {assignment.id}"
                )
            else:
                logger.warning(f"[AssignmentCreateView] No enrolled students found for class {course_class.id}")

        except Exception as e:
            # PersonalAssignment 생성 실패하면 Assignment 생성 롤백
            logger.error(f"[AssignmentCreateView] Failed to create PersonalAssignments: {e}", exc_info=True)
            assignment.delete()
            return create_api_response(
                success=False,
                error="Failed to create PersonalAssignments",
                message="Failed to create PersonalAssignments",
                status_code=status.HTTP_500_INTERNAL_SERVER_ERROR,
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

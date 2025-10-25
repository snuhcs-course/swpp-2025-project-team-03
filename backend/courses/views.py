import logging

from django.contrib.auth import get_user_model
from drf_yasg import openapi
from drf_yasg.utils import swagger_auto_schema
from rest_framework import status
from rest_framework.response import Response
from rest_framework.views import APIView

from .models import CourseClass, Enrollment
from .request_serializers import StudentDeleteRequestSerializer, StudentEditRequestSerializer
from .serializers import (
    CourseClassSerializer,
    EnrollmentSerializer,
    StudentDetailSerializer,
    StudentEditResponseSerializer,
    StudentSerializer,
    StudentStatisticsSerializer,
)

logger = logging.getLogger(__name__)
Account = get_user_model()


def create_api_response(success=True, data=None, message="성공", error=None, status_code=status.HTTP_200_OK):
    """API 응답을 생성하는 헬퍼 함수"""
    return Response({"success": success, "data": data, "message": message, "error": error}, status=status_code)


class StudentListView(APIView):  # GET /students
    @swagger_auto_schema(
        operation_id="학생 목록 조회",
        operation_description="전체 학생 목록을 조회합니다. teacherId, classId로 필터링 가능합니다.",
        manual_parameters=[
            openapi.Parameter(
                name="teacherId",
                in_=openapi.IN_QUERY,
                description="선생님 ID",
                type=openapi.TYPE_STRING,
                required=False,
            ),
            openapi.Parameter(
                name="classId", in_=openapi.IN_QUERY, description="클래스 ID", type=openapi.TYPE_STRING, required=False
            ),
        ],
        responses={200: "Student list"},
    )
    def get(self, request):
        try:
            teacher_id = request.query_params.get("teacherId")
            class_id = request.query_params.get("classId")

            # 학생만 필터링
            students = Account.objects.filter(is_student=True)

            # teacherId로 필터링 (해당 선생님의 클래스에 등록된 학생들)
            if teacher_id:
                students = students.filter(enrollments__course_class__teacher=teacher_id)

            # classId로 필터링
            if class_id:
                students = students.filter(enrollments__course_class=class_id)

            # 중복 제거
            students = students.distinct()

            serializer = StudentSerializer(students, many=True)
            return create_api_response(data=serializer.data, message="학생 목록 조회 성공")

        except Exception as e:
            logger.error(f"[StudentListView] {e}", exc_info=True)
            return create_api_response(
                success=False,
                error=str(e),
                message="학생 목록 조회 중 오류가 발생했습니다.",
                status_code=status.HTTP_500_INTERNAL_SERVER_ERROR,
            )


class StudentDetailView(APIView):  # GET /students/{id}
    @swagger_auto_schema(
        operation_id="학생 상세 조회",
        operation_description="특정 학생의 상세 정보를 조회합니다.",
        responses={200: "Student detail"},
    )
    def get(self, request, id):
        try:
            student = Account.objects.get(id=id, is_student=True)
            serializer = StudentDetailSerializer(student)
            return create_api_response(data=serializer.data, message="학생 상세 조회 성공")

        except Account.DoesNotExist:
            return create_api_response(
                success=False,
                error="Student not found",
                message="해당 학생을 찾을 수 없습니다.",
                status_code=status.HTTP_404_NOT_FOUND,
            )
        except Exception as e:
            logger.error(f"[StudentDetailView] {e}", exc_info=True)
            return create_api_response(
                success=False,
                error=str(e),
                message="학생 상세 조회 중 오류가 발생했습니다.",
                status_code=status.HTTP_500_INTERNAL_SERVER_ERROR,
            )

    # PUT /students/{id}/
    @swagger_auto_schema(
        operation_id="학생 정보 수정",
        operation_description="특정 학생의 정보를 수정합니다.",
        request_body=StudentEditRequestSerializer,
        responses={200: "Student updated"},
    )
    def put(self, request, id):
        try:
            student = Account.objects.get(id=id, is_student=True)
            serializer = StudentEditRequestSerializer(data=request.data)

            if serializer.is_valid():
                # 유효한 필드만 업데이트
                for field, value in serializer.validated_data.items():
                    if value is not None:
                        setattr(student, field, value)
                student.save()

                response_serializer = StudentEditResponseSerializer(student)
                return create_api_response(
                    data=response_serializer.data, message="학생 정보 수정 성공", status_code=status.HTTP_200_OK
                )
            else:
                return create_api_response(
                    success=False,
                    error=serializer.errors,
                    message="입력값 오류",
                    status_code=status.HTTP_400_BAD_REQUEST,
                )

        except Account.DoesNotExist:
            return create_api_response(
                success=False,
                error="Student not found",
                message="해당 학생을 찾을 수 없습니다.",
                status_code=status.HTTP_404_NOT_FOUND,
            )
        except Exception as e:
            logger.error(f"[StudentEditView] {e}", exc_info=True)
            return create_api_response(
                success=False,
                error=str(e),
                message="학생 정보 수정 중 오류가 발생했습니다.",
                status_code=status.HTTP_500_INTERNAL_SERVER_ERROR,
            )

    # DELETE /students/{id}/
    @swagger_auto_schema(
        operation_id="학생 삭제",
        operation_description="특정 학생을 삭제합니다.",
        request_body=StudentDeleteRequestSerializer,
        responses={200: "Student deleted"},
    )
    def delete(self, request, id):
        try:
            student = Account.objects.get(id=id, is_student=True)
            serializer = StudentDeleteRequestSerializer(data=request.data)

            if serializer.is_valid():
                student.delete()

                return create_api_response(
                    data={"success": True, "message": "학생이 성공적으로 삭제되었습니다."},
                    message="학생 삭제 성공",
                    status_code=status.HTTP_200_OK,
                )
            else:
                return create_api_response(
                    success=False,
                    error=serializer.errors,
                    message="입력값 오류",
                    status_code=status.HTTP_400_BAD_REQUEST,
                )

        except Account.DoesNotExist:
            return create_api_response(
                success=False,
                error="Student not found",
                message="해당 학생을 찾을 수 없습니다.",
                status_code=status.HTTP_404_NOT_FOUND,
            )
        except Exception as e:
            logger.error(f"[StudentDeleteView] {e}", exc_info=True)
            return create_api_response(
                success=False,
                error=str(e),
                message="학생 삭제 중 오류가 발생했습니다.",
                status_code=status.HTTP_500_INTERNAL_SERVER_ERROR,
            )


class StudentStatisticsView(APIView):  # GET /students/{id}/statistics
    @swagger_auto_schema(
        operation_id="학생 진도 통계량 조회",
        operation_description="특정 학생의 진도 현황을 조회합니다.",
        responses={200: "Student progress"},
    )
    def get(self, request, id):
        try:
            student = Account.objects.get(id=id, is_student=True)
            personal_assignments = student.personal_assignments.all()

            total_assignments_num = personal_assignments.count()
            submitted_assignments_num = personal_assignments.filter(status="SUBMITTED").count()
            in_progress_assignments_num = personal_assignments.filter(status="IN_PROGRESS").count()
            not_started_assignments_num = personal_assignments.filter(status="NOT_STARTED").count()

            student_progress_statistics = {
                "total_assignments": total_assignments_num,
                "submitted_assignments": submitted_assignments_num,
                "in_progress_assignments": in_progress_assignments_num,
                "not_started_assignments": not_started_assignments_num,
            }

            serializer = StudentStatisticsSerializer(student_progress_statistics)
            return create_api_response(
                data=serializer.data, message="학생 진도 통계량 조회 성공", status_code=status.HTTP_200_OK
            )
        except Account.DoesNotExist:
            return create_api_response(
                success=False,
                error="Student not found",
                message="해당 학생을 찾을 수 없습니다.",
                status_code=status.HTTP_404_NOT_FOUND,
            )
        except Exception as e:
            logger.error(f"[StudentStatisticsView] {e}", exc_info=True)
            return create_api_response(
                success=False,
                error=str(e),
                message="학생 진도 통계량 조회 중 오류가 발생했습니다.",
                status_code=status.HTTP_500_INTERNAL_SERVER_ERROR,
            )


class ClassListView(APIView):  # GET /classes
    @swagger_auto_schema(
        operation_id="클래스 목록 조회",
        operation_description="전체 클래스 목록을 조회합니다. teacherId로 필터링 가능합니다.",
        manual_parameters=[
            openapi.Parameter(
                name="teacherId",
                in_=openapi.IN_QUERY,
                description="선생님 ID",
                type=openapi.TYPE_STRING,
                required=False,
            ),
        ],
        responses={200: "Class list"},
    )
    def get(self, request):
        try:
            teacher_id = request.query_params.get("teacherId")

            classes = CourseClass.objects.all()

            if teacher_id:
                classes = classes.filter(teacher_id=teacher_id)

            serializer = CourseClassSerializer(classes, many=True)
            return create_api_response(data=serializer.data, message="클래스 목록 조회 성공")

        except Exception as e:
            logger.error(f"[ClassListView] {e}", exc_info=True)
            return create_api_response(
                success=False,
                error=str(e),
                message="클래스 목록 조회 중 오류가 발생했습니다.",
                status_code=status.HTTP_500_INTERNAL_SERVER_ERROR,
            )


class ClassDetailView(APIView):  # GET /classes/{id}
    @swagger_auto_schema(
        operation_id="클래스 상세 조회",
        operation_description="특정 클래스의 상세 정보를 조회합니다.",
        responses={200: "Class detail"},
    )
    def get(self, request, id):
        try:
            course_class = CourseClass.objects.get(id=id)
            serializer = CourseClassSerializer(course_class)
            return create_api_response(data=serializer.data, message="클래스 상세 조회 성공")

        except CourseClass.DoesNotExist:
            return create_api_response(
                success=False,
                error="Class not found",
                message="해당 클래스를 찾을 수 없습니다.",
                status_code=status.HTTP_404_NOT_FOUND,
            )
        except Exception as e:
            logger.error(f"[ClassDetailView] {e}", exc_info=True)
            return create_api_response(
                success=False,
                error=str(e),
                message="클래스 상세 조회 중 오류가 발생했습니다.",
                status_code=status.HTTP_500_INTERNAL_SERVER_ERROR,
            )

    @swagger_auto_schema(
        operation_id="클래스 정보 수정",
        operation_description="특정 클래스의 정보를 수정합니다.",
        request_body=CourseClassSerializer,
        responses={200: "Class updated"},
    )
    def put(self, request, id):
        try:
            course_class = CourseClass.objects.get(id=id)
            serializer = CourseClassSerializer(course_class, data=request.data, partial=True)

            if serializer.is_valid():
                serializer.save()
                return create_api_response(
                    data=serializer.data, message="클래스 정보 수정 성공", status_code=status.HTTP_200_OK
                )
            else:
                return create_api_response(
                    success=False,
                    error=serializer.errors,
                    message="입력값 오류",
                    status_code=status.HTTP_400_BAD_REQUEST,
                )

        except CourseClass.DoesNotExist:
            return create_api_response(
                success=False,
                error="Class not found",
                message="해당 클래스를 찾을 수 없습니다.",
                status_code=status.HTTP_404_NOT_FOUND,
            )
        except Exception as e:
            logger.error(f"[ClassDetailView PUT] {e}", exc_info=True)
            return create_api_response(
                success=False,
                error=str(e),
                message="클래스 정보 수정 중 오류가 발생했습니다.",
                status_code=status.HTTP_500_INTERNAL_SERVER_ERROR,
            )

    @swagger_auto_schema(
        operation_id="클래스 삭제",
        operation_description="특정 클래스를 삭제합니다.",
        responses={200: "Class deleted"},
    )
    def delete(self, request, id):
        try:
            course_class = CourseClass.objects.get(id=id)
            class_name = course_class.name
            course_class.delete()

            return create_api_response(
                data={"id": id, "name": class_name},
                message="클래스가 성공적으로 삭제되었습니다.",
                status_code=status.HTTP_200_OK,
            )

        except CourseClass.DoesNotExist:
            return create_api_response(
                success=False,
                error="Class not found",
                message="해당 클래스를 찾을 수 없습니다.",
                status_code=status.HTTP_404_NOT_FOUND,
            )
        except Exception as e:
            logger.error(f"[ClassDetailView DELETE] {e}", exc_info=True)
            return create_api_response(
                success=False,
                error=str(e),
                message="클래스 삭제 중 오류가 발생했습니다.",
                status_code=status.HTTP_500_INTERNAL_SERVER_ERROR,
            )


class ClassStudentsView(APIView):  # GET /classes/{id}/students
    @swagger_auto_schema(
        operation_id="클래스 학생 목록 조회",
        operation_description="특정 클래스에 속한 학생 목록을 조회합니다.",
        responses={200: "Class students"},
    )
    def get(self, request, id):
        try:
            course_class = CourseClass.objects.get(id=id)
            enrollments = course_class.enrollments.filter(status=Enrollment.Status.ENROLLED)
            students = [enrollment.student for enrollment in enrollments]

            serializer = StudentSerializer(students, many=True)
            return create_api_response(data=serializer.data, message="클래스 학생 목록 조회 성공")

        except CourseClass.DoesNotExist:
            return create_api_response(
                success=False,
                error="Class not found",
                message="해당 클래스를 찾을 수 없습니다.",
                status_code=status.HTTP_404_NOT_FOUND,
            )
        except Exception as e:
            logger.error(f"[ClassStudentsView] {e}", exc_info=True)
            return create_api_response(
                success=False,
                error=str(e),
                message="클래스 학생 목록 조회 중 오류가 발생했습니다.",
                status_code=status.HTTP_500_INTERNAL_SERVER_ERROR,
            )

    @swagger_auto_schema(
        operation_id="클래스에 학생 등록",
        operation_description="클래스 id를 받아서 학생을 해당 클래스에 등록합니다. studentId, name, email 중 최소 하나를 제공해야 합니다.",
        manual_parameters=[
            openapi.Parameter(
                "studentId", openapi.IN_QUERY, description="학생 ID", type=openapi.TYPE_STRING, required=False
            ),
            openapi.Parameter(
                "name", openapi.IN_QUERY, description="학생 이름", type=openapi.TYPE_STRING, required=False
            ),
            openapi.Parameter(
                "email", openapi.IN_QUERY, description="학생 이메일", type=openapi.TYPE_STRING, required=False
            ),
        ],
        responses={200: "Student enrolled in class"},
    )
    def put(self, request, id):
        try:
            course_class = CourseClass.objects.get(id=id)
            student_id = request.query_params.get("studentId")
            student_display_name = request.query_params.get("name")
            student_email = request.query_params.get("email")

            # 모든 파라미터가 없으면 에러
            if not student_id and not student_display_name and not student_email:
                return create_api_response(
                    success=False,
                    message="At least one of studentId, name, or email must be provided",
                    status_code=status.HTTP_400_BAD_REQUEST,
                )

            # id, display_name, email을 이용해서 Account 찾기
            query_filter = {}
            if student_id:
                query_filter["id"] = student_id
            if student_display_name:
                query_filter["display_name"] = student_display_name
            if student_email:
                query_filter["email"] = student_email

            try:
                student = Account.objects.get(**query_filter, is_student=True)
            except Account.DoesNotExist:
                return create_api_response(
                    success=False,
                    error="Student not found",
                    message="해당 조건에 맞는 학생을 찾을 수 없습니다.",
                    status_code=status.HTTP_404_NOT_FOUND,
                )
            except Account.MultipleObjectsReturned:
                return create_api_response(
                    success=False,
                    error="Multiple students found",
                    message="조건에 맞는 학생이 여러 명입니다. 더 구체적인 정보를 제공해주세요.",
                    status_code=status.HTTP_400_BAD_REQUEST,
                )

            # 중복 등록 방지
            enrollment, created = Enrollment.objects.get_or_create(
                student=student,
                course_class=course_class,
                defaults={"status": Enrollment.Status.ENROLLED},
            )
            if not created:
                enrollment.status = Enrollment.Status.ENROLLED
                enrollment.save()

            serializer = EnrollmentSerializer(enrollment)

            return create_api_response(
                data=serializer.data,
                message="학생이 클래스에 성공적으로 등록되었습니다.",
                status_code=status.HTTP_200_OK,
            )

        except CourseClass.DoesNotExist:
            return create_api_response(
                success=False,
                error="Class not found",
                message="해당 클래스를 찾을 수 없습니다.",
                status_code=status.HTTP_404_NOT_FOUND,
            )
        except Exception as e:
            logger.error(f"[ClassStudentsView PUT] {e}", exc_info=True)
            return create_api_response(
                success=False,
                error=str(e),
                message="학생 등록 중 오류가 발생했습니다.",
                status_code=status.HTTP_500_INTERNAL_SERVER_ERROR,
            )

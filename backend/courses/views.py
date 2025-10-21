import logging

from django.contrib.auth import get_user_model
from drf_yasg.utils import swagger_auto_schema
from rest_framework import status
from rest_framework.response import Response
from rest_framework.views import APIView

from .models import CourseClass, Enrollment
from .request_serializers import StudentDeleteRequestSerializer, StudentEditRequestSerializer
from .serializers import StudentDetailSerializer, StudentEditResponseSerializer, StudentSerializer

logger = logging.getLogger(__name__)
Account = get_user_model()


def create_api_response(success=True, data=None, message="성공", error=None, status_code=status.HTTP_200_OK):
    """API 응답을 생성하는 헬퍼 함수"""
    return Response({"success": success, "data": data, "message": message, "error": error}, status=status_code)


class StudentListView(APIView):  # GET /students
    @swagger_auto_schema(
        operation_id="학생 목록 조회",
        operation_description="전체 학생 목록을 조회합니다. teacherId, classId로 필터링 가능합니다.",
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


class StudentAssignmentsView(APIView):  # GET /students/{id}/assignments
    @swagger_auto_schema(
        operation_id="학생 과제 조회",
        operation_description="특정 학생의 과제 목록을 조회합니다.",
        responses={200: "Student assignments"},
    )
    def get(self, request, id):
        return Response({"message": "학생 과제 조회"}, status=status.HTTP_200_OK)


class StudentProgressView(APIView):  # GET /students/{id}/progress
    @swagger_auto_schema(
        operation_id="학생 진도 조회",
        operation_description="특정 학생의 진도 현황을 조회합니다.",
        responses={200: "Student progress"},
    )
    def get(self, request, id):
        return Response({"message": "학생 진도 조회"}, status=status.HTTP_200_OK)


class ClassListView(APIView):  # GET /classes
    @swagger_auto_schema(
        operation_id="클래스 목록 조회",
        operation_description="전체 클래스 목록을 조회합니다. teacherId로 필터링 가능합니다.",
        responses={200: "Class list"},
    )
    def get(self, request):
        return Response({"message": "클래스 목록 조회"}, status=status.HTTP_200_OK)


class ClassDetailView(APIView):  # GET /classes/{id}
    @swagger_auto_schema(
        operation_id="클래스 상세 조회",
        operation_description="특정 클래스의 상세 정보를 조회합니다.",
        responses={200: "Class detail"},
    )
    def get(self, request, id):
        return Response({"message": "클래스 상세 조회"}, status=status.HTTP_200_OK)


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

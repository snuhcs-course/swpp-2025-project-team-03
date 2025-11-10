import logging

from django.contrib.auth import get_user_model
from drf_yasg import openapi
from drf_yasg.utils import swagger_auto_schema
from rest_framework import status
from rest_framework.response import Response
from rest_framework.views import APIView

from .models import CourseClass, Enrollment
from .request_serializers import ClassCreateRequestSerializer, StudentEditRequestSerializer
from .serializers import (
    ClassStudentsStatisticsSerializer,
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


class ClassListView(APIView):  # GET, POST /classes
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

    @swagger_auto_schema(
        operation_id="클래스 생성",
        operation_description="새로운 클래스를 생성합니다.",
        request_body=ClassCreateRequestSerializer,
        responses={201: "Class created", 400: "Invalid input"},
    )
    def post(self, request):
        try:
            serializer = ClassCreateRequestSerializer(data=request.data)
            if serializer.is_valid(raise_exception=True):
                course_class = serializer.save()
                response_serializer = CourseClassSerializer(course_class)
                return create_api_response(
                    data=response_serializer.data, message="클래스 생성 성공", status_code=status.HTTP_201_CREATED
                )
        except Exception as e:
            logger.error(f"[ClassListView POST] {e}", exc_info=True)
            return create_api_response(
                success=False,
                error=str(e),
                message="클래스 생성 중 오류가 발생했습니다.",
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
        operation_description="클래스 id를 받아서 학생을 해당 클래스에 등록합니다. studentId는 필수입니다.",
        manual_parameters=[
            openapi.Parameter(
                "studentId", openapi.IN_QUERY, description="학생 ID", type=openapi.TYPE_INTEGER, required=True
            ),
        ],
        responses={200: "Student enrolled in class"},
    )
    def put(self, request, id):
        try:
            course_class = CourseClass.objects.get(id=id)
            student_id_param = request.query_params.get("studentId")

            # studentId 파라미터 검증
            if not student_id_param:
                return create_api_response(
                    success=False,
                    error="studentId is required",
                    message="학생 ID는 필수입니다.",
                    status_code=status.HTTP_400_BAD_REQUEST,
                )

            # studentId가 정수인지 검증
            try:
                student_id = int(student_id_param)
            except (ValueError, TypeError):
                return create_api_response(
                    success=False,
                    error="Invalid studentId format",
                    message="학생 ID는 정수여야 합니다.",
                    status_code=status.HTTP_400_BAD_REQUEST,
                )

            # studentId가 양수인지 검증
            if student_id <= 0:
                return create_api_response(
                    success=False,
                    error="Invalid studentId value",
                    message="학생 ID는 양수여야 합니다.",
                    status_code=status.HTTP_400_BAD_REQUEST,
                )

            # 학생 존재 여부 및 학생 여부 검증
            try:
                student = Account.objects.get(id=student_id, is_student=True)
            except Account.DoesNotExist:
                return create_api_response(
                    success=False,
                    error="Student not found",
                    message="해당 ID의 학생을 찾을 수 없습니다.",
                    status_code=status.HTTP_404_NOT_FOUND,
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


class ClassStudentDeleteView(APIView):  # DELETE /classes/{id}/students/{student_id}/
    @swagger_auto_schema(
        operation_id="반에서 학생 제거",
        operation_description="특정 반에서 특정 학생을 제거합니다. 해당 학생의 personal assignment, questions, answers가 모두 삭제됩니다.",
        responses={200: "Student removed from class"},
    )
    def delete(self, request, id, student_id):
        try:
            from assignments.models import Assignment
            from questions.models import Question
            from submissions.models import Answer, PersonalAssignment

            # 클래스 확인
            course_class = CourseClass.objects.get(id=id)

            # 학생 확인
            student = Account.objects.get(id=student_id, is_student=True)

            # Enrollment 확인
            try:
                enrollment = Enrollment.objects.get(course_class=course_class, student=student)
            except Enrollment.DoesNotExist:
                return create_api_response(
                    success=False,
                    error="Enrollment not found",
                    message="해당 학생이 이 반에 등록되어 있지 않습니다.",
                    status_code=status.HTTP_404_NOT_FOUND,
                )

            # 해당 반의 모든 Assignment 가져오기
            assignments = Assignment.objects.filter(course_class=course_class)

            # 해당 학생의 PersonalAssignment들 가져오기 (이 반의 과제들에 대한 것만)
            personal_assignments = PersonalAssignment.objects.filter(student=student, assignment__in=assignments)

            # PersonalAssignment의 Questions 가져오기
            questions = Question.objects.filter(personal_assignment__in=personal_assignments)

            # Questions의 Answers 가져오기
            answers = Answer.objects.filter(question__in=questions, student=student)

            # 삭제 순서: Answers -> Questions -> PersonalAssignments -> Enrollment
            # (CASCADE로 자동 삭제되지만 명시적으로 삭제)
            answers.delete()
            questions.delete()
            personal_assignments.delete()
            enrollment.delete()

            return create_api_response(
                data={"class_id": id, "student_id": student_id},
                message="학생이 반에서 성공적으로 제거되었습니다.",
                status_code=status.HTTP_200_OK,
            )

        except CourseClass.DoesNotExist:
            return create_api_response(
                success=False,
                error="Class not found",
                message="해당 클래스를 찾을 수 없습니다.",
                status_code=status.HTTP_404_NOT_FOUND,
            )
        except Account.DoesNotExist:
            return create_api_response(
                success=False,
                error="Student not found",
                message="해당 학생을 찾을 수 없습니다.",
                status_code=status.HTTP_404_NOT_FOUND,
            )
        except Exception as e:
            logger.error(f"[ClassStudentDeleteView] {e}", exc_info=True)
            return create_api_response(
                success=False,
                error=str(e),
                message="학생 제거 중 오류가 발생했습니다.",
                status_code=status.HTTP_500_INTERNAL_SERVER_ERROR,
            )


class ClassStudentsStatisticsView(APIView):  # GET /classes/{classId}/students-statistics
    @swagger_auto_schema(
        operation_id="반의 모든 학생 통계 조회",
        operation_description="특정 반의 모든 학생에 대한 평균 점수와 완료율, 그리고 전체 평균 점수를 조회합니다.",
        responses={200: "Class students statistics"},
    )
    def get(self, request, classId):
        try:
            from assignments.models import Assignment
            from questions.models import Question
            from submissions.models import Answer, PersonalAssignment

            # 클래스 확인
            course_class = CourseClass.objects.get(id=classId)

            # 해당 반의 모든 학생
            enrollments = Enrollment.objects.filter(course_class=course_class, status=Enrollment.Status.ENROLLED)
            students = [enrollment.student for enrollment in enrollments]

            if not students:
                return create_api_response(
                    data={"overall_average_score": 0.0, "students": []},
                    message="반 학생 통계 조회 성공",
                )

            # 해당 반의 모든 과제
            assignments = Assignment.objects.filter(course_class=course_class)
            total_assignments = assignments.count()

            if total_assignments == 0:
                return create_api_response(
                    data={
                        "overall_average_score": 0.0,
                        "students": [
                            {"student_id": student.id, "average_score": 0.0, "completion_rate": 0.0}
                            for student in students
                        ],
                    },
                    message="반 학생 통계 조회 성공",
                )

            # 모든 학생의 통계 계산
            students_statistics = []

            for student in students:
                # 해당 학생의 PersonalAssignment들
                personal_assignments = PersonalAssignment.objects.filter(
                    assignment__in=assignments, student=student
                ).select_related("assignment")

                # 완료율 계산
                submitted_count = personal_assignments.filter(status=PersonalAssignment.Status.SUBMITTED).count()
                completion_rate = (submitted_count / total_assignments * 100) if total_assignments > 0 else 0

                # 평균 점수 계산
                total_average_score = 0.0
                assignments_with_scores = 0

                for assignment in assignments:
                    personal_assignment = personal_assignments.filter(assignment=assignment).first()
                    if not personal_assignment:
                        continue

                    # 해당 과제의 모든 질문과 답변 가져오기
                    all_questions = list(
                        Question.objects.filter(personal_assignment=personal_assignment).order_by(
                            "number", "recalled_num"
                        )
                    )

                    if not all_questions:
                        continue

                    # 답변 가져오기
                    answers_qs = Answer.objects.filter(question__in=all_questions, student=student).select_related(
                        "question"
                    )
                    answers_by_question_id = {answer.question_id: answer for answer in answers_qs}

                    # question_number별로 그룹화
                    questions_by_number = {}
                    base_question_count = 0

                    for question in all_questions:
                        if question.number not in questions_by_number:
                            questions_by_number[question.number] = []
                        questions_by_number[question.number].append(question)

                        if question.recalled_num == 0:
                            base_question_count += 1

                    if base_question_count == 0:
                        continue

                    # 과제별 점수 계산
                    total_score = 0
                    for question_number, questions in questions_by_number.items():
                        question_score = 0

                        for question in questions:
                            answer = answers_by_question_id.get(question.id)
                            if answer is None:
                                break

                            if answer.state == Answer.State.CORRECT:
                                if question.recalled_num == 0:
                                    question_score = 100
                                elif question.recalled_num == 1:
                                    question_score = 75
                                elif question.recalled_num == 2:
                                    question_score = 50
                                elif question.recalled_num == 3:
                                    question_score = 25
                                break

                        total_score += question_score

                    assignment_average_score = (total_score / base_question_count) if base_question_count > 0 else 0
                    total_average_score += assignment_average_score
                    assignments_with_scores += 1

                # 학생별 평균 점수 계산
                average_score = (total_average_score / assignments_with_scores) if assignments_with_scores > 0 else 0

                students_statistics.append(
                    {
                        "student_id": student.id,
                        "average_score": round(average_score, 1),
                        "completion_rate": round(completion_rate, 1),
                        "total_assignments": total_assignments,
                        "completed_assignments": submitted_count,
                    }
                )

            # 전체 평균 완료율 계산: 전체 학생 완료한 과제 수 / 전체 학생 과제 수
            total_completed_assignments = sum(stat["completed_assignments"] for stat in students_statistics)
            total_student_assignments = sum(stat["total_assignments"] for stat in students_statistics)
            overall_completion_rate = (
                (total_completed_assignments / total_student_assignments * 100) if total_student_assignments > 0 else 0
            )

            data = {
                "overall_completion_rate": round(overall_completion_rate, 1),
                "students": students_statistics,
            }

            serializer = ClassStudentsStatisticsSerializer(data=data)
            serializer.is_valid(raise_exception=True)

            return create_api_response(
                data=serializer.data, message="반 학생 통계 조회 성공", status_code=status.HTTP_200_OK
            )

        except CourseClass.DoesNotExist:
            return create_api_response(
                success=False,
                error="Class not found",
                message="해당 클래스를 찾을 수 없습니다.",
                status_code=status.HTTP_404_NOT_FOUND,
            )
        except Exception as e:
            logger.error(f"[ClassStudentsStatisticsView] {e}", exc_info=True)
            return create_api_response(
                success=False,
                error=str(e),
                message="반 학생 통계 조회 중 오류가 발생했습니다.",
                status_code=status.HTTP_500_INTERNAL_SERVER_ERROR,
            )


class StudentClassesView(APIView):  # GET /students/{id}/classes
    @swagger_auto_schema(
        operation_id="학생 클래스 목록 조회",
        operation_description="특정 학생이 속한 클래스 목록을 조회합니다.",
        responses={200: "Student classes"},
    )
    def get(self, request, id):
        try:
            student = Account.objects.get(id=id, is_student=True)
            enrollments = Enrollment.objects.filter(student=student, status=Enrollment.Status.ENROLLED)
            classes = [enrollment.course_class for enrollment in enrollments]

            serializer = CourseClassSerializer(classes, many=True)
            return create_api_response(data=serializer.data, message="학생 클래스 목록 조회 성공")

        except Account.DoesNotExist:
            return create_api_response(
                success=False,
                error="Student not found",
                message="해당 학생을 찾을 수 없습니다.",
                status_code=status.HTTP_404_NOT_FOUND,
            )
        except Exception as e:
            logger.error(f"[StudentClassesView] {e}", exc_info=True)
            return create_api_response(
                success=False,
                error=str(e),
                message="학생 클래스 목록 조회 중 오류가 발생했습니다.",
                status_code=status.HTTP_500_INTERNAL_SERVER_ERROR,
            )

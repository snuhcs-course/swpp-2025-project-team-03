import logging
import os
import tempfile
from datetime import timedelta

from django.contrib.auth import get_user_model
from django.db import models
from django.utils import timezone
from drf_yasg import openapi
from drf_yasg.utils import swagger_auto_schema
from questions.models import Question
from questions.serializers import TailQuestionSerializer
from rest_framework import status
from rest_framework.parsers import FormParser, MultiPartParser
from rest_framework.response import Response
from rest_framework.views import APIView

from .models import Answer, PersonalAssignment
from .serializers import PersonalAssignmentSerializer, PersonalAssignmentStatisticsSerializer
from .utils.feature_extractor.extract_all_features import extract_all_features
from .utils.inference import run_inference
from .utils.tail_question_generator.generate_questions_routed import generate_tail_question

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
        manual_parameters=[
            openapi.Parameter(
                name="assignment_id",
                in_=openapi.IN_QUERY,
                description="과제 ID",
                type=openapi.TYPE_INTEGER,
                required=False,
            ),
            openapi.Parameter(
                name="student_id",
                in_=openapi.IN_QUERY,
                description="학생 ID",
                type=openapi.TYPE_INTEGER,
                required=False,
            ),
        ],
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
    parser_classes = [MultiPartParser, FormParser]

    @swagger_auto_schema(
        operation_id="답안 제출",
        operation_description="""
        학생이 문제에 대한 음성 답안을 제출합니다.
        
        **Content-Type**: multipart/form-data
        
        **Request Body (form-data)**:
        - studentId (integer): 제출하는 학생의 ID
        - questionId (integer): 제출한 문제의 ID
        - audioFile (file): 음성 답변 파일 (.wav)
        """,
        responses={
            201: "답안 제출 성공",
            400: "잘못된 요청",
            404: "학생 또는 문제를 찾을 수 없음",
            500: "서버 오류",
        },
    )
    def post(self, request):
        """
        음성 답안 제출 API

        Step 1: multipart/form-data로 .wav 파일을 받아서 STT 변환 및 Feature 추출
        - extract_all_features() 함수 사용 (STT + 음향 특징 + 스크립트 특징 + 의미론적 특징)
        """
        try:
            # Step 1-1: 요청 데이터 검증
            student_id = request.data.get("studentId")
            question_id = request.data.get("questionId")
            audio_file = request.FILES.get("audioFile")

            # 필수 파라미터 체크
            if not student_id:
                return create_api_response(
                    success=False,
                    error="Missing studentId",
                    message="studentId는 필수 파라미터입니다.",
                    status_code=status.HTTP_400_BAD_REQUEST,
                )

            if not question_id:
                return create_api_response(
                    success=False,
                    error="Missing questionId",
                    message="questionId는 필수 파라미터입니다.",
                    status_code=status.HTTP_400_BAD_REQUEST,
                )

            if not audio_file:
                return create_api_response(
                    success=False,
                    error="Missing audioFile",
                    message="audioFile은 필수 파라미터입니다.",
                    status_code=status.HTTP_400_BAD_REQUEST,
                )

            # 파일 확장자 검증
            if not audio_file.name.endswith(".wav"):
                return create_api_response(
                    success=False,
                    error="Invalid file format",
                    message="audioFile은 .wav 파일이어야 합니다.",
                    status_code=status.HTTP_400_BAD_REQUEST,
                )

            # Step 1-2: DB에서 학생과 문제 확인
            try:
                student = Account.objects.get(id=student_id, is_student=True)
            except Account.DoesNotExist:
                return create_api_response(
                    success=False,
                    error="Student not found",
                    message=f"ID가 {student_id}인 학생을 찾을 수 없습니다.",
                    status_code=status.HTTP_404_NOT_FOUND,
                )

            try:
                question = Question.objects.get(id=question_id)
            except Question.DoesNotExist:
                return create_api_response(
                    success=False,
                    error="Question not found",
                    message=f"ID가 {question_id}인 문제를 찾을 수 없습니다.",
                    status_code=status.HTTP_404_NOT_FOUND,
                )

            # Step 1-3: 임시 파일로 저장 (extract_all_features 함수가 파일 경로를 요구함)
            # 임시 파일 생성
            with tempfile.NamedTemporaryFile(suffix=".wav", delete=False) as temp_file:
                # 업로드된 파일 내용을 임시 파일에 쓰기
                for chunk in audio_file.chunks():
                    temp_file.write(chunk)
                temp_file_path = temp_file.name

            logger.info(f"[AnswerSubmitView] 임시 파일 생성: {temp_file_path}")

            try:
                # Step 1-4: STT 변환 및 Feature 추출 (extract_all_features 사용)
                logger.info(f"[AnswerSubmitView] Feature 추출 시작 - Question ID: {question_id}")
                features = extract_all_features(temp_file_path)
                logger.info("[AnswerSubmitView] Feature 추출 완료")

                # features 에서 음성 파일 길이 구해서 timezone.now()에 빼는 로직
                audio_duration_sec = features.get("total_length", 0.0)
                started_at = timezone.now() - timedelta(seconds=audio_duration_sec)
                logger.info(f"[AnswerSubmitView] 음성 길이: {audio_duration_sec:.2f}초, 시작 시간: {started_at}")

                # STT 결과 (transcript) 확인
                transcript = features.get("script", "")
                if not transcript or transcript.strip() == "":
                    return create_api_response(
                        success=False,
                        error="STT failed",
                        message="음성 인식 결과가 없습니다. 다시 녹음해주세요.",
                        status_code=status.HTTP_400_BAD_REQUEST,
                    )

                logger.info(f"[AnswerSubmitView] STT 결과: {transcript[:100]}...")  # 처음 100자만 로깅

                # Step 2: ML 추론 (Inference)
                xgbmodel_path = "submissions/machine/model.joblib"
                logger.info("[AnswerSubmitView] ML 추론 시작")

                try:
                    inference_results = run_inference(xgbmodel_path, features)
                    confidence_score = inference_results.get("pred_cont")

                    if confidence_score is None:
                        raise ValueError("Inference result does not contain 'pred_cont'")

                    logger.info(f"[AnswerSubmitView] ML 추론 완료 - Confidence: {confidence_score}")
                except Exception as inf_error:
                    logger.error(f"[AnswerSubmitView] ML 추론 실패: {inf_error}", exc_info=True)
                    return create_api_response(
                        success=False,
                        error="Inference failed",
                        message="답변 평가 중 오류가 발생했습니다.",
                        status_code=status.HTTP_500_INTERNAL_SERVER_ERROR,
                    )

                # Step 3: Tail Question 생성
                logger.info("[AnswerSubmitView] Tail Question 생성 시작")

                try:
                    tail_payload = generate_tail_question(
                        question=question.content,
                        model_answer=question.model_answer,
                        student_answer=transcript,
                        eval_grade=confidence_score,
                        recalled_time=question.recalled_num,
                    )

                    if not tail_payload:
                        raise ValueError("Tail question generation returned None")

                    logger.info(f"[AnswerSubmitView] Tail Question 생성 완료 - Plan: {tail_payload.get('plan')}")
                except Exception as tail_error:
                    logger.error(f"[AnswerSubmitView] Tail Question 생성 실패: {tail_error}", exc_info=True)
                    return create_api_response(
                        success=False,
                        error="Tail question generation failed",
                        message="꼬리 질문 생성 중 오류가 발생했습니다.",
                        status_code=status.HTTP_500_INTERNAL_SERVER_ERROR,
                    )

                # Step 4: Answer 레코드 생성
                is_correct = tail_payload.get("is_correct", False)
                answer_state = Answer.State.CORRECT if is_correct else Answer.State.INCORRECT

                logger.info("[AnswerSubmitView] Answer 레코드 생성 시작")

                try:
                    # PersonalAssignment 가져오기
                    personal_assignment = question.personal_assignment

                    # personal_assignment의 STATUS: NOT_STARTED 이면 personal_assignment의 STATUS: IN_PROGRESS로 변경
                    # plan 이 ONLY_CORRECT 면 STATUS: SUBMITTED 로 변경, solved_num +1
                    if personal_assignment.status == PersonalAssignment.Status.NOT_STARTED:
                        personal_assignment.status = PersonalAssignment.Status.IN_PROGRESS

                    if tail_payload.get("plan") == "ONLY_CORRECT" and is_correct:
                        personal_assignment.solved_num += 1
                        personal_assignment.status = PersonalAssignment.Status.SUBMITTED

                    personal_assignment.save()

                    # Answer 생성 또는 업데이트
                    answer, created = Answer.objects.update_or_create(
                        question=question,
                        student=student,
                        defaults={
                            "text_answer": transcript,
                            "state": answer_state,
                            "eval_grade": confidence_score,
                            "started_at": started_at,
                            "submitted_at": timezone.now(),
                        },
                    )

                    action = "생성" if created else "업데이트"
                    logger.info(f"[AnswerSubmitView] Answer 레코드 {action} 완료 - Answer ID: {answer.id}")
                except Exception as answer_error:
                    logger.error(f"[AnswerSubmitView] Answer 레코드 생성 실패: {answer_error}", exc_info=True)
                    return create_api_response(
                        success=False,
                        error="Answer creation failed",
                        message="답변 저장 중 오류가 발생했습니다.",
                        status_code=status.HTTP_500_INTERNAL_SERVER_ERROR,
                    )

                # Step 5: Tail Question 객체 생성 (plan이 "ASK"인 경우만)
                tail_question_obj = None
                plan = tail_payload.get("plan")

                if plan == "ASK":
                    tail_question_data = tail_payload.get("tail_question", {})

                    if not tail_question_data:
                        logger.warning("[AnswerSubmitView] Plan이 ASK이지만 tail_question 데이터가 없습니다.")
                    else:
                        logger.info("[AnswerSubmitView] Tail Question 객체 생성 시작")

                        try:
                            # 다음 문제 번호 계산
                            max_number = (
                                Question.objects.filter(personal_assignment=personal_assignment).aggregate(
                                    models.Max("number")
                                )["number__max"]
                                or 1
                            )
                            next_number = max_number + 1

                            # Tail Question 생성
                            tail_question_obj = Question.objects.create(
                                personal_assignment=personal_assignment,
                                number=next_number,
                                content=tail_question_data.get("question", ""),
                                model_answer=tail_question_data.get("model_answer", ""),
                                explanation=tail_question_data.get("explanation", ""),
                                difficulty=tail_question_data.get("difficulty", Question.Difficulty.MEDIUM),
                                recalled_num=tail_payload.get("recalled_time", question.recalled_num + 1),
                                base_question=question,  # 원본 질문 연결
                            )

                            logger.info(
                                f"[AnswerSubmitView] Tail Question 객체 생성 완료 - Question ID: {tail_question_obj.id}"
                            )
                        except Exception as tq_error:
                            logger.error(f"[AnswerSubmitView] Tail Question 객체 생성 실패: {tq_error}", exc_info=True)
                            # 이 경우 에러를 반환하지 않고 계속 진행 (tail question 없이)
                            tail_question_obj = None
                else:
                    logger.info(f"[AnswerSubmitView] Plan이 '{plan}'이므로 Tail Question 생성하지 않음")

                # Step 6: 응답 데이터 준비
                # TailQuestionSerializer 사용
                if tail_question_obj:
                    # Question 객체를 QuestionSerializer 형식에 맞게 변환
                    tail_question_data = {
                        "id": tail_question_obj.id,
                        "number": tail_question_obj.number,
                        "question": tail_question_obj.content,  # content -> question
                        "answer": tail_question_obj.model_answer,  # model_answer -> answer
                        "explanation": tail_question_obj.explanation,
                        "difficulty": tail_question_obj.difficulty,
                    }

                    tail_serializer = TailQuestionSerializer(
                        {"tail_question": tail_question_data, "is_correct": is_correct}
                    )
                    response_data = tail_serializer.data
                else:
                    # tail_question이 없는 경우 (plan이 "PASS" 또는 "STOP")
                    response_data = {
                        "is_correct": is_correct,
                        "tail_question": None,
                    }

                # 최종 응답 반환
                return create_api_response(
                    data=response_data,
                    message="답안이 성공적으로 제출되었습니다.",
                    status_code=status.HTTP_201_CREATED,
                )

            finally:
                # 임시 파일 삭제 (성공/실패 여부와 관계없이 항상 실행)
                if os.path.exists(temp_file_path):
                    os.unlink(temp_file_path)
                    logger.info(f"[AnswerSubmitView] 임시 파일 삭제: {temp_file_path}")

        except Exception as e:
            logger.error(f"[AnswerSubmitView] {e}", exc_info=True)
            return create_api_response(
                success=False,
                error=str(e),
                message="답안 제출 중 오류가 발생했습니다.",
                status_code=status.HTTP_500_INTERNAL_SERVER_ERROR,
            )


class PersonalAssignmentStatisticsView(APIView):
    @swagger_auto_schema(
        operation_id="개인 과제 통계 조회",
        operation_description="학생 개인 과제의 통계 정보를 조회합니다.",
        responses={200: "통계 정보"},
    )
    def get(self, request, id):
        """
        개인 과제 통계 조회

        Path Parameters:
            - id: PersonalAssignment ID
        """
        try:
            # PersonalAssignment 조회
            personal_assignment = PersonalAssignment.objects.get(id=id)

            # 통계 정보 계산
            total_questions = personal_assignment.questions.count()
            answered_questions = Answer.objects.filter(
                question__in=personal_assignment.questions.all(), student=personal_assignment.student
            ).count()
            correct_answers = Answer.objects.filter(
                question__in=personal_assignment.questions.all(),
                student=personal_assignment.student,
                state=Answer.State.CORRECT,
            ).count()

            total_problem = personal_assignment.assignment.total_questions
            solved_problem = personal_assignment.solved_num

            statistics = {
                "total_questions": total_questions,
                "answered_questions": answered_questions,
                "correct_answers": correct_answers,
                "accuracy": (correct_answers / answered_questions * 100) if answered_questions > 0 else 0,
                "total_problem": total_problem,
                "solved_problem": solved_problem,
                "progress": (solved_problem / total_problem * 100) if total_problem > 0 else 0,
            }

            serializer = PersonalAssignmentStatisticsSerializer(data=statistics)
            serializer.is_valid(raise_exception=True)

            return create_api_response(
                data=serializer.data, message="개인 과제 통계 조회 성공", status_code=status.HTTP_200_OK
            )

        except PersonalAssignment.DoesNotExist:
            return create_api_response(
                success=False,
                error="PersonalAssignment not found",
                message="해당 개인 과제를 찾을 수 없습니다.",
                status_code=status.HTTP_404_NOT_FOUND,
            )
        except Exception as e:
            logger.error(f"[PersonalAssignmentStatisticsView] {e}", exc_info=True)
            return create_api_response(
                success=False,
                error=str(e),
                message="개인 과제 통계 조회 중 오류가 발생했습니다.",
                status_code=status.HTTP_500_INTERNAL_SERVER_ERROR,
            )

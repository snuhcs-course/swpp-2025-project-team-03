import logging
import os
import tempfile
from datetime import timedelta

from django.contrib.auth import get_user_model
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
        operation_id="다음 풀이할 문제 조회",
        operation_description="개인 과제에서 다음으로 풀이할 문제를 조회합니다. number와 recalled_num 순으로 정렬하여 아직 풀이되지 않은 문제를 반환합니다.",
        manual_parameters=[
            openapi.Parameter(
                name="personal_assignment_id",
                in_=openapi.IN_QUERY,
                description="개인 과제 ID",
                type=openapi.TYPE_INTEGER,
                required=True,
            ),
        ],
        responses={200: "다음 문제", 404: "문제를 찾을 수 없음"},
    )
    def get(self, request):
        """
        다음 풀이할 문제 조회

        Query Parameters:
            - personal_assignment_id: 개인 과제 ID

        Logic:
            1. personal_assignment에 연결된 모든 question 조회
            2. number, recalled_num 순으로 정렬
            3. number가 같은 그룹 중 recalled_num이 가장 큰 것이 3 미만이면 반환
            4. recalled_num 최대값이 3이면 다음 number로 넘어감
        """
        try:
            personal_assignment_id = request.query_params.get("personal_assignment_id")

            if not personal_assignment_id:
                return create_api_response(
                    success=False,
                    error="Missing personal_assignment_id",
                    message="personal_assignment_id는 필수 파라미터입니다.",
                    status_code=status.HTTP_400_BAD_REQUEST,
                )

            # PersonalAssignment 조회
            try:
                personal_assignment = PersonalAssignment.objects.get(id=personal_assignment_id)
            except PersonalAssignment.DoesNotExist:
                return create_api_response(
                    success=False,
                    error="PersonalAssignment not found",
                    message=f"ID가 {personal_assignment_id}인 개인 과제를 찾을 수 없습니다.",
                    status_code=status.HTTP_404_NOT_FOUND,
                )

            # 모든 question 조회 (number, recalled_num 순으로 정렬)
            questions = personal_assignment.questions.order_by("number", "recalled_num")

            if not questions.exists():
                return create_api_response(
                    success=False,
                    error="No questions found",
                    message="해당 개인 과제에 문제가 없습니다.",
                    status_code=status.HTTP_404_NOT_FOUND,
                )

            # 최적화: 한 번의 쿼리로 모든 답변 정보 가져오기
            answered_question_ids = set(
                Answer.objects.filter(question__in=questions, student=personal_assignment.student).values_list(
                    "question_id", flat=True
                )
            )

            # number별로 그룹화하여 처리
            current_number = None
            max_recalled_num = -1
            candidate_question = None

            for question in questions:
                # 새로운 number 그룹 시작
                if current_number != question.number:
                    # 이전 그룹에서 후보를 찾았다면 반환
                    if candidate_question:
                        break

                    # 새 그룹 초기화
                    current_number = question.number
                    max_recalled_num = question.recalled_num

                    # 이 질문이 아직 답변되지 않았는지 확인 (메모리에서 체크)
                    if question.id not in answered_question_ids:
                        candidate_question = question
                else:
                    # 같은 number 그룹 내에서
                    max_recalled_num = max(max_recalled_num, question.recalled_num)

                    # 아직 답변되지 않은 질문 찾기 (메모리에서 체크)
                    if question.id not in answered_question_ids:
                        candidate_question = question

            # 마지막 그룹의 후보 확인
            if candidate_question:
                # recalled_num이 3 미만인지 확인
                if candidate_question.recalled_num < 3:
                    if candidate_question.recalled_num == 0:
                        number_str = f"{candidate_question.number}"
                    else:
                        number_str = f"{candidate_question.number}-{candidate_question.recalled_num}"
                    question_data = {
                        "id": candidate_question.id,
                        "number": number_str,
                        "question": candidate_question.content,
                        "answer": candidate_question.model_answer,
                        "explanation": candidate_question.explanation,
                        "difficulty": candidate_question.difficulty,
                    }

                    return create_api_response(
                        data=question_data,
                        message="다음 문제 조회 성공",
                        status_code=status.HTTP_200_OK,
                    )

            # 모든 문제를 다 풀었거나 recalled_num이 3에 도달한 경우
            return create_api_response(
                success=False,
                error="No more questions",
                message="모든 문제를 완료했습니다.",
                status_code=status.HTTP_404_NOT_FOUND,
            )

        except Exception as e:
            logger.error(f"[AnswerSubmitView GET] {e}", exc_info=True)
            return create_api_response(
                success=False,
                error=str(e),
                message="다음 문제 조회 중 오류가 발생했습니다.",
                status_code=status.HTTP_500_INTERNAL_SERVER_ERROR,
            )

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
        manual_parameters=[
            openapi.Parameter(
                "studentId",
                openapi.IN_FORM,
                description="제출하는 학생의 ID",
                type=openapi.TYPE_INTEGER,
                required=True,
            ),
            openapi.Parameter(
                "questionId",
                openapi.IN_FORM,
                description="제출한 문제의 ID",
                type=openapi.TYPE_INTEGER,
                required=True,
            ),
            openapi.Parameter(
                "audioFile",
                openapi.IN_FORM,
                description="음성 답변 파일 (.wav 형식)",
                type=openapi.TYPE_FILE,
                required=True,
            ),
        ],
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

            # 임시 파일 생성 (extract_all_features 함수용)
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

                if plan == "ASK" and tail_payload.get("recalled_time") < 4:
                    tail_question_data = tail_payload.get("tail_question", {})

                    if not tail_question_data:
                        logger.warning("[AnswerSubmitView] Plan이 ASK이지만 tail_question 데이터가 없습니다.")
                    else:
                        logger.info("[AnswerSubmitView] Tail Question 객체 생성 시작")

                        try:
                            recalled_time = tail_payload.get("recalled_time", question.recalled_num + 1)

                            # 먼저 동일한 조합의 Question이 이미 존재하는지 확인
                            existing_tail_question = Question.objects.filter(
                                personal_assignment=personal_assignment,
                                number=question.number,
                                recalled_num=recalled_time,
                            ).first()

                            if existing_tail_question:
                                logger.info(
                                    f"[AnswerSubmitView] 동일한 Tail Question이 이미 존재함 - Question ID: {existing_tail_question.id}"
                                )
                                tail_question_obj = existing_tail_question
                            else:
                                # Tail Question 생성
                                tail_question_obj = Question.objects.create(
                                    personal_assignment=personal_assignment,
                                    number=question.number,  # 원본 질문과 동일한 번호 사용 (base question number)
                                    content=tail_question_data.get("question", ""),
                                    model_answer=tail_question_data.get("model_answer", ""),
                                    explanation=tail_question_data.get("explanation", ""),
                                    difficulty=tail_question_data.get("difficulty", Question.Difficulty.MEDIUM),
                                    recalled_num=recalled_time,
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
                    logger.info(
                        f"[AnswerSubmitView] Plan이 '{plan}'이므로 Tail Question 생성하지 않고 다음 base 문제로 이동"
                    )
                    try:
                        tail_question_obj = Question.objects.get(
                            personal_assignment=personal_assignment,
                            number=question.number + 1,
                            recalled_num=0,
                        )
                    except Question.DoesNotExist:
                        logger.info(
                            f"[AnswerSubmitView] 다음 base Question이 존재하지 않음 - PersonalAssignment ID: {personal_assignment.id}, Number: {question.number + 1}"
                        )
                        tail_question_obj = None

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

                    # TailQuestionSerializer에 전달할 데이터에는 recalled_num과 number가 필요
                    tail_serializer = TailQuestionSerializer(
                        {
                            "tail_question": tail_question_data,
                            "is_correct": is_correct,
                            "number": tail_question_obj.number,
                            "recalled_num": tail_question_obj.recalled_num,
                        }
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


# 개인 과제 완료
class PersonalAssignmentCompleteView(APIView):
    @swagger_auto_schema(
        operation_id="개인 과제 완료",
        operation_description="개인 과제를 완료 상태로 변경합니다.",
        responses={
            200: openapi.Response(description="과제 완료 성공"),
            404: openapi.Response(description="과제를 찾을 수 없음"),
            500: openapi.Response(description="서버 오류"),
        },
    )
    def post(self, request, id):
        try:
            personal_assignment = PersonalAssignment.objects.get(pk=id)

            # 과제 상태를 SUBMITTED로 변경
            personal_assignment.status = PersonalAssignment.Status.SUBMITTED
            personal_assignment.submitted_at = timezone.now()
            personal_assignment.save()

            logger.info(f"[PersonalAssignmentCompleteView] Personal assignment {id} completed successfully")

            return create_api_response(
                success=True,
                data=None,
                message="과제가 성공적으로 완료되었습니다.",
                status_code=status.HTTP_200_OK,
            )

        except PersonalAssignment.DoesNotExist:
            logger.warning(f"[PersonalAssignmentCompleteView] Personal assignment {id} not found")
            return create_api_response(
                success=False,
                error="PersonalAssignment not found",
                message="해당 과제를 찾을 수 없습니다.",
                status_code=status.HTTP_404_NOT_FOUND,
            )
        except Exception as e:
            logger.error(f"[PersonalAssignmentCompleteView] {e}", exc_info=True)
            return create_api_response(
                success=False,
                error=str(e),
                message="과제 완료 중 오류가 발생했습니다.",
                status_code=status.HTTP_500_INTERNAL_SERVER_ERROR,
            )

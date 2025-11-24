package com.example.voicetutor.data.models

/**
 * Factory Pattern을 사용하여 다양한 소스에서 DetailedQuestionResult를 생성합니다.
 *
 * 이 Factory는 다음 소스들에서 DetailedQuestionResult를 생성할 수 있습니다:
 * - AssignmentCorrectnessItem: API에서 받은 정답 여부 데이터
 * - PersonalAssignmentQuestion: 개인 과제 질문 데이터
 * - TailQuestion: 꼬리 질문 데이터
 * - DetailedAnswer: 상세 답변 데이터
 */
object DetailedQuestionResultFactory {

    /**
     * AssignmentCorrectnessItem에서 DetailedQuestionResult를 생성합니다.
     * 이는 API에서 받은 정답 여부 데이터를 UI 표시용 형식으로 변환합니다.
     *
     * @param item API에서 받은 정답 여부 데이터
     * @return DetailedQuestionResult 객체
     */
    fun fromCorrectnessItem(item: AssignmentCorrectnessItem): DetailedQuestionResult {
        return DetailedQuestionResult(
            questionNumber = item.questionNum,
            question = item.questionContent,
            myAnswer = item.studentAnswer,
            correctAnswer = item.questionModelAnswer,
            isCorrect = item.isCorrect,
            explanation = item.explanation,
        )
    }

    /**
     * PersonalAssignmentQuestion에서 DetailedQuestionResult를 생성합니다.
     * 주의: PersonalAssignmentQuestion은 학생 답변 정보가 없으므로,
     * myAnswer는 빈 문자열이고 isCorrect는 false로 설정됩니다.
     * 실제 정답 여부는 별도로 확인해야 합니다.
     *
     * @param question 개인 과제 질문 데이터
     * @param myAnswer 학생의 답변 (선택적, 기본값: 빈 문자열)
     * @param isCorrect 정답 여부 (선택적, 기본값: false)
     * @return DetailedQuestionResult 객체
     */
    fun fromPersonalAssignmentQuestion(
        question: PersonalAssignmentQuestion,
        myAnswer: String = "",
        isCorrect: Boolean = false,
    ): DetailedQuestionResult {
        return DetailedQuestionResult(
            questionNumber = question.number,
            question = question.question,
            myAnswer = myAnswer,
            correctAnswer = question.answer,
            isCorrect = isCorrect,
            explanation = question.explanation,
        )
    }

    /**
     * TailQuestion에서 DetailedQuestionResult를 생성합니다.
     * 주의: TailQuestion은 학생 답변 정보가 없으므로,
     * myAnswer는 빈 문자열이고 isCorrect는 false로 설정됩니다.
     * 실제 정답 여부는 별도로 확인해야 합니다.
     *
     * @param tailQuestion 꼬리 질문 데이터
     * @param myAnswer 학생의 답변 (선택적, 기본값: 빈 문자열)
     * @param isCorrect 정답 여부 (선택적, 기본값: false)
     * @return DetailedQuestionResult 객체
     */
    fun fromTailQuestion(
        tailQuestion: TailQuestion,
        myAnswer: String = "",
        isCorrect: Boolean = false,
    ): DetailedQuestionResult {
        return DetailedQuestionResult(
            questionNumber = tailQuestion.number,
            question = tailQuestion.question,
            myAnswer = myAnswer,
            correctAnswer = tailQuestion.answer,
            isCorrect = isCorrect,
            explanation = tailQuestion.explanation,
        )
    }

    /**
     * DetailedAnswer에서 DetailedQuestionResult를 생성합니다.
     * DetailedAnswer는 이미 학생 답변과 정답 여부 정보를 포함하고 있습니다.
     *
     * @param answer 상세 답변 데이터
     * @return DetailedQuestionResult 객체
     */
    fun fromDetailedAnswer(answer: DetailedAnswer): DetailedQuestionResult {
        return DetailedQuestionResult(
            questionNumber = answer.questionNumber.toString(),
            question = answer.question,
            myAnswer = answer.studentAnswer,
            correctAnswer = answer.correctAnswer,
            isCorrect = answer.isCorrect,
            explanation = null, // DetailedAnswer에는 explanation 필드가 없음
        )
    }

    /**
     * 여러 AssignmentCorrectnessItem을 한 번에 DetailedQuestionResult 리스트로 변환합니다.
     *
     * @param items AssignmentCorrectnessItem 리스트
     * @return DetailedQuestionResult 리스트
     */
    fun fromCorrectnessItems(items: List<AssignmentCorrectnessItem>): List<DetailedQuestionResult> {
        return items.map { fromCorrectnessItem(it) }
    }

    /**
     * 여러 PersonalAssignmentQuestion을 한 번에 DetailedQuestionResult 리스트로 변환합니다.
     *
     * @param questions PersonalAssignmentQuestion 리스트
     * @param answerMap 질문 번호를 키로 하는 학생 답변 맵 (선택적)
     * @param correctnessMap 질문 번호를 키로 하는 정답 여부 맵 (선택적)
     * @return DetailedQuestionResult 리스트
     */
    fun fromPersonalAssignmentQuestions(
        questions: List<PersonalAssignmentQuestion>,
        answerMap: Map<String, String> = emptyMap(),
        correctnessMap: Map<String, Boolean> = emptyMap(),
    ): List<DetailedQuestionResult> {
        return questions.map { question ->
            fromPersonalAssignmentQuestion(
                question = question,
                myAnswer = answerMap[question.number] ?: "",
                isCorrect = correctnessMap[question.number] ?: false,
            )
        }
    }

    /**
     * 여러 TailQuestion을 한 번에 DetailedQuestionResult 리스트로 변환합니다.
     *
     * @param tailQuestions TailQuestion 리스트
     * @param answerMap 질문 번호를 키로 하는 학생 답변 맵 (선택적)
     * @param correctnessMap 질문 번호를 키로 하는 정답 여부 맵 (선택적)
     * @return DetailedQuestionResult 리스트
     */
    fun fromTailQuestions(
        tailQuestions: List<TailQuestion>,
        answerMap: Map<String, String> = emptyMap(),
        correctnessMap: Map<String, Boolean> = emptyMap(),
    ): List<DetailedQuestionResult> {
        return tailQuestions.map { tailQuestion ->
            fromTailQuestion(
                tailQuestion = tailQuestion,
                myAnswer = answerMap[tailQuestion.number] ?: "",
                isCorrect = correctnessMap[tailQuestion.number] ?: false,
            )
        }
    }

    /**
     * 여러 DetailedAnswer를 한 번에 DetailedQuestionResult 리스트로 변환합니다.
     *
     * @param answers DetailedAnswer 리스트
     * @return DetailedQuestionResult 리스트
     */
    fun fromDetailedAnswers(answers: List<DetailedAnswer>): List<DetailedQuestionResult> {
        return answers.map { fromDetailedAnswer(it) }
    }
}


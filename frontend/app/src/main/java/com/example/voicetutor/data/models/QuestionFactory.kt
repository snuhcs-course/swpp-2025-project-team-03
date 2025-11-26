package com.example.voicetutor.data.models

/**
 * Factory Pattern을 사용하여 Base Question과 Tail Question을 구분해서 생성합니다.
 *
 * 이 Factory는 다음 기능을 제공합니다:
 * - Base Question 생성: 질문 번호에 "-"가 없는 경우
 * - Tail Question 생성: 질문 번호에 "-"가 있는 경우 (예: "1-1", "1-2")
 * - QuestionGroup 생성: Base Question과 Tail Questions를 그룹화
 */
object QuestionFactory {

    /**
     * Question Factory가 생산하는 기본 Question 클래스
     */
    open class Question(
        open val questionNumber: String,
        open val questionText: String,
    )

    /**
     * Base Question 구현체 (하이픈이 없는 질문 번호)
     */
    class BaseQuestion(
        override val questionNumber: String,
        override val questionText: String,
    ) : Question(questionNumber, questionText)

    /**
     * Tail Question 구현체 (하이픈이 있는 질문 번호)
     */
    class TailQuestion(
        val baseNumber: String,
        override val questionNumber: String,
        override val questionText: String,
    ) : Question(questionNumber, questionText)

    /**
     * 전달받은 질문 번호/본문으로 Base 또는 Tail Question을 생성합니다.
     *
     * @param number 질문 번호
     * @param text 질문 본문
     * @return BaseQuestion 또는 TailQuestion 인스턴스
     */
    fun createQuestion(number: String, text: String): Question {
        return if ("-" in number) {
            TailQuestion(
                baseNumber = number.substringBefore("-"),
                questionNumber = number,
                questionText = text,
            )
        } else {
            BaseQuestion(
                questionNumber = number,
                questionText = text,
            )
        }
    }

    /**
     * AssignmentCorrectnessItem에서 DetailedQuestionResult를 생성합니다.
     *
     * @param item API에서 받은 정답 여부 데이터
     * @return DetailedQuestionResult 객체
     */
    fun createDetailedResult(item: AssignmentCorrectnessItem): DetailedQuestionResult {
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
     * 여러 AssignmentCorrectnessItem을 한 번에 DetailedQuestionResult 리스트로 변환합니다.
     *
     * @param items AssignmentCorrectnessItem 리스트
     * @return DetailedQuestionResult 리스트
     */
    fun createQuestions(items: List<AssignmentCorrectnessItem>): List<DetailedQuestionResult> {
        return items.map { createDetailedResult(it) }
    }

    /**
     * 질문 번호가 Base Question인지 확인합니다.
     * Base Question은 "-"가 없는 질문 번호입니다 (예: "1", "2", "3").
     *
     * @param questionNumber 질문 번호
     * @return Base Question이면 true, Tail Question이면 false
     */
    fun isBaseQuestion(questionNumber: String): Boolean {
        return !questionNumber.contains("-")
    }

    /**
     * 질문 번호가 Tail Question인지 확인합니다.
     * Tail Question은 "-"가 있는 질문 번호입니다 (예: "1-1", "1-2", "2-1").
     *
     * @param questionNumber 질문 번호
     * @return Tail Question이면 true, Base Question이면 false
     */
    fun isTailQuestion(questionNumber: String): Boolean {
        return questionNumber.contains("-")
    }

    /**
     * 질문 번호에서 Base Question 번호를 추출합니다.
     * 예: "1-1" -> "1", "2-2" -> "2", "3" -> "3"
     *
     * @param questionNumber 질문 번호
     * @return Base Question 번호
     */
    fun extractBaseNumber(questionNumber: String): String {
        return if (questionNumber.contains("-")) {
            questionNumber.substringBefore("-")
        } else {
            questionNumber
        }
    }

    /**
     * DetailedQuestionResult 리스트를 QuestionGroup 리스트로 변환합니다.
     * Base Question과 Tail Question을 그룹화하여 QuestionGroup을 생성합니다.
     *
     * @param questions DetailedQuestionResult 리스트
     * @return QuestionGroup 리스트 (Base Question 번호로 정렬됨)
     */
    fun createQuestionGroups(questions: List<DetailedQuestionResult>): List<QuestionGroup> {
        // Base Question 번호별로 그룹화
        val grouped = mutableMapOf<String, MutableList<DetailedQuestionResult>>()

        questions.forEach { result ->
            val questionMeta = createQuestion(result.questionNumber, result.question)
            val baseNum = when (questionMeta) {
                is BaseQuestion -> questionMeta.questionNumber
                is TailQuestion -> questionMeta.baseNumber
                else -> extractBaseNumber(questionMeta.questionNumber)
            }
            grouped.getOrPut(baseNum) { mutableListOf() }.add(result)
        }

        // QuestionGroup 리스트로 변환 (Base Question 번호로 정렬)
        return grouped.entries
            .sortedBy { it.key.toIntOrNull() ?: 0 }
            .map { (baseNum, questionList) ->
                // Base Question 찾기 (질문 번호가 baseNum과 정확히 일치하는 것)
                val baseQuestion = questionList.find { it.questionNumber == baseNum }
                    ?: questionList.first() // Base Question이 없으면 첫 번째 질문을 Base로 사용

                // Tail Questions 찾기 (질문 번호가 baseNum과 다른 것들)
                val tailQuestions = questionList
                    .filter { it.questionNumber != baseNum }
                    .sortedBy { it.questionNumber }

                QuestionGroup(
                    baseQuestion = baseQuestion,
                    tailQuestions = tailQuestions,
                )
            }
    }

    /**
     * AssignmentCorrectnessItem 리스트를 직접 QuestionGroup 리스트로 변환합니다.
     * 이 메서드는 가장 편리한 방법으로, API 응답을 바로 QuestionGroup으로 변환합니다.
     *
     * @param items AssignmentCorrectnessItem 리스트
     * @return QuestionGroup 리스트
     */
    fun createQuestionGroupsFromCorrectnessItems(items: List<AssignmentCorrectnessItem>): List<QuestionGroup> {
        val questions = createQuestions(items)
        return createQuestionGroups(questions)
    }
}

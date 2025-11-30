package com.example.voicetutor.data.models

/**
 * Question 타입을 나타내는 sealed class
 */
sealed class QuestionType {
    abstract val questionNumber: String

    /**
     * Base Question - 기본 문제 (예: "1", "2", "3")
     */
    data class BaseQuestion(
        override val questionNumber: String,
        val question: DetailedQuestionResult,
    ) : QuestionType()

    /**
     * Tail Question - 꼬리 문제 (예: "1-1", "1-2", "2-1")
     */
    data class TailQuestion(
        override val questionNumber: String,
        val baseNumber: String, // 기본 문제 번호 (예: "1", "2")
        val question: DetailedQuestionResult,
    ) : QuestionType()

    companion object {
        /**
         * Question number를 분석하여 Base 또는 Tail Question을 생성
         */
        fun from(question: DetailedQuestionResult): QuestionType {
            val questionNumber = question.questionNumber

            return if (questionNumber.contains("-")) {
                // Tail question (예: "1-1", "2-3")
                val baseNumber = questionNumber.substringBefore("-")
                TailQuestion(
                    questionNumber = questionNumber,
                    baseNumber = baseNumber,
                    question = question,
                )
            } else {
                // Base question (예: "1", "2", "3")
                BaseQuestion(
                    questionNumber = questionNumber,
                    question = question,
                )
            }
        }
    }
}

/**
 * QuestionGroup을 생성하는 Factory
 */
object QuestionGroupFactory {
    /**
     * DetailedQuestionResult 리스트를 받아서 QuestionGroup 리스트로 변환
     *
     * @param questions 질문 결과 리스트
     * @return QuestionGroup 리스트 (base question과 tail questions로 그룹화됨)
     */
    fun createQuestionGroups(questions: List<DetailedQuestionResult>): List<QuestionGroup> {
        // QuestionType으로 변환
        val questionTypes = questions.map { QuestionType.from(it) }

        // Base question 번호별로 그룹화
        val grouped = mutableMapOf<String, MutableList<QuestionType>>()

        questionTypes.forEach { questionType ->
            val baseNumber = when (questionType) {
                is QuestionType.BaseQuestion -> questionType.questionNumber
                is QuestionType.TailQuestion -> questionType.baseNumber
            }

            grouped.getOrPut(baseNumber) { mutableListOf() }.add(questionType)
        }

        // QuestionGroup으로 변환
        return grouped.entries
            .sortedBy { it.key.toIntOrNull() ?: 0 }
            .map { (baseNumber, questionTypes) ->
                // Base question 찾기
                val baseQuestion = questionTypes
                    .filterIsInstance<QuestionType.BaseQuestion>()
                    .firstOrNull()
                    ?.question
                    ?: questionTypes.first().let {
                        when (it) {
                            is QuestionType.BaseQuestion -> it.question
                            is QuestionType.TailQuestion -> it.question
                        }
                    }

                // Tail questions 찾기
                val tailQuestions = questionTypes
                    .filterIsInstance<QuestionType.TailQuestion>()
                    .map { it.question }
                    .sortedBy { it.questionNumber }

                QuestionGroup(
                    baseQuestion = baseQuestion,
                    tailQuestions = tailQuestions,
                )
            }
    }

    /**
     * PersonalAssignmentQuestion 리스트를 받아서 QuestionGroup 리스트로 변환
     *
     * @param questions PersonalAssignmentQuestion 리스트
     * @return QuestionGroup 리스트
     */
    fun createQuestionGroupsFromPersonalAssignment(
        questions: List<PersonalAssignmentQuestion>,
    ): List<QuestionGroup> {
        // PersonalAssignmentQuestion을 DetailedQuestionResult로 변환
        val detailedResults = questions.map { question ->
            DetailedQuestionResult(
                questionNumber = question.number,
                question = question.question,
                myAnswer = "", // PersonalAssignmentQuestion에는 답변이 없음
                correctAnswer = question.answer,
                isCorrect = false, // 아직 답변하지 않았으므로 false
                explanation = question.explanation,
            )
        }

        return createQuestionGroups(detailedResults)
    }

    /**
     * Question number가 base question인지 확인
     *
     * @param questionNumber 질문 번호 (예: "1", "1-1", "2-2")
     * @return base question이면 true, tail question이면 false
     */
    fun isBaseQuestion(questionNumber: String): Boolean {
        return !questionNumber.contains("-")
    }

    /**
     * Question number에서 base number 추출
     *
     * @param questionNumber 질문 번호 (예: "1", "1-1", "2-2")
     * @return base number (예: "1", "2")
     */
    fun extractBaseNumber(questionNumber: String): String {
        return if (questionNumber.contains("-")) {
            questionNumber.substringBefore("-")
        } else {
            questionNumber
        }
    }
}

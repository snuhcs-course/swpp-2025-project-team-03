package com.example.voicetutor.data.models

import org.junit.Assert.*
import org.junit.Test

class QuestionResultModelsTest {

    @Test
    fun detailedQuestionResult_createsCorrectly() {
        // Given
        val questionResult = DetailedQuestionResult(
            questionNumber = "1",
            question = "What is 2+2?",
            myAnswer = "4",
            correctAnswer = "4",
            isCorrect = true,
            explanation = "Basic addition",
        )

        // Then
        assertEquals("1", questionResult.questionNumber)
        assertEquals("What is 2+2?", questionResult.question)
        assertEquals("4", questionResult.myAnswer)
        assertEquals("4", questionResult.correctAnswer)
        assertTrue(questionResult.isCorrect)
        assertEquals("Basic addition", questionResult.explanation)
    }

    @Test
    fun detailedQuestionResult_withoutExplanation_createsCorrectly() {
        // Given
        val questionResult = DetailedQuestionResult(
            questionNumber = "2",
            question = "What is 3+3?",
            myAnswer = "5",
            correctAnswer = "6",
            isCorrect = false,
        )

        // Then
        assertEquals("2", questionResult.questionNumber)
        assertFalse(questionResult.isCorrect)
        assertNull(questionResult.explanation)
    }

    @Test
    fun questionGroup_createsCorrectly() {
        // Given
        val baseQuestion = DetailedQuestionResult(
            questionNumber = "1",
            question = "Base question",
            myAnswer = "Answer",
            correctAnswer = "Answer",
            isCorrect = true,
        )
        val tailQuestion1 = DetailedQuestionResult(
            questionNumber = "1-1",
            question = "Tail question 1",
            myAnswer = "Answer",
            correctAnswer = "Answer",
            isCorrect = true,
        )
        val tailQuestion2 = DetailedQuestionResult(
            questionNumber = "1-2",
            question = "Tail question 2",
            myAnswer = "Wrong",
            correctAnswer = "Answer",
            isCorrect = false,
        )

        val questionGroup = QuestionGroup(
            baseQuestion = baseQuestion,
            tailQuestions = listOf(tailQuestion1, tailQuestion2),
        )

        // Then
        assertEquals(baseQuestion, questionGroup.baseQuestion)
        assertEquals(2, questionGroup.tailQuestions.size)
        assertEquals(tailQuestion1, questionGroup.tailQuestions[0])
        assertEquals(tailQuestion2, questionGroup.tailQuestions[1])
    }

    @Test
    fun questionGroup_withEmptyTailQuestions_createsCorrectly() {
        // Given
        val baseQuestion = DetailedQuestionResult(
            questionNumber = "1",
            question = "Base question",
            myAnswer = "Answer",
            correctAnswer = "Answer",
            isCorrect = true,
        )

        val questionGroup = QuestionGroup(
            baseQuestion = baseQuestion,
            tailQuestions = emptyList(),
        )

        // Then
        assertEquals(baseQuestion, questionGroup.baseQuestion)
        assertTrue(questionGroup.tailQuestions.isEmpty())
    }

    @Test
    fun questionGroup_withDefaultTailQuestions_createsCorrectly() {
        // Given
        val baseQuestion = DetailedQuestionResult(
            questionNumber = "1",
            question = "Base question",
            myAnswer = "Answer",
            correctAnswer = "Answer",
            isCorrect = true,
        )

        val questionGroup = QuestionGroup(baseQuestion = baseQuestion)

        // Then
        assertEquals(baseQuestion, questionGroup.baseQuestion)
        assertTrue(questionGroup.tailQuestions.isEmpty())
    }
}

package com.example.voicetutor.ui.screens

import com.example.voicetutor.ui.theme.*
import org.junit.Assert.*
import org.junit.Test

/**
 * Unit tests for data classes used in UI screens.
 */
class ScreenDataClassesTest {

    @Test
    fun classRoom_creation_withAllFields_createsCorrectInstance() {
        val classRoom = ClassRoom(
            id = 1,
            name = "수학 A반",
            subject = "수학",
            description = "기초 수학 수업",
            studentCount = 30,
            assignmentCount = 5,
            completionRate = 0.8f,
            color = PrimaryIndigo,
        )

        assertEquals(1, classRoom.id)
        assertEquals("수학 A반", classRoom.name)
        assertEquals("수학", classRoom.subject)
        assertEquals("기초 수학 수업", classRoom.description)
        assertEquals(30, classRoom.studentCount)
        assertEquals(5, classRoom.assignmentCount)
        assertEquals(0.8f, classRoom.completionRate)
        assertEquals(PrimaryIndigo, classRoom.color)
    }

    @Test
    fun classRoom_copy_createsNewInstance() {
        val original = ClassRoom(
            id = 1,
            name = "수학 A반",
            subject = "수학",
            description = "기초 수학 수업",
            studentCount = 30,
            assignmentCount = 5,
            completionRate = 0.8f,
            color = PrimaryIndigo,
        )
        val copy = original.copy(name = "수학 B반", studentCount = 25)

        assertEquals("수학 B반", copy.name)
        assertEquals(25, copy.studentCount)
        assertEquals("수학 A반", original.name)
        assertEquals(30, original.studentCount)
    }

    @Test
    fun classRoom_equality_worksCorrectly() {
        val room1 = ClassRoom(1, "수학 A반", "수학", "설명", 30, 5, 0.8f, PrimaryIndigo)
        val room2 = ClassRoom(1, "수학 A반", "수학", "설명", 30, 5, 0.8f, PrimaryIndigo)
        val room3 = ClassRoom(2, "수학 A반", "수학", "설명", 30, 5, 0.8f, PrimaryIndigo)

        assertEquals(room1, room2)
        assertNotEquals(room1, room3)
    }

    @Test
    fun classRoom_hashCode_isConsistent() {
        val room1 = ClassRoom(1, "수학 A반", "수학", "설명", 30, 5, 0.8f, PrimaryIndigo)
        val room2 = ClassRoom(1, "수학 A반", "수학", "설명", 30, 5, 0.8f, PrimaryIndigo)

        assertEquals(room1.hashCode(), room2.hashCode())
    }

    @Test
    fun classRoom_componentAccess_worksCorrectly() {
        val room = ClassRoom(1, "수학 A반", "수학", "설명", 30, 5, 0.8f, PrimaryIndigo)
        val (id, name, subject, description, studentCount, assignmentCount, completionRate, color) = room

        assertEquals(1, id)
        assertEquals("수학 A반", name)
        assertEquals("수학", subject)
        assertEquals("설명", description)
        assertEquals(30, studentCount)
        assertEquals(5, assignmentCount)
        assertEquals(0.8f, completionRate)
        assertEquals(PrimaryIndigo, color)
    }

    @Test
    fun classAssignment_creation_withAllFields_createsCorrectInstance() {
        val assignment = ClassAssignment(
            id = 1,
            title = "과제 1",
            subject = "수학",
            dueDate = "2024-12-31",
            completionRate = 0.75f,
            totalStudents = 30,
            completedStudents = 22,
            averageScore = 85,
        )

        assertEquals(1, assignment.id)
        assertEquals("과제 1", assignment.title)
        assertEquals("수학", assignment.subject)
        assertEquals("2024-12-31", assignment.dueDate)
        assertEquals(0.75f, assignment.completionRate)
        assertEquals(30, assignment.totalStudents)
        assertEquals(22, assignment.completedStudents)
        assertEquals(85, assignment.averageScore)
    }

    @Test
    fun classAssignment_copy_createsNewInstance() {
        val original = ClassAssignment(1, "과제 1", "수학", "2024-12-31", 0.75f, 30, 22, 85)
        val copy = original.copy(title = "과제 2", averageScore = 90)

        assertEquals("과제 2", copy.title)
        assertEquals(90, copy.averageScore)
        assertEquals("과제 1", original.title)
        assertEquals(85, original.averageScore)
    }

    @Test
    fun classAssignment_equality_worksCorrectly() {
        val assignment1 = ClassAssignment(1, "과제 1", "수학", "2024-12-31", 0.75f, 30, 22, 85)
        val assignment2 = ClassAssignment(1, "과제 1", "수학", "2024-12-31", 0.75f, 30, 22, 85)
        val assignment3 = ClassAssignment(2, "과제 1", "수학", "2024-12-31", 0.75f, 30, 22, 85)

        assertEquals(assignment1, assignment2)
        assertNotEquals(assignment1, assignment3)
    }

    @Test
    fun classAssignment_withZeroCompletionRate_handlesCorrectly() {
        val assignment = ClassAssignment(1, "과제 1", "수학", "2024-12-31", 0f, 30, 0, 0)
        assertEquals(0f, assignment.completionRate)
        assertEquals(0, assignment.completedStudents)
    }

    @Test
    fun classAssignment_withFullCompletionRate_handlesCorrectly() {
        val assignment = ClassAssignment(1, "과제 1", "수학", "2024-12-31", 1f, 30, 30, 100)
        assertEquals(1f, assignment.completionRate)
        assertEquals(30, assignment.completedStudents)
    }

    @Test
    fun quizQuestionData_creation_withAllFields_createsCorrectInstance() {
        val question = QuizQuestionData(
            questionNumber = 1,
            question = "질문",
            hint = "힌트",
            modelAnswer = "정답",
        )

        assertEquals(1, question.questionNumber)
        assertEquals("질문", question.question)
        assertEquals("힌트", question.hint)
        assertEquals("정답", question.modelAnswer)
    }

    @Test
    fun quizQuestionData_copy_createsNewInstance() {
        val original = QuizQuestionData(1, "질문", "힌트", "정답")
        val copy = original.copy(question = "새 질문")

        assertEquals("새 질문", copy.question)
        assertEquals("질문", original.question)
    }

    @Test
    fun quizQuestionData_equality_worksCorrectly() {
        val question1 = QuizQuestionData(1, "질문", "힌트", "정답")
        val question2 = QuizQuestionData(1, "질문", "힌트", "정답")
        val question3 = QuizQuestionData(2, "질문", "힌트", "정답")

        assertEquals(question1, question2)
        assertNotEquals(question1, question3)
    }

    @Test
    fun quizQuestionData_componentAccess_worksCorrectly() {
        val question = QuizQuestionData(1, "질문", "힌트", "정답")
        val (number, q, hint, answer) = question

        assertEquals(1, number)
        assertEquals("질문", q)
        assertEquals("힌트", hint)
        assertEquals("정답", answer)
    }

    @Test
    fun assignmentDetail_creation_withAllFields_createsCorrectInstance() {
        val detail = AssignmentDetail(
            title = "과제 1",
            subject = "수학",
            className = "수학 A반",
            dueDate = "2024-12-31",
            createdAt = "2024-01-01",
            status = "진행중",
            type = "퀴즈",
            description = "설명",
            totalStudents = 30,
            submittedStudents = 22,
            averageScore = 85,
            completionRate = 73,
        )

        assertEquals("과제 1", detail.title)
        assertEquals("수학", detail.subject)
        assertEquals("수학 A반", detail.className)
        assertEquals("2024-12-31", detail.dueDate)
        assertEquals("2024-01-01", detail.createdAt)
        assertEquals("진행중", detail.status)
        assertEquals("퀴즈", detail.type)
        assertEquals("설명", detail.description)
        assertEquals(30, detail.totalStudents)
        assertEquals(22, detail.submittedStudents)
        assertEquals(85, detail.averageScore)
        assertEquals(73, detail.completionRate)
    }

    @Test
    fun assignmentDetail_copy_createsNewInstance() {
        val original = AssignmentDetail(
            "과제 1", "수학", "수학 A반", "2024-12-31", "2024-01-01",
            "진행중", "퀴즈", "설명", 30, 22, 85, 73,
        )
        val copy = original.copy(title = "과제 2", averageScore = 90)

        assertEquals("과제 2", copy.title)
        assertEquals(90, copy.averageScore)
        assertEquals("과제 1", original.title)
        assertEquals(85, original.averageScore)
    }

    @Test
    fun assignmentDetail_equality_worksCorrectly() {
        val detail1 = AssignmentDetail(
            "과제 1", "수학", "수학 A반", "2024-12-31", "2024-01-01",
            "진행중", "퀴즈", "설명", 30, 22, 85, 73,
        )
        val detail2 = AssignmentDetail(
            "과제 1", "수학", "수학 A반", "2024-12-31", "2024-01-01",
            "진행중", "퀴즈", "설명", 30, 22, 85, 73,
        )
        val detail3 = AssignmentDetail(
            "과제 2", "수학", "수학 A반", "2024-12-31", "2024-01-01",
            "진행중", "퀴즈", "설명", 30, 22, 85, 73,
        )

        assertEquals(detail1, detail2)
        assertNotEquals(detail1, detail3)
    }

    @Test
    fun studentSubmission_creation_withAllFields_createsCorrectInstance() {
        val submission = StudentSubmission(
            name = "홍길동",
            studentId = "2024001",
            submittedAt = "2024-12-30",
            score = 85,
            status = "제출완료",
        )

        assertEquals("홍길동", submission.name)
        assertEquals("2024001", submission.studentId)
        assertEquals("2024-12-30", submission.submittedAt)
        assertEquals(85, submission.score)
        assertEquals("제출완료", submission.status)
    }

    @Test
    fun studentSubmission_copy_createsNewInstance() {
        val original = StudentSubmission("홍길동", "2024001", "2024-12-30", 85, "제출완료")
        val copy = original.copy(name = "김철수", score = 90)

        assertEquals("김철수", copy.name)
        assertEquals(90, copy.score)
        assertEquals("홍길동", original.name)
        assertEquals(85, original.score)
    }

    @Test
    fun studentSubmission_equality_worksCorrectly() {
        val submission1 = StudentSubmission("홍길동", "2024001", "2024-12-30", 85, "제출완료")
        val submission2 = StudentSubmission("홍길동", "2024001", "2024-12-30", 85, "제출완료")
        val submission3 = StudentSubmission("김철수", "2024001", "2024-12-30", 85, "제출완료")

        assertEquals(submission1, submission2)
        assertNotEquals(submission1, submission3)
    }

    @Test
    fun studentSubmission_componentAccess_worksCorrectly() {
        val submission = StudentSubmission("홍길동", "2024001", "2024-12-30", 85, "제출완료")
        val (name, studentId, submittedAt, score, status) = submission

        assertEquals("홍길동", name)
        assertEquals("2024001", studentId)
        assertEquals("2024-12-30", submittedAt)
        assertEquals(85, score)
        assertEquals("제출완료", status)
    }

    @Test
    fun classRoom_withDifferentColors_handlesCorrectly() {
        val colors = listOf(PrimaryIndigo, Success, Warning, Error)
        colors.forEachIndexed { index, color ->
            val room = ClassRoom(
                id = index,
                name = "반 $index",
                subject = "과목",
                description = "설명",
                studentCount = 30,
                assignmentCount = 5,
                completionRate = 0.8f,
                color = color,
            )
            assertEquals(color, room.color)
        }
    }

    @Test
    fun classRoom_withZeroStudents_handlesCorrectly() {
        val room = ClassRoom(1, "반", "과목", "설명", 0, 0, 0f, PrimaryIndigo)
        assertEquals(0, room.studentCount)
        assertEquals(0, room.assignmentCount)
        assertEquals(0f, room.completionRate)
    }

    @Test
    fun classAssignment_withMaxScore_handlesCorrectly() {
        val assignment = ClassAssignment(1, "과제", "과목", "2024-12-31", 1f, 30, 30, 100)
        assertEquals(100, assignment.averageScore)
        assertEquals(1f, assignment.completionRate)
    }

    @Test
    fun quizQuestionData_withLargeQuestionNumber_handlesCorrectly() {
        val question = QuizQuestionData(Int.MAX_VALUE, "질문", "힌트", "정답")
        assertEquals(Int.MAX_VALUE, question.questionNumber)
    }

    @Test
    fun assignmentDetail_withZeroCompletionRate_handlesCorrectly() {
        val detail = AssignmentDetail(
            "과제", "과목", "반", "2024-12-31", "2024-01-01",
            "진행중", "퀴즈", "설명", 30, 0, 0, 0,
        )
        assertEquals(0, detail.completionRate)
        assertEquals(0, detail.submittedStudents)
        assertEquals(0, detail.averageScore)
    }

    @Test
    fun studentSubmission_withZeroScore_handlesCorrectly() {
        val submission = StudentSubmission("학생", "2024001", "2024-12-30", 0, "미제출")
        assertEquals(0, submission.score)
        assertEquals("미제출", submission.status)
    }

    @Test
    fun classRoom_toString_containsFields() {
        val room = ClassRoom(1, "수학 A반", "수학", "설명", 30, 5, 0.8f, PrimaryIndigo)
        val string = room.toString()
        assertTrue(string.contains("수학 A반") || string.contains("1"))
    }

    @Test
    fun classAssignment_toString_containsFields() {
        val assignment = ClassAssignment(1, "과제 1", "수학", "2024-12-31", 0.75f, 30, 22, 85)
        val string = assignment.toString()
        assertTrue(string.contains("과제 1") || string.contains("1"))
    }

    @Test
    fun quizQuestionData_toString_containsFields() {
        val question = QuizQuestionData(1, "질문", "힌트", "정답")
        val string = question.toString()
        assertTrue(string.contains("질문") || string.contains("1"))
    }

    @Test
    fun assignmentDetail_toString_containsFields() {
        val detail = AssignmentDetail(
            "과제 1", "수학", "수학 A반", "2024-12-31", "2024-01-01",
            "진행중", "퀴즈", "설명", 30, 22, 85, 73,
        )
        val string = detail.toString()
        assertTrue(string.contains("과제 1") || string.contains("수학"))
    }

    @Test
    fun studentSubmission_toString_containsFields() {
        val submission = StudentSubmission("홍길동", "2024001", "2024-12-30", 85, "제출완료")
        val string = submission.toString()
        assertTrue(string.contains("홍길동") || string.contains("2024001"))
    }

    @Test
    fun classAssignment_hashCode_isConsistent() {
        val assignment1 = ClassAssignment(1, "과제 1", "수학", "2024-12-31", 0.75f, 30, 22, 85)
        val assignment2 = ClassAssignment(1, "과제 1", "수학", "2024-12-31", 0.75f, 30, 22, 85)

        assertEquals(assignment1.hashCode(), assignment2.hashCode())
    }

    @Test
    fun quizQuestionData_hashCode_isConsistent() {
        val question1 = QuizQuestionData(1, "질문", "힌트", "정답")
        val question2 = QuizQuestionData(1, "질문", "힌트", "정답")

        assertEquals(question1.hashCode(), question2.hashCode())
    }

    @Test
    fun assignmentDetail_hashCode_isConsistent() {
        val detail1 = AssignmentDetail(
            "과제 1", "수학", "수학 A반", "2024-12-31", "2024-01-01",
            "진행중", "퀴즈", "설명", 30, 22, 85, 73,
        )
        val detail2 = AssignmentDetail(
            "과제 1", "수학", "수학 A반", "2024-12-31", "2024-01-01",
            "진행중", "퀴즈", "설명", 30, 22, 85, 73,
        )

        assertEquals(detail1.hashCode(), detail2.hashCode())
    }

    @Test
    fun studentSubmission_hashCode_isConsistent() {
        val submission1 = StudentSubmission("홍길동", "2024001", "2024-12-30", 85, "제출완료")
        val submission2 = StudentSubmission("홍길동", "2024001", "2024-12-30", 85, "제출완료")

        assertEquals(submission1.hashCode(), submission2.hashCode())
    }
}

package com.example.voicetutor.data.models

import com.example.voicetutor.R
import com.example.voicetutor.ui.components.OnboardingPage
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class OnboardingDataTest {

    @Test
    fun teacherOnboardingData_exists() {
        // Given/When
        val data = TeacherOnboardingData

        // Then
        assertNotNull(data)
    }

    @Test
    fun teacherOnboardingPages_hasCorrectCount() {
        // Given/When
        val pages = TeacherOnboardingData.teacherOnboardingPages

        // Then - 7단계 온보딩
        assertEquals(7, pages.size)
    }

    @Test
    fun teacherOnboardingPages_allPagesHaveValidFields() {
        // Given/When
        val pages = TeacherOnboardingData.teacherOnboardingPages

        // Then
        pages.forEachIndexed { index, page ->
            assertNotNull("Page $index should not be null", page)
            assertTrue("Page $index title should not be empty", page.title.isNotBlank())
            assertTrue("Page $index description should not be empty", page.description.isNotBlank())
            assertTrue("Page $index imageRes should be valid", page.imageRes != 0)
            assertNull("Page $index icon should be null", page.icon)
        }
    }

    @Test
    fun teacherOnboardingPages_firstPage_hasCorrectContent() {
        // Given/When
        val firstPage = TeacherOnboardingData.teacherOnboardingPages[0]

        // Then
        assertEquals("수업과 과제를 한눈에 관리해요", firstPage.title)
        assertTrue(firstPage.description.contains("대시보드"))
        assertEquals(R.drawable.teacherdashboard, firstPage.imageRes)
        assertNull(firstPage.icon)
    }

    @Test
    fun teacherOnboardingPages_secondPage_hasCorrectContent() {
        // Given/When
        val secondPage = TeacherOnboardingData.teacherOnboardingPages[1]

        // Then
        assertEquals("내 수업을 만들고 정리해요", secondPage.title)
        assertTrue(secondPage.description.contains("수업"))
        assertEquals(R.drawable.teacherclass, secondPage.imageRes)
        assertNull(secondPage.icon)
    }

    @Test
    fun teacherOnboardingPages_thirdPage_hasCorrectContent() {
        // Given/When
        val thirdPage = TeacherOnboardingData.teacherOnboardingPages[2]

        // Then
        assertEquals("각 수업에 배당할 과제를 생성해요", thirdPage.title)
        assertTrue(thirdPage.description.contains("과제"))
        assertEquals(R.drawable.assignmentcreate, thirdPage.imageRes)
        assertNull(thirdPage.icon)
    }

    @Test
    fun teacherOnboardingPages_fourthPage_hasCorrectContent() {
        // Given/When
        val fourthPage = TeacherOnboardingData.teacherOnboardingPages[3]

        // Then
        assertEquals("수업별 과제 진행 상황을 확인해요", fourthPage.title)
        assertTrue(fourthPage.description.contains("과제"))
        assertEquals(R.drawable.teacherclassassignment, fourthPage.imageRes)
        assertNull(fourthPage.icon)
    }

    @Test
    fun teacherOnboardingPages_fifthPage_hasCorrectContent() {
        // Given/When
        val fifthPage = TeacherOnboardingData.teacherOnboardingPages[4]

        // Then
        assertEquals("학생별 과제 결과를 자세히 확인해요", fifthPage.title)
        assertTrue(fifthPage.description.contains("학생"))
        assertEquals(R.drawable.teacherassignmentresult, fifthPage.imageRes)
        assertNull(fifthPage.icon)
    }

    @Test
    fun teacherOnboardingPages_sixthPage_hasCorrectContent() {
        // Given/When
        val sixthPage = TeacherOnboardingData.teacherOnboardingPages[5]

        // Then
        assertEquals("수업 별 학생 목록과 진도를 관리해요", sixthPage.title)
        assertTrue(sixthPage.description.contains("학생"))
        assertEquals(R.drawable.teacherclassstudent, sixthPage.imageRes)
        assertNull(sixthPage.icon)
    }

    @Test
    fun teacherOnboardingPages_seventhPage_hasCorrectContent() {
        // Given/When
        val seventhPage = TeacherOnboardingData.teacherOnboardingPages[6]

        // Then
        assertEquals("성취기준 리포트를 확인해요", seventhPage.title)
        assertTrue(seventhPage.description.contains("성취기준"))
        assertEquals(R.drawable.teacherreport, seventhPage.imageRes)
        assertNull(seventhPage.icon)
    }

    @Test
    fun studentOnboardingData_exists() {
        // Given/When
        val data = StudentOnboardingData

        // Then
        assertNotNull(data)
    }

    @Test
    fun studentOnboardingPages_hasCorrectCount() {
        // Given/When
        val pages = StudentOnboardingData.studentOnboardingPages

        // Then - 5단계 온보딩
        assertEquals(5, pages.size)
    }

    @Test
    fun studentOnboardingPages_allPagesHaveValidFields() {
        // Given/When
        val pages = StudentOnboardingData.studentOnboardingPages

        // Then
        pages.forEachIndexed { index, page ->
            assertNotNull("Page $index should not be null", page)
            assertTrue("Page $index title should not be empty", page.title.isNotBlank())
            assertTrue("Page $index description should not be empty", page.description.isNotBlank())
            assertTrue("Page $index imageRes should be valid", page.imageRes != 0)
            assertNull("Page $index icon should be null", page.icon)
        }
    }

    @Test
    fun studentOnboardingPages_firstPage_hasCorrectContent() {
        // Given/When
        val firstPage = StudentOnboardingData.studentOnboardingPages[0]

        // Then
        assertEquals("나에게 할당된 과제를 관리해요", firstPage.title)
        assertTrue(firstPage.description.contains("과제"))
        assertEquals(R.drawable.studentdashboard, firstPage.imageRes)
        assertNull(firstPage.icon)
    }

    @Test
    fun studentOnboardingPages_secondPage_hasCorrectContent() {
        // Given/When
        val secondPage = StudentOnboardingData.studentOnboardingPages[1]

        // Then
        assertEquals("과제 정보를 한눈에 확인해요", secondPage.title)
        assertTrue(secondPage.description.contains("과제"))
        assertEquals(R.drawable.studentassignment, secondPage.imageRes)
        assertNull(secondPage.icon)
    }

    @Test
    fun studentOnboardingPages_thirdPage_hasCorrectContent() {
        // Given/When
        val thirdPage = StudentOnboardingData.studentOnboardingPages[2]

        // Then
        assertEquals("문제를 음성으로 풀어보세요", thirdPage.title)
        assertTrue(thirdPage.description.contains("음성"))
        assertEquals(R.drawable.studentassignmentstudy, thirdPage.imageRes)
        assertNull(thirdPage.icon)
    }

    @Test
    fun studentOnboardingPages_fourthPage_hasCorrectContent() {
        // Given/When
        val fourthPage = StudentOnboardingData.studentOnboardingPages[3]

        // Then
        assertEquals("완료한 과제의 리포트를 확인해요", fourthPage.title)
        assertTrue(fourthPage.description.contains("과제"))
        assertEquals(R.drawable.studentreport, fourthPage.imageRes)
        assertNull(fourthPage.icon)
    }

    @Test
    fun studentOnboardingPages_fifthPage_hasCorrectContent() {
        // Given/When
        val fifthPage = StudentOnboardingData.studentOnboardingPages[4]

        // Then
        assertEquals("문제별 정답과 해설을 볼 수 있어요", fifthPage.title)
        assertTrue(fifthPage.description.contains("정답"))
        assertEquals(R.drawable.studentreportspecific, fifthPage.imageRes)
        assertNull(fifthPage.icon)
    }

    @Test
    fun teacherOnboardingPages_isImmutable() {
        // Given
        val pages1 = TeacherOnboardingData.teacherOnboardingPages
        val pages2 = TeacherOnboardingData.teacherOnboardingPages

        // Then - 같은 인스턴스여야 함 (object의 property이므로)
        assertEquals(pages1, pages2)
    }

    @Test
    fun studentOnboardingPages_isImmutable() {
        // Given
        val pages1 = StudentOnboardingData.studentOnboardingPages
        val pages2 = StudentOnboardingData.studentOnboardingPages

        // Then - 같은 인스턴스여야 함 (object의 property이므로)
        assertEquals(pages1, pages2)
    }

    @Test
    fun teacherOnboardingPages_allTitlesAreUnique() {
        // Given/When
        val pages = TeacherOnboardingData.teacherOnboardingPages
        val titles = pages.map { it.title }

        // Then
        assertEquals(titles.size, titles.distinct().size)
    }

    @Test
    fun studentOnboardingPages_allTitlesAreUnique() {
        // Given/When
        val pages = StudentOnboardingData.studentOnboardingPages
        val titles = pages.map { it.title }

        // Then
        assertEquals(titles.size, titles.distinct().size)
    }

    @Test
    fun teacherOnboardingPages_allImageResAreUnique() {
        // Given/When
        val pages = TeacherOnboardingData.teacherOnboardingPages
        val imageReses = pages.map { it.imageRes }

        // Then
        assertEquals(imageReses.size, imageReses.distinct().size)
    }

    @Test
    fun studentOnboardingPages_allImageResAreUnique() {
        // Given/When
        val pages = StudentOnboardingData.studentOnboardingPages
        val imageReses = pages.map { it.imageRes }

        // Then
        assertEquals(imageReses.size, imageReses.distinct().size)
    }
}


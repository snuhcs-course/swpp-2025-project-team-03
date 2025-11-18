package com.example.voicetutor.data.models

import com.example.voicetutor.R
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import com.example.voicetutor.ui.components.OnboardingPage

/**
 * 선생님용 온보딩 튜토리얼 데이터
 * 
 * 7단계 온보딩:
 * 1. Teacher Home Dashboard 화면
 * 2. 수업 관리(클래스 목록) 화면
 * 3. 과제 생성 화면
 * 4. 수업별 과제 목록 화면
 * 5. 과제 결과 화면 (학생별 결과)
 * 6. 학생 관리 화면
 * 7. 성취기준 리포트 화면
 */
object TeacherOnboardingData {
    
    val teacherOnboardingPages = listOf(
        OnboardingPage(
            title = "수업과 과제를 한눈에 관리해요",
            description = "대시보드에서 내가 만든 모든 과제를 확인하고\n자주 사용하는 작업을 빠르게 시작할 수 있어요.",
            imageRes = R.drawable.teacherdashboard,
            icon = null
        ),
        
        OnboardingPage(
            title = "내 수업을 만들고 정리해요",
            description = "여기에서 새 수업을 만들고, 각 수업의 정보를\n한 화면에서 확인할 수 있어요.",
            imageRes = R.drawable.teacherclass,
            icon = null
        ),
        
        OnboardingPage(
            title = "각 수업에 배당할 과제를 생성해요",
            description = "과제 제목, 반 선택, 학년, 과목, 설명, 마감일을 설정하여 새로운 과제를 만들고 수업에 배당할 수 있어요.",
            imageRes = R.drawable.assignmentcreate,
            icon = null
        ),
        
        OnboardingPage(
            title = "수업별 과제 진행 상황을 확인해요",
            description = "내 수업의 모든 과제의 제출률과\n평균 점수를 볼 수 있어요.",
            imageRes = R.drawable.teacherclassassignment,
            icon = null
        ),
        
        OnboardingPage(
            title = "학생별 과제 결과를 자세히 확인해요",
            description = "제출 학생 수와 평균 점수를 한눈에 보고, 학생별 점수와 제출 시간을 확인하며 개별 피드백을 줄 수 있어요.",
            imageRes = R.drawable.teacherassignmentresult,
            icon = null
        ),
        
        OnboardingPage(
            title = "수업 별 학생 목록과 진도를 관리해요",
            description = "수업에 학생을 등록하거나 삭제하여\n명단을 관리할 수 있어요.",
            imageRes = R.drawable.teacherclassstudent,
            icon = null
        ),
        
        OnboardingPage(
            title = "성취기준 리포트를 확인해요",
            description = "반을 선택해 학생별 성취기준 리포트를 확인하고\n취약 유형을 한눈에 분석할 수 있어요.",
            imageRes = R.drawable.teacherreport,
            icon = null
        )
    )
}


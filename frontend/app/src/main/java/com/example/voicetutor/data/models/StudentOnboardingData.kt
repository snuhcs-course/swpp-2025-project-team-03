package com.example.voicetutor.data.models

import com.example.voicetutor.R
import com.example.voicetutor.ui.components.OnboardingPage

object StudentOnboardingData {

    val studentOnboardingPages = listOf(
        OnboardingPage(
            title = "나에게 할당된 과제를 관리해요",
            description = "나에게 주어진 과제를\n홈 화면에서 빠르게 확인할 수 있어요.",
            imageRes = R.drawable.studentdashboard,
            icon = null,
        ),

        OnboardingPage(
            title = "과제 정보를 한눈에 확인해요",
            description = "과제 이름, 내용, 진행 현황을 볼 수 있어요.\n과제 시작을 누르면 바로 답변을 진행할 수 있어요.",
            imageRes = R.drawable.studentassignment,
            icon = null,
        ),

        OnboardingPage(
            title = "문제를 음성으로 풀어보세요",
            description = "문제를 읽고 음성을 녹음해 답변을 제출해요.\n건너뛰기 버튼도 사용할 수 있어요.",
            imageRes = R.drawable.studentassignmentstudy,
            icon = null,
        ),

        OnboardingPage(
            title = "완료한 과제의 리포트를 확인해요",
            description = "제출한 과제들의 점수와 결과를\n리스트에서 편하게 모아볼 수 있어요.",
            imageRes = R.drawable.studentreport,
            icon = null,
        ),

        OnboardingPage(
            title = "문제별 정답과 해설을 볼 수 있어요",
            description = "내 답변, 정답 여부까지\n하나의 화면에서 모두 확인할 수 있어요.",
            imageRes = R.drawable.studentreportspecific,
            icon = null,
        ),
    )
}

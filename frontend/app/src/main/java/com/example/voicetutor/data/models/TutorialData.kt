package com.example.voicetutor.data.models

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import com.example.voicetutor.ui.components.TutorialStep
import com.example.voicetutor.ui.theme.*

/**
 * 튜토리얼 데이터 정의
 */
object TutorialData {
    
    /**
     * 선생님 계정용 튜토리얼
     */
    val teacherTutorialSteps = listOf(
        TutorialStep(
            title = "환영합니다!",
            description = "VoiceTutor는 음성 기반 학습 관리 시스템입니다.\n학생들의 학습을 효과적으로 관리할 수 있습니다.",
            icon = Icons.Filled.EmojiEmotions,
            iconColor = PrimaryIndigo
        ),
        TutorialStep(
            title = "반 관리",
            description = "여러 반을 생성하고 학생들을 등록하세요.\n각 반별로 과제와 성적을 관리할 수 있습니다.",
            icon = Icons.Filled.School,
            iconColor = Success
        ),
        TutorialStep(
            title = "과제 생성",
            description = "음성 녹음이나 텍스트로 과제를 쉽게 만들 수 있습니다.\n AI가 자동으로 문제를 생성해드립니다.",
            icon = Icons.Filled.Assignment,
            iconColor = Warning
        ),
        TutorialStep(
            title = "학생 관리",
            description = "학생별 성적과 진도를 확인하고\n개별 피드백을 제공할 수 있습니다.",
            icon = Icons.Filled.People,
            iconColor = PrimaryIndigo
        ),
        TutorialStep(
            title = "성과 분석",
            description = "학생들의 학습 데이터를 분석하여\n인사이트를 얻고 교육 방향을 개선하세요.",
            icon = Icons.Filled.Analytics,
            iconColor = Success
        )
    )
    
    /**
     * 학생 계정용 튜토리얼
     */
    val studentTutorialSteps = listOf(
        TutorialStep(
            title = "환영합니다!",
            description = "VoiceTutor와 함께 재미있게 학습해보세요.\n음성으로 간편하게 과제를 수행할 수 있습니다.",
            icon = Icons.Filled.EmojiEmotions,
            iconColor = PrimaryIndigo
        ),
        TutorialStep(
            title = "대시보드",
            description = "홈 화면에서 해야 할 과제와\n최근 성적을 한눈에 확인할 수 있습니다.",
            icon = Icons.Filled.Dashboard,
            iconColor = Success
        ),
        TutorialStep(
            title = "과제 수행",
            description = "음성이나 텍스트로 답변을 작성하고\nAI가 즉시 피드백을 제공합니다.",
            icon = Icons.Filled.Assignment,
            iconColor = Warning
        ),
        TutorialStep(
            title = "진도 확인",
            description = "내 학습 진도와 성적을 확인하고\n부족한 부분을 보완하세요.",
            icon = Icons.Filled.TrendingUp,
            iconColor = PrimaryIndigo
        ),
        TutorialStep(
            title = "선생님과 소통",
            description = "궁금한 점이 있다면 언제든지\n선생님께 메시지를 보낼 수 있습니다.",
            icon = Icons.Filled.Message,
            iconColor = Success
        )
    )
}


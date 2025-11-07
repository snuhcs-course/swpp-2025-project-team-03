package com.example.voicetutor.data.models

import com.example.voicetutor.ui.components.*
import com.example.voicetutor.R

/**
 * 인터랙티브 튜토리얼 데이터
 * 
 * 주의: imageRes에는 실제 스크린샷 이미지를 drawable에 추가해야 합니다.
 * 예시: R.drawable.tutorial_teacher_dashboard_screenshot
 * 
 * 스크린샷 준비 방법:
 * 1. 앱의 각 화면을 스크린샷으로 캡처
 * 2. drawable 폴더에 추가 (예: drawable/tutorial_teacher_create_class.png)
 * 3. 아래 imageRes를 실제 리소스 ID로 변경
 */
object InteractiveTutorialData {
    
    /**
     * 선생님 인터랙티브 튜토리얼
     */
    val teacherInteractiveTutorialSpots = listOf(
        TutorialSpot(
            title = "반 생성하기",
            description = "여기서 새로운 반을 생성할 수 있습니다.\n반 이름, 과목, 학생 등을 설정하세요.",
            imageRes = android.R.drawable.ic_menu_gallery, // TODO: 실제 스크린샷으로 교체
            highlightArea = HighlightArea(
                x = 0.1f,
                y = 0.15f,
                width = 0.4f,
                height = 0.08f,
                shape = HighlightShape.RECTANGLE
            ),
            tapInstruction = "여기를 눌러 반을 생성하세요!",
            arrowDirection = ArrowDirection.TOP
        ),
        
        TutorialSpot(
            title = "과제 생성하기",
            description = "음성 녹음이나 텍스트로 간편하게 과제를 만들 수 있습니다.\nAI가 자동으로 문제를 생성해드립니다.",
            imageRes = android.R.drawable.ic_menu_gallery, // TODO: 실제 스크린샷으로 교체
            highlightArea = HighlightArea(
                x = 0.55f,
                y = 0.15f,
                width = 0.35f,
                height = 0.08f,
                shape = HighlightShape.RECTANGLE
            ),
            tapInstruction = "새 과제 버튼을 클릭!",
            arrowDirection = ArrowDirection.TOP
        ),
        
        TutorialSpot(
            title = "과제 진행률 확인",
            description = "각 과제의 제출 현황을 실시간으로 확인할 수 있습니다.\n학생들이 얼마나 과제를 완료했는지 한눈에 보세요.",
            imageRes = android.R.drawable.ic_menu_gallery, // TODO: 실제 스크린샷으로 교체
            highlightArea = HighlightArea(
                x = 0.1f,
                y = 0.4f,
                width = 0.8f,
                height = 0.15f,
                shape = HighlightShape.RECTANGLE
            ),
            tapInstruction = "진행률이 표시됩니다!",
            arrowDirection = ArrowDirection.BOTTOM
        ),
        
        TutorialSpot(
            title = "결과 보기",
            description = "과제 카드의 '결과 보기' 버튼을 누르면\n학생별 답변과 점수를 자세히 확인할 수 있습니다.",
            imageRes = android.R.drawable.ic_menu_gallery, // TODO: 실제 스크린샷으로 교체
            highlightArea = HighlightArea(
                x = 0.1f,
                y = 0.58f,
                width = 0.38f,
                height = 0.06f,
                shape = HighlightShape.RECTANGLE
            ),
            tapInstruction = "결과 보기를 클릭하세요!",
            arrowDirection = ArrowDirection.BOTTOM
        ),
        
        TutorialSpot(
            title = "학생별 리포트",
            description = "학생 탭에서 개별 학생을 선택하면\n상세한 성적과 학습 패턴을 분석한 리포트를 볼 수 있습니다.",
            imageRes = android.R.drawable.ic_menu_gallery, // TODO: 실제 스크린샷으로 교체
            highlightArea = HighlightArea(
                x = 0.1f,
                y = 0.3f,
                width = 0.8f,
                height = 0.12f,
                shape = HighlightShape.RECTANGLE
            ),
            tapInstruction = "학생 카드를 클릭!",
            arrowDirection = ArrowDirection.TOP
        )
    )
    
    /**
     * 학생 인터랙티브 튜토리얼
     */
    val studentInteractiveTutorialSpots = listOf(
        TutorialSpot(
            title = "과제 시작하기",
            description = "할당된 과제 카드를 클릭하면\n문제를 풀 수 있는 화면으로 이동합니다.",
            imageRes = android.R.drawable.ic_menu_gallery, // TODO: 실제 스크린샷으로 교체
            highlightArea = HighlightArea(
                x = 0.1f,
                y = 0.35f,
                width = 0.8f,
                height = 0.15f,
                shape = HighlightShape.RECTANGLE
            ),
            tapInstruction = "과제 카드를 클릭하세요!",
            arrowDirection = ArrowDirection.BOTTOM
        ),
        
        TutorialSpot(
            title = "이어하기 기능",
            description = "대시보드 상단에 표시되는 '이어하기' 버튼은\n마지막으로 풀던 과제로 바로 이동합니다.\n중단했던 곳부터 계속 진행할 수 있어요!",
            imageRes = android.R.drawable.ic_menu_gallery, // TODO: 실제 스크린샷으로 교체
            highlightArea = HighlightArea(
                x = 0.3f,
                y = 0.12f,
                width = 0.4f,
                height = 0.08f,
                shape = HighlightShape.RECTANGLE
            ),
            tapInstruction = "이어하기 버튼!",
            arrowDirection = ArrowDirection.TOP
        ),
        
        TutorialSpot(
            title = "진행률 확인",
            description = "각 과제 카드에서 현재 진행률을 확인할 수 있습니다.\n몇 문제 중 몇 개를 완료했는지 보여줍니다.",
            imageRes = android.R.drawable.ic_menu_gallery, // TODO: 실제 스크린샷으로 교체
            highlightArea = HighlightArea(
                x = 0.1f,
                y = 0.45f,
                width = 0.8f,
                height = 0.08f,
                shape = HighlightShape.RECTANGLE
            ),
            tapInstruction = "진행률 바를 확인!",
            arrowDirection = ArrowDirection.BOTTOM
        ),
        
        TutorialSpot(
            title = "내 리포트 보기",
            description = "하단 탭의 '리포트' 버튼을 누르면\n내 성적과 학습 현황을 자세히 볼 수 있습니다.",
            imageRes = android.R.drawable.ic_menu_gallery, // TODO: 실제 스크린샷으로 교체
            highlightArea = HighlightArea(
                x = 0.5f,
                y = 0.9f,
                width = 0.2f,
                height = 0.07f,
                shape = HighlightShape.CIRCLE
            ),
            tapInstruction = "리포트 탭을 클릭!",
            arrowDirection = ArrowDirection.BOTTOM
        ),
        
        TutorialSpot(
            title = "전체 과제 목록",
            description = "'전체 보기' 버튼을 누르면\n모든 과제 목록을 확인할 수 있습니다.\n완료한 과제와 해야 할 과제를 모두 볼 수 있어요!",
            imageRes = android.R.drawable.ic_menu_gallery, // TODO: 실제 스크린샷으로 교체
            highlightArea = HighlightArea(
                x = 0.65f,
                y = 0.28f,
                width = 0.25f,
                height = 0.05f,
                shape = HighlightShape.RECTANGLE
            ),
            tapInstruction = "전체 보기 클릭!",
            arrowDirection = ArrowDirection.TOP
        )
    )
}


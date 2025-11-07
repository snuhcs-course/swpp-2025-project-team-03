# 📸 인터랙티브 튜토리얼 스크린샷 추가 가이드

## 🎯 개요

인터랙티브 튜토리얼에서 사용할 실제 앱 스크린샷을 추가하는 방법을 안내합니다.

## 📋 필요한 스크린샷 목록

### 선생님 튜토리얼용 스크린샷 (5개)

1. **teacher_create_class.png**
   - 화면: 선생님 대시보드 또는 반 목록 화면
   - 강조 부분: "새 반 만들기" 또는 "반 생성" 버튼

2. **teacher_create_assignment.png**
   - 화면: 선생님 대시보드
   - 강조 부분: "새 과제" 버튼

3. **teacher_assignment_progress.png**
   - 화면: 선생님 대시보드의 과제 목록
   - 강조 부분: 과제 카드 전체 (진행률이 보이는 부분)

4. **teacher_view_results.png**
   - 화면: 과제 카드가 보이는 화면
   - 강조 부분: "결과 보기" 버튼

5. **teacher_student_report.png**
   - 화면: 학생 목록 화면
   - 강조 부분: 학생 카드

### 학생 튜토리얼용 스크린샷 (5개)

1. **student_assignment_card.png**
   - 화면: 학생 대시보드
   - 강조 부분: 과제 카드

2. **student_continue_button.png**
   - 화면: 학생 대시보드
   - 강조 부분: 상단의 "이어하기" 버튼 (있는 경우)

3. **student_progress_bar.png**
   - 화면: 학생 대시보드
   - 강조 부분: 과제 카드의 진행률 바

4. **student_report_tab.png**
   - 화면: 학생 대시보드
   - 강조 부분: 하단 탭의 "리포트" 버튼

5. **student_view_all.png**
   - 화면: 학생 대시보드
   - 강조 부분: "전체 보기" 버튼

## 🔧 스크린샷 추가 방법

### 1단계: 스크린샷 캡처

1. Android Studio에서 앱 실행
2. 각 화면에서 스크린샷 캡처
   - Android Studio 상단 카메라 아이콘 클릭
   - 또는 에뮬레이터/실제 기기에서 직접 캡처

### 2단계: 이미지 편집 (선택사항)

- 크기 조정 (권장: 1080x2400 또는 비슷한 비율)
- 필요시 개인정보 블러 처리
- PNG 또는 JPG 형식으로 저장

### 3단계: drawable 폴더에 추가

```
frontend/app/src/main/res/drawable/
├── tutorial_teacher_create_class.png
├── tutorial_teacher_create_assignment.png
├── tutorial_teacher_assignment_progress.png
├── tutorial_teacher_view_results.png
├── tutorial_teacher_student_report.png
├── tutorial_student_assignment_card.png
├── tutorial_student_continue_button.png
├── tutorial_student_progress_bar.png
├── tutorial_student_report_tab.png
└── tutorial_student_view_all.png
```

### 4단계: 코드 업데이트

`InteractiveTutorialData.kt` 파일을 열고 각 TutorialSpot의 `imageRes`를 업데이트하세요:

```kotlin
// 변경 전:
imageRes = android.R.drawable.ic_menu_gallery

// 변경 후:
imageRes = R.drawable.tutorial_teacher_create_class
```

#### 선생님 튜토리얼 업데이트 예시:

```kotlin
val teacherInteractiveTutorialSpots = listOf(
    TutorialSpot(
        title = "반 생성하기",
        // ...
        imageRes = R.drawable.tutorial_teacher_create_class,
        // ...
    ),
    TutorialSpot(
        title = "과제 생성하기",
        // ...
        imageRes = R.drawable.tutorial_teacher_create_assignment,
        // ...
    ),
    // ... 나머지도 동일하게
)
```

#### 학생 튜토리얼 업데이트 예시:

```kotlin
val studentInteractiveTutorialSpots = listOf(
    TutorialSpot(
        title = "과제 시작하기",
        // ...
        imageRes = R.drawable.tutorial_student_assignment_card,
        // ...
    ),
    // ... 나머지도 동일하게
)
```

## 📐 강조 영역(HighlightArea) 조정하기

스크린샷을 추가한 후, 실제 화면과 맞도록 강조 영역을 조정해야 할 수 있습니다.

### HighlightArea 파라미터 설명:

```kotlin
HighlightArea(
    x = 0.1f,      // 화면 왼쪽에서의 시작 위치 (0.0~1.0)
    y = 0.15f,     // 화면 위쪽에서의 시작 위치 (0.0~1.0)
    width = 0.4f,  // 강조 영역의 너비 (화면 너비의 비율)
    height = 0.08f,// 강조 영역의 높이 (화면 높이의 비율)
    shape = HighlightShape.RECTANGLE // 또는 CIRCLE
)
```

### 조정 팁:

1. **x, y 값**: 강조하고 싶은 UI 요소의 왼쪽 상단 모서리 위치
2. **width, height**: 해당 UI 요소의 크기
3. **shape**: 
   - 버튼/카드 → `RECTANGLE`
   - 둥근 버튼/아이콘 → `CIRCLE`

### 예시: "새 과제" 버튼 강조

만약 "새 과제" 버튼이:
- 화면 오른쪽 상단 (가로 55% 지점)
- 화면 상단에서 15% 지점
- 버튼 너비가 화면의 35%
- 버튼 높이가 화면의 8%

라면:

```kotlin
HighlightArea(
    x = 0.55f,
    y = 0.15f,
    width = 0.35f,
    height = 0.08f,
    shape = HighlightShape.RECTANGLE
)
```

## 🎨 커스터마이징

### 화살표 방향 변경:

```kotlin
arrowDirection = ArrowDirection.TOP    // 위에서 아래로
arrowDirection = ArrowDirection.BOTTOM // 아래에서 위로
arrowDirection = ArrowDirection.LEFT   // 왼쪽에서 오른쪽으로
arrowDirection = ArrowDirection.RIGHT  // 오른쪽에서 왼쪽으로
```

### 안내 텍스트 변경:

```kotlin
tapInstruction = "여기를 눌러주세요!"
tapInstruction = "이 버튼을 클릭하세요!"
tapInstruction = "이곳을 확인하세요!"
```

## ✅ 테스트

1. 앱을 실행하고 새 계정으로 로그인
2. 튜토리얼이 자동으로 표시되는지 확인
3. 각 단계에서:
   - 스크린샷이 올바르게 표시되는지
   - 강조 영역이 올바른 위치에 있는지
   - 반짝이는 효과가 작동하는지
   - 화살표와 안내 텍스트가 적절한지 확인

## 🐛 문제 해결

### 스크린샷이 표시되지 않아요
- drawable 폴더에 파일이 있는지 확인
- 파일 이름이 올바른지 확인 (소문자, 언더스코어만 사용)
- 프로젝트를 Clean & Rebuild

### 강조 영역이 올바른 위치에 없어요
- HighlightArea의 x, y, width, height 값을 조정
- 0.0~1.0 사이의 값으로 비율 조정

### 이미지가 너무 크거나 작아요
- 스크린샷 크기를 조정 (권장: 1080x2400)
- 또는 InteractiveTutorial.kt의 Image 컴포넌트에서 contentScale 조정

## 💡 추가 팁

1. **일관된 스크린샷**: 모든 스크린샷을 같은 기기/해상도에서 캡처
2. **밝은 테마**: 스크린샷은 밝은 테마로 캡처하는 것이 좋음
3. **실제 데이터**: 더미 데이터가 아닌 실제처럼 보이는 데이터 사용
4. **개인정보 보호**: 실제 이메일, 이름 등은 블러 처리

## 📝 현재 상태

- ✅ 인터랙티브 튜토리얼 시스템 구현 완료
- ⏳ 스크린샷 추가 대기 (placeholder 이미지 사용 중)
- ⏳ 강조 영역 위치 조정 필요

스크린샷을 추가한 후 `InteractiveTutorialData.kt`의 TODO 주석을 제거하고 실제 리소스 ID로 변경하세요!

---

궁금한 점이 있다면 언제든지 문의하세요! 🚀


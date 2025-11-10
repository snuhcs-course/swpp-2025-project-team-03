# 커버리지 리포트 가이드

## 리포트 위치

JaCoCo 커버리지 리포트는 다음 위치에 생성됩니다:

### HTML 리포트 (권장)
```
frontend/app/build/reports/jacoco/jacocoTestReport/html/index.html
```

### XML 리포트
```
frontend/app/build/reports/jacoco/jacocoTestReport/jacocoTestReport.xml
```

## 리포트 생성 방법

### 방법 1: Android Studio에서 (권장)
1. **먼저 테스트 실행** (중요!)
   - Gradle 패널에서 `app` → `verification` → `testDebugUnitTest` 실행
   - 또는 `app` → `verification` → `connectedDebugAndroidTest` 실행 (Android 테스트)

2. **리포트 생성**
   - Gradle 패널에서 `app` → `verification` → `jacocoTestReport` 더블 클릭
   - 리포트가 생성되면 `build/reports/jacoco/jacocoTestReport/html/index.html` 파일을 브라우저로 열기

### 방법 2: 터미널에서 (권장)
```bash
cd frontend
# 테스트 실행 + 리포트 생성 (한 번에)
./gradlew testDebugUnitTest jacocoTestReport
```

또는 Android 테스트도 포함하려면:
```bash
cd frontend
./gradlew testDebugUnitTest connectedDebugAndroidTest jacocoTestReport
```

### 방법 3: 한 번에 실행
```bash
cd frontend
# 테스트와 리포트를 한 번에 실행
./gradlew clean testDebugUnitTest jacocoTestReport
```

## ⚠️ 중요: 리포트가 업데이트되지 않는 경우

리포트가 업데이트되지 않거나 0%로 표시되는 경우:

1. **테스트가 실행되었는지 확인**
   ```bash
   cd frontend
   ./gradlew testDebugUnitTest
   ```

2. **실행 데이터 파일 확인**
   - 파일 위치: `frontend/app/build/jacoco/testDebugUnitTest.exec`
   - 이 파일이 없으면 리포트가 생성되지 않습니다

3. **리포트 재생성**
   ```bash
   cd frontend
   ./gradlew clean testDebugUnitTest jacocoTestReport
   ```

4. **브라우저 캐시 문제**
   - 브라우저에서 `Ctrl + F5` (강력 새로고침)
   - 또는 시크릿 모드에서 리포트 열기

## 포함되는 테스트

현재 설정은 다음 테스트의 커버리지를 포함합니다:

1. **단위 테스트 (Unit Tests)**
   - `src/test/java/` 디렉토리의 모든 테스트
   - 실행 데이터: `build/jacoco/testDebugUnitTest.exec`

2. **Android 테스트 (Instrumentation Tests)**
   - `src/androidTest/java/` 디렉토리의 모든 테스트
   - 실행 데이터: `build/outputs/code-coverage/connected/*coverage.ec`

## 리포트 확인

1. 리포트 생성 후 `index.html` 파일을 브라우저로 열기
2. 패키지별 커버리지 확인
3. 클래스별 상세 커버리지 확인
4. 라인별 커버리지 확인 (초록색 = 커버됨, 빨간색 = 커버 안됨)

## 커버리지 목표

- 전체 커버리지: **80% 이상**
- 클래스별 커버리지: **70% 이상**

## 문제 해결

### 리포트가 0%로 표시되는 경우
1. 테스트가 실행되었는지 확인: `./gradlew testDebugUnitTest`
2. 실행 데이터 파일 확인: `build/jacoco/testDebugUnitTest.exec` 파일 존재 여부
3. Android 테스트 실행 데이터 확인: `build/outputs/code-coverage/connected/` 디렉토리 확인

### Android 테스트 커버리지가 포함되지 않는 경우
1. Android 테스트 실행: `./gradlew connectedDebugAndroidTest`
2. 에뮬레이터 또는 실제 기기 연결 확인
3. `build/outputs/code-coverage/connected/` 디렉토리에 `.ec` 파일 생성 확인

### 리포트가 업데이트되지 않는 경우
1. **테스트를 먼저 실행**: `./gradlew testDebugUnitTest`
2. **리포트 재생성**: `./gradlew jacocoTestReport`
3. **브라우저 캐시 삭제**: `Ctrl + F5` 또는 시크릿 모드 사용
4. **빌드 디렉토리 정리 후 재생성**: `./gradlew clean testDebugUnitTest jacocoTestReport`

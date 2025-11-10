# 전체 커버리지 측정 가이드

## 80% 커버리지 달성 방법

현재 커버리지가 11%인데 80%로 올리려면 **반드시 Android 테스트(Espresso)를 실행**해야 합니다.

### 왜 Android 테스트가 필요한가?

- **UI Screens**: 전체 코드의 64% (78,969 라인)
- 이 부분은 **Espresso 테스트**로만 측정 가능
- 단위 테스트만으로는 11% 커버리지만 달성

## 전체 커버리지 측정 방법

### 방법 1: Android Studio에서

1. **에뮬레이터 시작**
   - AVD Manager에서 에뮬레이터 실행
   - 또는 실제 Android 기기 연결

2. **Android 테스트 실행**
   - Gradle 패널: `app` → `verification` → `connectedDebugAndroidTest`
   - 실행 시간: 약 5-10분

3. **커버리지 리포트 생성**
   - Gradle 패널: `app` → `verification` → `jacocoTestReport`

4. **리포트 확인**
   - `build/reports/jacoco/jacocoTestReport/html/index.html`

### 방법 2: 터미널에서 (권장)

```bash
cd frontend

# 1. 단위 테스트 실행
./gradlew testDebugUnitTest

# 2. Android 테스트 실행 (에뮬레이터 필요)
./gradlew connectedDebugAndroidTest

# 3. 전체 커버리지 리포트 생성
./gradlew jacocoTestReport
```

또는 한 번에:
```bash
cd frontend
./gradlew testDebugUnitTest connectedDebugAndroidTest jacocoTestReport
```

## 에뮬레이터 설정

### Android Studio에서 에뮬레이터 생성
1. Tools → Device Manager
2. Create Device
3. 권장 설정:
   - Device: Pixel 5
   - System Image: Android 13 (API 33)
   - 이름: Medium_Phone

### 에뮬레이터 상태 확인
```bash
# 연결된 기기 확인
adb devices
```

출력 예시:
```
List of devices attached
emulator-5554   device
```

## 예상 커버리지

각 테스트 유형별 기여도:

| 테스트 유형 | 커버리지 기여 | 소요 시간 |
|------------|--------------|----------|
| 단위 테스트 | ~15% | 1분 |
| Android 테스트 (Espresso) | ~65% | 5-10분 |
| **합계** | **~80%** | **6-11분** |

## 문제 해결

### "No connected devices!" 오류
- 에뮬레이터가 실행 중인지 확인
- `adb devices` 명령으로 연결 확인
- Android Studio에서 에뮬레이터 재시작

### Android 테스트가 실패하는 경우
```bash
# 1. 기존 앱 제거
adb uninstall com.example.voicetutor

# 2. 캐시 정리 후 재실행
cd frontend
./gradlew clean
./gradlew connectedDebugAndroidTest
```

### 커버리지가 여전히 낮은 경우
1. Android 테스트가 실행되었는지 확인:
   ```bash
   ls -lh frontend/app/build/outputs/code-coverage/connected/
   ```
   `.ec` 파일이 있어야 함

2. 실행 데이터 포함 여부 확인:
   - 리포트 생성 시 로그에서 "Found execution data file" 확인

## 빠른 실행 스크립트

PowerShell 스크립트 생성:
```powershell
# run_full_coverage.ps1
cd frontend
Write-Host "1. Running unit tests..." -ForegroundColor Green
./gradlew testDebugUnitTest

Write-Host "2. Running Android tests (requires emulator)..." -ForegroundColor Green
./gradlew connectedDebugAndroidTest

Write-Host "3. Generating coverage report..." -ForegroundColor Green
./gradlew jacocoTestReport

Write-Host "4. Opening report..." -ForegroundColor Green
Start-Process "app/build/reports/jacoco/jacocoTestReport/html/index.html"
```

실행:
```powershell
cd frontend
./run_full_coverage.ps1
```


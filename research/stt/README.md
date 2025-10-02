# 🎤 Google Cloud Speech-to-Text 음성 인식 프로젝트

Google Cloud Speech-to-Text API를 사용한 실시간 음성 인식 웹 애플리케이션입니다. 단어별 타임스탬프와 높은 정확도를 제공합니다.

## ✨ 주요 기능

- 🎯 **단어별 타임스탬프**: 각 단어의 정확한 시작/종료 시간 제공
- 🎤 **실시간 녹음**: 웹 브라우저에서 직접 음성 녹음
- 📊 **높은 정확도**: Google의 최신 AI 모델 사용
- 🌐 **웹 인터페이스**: React 기반의 직관적인 UI
- ⚡ **빠른 처리**: Flask 백엔드로 효율적인 API 처리
- 🔄 **자동 변환**: 다양한 오디오 포맷 자동 변환 지원

## 🏗️ 프로젝트 구조

```
stt/
├── flask-server/          # Flask 백엔드 서버
│   ├── app.py            # 메인 서버 파일
│   ├── requirements.txt  # Python 의존성
│   └── uploads/          # 임시 파일 저장소
├── stt-ui/               # React 프론트엔드
│   ├── src/
│   │   ├── App.js        # 메인 앱 컴포넌트
│   │   └── components/
│   │       ├── STTRecorder.js  # 음성 녹음 컴포넌트
│   │       └── STTRecorder.css # 스타일시트
│   └── package.json      # Node.js 의존성
├── google_stt.py         # Google STT API 클라이언트
├── google-cloud-setup.md # Google Cloud 설정 가이드
├── start-server.bat      # 서버 시작 스크립트
├── start-ui.bat          # UI 시작 스크립트
└── stt-project-*.json    # Google Cloud 서비스 계정 키
```

## 🚀 빠른 시작

### 1. 사전 요구사항

- Python 3.8+
- Node.js 16+
- Google Cloud 계정
- FFmpeg (오디오 변환용, 선택사항)

### 2. Google Cloud 설정

1. [Google Cloud Console](https://console.cloud.google.com/)에서 프로젝트 생성
2. Speech-to-Text API 활성화
3. 서비스 계정 생성 및 JSON 키 다운로드
4. 환경변수 설정:

```bash
# Windows
set GOOGLE_APPLICATION_CREDENTIALS=C:\path\to\service-account-key.json

# Linux/Mac
export GOOGLE_APPLICATION_CREDENTIALS="/path/to/service-account-key.json"
```

자세한 설정 방법은 [google-cloud-setup.md](google-cloud-setup.md)를 참조하세요.

### 3. 백엔드 서버 설정

```bash
# Flask 서버 디렉토리로 이동
cd flask-server

# 가상환경 생성 및 활성화
python -m venv venv
venv\Scripts\activate  # Windows
# source venv/bin/activate  # Linux/Mac

# 의존성 설치
pip install -r requirements.txt
```

### 4. 프론트엔드 설정

```bash
# React UI 디렉토리로 이동
cd stt-ui

# 의존성 설치
npm install
```

### 5. 애플리케이션 실행

#### 방법 1: 배치 파일 사용 (Windows)

```bash
# 터미널 1: 백엔드 서버 시작
./start-server.bat

# 터미널 2: 프론트엔드 시작
./start-ui.bat
```

#### 방법 2: 수동 실행

```bash
# 터미널 1: 백엔드 서버
cd flask-server
python app.py

# 터미널 2: 프론트엔드
cd stt-ui
npm start
```

### 6. 애플리케이션 접속

- **웹 인터페이스**: http://localhost:3000
- **API 서버**: http://localhost:5000
- **API 상태 확인**: http://localhost:5000/api/health

## 🎯 사용 방법

1. **🎤 녹음 시작**: "녹음 시작" 버튼 클릭
2. **🗣️ 음성 입력**: 마이크에 대고 명확하게 말하기
3. **⏹️ 녹음 중지**: "녹음 중지" 버튼 클릭
4. **📊 결과 확인**: 단어별 타임스탬프와 신뢰도 확인

## 📊 API 엔드포인트

### POST /api/google-speech
음성 파일을 Google Cloud Speech-to-Text로 변환

**요청:**
- `multipart/form-data`
- `audio`: 오디오 파일 (WebM, MP3, WAV 등)

**응답:**
```json
{
  "success": true,
  "results": [
    {
      "text": "전체 인식된 텍스트",
      "confidence": 0.95,
      "words": [
        {
          "word": "단어",
          "start_time": 0.5,
          "end_time": 1.2,
          "confidence": 0.98
        }
      ]
    }
  ],
  "total_words": 10,
  "processing_time": 2.5,
  "language": "ko-KR"
}
```

### GET /api/health
서버 상태 확인

## 🔧 지원 기능

### 오디오 포맷
- WebM (Opus) - 권장
- MP3, WAV, AAC, OGG, FLAC

### 언어 지원
- 한국어 (ko-KR) - 기본

## 🛠️ 개발 및 디버깅

### 로그 확인
```bash
# Flask 서버 로그
cd flask-server
python app.py

# React 개발자 도구
# 브라우저 F12 > Console 탭
```
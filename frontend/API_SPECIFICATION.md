# VoiceTutor API 명세서

## 📋 개요

VoiceTutor는 음성 인식 기반 교육 플랫폼으로, 선생님과 학생 간의 상호작용을 위한 RESTful API를 제공합니다.

### Base URL
- **개발 환경**: `http://10.0.2.2:8080/api/` (에뮬레이터)
- **실제 디바이스**: `http://192.168.1.100:8080/api/` (로컬 네트워크)
- **운영 환경**: `http://your-ec2-ip:8080/api/` (EC2 서버)

### 인증
- JWT 토큰 기반 인증
- `Authorization: Bearer <token>` 헤더 사용

### 응답 형식
```json
{
  "success": boolean,
  "data": object | array | null,
  "message": string | null,
  "error": string | null
}
```

---

## 🔐 인증 API

### 1. 로그인
**POST** `/auth/login`

**Request Body:**
```json
{
  "email": "string",
  "password": "string"
}
```

**Response:**
```json
{
  "success": true,
  "data": {
    "id": 1,
    "name": "홍길동",
    "email": "hong@example.com",
    "role": "TEACHER",
    "avatar": "string",
    "className": "1학년 A반",
    "classId": 1,
    "lastLoginAt": "2024-01-15T10:30:00Z"
  },
  "token": "jwt_token_string",
  "message": "로그인 성공",
  "error": null
}
```

### 2. 회원가입
**POST** `/auth/signup`

**Request Body:**
```json
{
  "name": "string",
  "email": "string",
  "password": "string",
  "role": "TEACHER" | "STUDENT",
  "className": "string"
}
```

**Response:**
```json
{
  "success": true,
  "data": {
    "id": 1,
    "name": "홍길동",
    "email": "hong@example.com",
    "role": "TEACHER",
    "avatar": null,
    "className": "1학년 A반",
    "classId": 1,
    "lastLoginAt": null
  },
  "token": "jwt_token_string",
  "message": "회원가입 성공",
  "error": null
}
```

### 3. 로그아웃
**POST** `/auth/logout`

**Response:**
```json
{
  "success": true,
  "data": null,
  "message": "로그아웃 성공",
  "error": null
}
```

---

## 📚 과제 API

### 1. 모든 과제 조회
**GET** `/assignments`

**Query Parameters:**
- `teacherId` (optional): 선생님 ID
- `classId` (optional): 클래스 ID
- `status` (optional): 과제 상태 (IN_PROGRESS, COMPLETED, DRAFT)

**Response:**
```json
{
  "success": true,
  "data": [
    {
      "id": 1,
      "title": "영어 발음 연습",
      "subject": "영어",
      "className": "1학년 A반",
      "classId": 1,
      "dueDate": "2024-01-20T23:59:00Z",
      "submittedCount": 15,
      "totalCount": 20,
      "status": "IN_PROGRESS",
      "type": "Continuous",
      "description": "영어 단어 발음 연습 과제",
      "createdAt": "2024-01-15T09:00:00Z",
      "updatedAt": "2024-01-15T09:00:00Z"
    }
  ],
  "message": null,
  "error": null
}
```

### 2. 특정 과제 조회
**GET** `/assignments/{id}`

**Response:**
```json
{
  "success": true,
  "data": {
    "id": 1,
    "title": "영어 발음 연습",
    "subject": "영어",
    "className": "1학년 A반",
    "classId": 1,
    "dueDate": "2024-01-20T23:59:00Z",
    "submittedCount": 15,
    "totalCount": 20,
    "status": "IN_PROGRESS",
    "type": "Continuous",
    "description": "영어 단어 발음 연습 과제",
    "createdAt": "2024-01-15T09:00:00Z",
    "updatedAt": "2024-01-15T09:00:00Z"
  },
  "message": null,
  "error": null
}
```

### 3. 과제 생성
**POST** `/assignments`

**Request Body:**
```json
{
  "title": "string",
  "subject": "string",
  "classId": 1,
  "dueDate": "2024-01-20T23:59:00Z",
  "type": "Quiz" | "Continuous" | "Discussion",
  "description": "string",
  "questions": [
    {
      "id": 1,
      "question": "string",
      "type": "MULTIPLE_CHOICE" | "SHORT_ANSWER" | "VOICE_RESPONSE",
      "options": ["string"],
      "correctAnswer": "string",
      "points": 1,
      "explanation": "string"
    }
  ]
}
```

**Response:**
```json
{
  "success": true,
  "data": {
    "id": 1,
    "title": "영어 발음 연습",
    "subject": "영어",
    "className": "1학년 A반",
    "classId": 1,
    "dueDate": "2024-01-20T23:59:00Z",
    "submittedCount": 0,
    "totalCount": 20,
    "status": "DRAFT",
    "type": "Continuous",
    "description": "영어 단어 발음 연습 과제",
    "createdAt": "2024-01-15T09:00:00Z",
    "updatedAt": "2024-01-15T09:00:00Z"
  },
  "message": "과제가 생성되었습니다",
  "error": null
}
```

### 4. 과제 수정
**PUT** `/assignments/{id}`

**Request Body:**
```json
{
  "title": "string",
  "subject": "string",
  "classId": 1,
  "dueDate": "2024-01-20T23:59:00Z",
  "type": "Quiz" | "Continuous" | "Discussion",
  "description": "string",
  "questions": [
    {
      "id": 1,
      "question": "string",
      "type": "MULTIPLE_CHOICE" | "SHORT_ANSWER" | "VOICE_RESPONSE",
      "options": ["string"],
      "correctAnswer": "string",
      "points": 1,
      "explanation": "string"
    }
  ]
}
```

**Response:**
```json
{
  "success": true,
  "data": {
    "id": 1,
    "title": "수정된 과제 제목",
    "subject": "영어",
    "className": "1학년 A반",
    "classId": 1,
    "dueDate": "2024-01-20T23:59:00Z",
    "submittedCount": 15,
    "totalCount": 20,
    "status": "IN_PROGRESS",
    "type": "Continuous",
    "description": "수정된 과제 설명",
    "createdAt": "2024-01-15T09:00:00Z",
    "updatedAt": "2024-01-15T10:30:00Z"
  },
  "message": "과제가 수정되었습니다",
  "error": null
}
```

### 5. 과제 삭제
**DELETE** `/assignments/{id}`

**Response:**
```json
{
  "success": true,
  "data": null,
  "message": "과제가 삭제되었습니다",
  "error": null
}
```

### 6. 과제 초안 저장
**POST** `/assignments/{id}/draft`

**Request Body:**
```json
"초안 내용 텍스트"
```

**Response:**
```json
{
  "success": true,
  "data": null,
  "message": "초안이 저장되었습니다",
  "error": null
}
```

### 7. 과제 결과 조회
**GET** `/assignments/{id}/results`

**Response:**
```json
{
  "success": true,
  "data": [
    {
      "studentId": "2024001",
      "name": "김학생",
      "completionTime": "00:05:30",
      "score": 85,
      "confidenceScore": 78,
      "status": "COMPLETED",
      "submittedAt": "2024-01-15T14:30:00Z",
      "answers": ["A", "B", "C"],
      "detailedAnswers": [
        {
          "questionNumber": 1,
          "question": "다음 중 올바른 발음은?",
          "studentAnswer": "A",
          "correctAnswer": "A",
          "isCorrect": true,
          "confidenceScore": 85,
          "pronunciationScore": 78,
          "responseTime": "00:00:15"
        }
      ]
    }
  ],
  "message": null,
  "error": null
}
```

### 8. 과제 제출
**POST** `/assignments/{id}/submit`

**Request Body:**
```json
{
  "studentId": 1,
  "answers": [
    {
      "questionId": 1,
      "answer": "string",
      "audioFile": "base64_encoded_audio",
      "confidence": 0.85
    }
  ]
}
```

**Response:**
```json
{
  "success": true,
  "data": {
    "submissionId": 1,
    "score": 85,
    "totalQuestions": 10,
    "correctAnswers": 8,
    "feedback": [
      {
        "questionId": 1,
        "isCorrect": true,
        "studentAnswer": "A",
        "correctAnswer": "A",
        "explanation": "정답입니다!",
        "confidence": 0.85,
        "pronunciationScore": 0.78
      }
    ]
  },
  "message": "과제가 제출되었습니다",
  "error": null
}
```

### 9. 과제 문제 조회
**GET** `/assignments/{id}/questions`

**Response:**
```json
{
  "success": true,
  "data": [
    {
      "id": 1,
      "question": "다음 중 올바른 발음은?",
      "type": "MULTIPLE_CHOICE",
      "options": ["apple", "aple", "appel", "apel"],
      "correctAnswer": "apple",
      "points": 1,
      "explanation": "사과는 'apple'로 발음합니다."
    }
  ],
  "message": null,
  "error": null
}
```

---

## 👥 학생 API

### 1. 모든 학생 조회
**GET** `/students`

**Query Parameters:**
- `teacherId` (optional): 선생님 ID
- `classId` (optional): 클래스 ID

**Response:**
```json
{
  "success": true,
  "data": [
    {
      "id": 1,
      "name": "김학생",
      "email": "kim@example.com",
      "className": "1학년 A반",
      "classId": 1,
      "completedAssignments": 15,
      "totalAssignments": 20,
      "averageScore": 85,
      "lastActive": "2024-01-15T14:30:00Z"
    }
  ],
  "message": null,
  "error": null
}
```

### 2. 특정 학생 조회
**GET** `/students/{id}`

**Response:**
```json
{
  "success": true,
  "data": {
    "id": 1,
    "name": "김학생",
    "email": "kim@example.com",
    "className": "1학년 A반",
    "classId": 1,
    "completedAssignments": 15,
    "totalAssignments": 20,
    "averageScore": 85,
    "lastActive": "2024-01-15T14:30:00Z"
  },
  "message": null,
  "error": null
}
```

### 3. 학생 과제 조회
**GET** `/students/{id}/assignments`

**Response:**
```json
{
  "success": true,
  "data": [
    {
      "id": 1,
      "title": "영어 발음 연습",
      "subject": "영어",
      "className": "1학년 A반",
      "classId": 1,
      "dueDate": "2024-01-20T23:59:00Z",
      "submittedCount": 15,
      "totalCount": 20,
      "status": "COMPLETED",
      "type": "Continuous",
      "description": "영어 단어 발음 연습 과제",
      "createdAt": "2024-01-15T09:00:00Z",
      "updatedAt": "2024-01-15T09:00:00Z"
    }
  ],
  "message": null,
  "error": null
}
```

### 4. 학생 진도 조회
**GET** `/students/{id}/progress`

**Response:**
```json
{
  "success": true,
  "data": {
    "studentId": 1,
    "totalAssignments": 20,
    "completedAssignments": 15,
    "averageScore": 85,
    "strengths": ["발음", "어휘"],
    "weaknesses": ["문법", "듣기"],
    "recentActivities": [
      {
        "assignmentTitle": "영어 발음 연습",
        "score": 90,
        "completedAt": "2024-01-15T14:30:00Z"
      }
    ]
  },
  "message": null,
  "error": null
}
```

### 5. 학생 정보 수정
**PUT** `/students/{studentId}`

**Request Body:**
```json
{
  "name": "string",
  "email": "string",
  "phoneNumber": "string",
  "parentName": "string",
  "parentPhone": "string",
  "address": "string",
  "birthDate": "string",
  "notes": "string",
  "isActive": true
}
```

**Response:**
```json
{
  "success": true,
  "data": {
    "studentId": 1,
    "updatedFields": ["name", "email"],
    "message": "학생 정보가 수정되었습니다"
  },
  "message": "학생 정보가 수정되었습니다",
  "error": null
}
```

### 6. 학생 삭제
**DELETE** `/students/{studentId}`

**Request Body:**
```json
{
  "reason": "string"
}
```

**Response:**
```json
{
  "success": true,
  "data": {
    "studentId": 1,
    "deletedAt": "2024-01-15T14:30:00Z",
    "message": "학생이 삭제되었습니다"
  },
  "message": "학생이 삭제되었습니다",
  "error": null
}
```

### 7. 학생 상태 변경
**PUT** `/students/{studentId}/status`

**Request Body:**
```json
{
  "isActive": true
}
```

**Response:**
```json
{
  "success": true,
  "data": {
    "studentId": 1,
    "isActive": true,
    "message": "학생 상태가 변경되었습니다"
  },
  "message": "학생 상태가 변경되었습니다",
  "error": null
}
```

### 8. 학생 비밀번호 재설정
**PUT** `/students/{studentId}/password`

**Request Body:**
```json
{
  "newPassword": "string"
}
```

**Response:**
```json
{
  "success": true,
  "data": {
    "studentId": 1,
    "temporaryPassword": "temp123456",
    "message": "비밀번호가 재설정되었습니다"
  },
  "message": "비밀번호가 재설정되었습니다",
  "error": null
}
```

### 9. 학생 반 변경
**PUT** `/students/{studentId}/class`

**Request Body:**
```json
{
  "newClassId": 2
}
```

**Response:**
```json
{
  "success": true,
  "data": {
    "studentId": 1,
    "oldClassId": 1,
    "newClassId": 2,
    "message": "학생의 반이 변경되었습니다"
  },
  "message": "학생의 반이 변경되었습니다",
  "error": null
}
```

---

## 🏫 클래스 API

### 1. 클래스 목록 조회
**GET** `/classes`

**Query Parameters:**
- `teacherId`: 선생님 ID

**Response:**
```json
{
  "success": true,
  "data": [
    {
      "id": 1,
      "name": "1학년 A반",
      "subject": "영어",
      "description": "기초 영어 수업",
      "studentCount": 20,
      "teacherId": 1,
      "createdAt": "2024-01-01T09:00:00Z"
    }
  ],
  "message": null,
  "error": null
}
```

### 2. 특정 클래스 조회
**GET** `/classes/{id}`

**Response:**
```json
{
  "success": true,
  "data": {
    "id": 1,
    "name": "1학년 A반",
    "subject": "영어",
    "description": "기초 영어 수업",
    "studentCount": 20,
    "teacherId": 1,
    "createdAt": "2024-01-01T09:00:00Z"
  },
  "message": null,
  "error": null
}
```

### 3. 클래스 학생 목록 조회
**GET** `/classes/{id}/students`

**Response:**
```json
{
  "success": true,
  "data": [
    {
      "id": 1,
      "name": "김학생",
      "email": "kim@example.com",
      "className": "1학년 A반",
      "classId": 1,
      "completedAssignments": 15,
      "totalAssignments": 20,
      "averageScore": 85,
      "lastActive": "2024-01-15T14:30:00Z"
    }
  ],
  "message": null,
  "error": null
}
```

---

## 💬 메시지 API

### 1. 메시지 전송
**POST** `/messages/send`

**Request Body:**
```json
{
  "teacherId": 1,
  "studentIds": [1, 2, 3],
  "message": "string",
  "messageType": "TEXT" | "ASSIGNMENT" | "ANNOUNCEMENT"
}
```

**Response:**
```json
{
  "success": true,
  "data": {
    "messageId": 1,
    "sentCount": 3,
    "failedCount": 0,
    "sentAt": "2024-01-15T14:30:00Z"
  },
  "message": "메시지가 전송되었습니다",
  "error": null
}
```

### 2. 클래스 메시지 조회
**GET** `/messages/{classId}`

**Response:**
```json
{
  "success": true,
  "data": [
    {
      "id": 1,
      "senderId": 1,
      "senderName": "홍선생님",
      "message": "내일 시험 준비하세요",
      "messageType": "ANNOUNCEMENT",
      "sentAt": "2024-01-15T14:30:00Z",
      "readBy": [1, 2, 3]
    }
  ],
  "message": null,
  "error": null
}
```

### 3. 사용자 메시지 조회
**GET** `/messages`

**Query Parameters:**
- `userId`: 사용자 ID
- `messageType` (optional): 메시지 타입
- `limit` (optional): 조회 개수 (기본값: 50)
- `offset` (optional): 오프셋 (기본값: 0)

**Response:**
```json
{
  "success": true,
  "data": {
    "messages": [
      {
        "id": 1,
        "senderId": 1,
        "senderName": "홍선생님",
        "message": "내일 시험 준비하세요",
        "messageType": "ANNOUNCEMENT",
        "sentAt": "2024-01-15T14:30:00Z",
        "isRead": true
      }
    ],
    "totalCount": 10,
    "hasMore": false
  },
  "message": null,
  "error": null
}
```

---

## 📊 대시보드 API

### 1. 대시보드 통계
**GET** `/dashboard/stats`

**Query Parameters:**
- `teacherId`: 선생님 ID

**Response:**
```json
{
  "success": true,
  "data": {
    "totalAssignments": 25,
    "totalStudents": 60,
    "completedAssignments": 18,
    "inProgressAssignments": 7
  },
  "message": null,
  "error": null
}
```

### 2. 최근 활동 조회
**GET** `/dashboard/recent-activities`

**Query Parameters:**
- `teacherId`: 선생님 ID
- `limit` (optional): 조회 개수 (기본값: 5)

**Response:**
```json
{
  "success": true,
  "data": [
    {
      "id": "1",
      "studentName": "김학생",
      "action": "과제 완료",
      "time": "5분 전",
      "iconType": "assignment",
      "assignmentTitle": "영어 발음 연습"
    }
  ],
  "message": null,
  "error": null
}
```

---

## 🤖 AI API

### 1. AI 대화
**POST** `/ai/conversation`

**Request Body:**
```json
{
  "message": "string",
  "context": "string",
  "studentId": 1
}
```

**Response:**
```json
{
  "success": true,
  "data": {
    "response": "string",
    "suggestions": ["string"],
    "confidence": 0.85
  },
  "message": null,
  "error": null
}
```

### 2. 음성 인식
**POST** `/ai/voice-recognition`

**Request Body:**
```json
{
  "audioData": "base64_encoded_audio",
  "language": "ko-KR",
  "context": "string"
}
```

**Response:**
```json
{
  "success": true,
  "data": {
    "transcript": "string",
    "confidence": 0.85,
    "pronunciationScore": 0.78,
    "suggestions": ["string"]
  },
  "message": null,
  "error": null
}
```

---

## 📝 퀴즈 API

### 1. 퀴즈 제출
**POST** `/quiz/submit`

**Request Body:**
```json
{
  "quizId": 1,
  "studentId": 1,
  "answers": [
    {
      "questionId": 1,
      "answer": "string",
      "audioFile": "base64_encoded_audio",
      "confidence": 0.85
    }
  ]
}
```

**Response:**
```json
{
  "success": true,
  "data": {
    "submissionId": 1,
    "score": 85,
    "totalQuestions": 10,
    "correctAnswers": 8,
    "feedback": [
      {
        "questionId": 1,
        "isCorrect": true,
        "studentAnswer": "A",
        "correctAnswer": "A",
        "explanation": "정답입니다!",
        "confidence": 0.85,
        "pronunciationScore": 0.78
      }
    ]
  },
  "message": "퀴즈가 제출되었습니다",
  "error": null
}
```

---

## 📈 분석 API

### 1. 학생 분석
**POST** `/analysis/student`

**Request Body:**
```json
{
  "studentId": 1,
  "period": "week" | "month" | "semester",
  "subject": "string"
}
```

**Response:**
```json
{
  "success": true,
  "data": {
    "studentId": 1,
    "period": "week",
    "totalAssignments": 5,
    "completedAssignments": 4,
    "averageScore": 85,
    "accuracyRate": 78.5,
    "pronunciationScore": 82.3,
    "improvementAreas": ["문법", "듣기"],
    "strengths": ["발음", "어휘"],
    "recommendations": ["문법 연습 과제 추천"]
  },
  "message": null,
  "error": null
}
```

### 2. 클래스 분석
**POST** `/analysis/class`

**Request Body:**
```json
{
  "classId": 1,
  "period": "week" | "month" | "semester",
  "subject": "string"
}
```

**Response:**
```json
{
  "success": true,
  "data": {
    "classId": 1,
    "period": "week",
    "totalStudents": 20,
    "averageScore": 82.5,
    "completionRate": 85.0,
    "topPerformers": [
      {
        "studentId": 1,
        "name": "김학생",
        "score": 95
      }
    ],
    "needsAttention": [
      {
        "studentId": 2,
        "name": "이학생",
        "score": 65
      }
    ]
  },
  "message": null,
  "error": null
}
```

### 3. 과목 분석
**POST** `/analysis/subject`

**Request Body:**
```json
{
  "subject": "영어",
  "period": "week" | "month" | "semester",
  "classId": 1
}
```

**Response:**
```json
{
  "success": true,
  "data": {
    "subject": "영어",
    "period": "week",
    "totalAssignments": 10,
    "averageScore": 82.5,
    "difficultyLevel": "medium",
    "commonMistakes": [
      {
        "question": "발음 문제",
        "frequency": 15,
        "percentage": 75.0
      }
    ],
    "improvementSuggestions": ["발음 연습 강화"]
  },
  "message": null,
  "error": null
}
```

---

## 📋 출석 API

### 1. 출석 기록
**POST** `/attendance/record`

**Request Body:**
```json
{
  "classId": 1,
  "date": "2024-01-15",
  "attendanceRecords": [
    {
      "studentId": 1,
      "status": "PRESENT" | "ABSENT" | "LATE" | "EXCUSED",
      "notes": "string"
    }
  ]
}
```

**Response:**
```json
{
  "success": true,
  "data": {
    "classId": 1,
    "date": "2024-01-15",
    "totalStudents": 20,
    "presentCount": 18,
    "absentCount": 2,
    "recordedAt": "2024-01-15T09:00:00Z"
  },
  "message": "출석이 기록되었습니다",
  "error": null
}
```

### 2. 출석 조회
**POST** `/attendance/query`

**Request Body:**
```json
{
  "classId": 1,
  "startDate": "2024-01-01",
  "endDate": "2024-01-31",
  "studentId": 1
}
```

**Response:**
```json
{
  "success": true,
  "data": {
    "classId": 1,
    "period": {
      "startDate": "2024-01-01",
      "endDate": "2024-01-31"
    },
    "attendanceRecords": [
      {
        "date": "2024-01-15",
        "status": "PRESENT",
        "notes": "정상 출석"
      }
    ],
    "summary": {
      "totalDays": 20,
      "presentDays": 18,
      "absentDays": 2,
      "attendanceRate": 90.0
    }
  },
  "message": null,
  "error": null
}
```

### 3. 학생 출석 요약
**GET** `/attendance/summary/{studentId}`

**Response:**
```json
{
  "success": true,
  "data": {
    "studentId": 1,
    "totalDays": 20,
    "presentDays": 18,
    "absentDays": 2,
    "lateDays": 0,
    "attendanceRate": 90.0,
    "recentAttendance": [
      {
        "date": "2024-01-15",
        "status": "PRESENT"
      }
    ]
  },
  "message": null,
  "error": null
}
```

### 4. 클래스 출석 조회
**GET** `/attendance/class/{classId}`

**Query Parameters:**
- `date`: 조회 날짜 (YYYY-MM-DD)

**Response:**
```json
{
  "success": true,
  "data": {
    "classId": 1,
    "date": "2024-01-15",
    "totalStudents": 20,
    "presentCount": 18,
    "absentCount": 2,
    "lateCount": 0,
    "attendanceList": [
      {
        "studentId": 1,
        "studentName": "김학생",
        "status": "PRESENT",
        "notes": "정상 출석"
      }
    ]
  },
  "message": null,
  "error": null
}
```

---

## 📊 진도 보고서 API

### 1. 진도 보고서 조회
**GET** `/reports/progress`

**Query Parameters:**
- `teacherId`: 선생님 ID
- `classId` (optional): 클래스 ID
- `period` (optional): 기간 (기본값: "week")

**Response:**
```json
{
  "success": true,
  "data": {
    "teacherId": 1,
    "classId": 1,
    "period": "week",
    "totalStudents": 20,
    "averageScore": 82.5,
    "completionRate": 85.0,
    "subjectBreakdown": [
      {
        "subject": "영어",
        "averageScore": 85.0,
        "completionRate": 90.0
      }
    ],
    "studentProgress": [
      {
        "studentId": 1,
        "studentName": "김학생",
        "score": 95,
        "completionRate": 100.0,
        "improvement": 5.0
      }
    ]
  },
  "message": null,
  "error": null
}
```

---

## 🔧 에러 코드

### HTTP 상태 코드
- `200`: 성공
- `400`: 잘못된 요청
- `401`: 인증 실패
- `403`: 권한 없음
- `404`: 리소스 없음
- `500`: 서버 오류

### 에러 응답 예시
```json
{
  "success": false,
  "data": null,
  "message": "요청이 실패했습니다",
  "error": "INVALID_CREDENTIALS"
}
```

### 일반적인 에러 코드
- `INVALID_CREDENTIALS`: 잘못된 인증 정보
- `USER_NOT_FOUND`: 사용자를 찾을 수 없음
- `ASSIGNMENT_NOT_FOUND`: 과제를 찾을 수 없음
- `STUDENT_NOT_FOUND`: 학생을 찾을 수 없음
- `CLASS_NOT_FOUND`: 클래스를 찾을 수 없음
- `INSUFFICIENT_PERMISSIONS`: 권한 부족
- `VALIDATION_ERROR`: 입력 데이터 검증 실패
- `SERVER_ERROR`: 서버 내부 오류

---

## 📝 데이터 타입 정의

### UserRole
```json
"TEACHER" | "STUDENT"
```

### AssignmentStatus
```json
"IN_PROGRESS" | "COMPLETED" | "DRAFT"
```

### AssignmentType
```json
"Quiz" | "Continuous" | "Discussion"
```

### QuestionType
```json
"MULTIPLE_CHOICE" | "SHORT_ANSWER" | "VOICE_RESPONSE"
```

### MessageType
```json
"TEXT" | "ASSIGNMENT" | "ANNOUNCEMENT"
```

### AttendanceStatus
```json
"PRESENT" | "ABSENT" | "LATE" | "EXCUSED"
```

---

## 🔄 API 버전 관리

현재 API 버전: **v1**

버전은 URL 경로에 포함되지 않으며, 헤더를 통해 관리됩니다:
```
API-Version: v1
```

---

## 📞 지원 및 문의

API 관련 문의사항이나 버그 리포트는 다음으로 연락해주세요:
- 이메일: support@voicetutor.com
- 문서 업데이트: 2024-01-15
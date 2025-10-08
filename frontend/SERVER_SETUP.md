# VoiceTutor 서버 설정 가이드

## 📱 앱에서 서버 설정하기

VoiceTutor 앱은 다양한 서버 환경에 연결할 수 있습니다.

### 1. 설정 화면 접근
1. 앱 실행 후 **설정** 화면으로 이동
2. **서버 설정** 메뉴 선택

### 2. 서버 옵션

#### 🏠 Localhost (개발용)
- **에뮬레이터**: `http://10.0.2.2:8080/api/`
- **실제 디바이스**: `http://192.168.1.100:8080/api/` (IP 변경 필요)

#### ☁️ EC2 서버 (운영용)
- EC2 퍼블릭 IP 입력 (예: `13.124.123.456`)
- 자동으로 `http://[IP]:8080/api/` 형태로 설정

#### 🔧 커스텀 서버
- 직접 URL 입력 (예: `http://your-domain.com/api/`)

## 🖥️ 백엔드 서버 설정

### Localhost 개발 환경

#### 1. Spring Boot 서버 실행
```bash
# 프로젝트 디렉토리로 이동
cd voice-tutor-backend

# 서버 실행
./mvnw spring-boot:run
```

#### 2. 포트 확인
- 기본 포트: `8080`
- 서버가 정상 실행되면 `http://localhost:8080`에서 접근 가능

#### 3. API 엔드포인트 확인
```bash
# 헬스 체크
curl http://localhost:8080/api/health

# 로그인 테스트
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"test@example.com","password":"password"}'
```

### EC2 서버 배포

#### 1. EC2 인스턴스 설정
```bash
# 보안 그룹에서 포트 8080 열기
# 인바운드 규칙 추가:
# - Type: Custom TCP
# - Port: 8080
# - Source: 0.0.0.0/0
```

#### 2. 서버 배포
```bash
# EC2 인스턴스에 접속
ssh -i your-key.pem ec2-user@your-ec2-ip

# Java 설치 (필요시)
sudo yum install java-11-openjdk

# 서버 실행
java -jar voice-tutor-backend.jar
```

#### 3. 퍼블릭 IP 확인
- EC2 콘솔에서 퍼블릭 IPv4 주소 확인
- 앱에서 해당 IP 입력

## 🔧 네트워크 문제 해결

### 에뮬레이터에서 localhost 접근 불가
- `10.0.2.2` 사용 (Android 에뮬레이터의 localhost 매핑)
- `localhost` 또는 `127.0.0.1` 사용 금지

### 실제 디바이스에서 localhost 접근 불가
1. 컴퓨터와 디바이스가 같은 Wi-Fi에 연결되어 있는지 확인
2. 컴퓨터의 로컬 IP 주소 확인:
   ```bash
   # Windows
   ipconfig
   
   # macOS/Linux
   ifconfig
   ```
3. 앱에서 `http://[컴퓨터IP]:8080/api/` 형태로 설정

### EC2 서버 연결 불가
1. 보안 그룹에서 포트 8080이 열려있는지 확인
2. 서버가 정상 실행 중인지 확인:
   ```bash
   curl http://your-ec2-ip:8080/api/health
   ```
3. 방화벽 설정 확인:
   ```bash
   sudo ufw status
   sudo ufw allow 8080
   ```

## 📋 연결 테스트

앱의 **서버 설정** 화면에서 **연결 테스트** 버튼을 눌러 서버 연결 상태를 확인할 수 있습니다.

### 성공 시
- "연결 성공" 메시지 표시
- API 호출이 정상적으로 작동

### 실패 시
- 에러 메시지 표시
- 네트워크 설정 및 서버 상태 확인 필요

## 🔄 서버 변경 시 주의사항

1. **앱 재시작**: 서버 URL 변경 후 앱을 완전히 종료하고 재시작
2. **캐시 클리어**: 필요시 앱 데이터 클리어
3. **네트워크 권한**: 앱에 인터넷 권한이 있는지 확인

## 📞 지원

서버 연결에 문제가 있으면 다음을 확인해주세요:
- 네트워크 연결 상태
- 서버 실행 상태
- 방화벽 설정
- 포트 번호 정확성

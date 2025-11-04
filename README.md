# AI-DreDaline Backend

러닝 경로 생성 및 추적 API

## 🚀 시작하기

### 요구사항
- Java 21
- PostgreSQL 16 with PostGIS
- Gradle

### 로컬 실행
```bash
# 1. 데이터베이스 연결 정보 설정
# application.yml에서 PostgreSQL 비밀번호 변경

# 2. 실행
./gradlew bootRun

# 3. Swagger 접속
http://localhost:8080/swagger-ui.html
```

## 📚 API 문서
- Swagger UI: http://localhost:8080/swagger-ui.html

## 👥 팀원 
- 역할 1: 템플릿 + 경로 생성
- 역할 2: 러닝 세션 + 기록

## 🌿 브랜치 전략
- `main`: 프로덕션
- `develop`: 개발
- `feature/기능명`: 기능 개발

## 📝 커밋 컨벤션
```
feat: 새로운 기능 추가
fix: 버그 수정
docs: 문서 수정
chore: 빌드, 패키지 관리
refactor: 코드 리팩토링
test: 테스트 코드
```
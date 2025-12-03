# AI-DreDaline Backend

러닝 경로 생성 및 추적 API

## 🚀 시작하기

### 요구사항
- Java 21
- PostgreSQL 16 with PostGIS 3.4.3
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
- api 테스트(도현) :https://accessible-drain-f22.notion.site/2a7312e1be938056bea7ebc2e0f1c47e

## 🌿 브랜치 전략
- `main`: 프로덕션
- `develop`: 개발
- `feat/기능명`: 기능 개발

## 📝 커밋 컨벤션
```
feat: 새로운 기능 추가
fix: 버그 수정
docs: 문서 수정
chore: 빌드, 패키지 관리
refactor: 코드 리팩토링
test: 테스트 코드
```



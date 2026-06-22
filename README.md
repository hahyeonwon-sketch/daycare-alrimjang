# daycare-alrimjang
어린이집 알림장 웹 시스템 | Spring Boot · Thymeleaf · MySQL

선생님의 잦은 서버 오류와 사진 미리보기 미지원 문제를 해결하기 위해 직접 기획·개발한 1인 프로젝트

---

## 기술 스택
- Java 21
- Spring Boot 4
- Spring Security
- Spring Data JPA
- Thymeleaf + Bootstrap
- MySQL
- JavaMailSender
- Gradle
- AWS EC2 + RDS

---

## 사용자 역할
| Role | 계정 생성 방식 | 설명 |
|------|-------------|------|
| ADMIN | 서버 최초 실행 시 환경변수로 자동 생성 | 원장. 반/교사/원아/학부모 전체 관리 |
| TEACHER | ADMIN이 직접 생성 | 알림장·공지·일정·행사보고 작성 |
| PARENT | 직접 회원가입 → ADMIN 승인 | 조회·메모·사진업로드·답장 |

`User.status`: PENDING / ACTIVE / REJECTED / INACTIVE

---

## 주요 도메인
- **User**: 모든 계정 통합 관리 (role, status, emailNotification, requestedChildName, requestedClassName)
- **Classroom**: 반 (N:M Teacher via ClassroomTeacher)
- **Child**: 원아 (1:1 Parent, 1:N Notice)
- **Notice**: 알림장 (1:N NoticePhoto, 1:1 NoticeRead, 1:N NoticeReply)
- **ParentMemo**: 학부모 등원 전 메모 (하루 1개, 1:N ParentMemoPhoto)
- **Schedule**: 일정 (notified 컬럼으로 중복 알림 방지)
- **Announcement**: 공지사항
- **EventReport**: 행사보고 (1:N EventPhoto)

---

## 패키지 구조
```
src/main/java/com/daycare/alrimjang/
├── domain/

│   ├── user/

│   ├── classroom/

│   ├── child/

│   ├── notice/

│   ├── parentmemo/

│   ├── schedule/

│   ├── announcement/

│   └── eventreport/
├── global/

│   ├── config/         # SecurityConfig 등

│   ├── exception/      # GlobalExceptionHandler

│   └── mail/           # JavaMailSender
└── AlrimjangApplication.java
```

---

## 핵심 구현 포인트
1. **사진 슬라이드 미리보기**: 교사가 사진 업로드 시 즉시 슬라이드 형태로 미리보기 제공 (핵심 기획 의도)
2. **학부모 승인 플로우**: 가입 → PENDING → ADMIN 승인 → ACTIVE
3. **이메일 알림**: 알림장 등록, 공지사항 등록, 일정 전날 오후 6시 (`@Scheduled`)
4. **역할별 접근 제어**: Spring Security로 `/admin/**`, `/teacher/**`, `/parent/**` URL 분리
5. **ParentMemo**: 하루 1개 제한 (없으면 생성, 있으면 수정)
6. **전역 예외 처리**: `@ControllerAdvice`로 `IllegalArgumentException` → 400, 그 외 → 500 에러 페이지 처리

---

## 이메일 알림 발송 시점
| 시점 | 수신자 |
|------|--------|
| 알림장 등록 | 해당 원아 학부모 |
| 공지사항 등록 | 소속 반 학부모 전체 |
| 일정 전날 오후 6시 | 소속 반 학부모 전체 |
| 학부모 승인/거절 | 해당 학부모 |
| 교사 계정 생성 | 해당 교사 |

※ `emailNotification = true` 인 경우에만 발송

---

## 기술 선택 이유

### Spring Security — 역할 기반 접근 제어
ADMIN / TEACHER / PARENT 3가지 역할이 각각 다른 페이지와 기능에 접근해야 하는 구조였습니다.
URL 레벨에서 `/admin/**`, `/teacher/**`, `/parent/**`로 접근을 분리하고,
서비스 레이어에서 추가 권한 검증을 수행해 이중으로 보호했습니다.
Spring Security를 선택한 이유는 세션 고정 공격 방지(`migrateSession`), CSRF 토큰, 비밀번호 암호화(BCrypt)를
별도 구현 없이 통합적으로 처리할 수 있기 때문입니다.

### SSE (Server-Sent Events) vs WebSocket — 실시간 알림
알림장 등록, 공지사항 등록 시 학부모에게 실시간 알림을 보내야 했습니다.
WebSocket은 양방향 통신이 필요한 채팅 등에 적합하지만, 이 서비스의 알림은 **서버 → 클라이언트 단방향**이므로
SSE가 더 적합하다고 판단했습니다.
SSE는 HTTP 기반으로 별도 프로토콜 업그레이드 없이 동작하고, Spring에서 `SseEmitter`로 간단히 구현할 수 있어 선택했습니다.
동시 접속 처리를 위해 `ConcurrentHashMap`으로 이메일 키 기반 연결을 관리했습니다.

### Jsoup Sanitize — XSS 방어
Quill 에디터가 굵게·색상 등 서식을 HTML 태그로 저장하므로, `th:text`로 교체하면 서식이 깨지는 문제가 있었습니다.
렌더링 방식을 바꾸는 대신 **저장 시점**에 Jsoup `Safelist`로 서버 사이드 sanitize를 적용해
위험 태그(`<script>`, `on*=` 등)만 제거하는 방식을 선택했습니다.
클라이언트 사이드 방어는 우회 가능성이 있어 서버 사이드 검증을 원칙으로 삼았습니다.

### 프로파일 분리 (prod / local) — 환경 분리 전략
- `local`: H2 인메모리 DB, `show-sql: true`, DataInitializer로 테스트 데이터 자동 생성
- `prod`: RDS MySQL, `show-sql: false`, `ddl-auto: validate`, 환경변수로 민감정보 관리

`DataInitializer`에 `@Profile({"local", "dev"})`를 적용해 운영 환경에서 테스트 계정이 생성되지 않도록 했고,
`ProdAdminInitializer`를 별도로 만들어 운영 환경에서는 환경변수(`ADMIN_PASSWORD`)로만 admin 계정을 생성하도록 구성했습니다.

### AWS EC2 + RDS — 배포 환경 선택
Heroku, Railway 등 PaaS도 고려했지만, EC2 + RDS 조합을 선택한 이유는 다음과 같습니다.
- 실무에서 가장 많이 사용되는 인프라 환경을 직접 경험하기 위해
- 보안 그룹, 인바운드 규칙, 프리티어 인스턴스 설정 등 인프라 레벨의 이해를 높이기 위해
- RDS를 EC2와 분리해 DB 서버를 독립적으로 관리하는 구조를 경험하기 위해

---

## 개발 순서 (GitHub Issues 기준)
- #1 프로젝트 세팅 & DB 설계
- #2 로그인 / 역할 분리 / 학부모 승인 플로우
- #3 알림장 - 교사 작성 + 사진 미리보기
- #4 알림장 - 학부모 메모·사진·답장·조회
- #5 일정 관리 + 이메일 알림
- #6 공지사항
- #7 행사보고
- #8 마이페이지
- #9 관리자 페이지
- #10 반응형 UI + 배포

---

## 컨벤션
- 브랜치: `feature/#이슈번호-작업명` (예: `feature/#3-notice-write`)
- 커밋: `[#이슈번호] 작업 내용` (예: `[#3] 알림장 작성 모달 구현`)
- PR: 이슈 단위로 생성

---

## 배포
- **URL**: http://15.164.231.42:8080/auth/login
- AWS EC2 (t3.micro) + RDS (MySQL, db.t3.micro)
- Spring Boot jar 직접 실행
- 환경변수로 DB 접속 정보 및 민감 정보 관리
- 운영/개발 프로파일 분리 (`application-prod.yml` / `application-local.yml`)

---

## 테스트 계정
| Role | ID | PW |
|------|----|----|
| ADMIN | admin@daycare.com | (포트폴리오 참고) |
| TEACHER | teacher@test.com | teacher1234 |
| PARENT | parent@test.com | parent1234 |

---

## 트러블슈팅

### 1. Stored XSS — `th:utext` 단순 교체 불가
Quill 에디터가 굵게·색상 등 서식을 HTML 태그로 저장하므로, `th:utext` → `th:text` 교체 시 서식이 전부 깨지는 문제 발생.
렌더링 방식을 바꾸는 대신 **저장 시점**에 Jsoup `Safelist`로 서버 사이드 sanitize를 적용해 위험 태그(`<script>`, `on*=` 등)만 제거하는 방식으로 해결.

### 2. SecurityConfig — `anyRequest()` 이후 `requestMatchers` 추가 불가
공통 메서드에서 `.anyRequest().authenticated()`로 체인을 닫은 뒤, 개발 프로파일에서 `/h2-console/**` 규칙을 추가하려다 `IllegalStateException` 발생.
`prodFilterChain` / `devFilterChain`을 완전히 분리하고 h2-console 규칙을 `anyRequest()` 앞에 배치하여 해결.

### 3. 서비스 반환 타입 변경 시 누락된 컨트롤러
`NotificationService.getNotifications()`의 반환 타입을 `List<Notification>` → `List<NotificationResponseDto>`로 변경했을 때, `NotificationPageController`가 여전히 구 타입으로 받고 있어 컴파일 에러 발생.
IDE의 Find Usages로 해당 서비스를 사용하는 모든 클래스를 파악한 뒤 일괄 수정하여 해결.

### 4. git 히스토리 민감정보 노출 및 제거
`application-local.yml`에 DB 비밀번호가 하드코딩된 채로 커밋되어 GitHub 히스토리에 노출됨.
`.gitignore`에 추가했어도 이미 추적된 파일은 소급 적용되지 않아 과거 커밋에 비밀번호가 그대로 남아 있었음.
`git filter-branch`로 전체 브랜치 히스토리에서 해당 파일 제거 후 force push.
이후 `application-local.yml`을 완전히 환경변수 방식(`${DB_PASSWORD}`)으로 전환하고 기본값 제거.

### 5. prod 환경 최초 배포 시 Schema Validation 실패
`ddl-auto: validate` 설정으로 인해 신규 RDS에 테이블이 없는 상태에서 앱 실행 시
`SchemaManagementException: missing table [announcements]` 에러 발생.
최초 1회만 `--spring.jpa.hibernate.ddl-auto=update` 옵션으로 실행해 테이블 자동 생성.
이후 정상 실행 시에는 `validate`로 복귀해 스키마 무결성 검증.

👉 [상세 내용 GitHub Issues 바로가기](https://github.com/hahyeonwon-sketch/daycare-alrimjang/issues)

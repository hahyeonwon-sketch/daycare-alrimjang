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
- AWS EC2

---

## 사용자 역할
| Role | 계정 생성 방식 | 설명 |
|------|-------------|------|
| ADMIN | 최초 하드코딩 1계정 | 원장. 반/교사/원아/학부모 전체 관리 |
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

👉 [상세 내용 GitHub Issues 바로가기](https://github.com/hahyeonwon-sketch/daycare-alrimjang/issues)

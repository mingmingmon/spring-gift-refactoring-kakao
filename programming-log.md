# claude-code 활용 결과

## 1. 테스트 코드와 구현 코드 간 불일치 수정

### Java 버전 호환성 문제

| 항목 | 내용 |
|---|---|
| **원인** | 시스템에 Java 25가 설치되어 있었는데, Kotlin 1.9.25가 Java 25 버전을 파싱하지 못함 |
| **에러** | `IllegalArgumentException: 25.0.1` at `JavaVersion.parse` |
| **해결** | Java 21로 전환 (build.gradle.kts에서 `JavaLanguageVersion.of(21)` 지정) |

### 테이블명 불일치 (`option` vs `options`)

| 항목 | 내용 |
|---|---|
| **원인** | `Option` 엔티티에 `@Table(name = "options")`로 지정되어 실제 테이블명은 `options`인데, SQL 스크립트와 테스트 코드에서 `option`으로 참조 |
| **에러** | `JdbcSQLSyntaxErrorException` |
| **수정 파일** | `truncate.sql`, `gift-data.sql`, `GiftAcceptanceTest.java` 내 SQL 전부 `option` → `options` |

### 엔티티 스키마와 테스트 데이터 불일치

**Member 엔티티**

| 항목 | 내용 |
|---|---|
| **원인** | `Member` 엔티티에 `name` 필드가 없는데 `gift-data.sql`에서 `name` 컬럼에 값을 넣고 있었음 |
| **에러** | `JdbcSQLSyntaxErrorException` |
| **해결** | `INSERT INTO member (id, name, email)` → `INSERT INTO member (id, email)` |

**Category 엔티티**

| 항목 | 내용 |
|---|---|
| **원인** | `Category` 엔티티의 `color` 컬럼이 NOT NULL인데 `gift-data.sql`에서 `name`만 삽입 |
| **에러** | `JdbcSQLIntegrityConstraintViolationException: NULL not allowed for column "COLOR"` |
| **해결** | `INSERT INTO category (id, name)` → `INSERT INTO category (id, name, color, image_url)` |

### 테스트 코드와 실제 API 스펙 불일치

**CategoryAcceptanceTest**

| 불일치 | 테스트 코드 | 실제 API |
|---|---|---|
| 필수 필드 누락 | `name`만 전송 | `name`, `color`, `imageUrl` 필수 (`@NotBlank`) |
| 생성 응답코드 | `statusCode(200)` | `201 Created` |
| validation 에러코드 | `statusCode(500)` | `400 Bad Request` |

**ProductAcceptanceTest**

| 불일치 | 테스트 코드 | 실제 API |
|---|---|---|
| 생성 응답코드 | `statusCode(200)` | `201 Created` |
| 조회 응답 구조 | `body("", hasSize(1))` | Page 응답이므로 `body("content", hasSize(1))` |
| 존재하지 않는 카테고리 | `statusCode(500)` | `404 Not Found` |
| validation 에러 | `statusCode(500)` | `400 Bad Request` |

### 미구현 API

| 항목 | 내용 |
|---|---|
| **원인** | `GiftAcceptanceTest`가 `POST /api/gifts`를 호출하지만 해당 컨트롤러가 존재하지 않음 |
| **에러** | `statusCode 404` |
| **해결** | `GiftAcceptanceTest.java` 삭제 |

### 핵심 요약

테스트 코드가 **실제 구현된 코드의 스펙과 맞지 않게 작성**되어 있었던 것이 근본 원인이다. 엔티티 필드, 테이블명, HTTP 상태코드, 응답 구조, 필수 파라미터 등이 전부 불일치했다.

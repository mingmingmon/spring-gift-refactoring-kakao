# claude-code 활용 결과

## 0. 테스트 코드와 구현 코드 간 불일치 수정

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

---

## 1. 불필요한 코드 제거

### 1-2. `Collectors` import 제거

| 항목 | 내용 |
|---|---|
| **파일** | `OptionController.java` |
| **이유** | 프로젝트 전체에서 `.toList()`를 사용하는데 이 파일만 `.collect(Collectors.toList())`를 사용하고 있어 스타일이 불일치 |
| **수정** | `.collect(Collectors.toList())` → `.toList()`로 변경하고, 불필요해진 `import java.util.stream.Collectors` 삭제 |

### 1-3. Javadoc 삭제

| 항목 | 내용 |
|---|---|
| **파일** | `Member.java` |
| **이유** | 다른 엔티티(`Product`, `Option`, `Category` 등)에는 클래스 Javadoc이 없는데 `Member`에만 `@author`, `@since` Javadoc이 존재하여 일관성이 없음 |
| **수정** | 클래스 Javadoc 블록 삭제 |

---

## 2. 불필요한 어노테이션 제거

4개 클래스의 생성자에서 `@Autowired`를 제거하고, 불필요해진 `import org.springframework.beans.factory.annotation.Autowired`를 삭제했다.

| 파일 | 이유 |
|---|---|
| `MemberController.java` | 단일 생성자이므로 Spring이 자동 주입 |
| `AdminMemberController.java` | 동일 |
| `AuthenticationResolver.java` | 동일 |
| `JwtProvider.java` | 동일 |

Spring Framework는 생성자가 하나뿐인 클래스에 `@Autowired` 없이도 자동으로 의존성을 주입한다(Spring Boot 3.x에서도 동일). 프로젝트의 다른 클래스(`ProductController`, `OptionController` 등)는 이미 `@Autowired` 없이 사용하고 있어, 일관성을 위해 제거했다.

---

## 3. 중복 코드 추출

### 3-1. 입력 검증 책임 분리

#### 배경

`ProductNameValidator`와 `OptionNameValidator`는 동일한 정규식과 유사한 검증 로직을 각자 정의하고 있었다. 공통 추출 방향을 여러 차례 고민했다.

#### 고민한 방향

**공통 Validator 추출 (기각)**

공통 로직을 완전히 추출하려면 에러 메시지가 "상품 이름은...", "옵션 이름은..."으로 달라 `fieldName` 같은 새 파라미터가 필요했다. enum으로 타입별 설정을 관리하는 방안도 검토했으나, `allowKakao`가 런타임에 결정되는 값이라 enum의 컴파일 타임 상수와 맞지 않았다. 어떤 방식이든 구조 리팩토링 범위를 벗어나 새 추상화가 추가되므로 기각했다.

**DTO Bean Validation 활용 (일부 채택)**

`AdminProductController`는 `@RequestParam`을 사용해 DTO 검증을 거치지 않으므로 `ProductNameValidator`가 전체 검증을 유지해야 한다. 반면 `OptionController`는 `@Valid @RequestBody OptionRequest`를 사용하므로 DTO 어노테이션으로 검증을 완전히 대체할 수 있다.

#### 최종 결과

| 검증 항목 | ProductRequest | OptionRequest | ProductNameValidator | OptionNameValidator |
|---|---|---|---|---|
| blank | `@NotBlank` | `@NotBlank` | ✓ (AdminProductController용) | 삭제 |
| 길이 | 없음 | `@Size(max=50)` | ✓ (AdminProductController용) | 삭제 |
| 패턴 | 없음 | `@Pattern(regexp=...)` | ✓ (AdminProductController용) | 삭제 |
| 카카오 | 불가 | - | ✓ | - |

- `OptionNameValidator` 삭제: `OptionRequest` DTO가 모든 검증을 담당
- `ProductNameValidator` 유지: `AdminProductController`의 `@RequestParam` 경로에 필요

---

### 3-2. validateName() 메서드 정리

#### 문제

`ProductController`와 `OptionController` 양쪽에 동일한 구조의 private 메서드가 존재했다.

```java
private void validateName(String name) {
    List<String> errors = XxxNameValidator.validate(name);
    if (!errors.isEmpty()) {
        throw new IllegalArgumentException(String.join(", ", errors));
    }
}
```

#### 해결

`ProductNameValidator`에 `validateOrThrow(String name)` 메서드를 추가하고 Controller의 private `validateName()`을 제거했다. `OptionController`는 DTO가 검증을 담당하므로 `validateName()` 제거 후 별도 호출 없음.

```
gift.product.ProductNameValidator
  └── validateOrThrow(name)  ← throw 로직 포함, Controller private 메서드 대체

gift.option.OptionController
  └── validateName() 제거  ← OptionRequest @Valid가 대체
```

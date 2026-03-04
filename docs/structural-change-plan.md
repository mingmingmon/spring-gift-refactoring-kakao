# Structural Change Plan (구조 변경 실행 계획)

구조 변경으로 인해 작동이 바뀌지 않아야 한다.

---

## 1. 불필요한 코드 제거

### 1-1. OrderController의 WishRepository 필드

현재 수정하지 않는다. 이후 서비스 계층 분리(4번) 시 각 서비스는 필요한 레포지토리만 가지도록 정리한다.

### 1-2. Collectors import 제거

`OptionController.java`에서 `.collect(Collectors.toList())`를 `.toList()`로 대체하고, `Collectors` import를 제거한다.

### 1-3. Javadoc 삭제

`Member.java`의 클래스 Javadoc(`@author`, `@since`)을 삭제한다. 나머지 엔티티에도 없으므로 통일한다.

---

## 2. 불필요한 어노테이션 제거

아래 4개 클래스에서 `@Autowired`를 제거한다. Spring 단일 생성자 자동 주입을 사용한다.

- `MemberController` (gift.member)
- `AdminMemberController` (gift.member)
- `AuthenticationResolver` (gift.auth)
- `JwtProvider` (gift.auth)

---

## 3. 중복 코드 추출

### 3-1. NameValidator 통합

`ProductNameValidator`와 `OptionNameValidator`의 공통 부분(정규식, blank 체크, 길이 체크, 패턴 체크)을 추출하고, 다른 부분(`maxLength`, `allowKakao`)은 매개변수화한다.

### 3-2. validateName() 메서드 정리

`ProductController`와 `OptionController`의 `validateName()` private 메서드를 3-1의 NameValidator 통합 시 함께 정리한다.

### 3-3. 인증 처리 통일

`OrderController`와 `WishController`에서 `Member` 객체를 직접 추출하지 않는다. `String authorization`을 Service 로직으로 넘겨서, Service에서 `Member` 객체를 생성하도록 통일한다.

---

## 4. 서비스 계층 추출

Controller가 Repository를 직접 의존하지 않도록, 모든 비즈니스 로직을 Service로 분리한다. Controller는 요청 검증과 위임만 담당한다. 신규 기능은 추가하지 않는다.

| 컨트롤러 | 현재 인라인 비즈니스 로직 | 추출 대상 서비스 |
|---|---|---|
| `OrderController` | 옵션 조회, 재고 차감, 포인트 차감, 주문 저장, 카카오 알림 (6단계) | `OrderService` |
| `WishController` | 상품 존재 확인, 중복 위시 체크, 멱등성 처리 | `WishService` |
| `MemberController` | 이메일 중복 체크, 비밀번호 비교, JWT 생성 | `MemberService` |
| `KakaoAuthController` | 토큰 교환, 사용자 조회, 회원 자동등록/갱신 | `MemberService` 또는 `KakaoAuthService` |
| `ProductController` | 이름 검증, 카테고리 조회 (타 도메인 Repository 접근) | `ProductService` |
| `OptionController` | 이름 검증, 최소 1개 옵션 보장 규칙 | `OptionService` |

---

## 5. 패키지 이동

`KakaoMessageClient`(현재 gift.order)와 `KakaoLoginClient`(현재 gift.auth)를 `gift.kakao` 패키지로 이동한다.

---

## 6. 네이밍 일관성

`AdminProductController`의 `populateNewForm` 메서드명을 `populateNewFormError`로 수정한다. `AdminMemberController`의 동일 역할 메서드와 이름을 통일한다.

---

## 7. 지역 변수 선언 스타일 통일

`var`, `final` 키워드를 모두 제거하고, 구체적 타입을 명시하는 방식으로 통일한다.

```java
// Before
var member = memberRepository.findById(id).orElseThrow();
final Member member = memberRepository.findById(id).orElseThrow();

// After
Member member = memberRepository.findById(id).orElseThrow();
```

---

## 8. HTTP 상태코드 표현 통일

`WishController`, `OrderController`의 숫자 리터럴을 `HttpStatus` enum으로 변경한다.

```java
// Before
ResponseEntity.status(401).build();
ResponseEntity.status(403).build();

// After
ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
ResponseEntity.status(HttpStatus.FORBIDDEN).build();
```

---

## 9. ResponseEntity 반환 타입 명시

`WishController`, `OrderController`의 `ResponseEntity<?>` 와일드카드를 구체적 타입(`ResponseEntity<XxxResponse>`)으로 변경한다.

---

## 10. Request DTO의 toEntity() 팩토리 메서드 통일

모든 Request DTO에 `toEntity()` 메서드를 제공한다. 변환 로직을 DTO에 응집시킨다.

현재 없는 대상:
- `MemberRequest`
- `OptionRequest`
- `WishRequest`
- `OrderRequest`

---

## 11. 외부 설정 주입 방식 통일

`JwtProvider`의 `@Value` 개별 주입을 `@ConfigurationProperties` + record 방식으로 변경한다. `KakaoLoginProperties`와 동일한 패턴을 적용한다.

```java
// Before (JwtProvider)
public JwtProvider(
    @Value("${jwt.secret}") String secret,
    @Value("${jwt.expiration}") long expiration
) {

// After
@ConfigurationProperties(prefix = "jwt")
public record JwtProperties(String secret, long expiration) {}
```
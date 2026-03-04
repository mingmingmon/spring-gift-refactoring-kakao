# 리팩토링 로그

## 1. CategoryController → CategoryService 계층 분리

- **무엇을 바꾸는지**: CategoryController가 CategoryRepository를 직접 참조하는 구조를 CategoryService를 경유하도록 변경한다.
- **무엇을 바꾸지 않는지**: API의 요청/응답 형식, HTTP 상태 코드, 예외 발생 조건 등 외부 동작은 동일하게 유지한다.
- **무엇이 이를 증명하는지**: `CategoryAcceptanceTest` — `카테고리를_생성하면_조회할_수_있다`, `여러_카테고리를_생성하면_모두_조회된다`, `이름이_null인_카테고리_생성_요청은_실패하고_카테고리는_생성되지_않는다` 통과로 검증.

## 2. CategoryController.updateCategory()에서 orElse(null) + null 체크를 orElseThrow()로 변경

- **무엇을 바꾸는지**: updateCategory()의 try-catch 패턴을 제거하고, Service에서 던지는 NoSuchElementException을 @ExceptionHandler로 일관되게 처리한다.
- **무엇을 바꾸지 않는지**: 존재하지 않는 카테고리 수정 시 404 응답을 반환하는 동작은 동일하다.
- **무엇이 이를 증명하는지**: `CategoryAcceptanceTest` 전체 3건 통과로 검증. (update 404 케이스는 현재 인수 테스트에 미포함)

## 3. AdminProductController → ProductService 계층 분리

- **무엇을 바꾸는지**: AdminProductController가 ProductRepository, CategoryRepository를 직접 참조하는 구조를 ProductService, CategoryService를 경유하도록 변경한다. ProductService에 `findAll`, `findById`, `create`, `update` 메서드를 추가하고, 기존 API 메서드의 카테고리 조회 중복도 `findCategory()` private 메서드로 추출한다.
- **무엇을 바꾸지 않는지**: Admin 폼의 검증 에러 표시 방식(List\<String\> errors), 카카오 이름 허용 정책(allowKakao=true), HTTP 응답은 동일하게 유지한다. ProductNameValidator 호출은 폼 에러 리스트 표시를 위해 컨트롤러에 유지한다.
- **무엇이 이를 증명하는지**: `ProductAcceptanceTest` — `상품을_생성하면_조회할_수_있다`, `서로_다른_카테고리에_상품을_각각_생성할_수_있다`, `존재하지_않는_카테고리로_상품을_생성하면_실패하고_상품은_생성되지_않는다` 등 전체 6건 통과로 검증.

## 4. KakaoAuthService의 MemberRepository 직접 참조 제거

- **무엇을 바꾸는지**: KakaoAuthService가 MemberRepository를 직접 참조하는 구조를 MemberService를 경유하도록 변경한다. MemberService에 `findByEmail()`, `save()` 메서드를 추가한다.
- **무엇을 바꾸지 않는지**: 카카오 로그인 시 신규 회원 자동 생성, 기존 회원 카카오 토큰 갱신, JWT 발급 동작은 동일하게 유지한다.
- **무엇이 이를 증명하는지**: `MemberAcceptanceTest` — `회원가입하면_토큰이_발급된다`, `로그인하면_토큰이_발급된다`, `중복_이메일로_가입하면_실패한다` 등 전체 7건 통과로 검증. (카카오 OAuth 플로우는 외부 API 의존으로 인수 테스트에 미포함)

## 5. OrderService의 MemberRepository, OptionRepository 직접 참조 제거

- **무엇을 바꾸는지**: OrderService가 OptionRepository, MemberRepository를 직접 참조하는 구조를 OptionService, MemberService를 경유하도록 변경한다. OptionService에 `findById()`, `subtractQuantity()` 메서드를 추가한다.
- **무엇을 바꾸지 않는지**: 주문 생성 시 옵션 수량 차감, 포인트 차감, 카카오 메시지 발송 동작은 동일하게 유지한다.
- **무엇이 이를 증명하는지**: `OrderAcceptanceTest` — `주문을_생성하면_조회할_수_있다`, `주문_후_옵션_수량이_차감된다`, `포인트가_부족하면_주문에_실패한다`, `재고보다_많은_수량을_주문하면_실패한다` 등 전체 6건 통과로 검증.

## 6. WishService의 ProductRepository 직접 참조 제거

- **무엇을 바꾸는지**: WishService가 ProductRepository를 직접 참조하는 구조를 ProductService를 경유하도록 변경한다. ProductService.findById()는 이미 존재하므로 그대로 활용한다.
- **무엇을 바꾸지 않는지**: 위시리스트 추가 시 상품 조회, 중복 위시 처리, 삭제 시 권한 검사 동작은 동일하게 유지한다.
- **무엇이 이를 증명하는지**: `WishAcceptanceTest` — `위시리스트에_추가하면_조회할_수_있다`, `동일_상품을_중복_추가하면_200을_반환한다`, `다른_사용자의_위시를_삭제하면_403을_반환한다` 등 전체 7건 통과로 검증.

## 7. OptionService의 ProductRepository 직접 참조 제거

- **무엇을 바꾸는지**: OptionService가 ProductRepository를 직접 참조하는 구조를 ProductService를 경유하도록 변경한다.
- **무엇을 바꾸지 않는지**: 옵션 조회/생성/삭제 시 상품 존재 확인, 중복 옵션명 검사, 최소 1개 옵션 제약 동작은 동일하게 유지한다.
- **무엇이 이를 증명하는지**: `OptionAcceptanceTest` — `옵션을_생성하면_조회할_수_있다`, `마지막_남은_옵션은_삭제할_수_없다`, `존재하지_않는_상품에_옵션을_생성하면_실패한다` 등 전체 9건 통과로 검증.

## 8. extractMember 중복 제거

- **무엇을 바꾸는지**: WishService와 OrderService에 중복되어 있던 `extractMember()` private 메서드를 제거하고, AuthenticationResolver에 `extractMemberOrThrow()` 메서드를 추가하여 한 곳에서 관리한다.
- **무엇을 바꾸지 않는지**: 인증 실패 시 AuthenticationException을 던지는 동작은 동일하게 유지한다. 기존 `extractMember()`(null 반환)도 그대로 유지한다.
- **무엇이 이를 증명하는지**: `OrderAcceptanceTest` — `토큰_없이_주문하면_실패한다` 등 전체 6건, `WishAcceptanceTest` — `토큰_없이_위시리스트를_조회하면_실패한다`, `토큰_없이_위시리스트에_추가하면_실패한다` 등 전체 7건 통과로 검증.

## 9. @ExceptionHandler 중복 제거 → GlobalExceptionHandler 도입

- **무엇을 바꾸는지**: 6개 컨트롤러에 반복되던 `@ExceptionHandler`를 `GlobalExceptionHandler`(`@RestControllerAdvice`)로 통합한다. `NoSuchElementException`(404), `AuthenticationException`(401), `ForbiddenException`(403)을 전역 처리한다.
- **무엇을 바꾸지 않는지**: 각 예외에 대한 HTTP 상태 코드 매핑은 동일하게 유지한다. `IllegalArgumentException`(400)은 기존에 핸들러가 있던 ProductController, OptionController, MemberController에만 유지한다(OrderController 등에 없던 핸들러를 전역으로 추가하면 기존 500 응답이 400으로 바뀌는 작동 변경이 발생하므로).
- **무엇이 이를 증명하는지**: 전체 38건 테스트 통과로 검증. 특히 `OrderAcceptanceTest.포인트가_부족하면_주문에_실패한다`(500), `OrderAcceptanceTest.재고보다_많은_수량을_주문하면_실패한다`(500)이 기존 상태 코드를 유지함을 확인.

## 10. OrderService.createOrder() 트랜잭션 경계 설정

- **무엇을 바꾸는지**: `OrderService.createOrder()`에 `@Transactional`을 추가하여 옵션 수량 차감 → 포인트 차감 → 주문 저장을 하나의 트랜잭션으로 묶는다. 중간 실패 시 부분 반영(옵션 수량만 차감되고 주문은 미생성)이 발생하지 않도록 한다.
- **무엇을 바꾸지 않는지**: 정상 주문 흐름(수량 차감, 포인트 차감, 주문 생성, 카카오 메시지 발송)은 동일하게 유지한다.
- **무엇이 이를 증명하는지**: `OrderAcceptanceTest.포인트가_부족하면_옵션_수량이_차감되지_않는다` (신규 추가) — 포인트 0인 회원이 주문 시도 후 옵션 수량을 재조회하여 100 그대로인지 확인. 기존 6건 + 신규 1건 = 전체 39건 테스트 통과.

## 11. CategoryService.update() 트랜잭션 경계 설정

- **무엇을 바꾸는지**: `CategoryService.update()`에 `@Transactional`을 추가하여 Category 조회 → 수정 → 저장을 하나의 트랜잭션으로 묶는다.
- **무엇을 바꾸지 않는지**: 카테고리 수정의 정상 동작과 존재하지 않는 카테고리 수정 시 예외 발생은 동일하게 유지한다.
- **무엇이 이를 증명하는지**: `CategoryAcceptanceTest` 전체 3건 통과로 검증. 단일 엔티티(Category) 수정이므로 롤백 시나리오는 해당 없음.

## 12. ProductService.update(), updateProduct() 트랜잭션 경계 설정

- **무엇을 바꾸는지**: `ProductService.update()`(Admin용)와 `updateProduct()`(API용)에 `@Transactional`을 추가하여 Category 조회 → Product 조회 → 수정 → 저장을 하나의 트랜잭션으로 묶는다.
- **무엇을 바꾸지 않는지**: 상품 수정의 정상 동작과 존재하지 않는 상품/카테고리 수정 시 예외 발생은 동일하게 유지한다.
- **무엇이 이를 증명하는지**: `ProductAcceptanceTest` 전체 6건 통과로 검증. 단일 엔티티(Product) 수정이므로 롤백 시나리오는 해당 없음.

## 13. ProductService.updateProduct()에서 update() 재사용 — 중복 로직 통합

- **무엇을 바꾸는지**: `updateProduct()`(API용)의 조회→수정→저장 로직을 제거하고, 내부에서 `update()`를 호출하도록 변경한다. 조회→수정→저장이 `update()` 한 곳에만 존재하게 된다.
- **무엇을 바꾸지 않는지**: API 상품 수정 시 이름 검증(`ProductNameValidator.validateOrThrow`) 후 수정하는 동작은 동일하게 유지한다.
- **무엇이 이를 증명하는지**: `ProductAcceptanceTest` 전체 6건 통과로 검증.

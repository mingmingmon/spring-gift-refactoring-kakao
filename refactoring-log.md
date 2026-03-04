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

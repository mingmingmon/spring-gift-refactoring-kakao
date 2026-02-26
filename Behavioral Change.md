# Behavioral Change (작동 변경)

구조 변경과 달리, 아래 항목들은 실제 동작이 달라지는 변경이다.
구조 변경이 완료된 이후에 진행한다.

---

## 1. 글로벌 예외 처리 도입

### 현황

- `MemberController`, `ProductController`, `OptionController`: 개별 `@ExceptionHandler(IllegalArgumentException.class)` 존재
- `CategoryController`, `WishController`, `OrderController`, `KakaoAuthController`: 예외 핸들러 없음 (예외 발생 시 500 반환)

### 변경 시 영향

`@RestControllerAdvice`를 도입하면, 기존에 핸들러가 **없던** 컨트롤러에서도 `IllegalArgumentException`이 400으로 처리된다. 이는 기존에 500으로 나가던 응답이 400으로 바뀌는 **응답 변경**이다.

---

## 2. 에러 메시지 언어 통일

### 현황

- `Member.chargePoint()`: 영어 (`"Amount must be greater than zero."`)
- `Member.deductPoint()`, `Option.subtractQuantity()`: 한국어

### 변경 시 영향

클라이언트가 에러 메시지를 파싱하거나 표시하는 경우, 언어 변경은 **클라이언트 동작에 영향**을 줄 수 있다.

---

## 3. 예외 타입 통일

### 현황

- 대부분: `IllegalArgumentException`
- `AdminProductController`만: `NoSuchElementException`

### 변경 시 영향

예외 타입을 변경하면 해당 예외를 catch하는 코드가 있을 경우 **기존 예외 처리 흐름이 달라진다**.

---

## 4. POST 응답 Location 헤더 통일

### 현황

- 대부분 컨트롤러: `ResponseEntity.created(URI).body(...)` (Location 헤더 포함)
- `MemberController.register()`: `ResponseEntity.status(HttpStatus.CREATED).body(...)` (Location 헤더 없음)

### 변경 시 영향

Location 헤더가 추가되면 **HTTP 응답 헤더가 변경**된다. 클라이언트가 Location 헤더를 참조하는 경우 동작이 달라질 수 있다.

---

## 5. @Transactional 추가

### 현황

프로젝트 전체에 `@Transactional`이 없다. `OrderController.createOrder()`는 재고 차감 → 포인트 차감 → 주문 저장을 하나의 트랜잭션으로 묶지 않고 있다.

### 변경 시 영향

트랜잭션 경계가 추가되면 **중간 실패 시 롤백 여부가 달라진다**. 기존에는 재고만 차감되고 주문은 저장되지 않는 부분 실패가 가능했으나, 트랜잭션 적용 후에는 전체 롤백된다.

# 리팩토링 로그

## 1. CategoryController → CategoryService 계층 분리

- **무엇을 바꾸는지**: CategoryController가 CategoryRepository를 직접 참조하는 구조를 CategoryService를 경유하도록 변경한다.
- **무엇을 바꾸지 않는지**: API의 요청/응답 형식, HTTP 상태 코드, 예외 발생 조건 등 외부 동작은 동일하게 유지한다.
- **무엇이 이를 증명하는지**: `./gradlew test` 전체 테스트(CategoryAcceptanceTest 포함) 통과로 검증한다.

## 2. CategoryController.updateCategory()에서 orElse(null) + null 체크를 orElseThrow()로 변경

- **무엇을 바꾸는지**: updateCategory()의 try-catch 패턴을 제거하고, Service에서 던지는 NoSuchElementException을 @ExceptionHandler로 일관되게 처리한다.
- **무엇을 바꾸지 않는지**: 존재하지 않는 카테고리 수정 시 404 응답을 반환하는 동작은 동일하다.
- **무엇이 이를 증명하는지**: `./gradlew test` 전체 테스트 통과.

## 3. AdminProductController → ProductService 계층 분리

- **무엇을 바꾸는지**: AdminProductController가 ProductRepository, CategoryRepository를 직접 참조하는 구조를 ProductService, CategoryService를 경유하도록 변경한다. ProductService에 `findAll`, `findById`, `create`, `update` 메서드를 추가하고, 기존 API 메서드의 카테고리 조회 중복도 `findCategory()` private 메서드로 추출한다.
- **무엇을 바꾸지 않는지**: Admin 폼의 검증 에러 표시 방식(List\<String\> errors), 카카오 이름 허용 정책(allowKakao=true), HTTP 응답은 동일하게 유지한다. ProductNameValidator 호출은 폼 에러 리스트 표시를 위해 컨트롤러에 유지한다.
- **무엇이 이를 증명하는지**: `./gradlew test` 전체 테스트 통과.

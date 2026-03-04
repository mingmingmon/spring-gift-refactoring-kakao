# 리팩토링 로그

## 1. CategoryController → CategoryService 계층 분리

- **무엇을 바꾸는지**: CategoryController가 CategoryRepository를 직접 참조하는 구조를 CategoryService를 경유하도록 변경한다.
- **무엇을 바꾸지 않는지**: API의 요청/응답 형식, HTTP 상태 코드, 예외 발생 조건 등 외부 동작은 동일하게 유지한다.
- **무엇이 이를 증명하는지**: `./gradlew test` 전체 테스트(CategoryAcceptanceTest 포함) 통과로 검증한다.

## 2. CategoryController.updateCategory()에서 orElse(null) + null 체크를 orElseThrow()로 변경

- **무엇을 바꾸는지**: updateCategory()의 try-catch 패턴을 제거하고, Service에서 던지는 NoSuchElementException을 @ExceptionHandler로 일관되게 처리한다.
- **무엇을 바꾸지 않는지**: 존재하지 않는 카테고리 수정 시 404 응답을 반환하는 동작은 동일하다.
- **무엇이 이를 증명하는지**: `./gradlew test` 전체 테스트 통과.

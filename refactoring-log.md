# 리팩토링 로그

## 1. CategoryController → CategoryService 계층 분리

- **무엇을 바꾸는지**: CategoryController가 CategoryRepository를 직접 참조하는 구조를 CategoryService를 경유하도록 변경한다.
- **무엇을 바꾸지 않는지**: API의 요청/응답 형식, HTTP 상태 코드, 예외 발생 조건 등 외부 동작은 동일하게 유지한다.
- **무엇이 이를 증명하는지**: `./gradlew test` 전체 테스트(CategoryAcceptanceTest 포함) 통과로 검증한다.

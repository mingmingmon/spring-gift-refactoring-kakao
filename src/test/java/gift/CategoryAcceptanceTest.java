package gift;

import static org.hamcrest.Matchers.*;

import java.util.Map;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.jdbc.Sql;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;

@Sql("classpath:sql/truncate.sql")
class CategoryAcceptanceTest extends BaseAcceptanceTest {

    // S-CAT-1
    @Test
    @DisplayName("카테고리를 생성하면 조회할 수 있다")
    void 카테고리를_생성하면_조회할_수_있다() {
        // Given & When: 카테고리 생성
        RestAssured.given()
                .contentType(ContentType.JSON)
                .body(Map.of("name", "음료", "color", "#000000", "imageUrl", "http://image.png"))
                .when()
                .post("/api/categories")
                .then()
                .statusCode(201)
                .body("name", equalTo("음료"));

        // Then: 후속 조회로 name이 요청한 값과 일치하는지 확인
        RestAssured.given()
                .when()
                .get("/api/categories")
                .then()
                .statusCode(200)
                .body("", hasSize(1))
                .body("[0].name", equalTo("음료"));
    }

    // S-CAT-2
    @Test
    @DisplayName("여러 카테고리를 생성하면 모두 조회된다")
    void 여러_카테고리를_생성하면_모두_조회된다() {
        // Given & When: 카테고리 3개 생성
        RestAssured.given().contentType(ContentType.JSON).body(Map.of("name", "음료", "color", "#000000", "imageUrl", "http://image.png")).post("/api/categories");
        RestAssured.given().contentType(ContentType.JSON).body(Map.of("name", "간식", "color", "#111111", "imageUrl", "http://image.png")).post("/api/categories");
        RestAssured.given().contentType(ContentType.JSON).body(Map.of("name", "선물세트", "color", "#222222", "imageUrl", "http://image.png")).post("/api/categories");

        // Then: 3개 레코드 존재하고 각 name이 일치
        RestAssured.given()
                .when()
                .get("/api/categories")
                .then()
                .statusCode(200)
                .body("", hasSize(3))
                .body("[0].name", equalTo("음료"))
                .body("[1].name", equalTo("간식"))
                .body("[2].name", equalTo("선물세트"));
    }

    // F-CAT-1
    @Test
    @DisplayName("이름이 null인 카테고리 생성 요청은 실패하고 카테고리는 생성되지 않는다")
    void 이름이_null인_카테고리_생성_요청은_실패하고_카테고리는_생성되지_않는다() {
        // Given & When: name 없이 카테고리 생성 요청
        RestAssured.given()
                .contentType(ContentType.JSON)
                .body(Map.of())
                .when()
                .post("/api/categories")
                .then()
                .statusCode(400);

        // Then: 카테고리가 생성되지 않았다 (상태 불변)
        RestAssured.given()
                .when()
                .get("/api/categories")
                .then()
                .statusCode(200)
                .body("", hasSize(0));
    }
}

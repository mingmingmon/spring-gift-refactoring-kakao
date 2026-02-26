package gift;

import static org.hamcrest.Matchers.*;

import java.util.Map;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.jdbc.Sql;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;

@Sql("classpath:sql/truncate.sql")
class ProductAcceptanceTest extends BaseAcceptanceTest {

    // S-PRD-1
    @Test
    @DisplayName("상품을 생성하면 조회할 수 있다")
    void 상품을_생성하면_조회할_수_있다() {
        // Given: 카테고리가 존재한다 (API를 통한 데이터 준비)
        long categoryId = 카테고리_생성("음료");

        // When: 상품 생성
        RestAssured.given()
                .contentType(ContentType.JSON)
                .body(Map.of(
                        "name", "아메리카노",
                        "price", 4500,
                        "imageUrl", "http://image.png",
                        "categoryId", categoryId
                ))
                .when()
                .post("/api/products")
                .then()
                .statusCode(201)
                .body("name", equalTo("아메리카노"))
                .body("price", equalTo(4500));

        // Then: 후속 조회로 상품이 존재하고 정보가 일치하는지 확인
        RestAssured.given()
                .when()
                .get("/api/products")
                .then()
                .statusCode(200)
                .body("content", hasSize(1))
                .body("content[0].name", equalTo("아메리카노"))
                .body("content[0].price", equalTo(4500));
    }

    // S-PRD-2
    @Test
    @DisplayName("서로 다른 카테고리에 상품을 각각 생성할 수 있다")
    void 서로_다른_카테고리에_상품을_각각_생성할_수_있다() {
        // Given: 카테고리 2개 생성 (API를 통한 데이터 준비)
        long categoryA = 카테고리_생성("음료");
        long categoryB = 카테고리_생성("간식");

        // When: 각 카테고리에 상품 생성
        RestAssured.given()
                .contentType(ContentType.JSON)
                .body(Map.of(
                        "name", "아메리카노",
                        "price", 4500,
                        "imageUrl", "http://image.png",
                        "categoryId", categoryA
                ))
                .when()
                .post("/api/products")
                .then()
                .statusCode(201);

        RestAssured.given()
                .contentType(ContentType.JSON)
                .body(Map.of(
                        "name", "쿠키",
                        "price", 3000,
                        "imageUrl", "http://image.png",
                        "categoryId", categoryB
                ))
                .when()
                .post("/api/products")
                .then()
                .statusCode(201);

        // Then: 2개 상품이 존재하고 각 정보가 일치
        RestAssured.given()
                .when()
                .get("/api/products")
                .then()
                .statusCode(200)
                .body("content", hasSize(2));
    }

    // F-PRD-1
    @Test
    @DisplayName("존재하지 않는 카테고리로 상품을 생성하면 실패하고 상품은 생성되지 않는다")
    void 존재하지_않는_카테고리로_상품을_생성하면_실패하고_상품은_생성되지_않는다() {
        // When: 존재하지 않는 카테고리로 상품 생성
        RestAssured.given()
                .contentType(ContentType.JSON)
                .body(Map.of(
                        "name", "아메리카노",
                        "price", 4500,
                        "imageUrl", "http://image.png",
                        "categoryId", 999999
                ))
                .when()
                .post("/api/products")
                .then()
                .statusCode(404);

        // Then: 상품이 생성되지 않았다 (상태 불변)
        RestAssured.given()
                .when()
                .get("/api/products")
                .then()
                .statusCode(200)
                .body("content", hasSize(0));
    }

    // F-PRD-2
    @Test
    @DisplayName("카테고리ID가 null이면 상품 생성이 실패하고 상품은 생성되지 않는다")
    void 카테고리ID가_null이면_상품_생성이_실패하고_상품은_생성되지_않는다() {
        // When: categoryId 없이 상품 생성
        RestAssured.given()
                .contentType(ContentType.JSON)
                .body(Map.of(
                        "name", "아메리카노",
                        "price", 4500,
                        "imageUrl", "http://image.png"
                ))
                .when()
                .post("/api/products")
                .then()
                .statusCode(400);

        // Then: 상품이 생성되지 않았다 (상태 불변)
        RestAssured.given()
                .when()
                .get("/api/products")
                .then()
                .statusCode(200)
                .body("content", hasSize(0));
    }

    // F-PRD-3
    @Test
    @DisplayName("가격이 음수인 상품 생성 요청은 실패하고 상품은 생성되지 않는다")
    void 가격이_음수인_상품_생성_요청은_실패하고_상품은_생성되지_않는다() {
        // Given: 카테고리 생성 (API를 통한 데이터 준비)
        long categoryId = 카테고리_생성("음료");

        // When: 음수 가격으로 상품 생성
        RestAssured.given()
                .contentType(ContentType.JSON)
                .body(Map.of(
                        "name", "아메리카노",
                        "price", -1000,
                        "imageUrl", "http://image.png",
                        "categoryId", categoryId
                ))
                .when()
                .post("/api/products")
                .then()
                .statusCode(400);

        // Then: 상품이 생성되지 않았다 (상태 불변)
        RestAssured.given()
                .when()
                .get("/api/products")
                .then()
                .statusCode(200)
                .body("content", hasSize(0));
    }

    // F-PRD-4
    @Test
    @DisplayName("가격이 0인 상품 생성 요청은 실패하고 상품은 생성되지 않는다")
    void 가격이_0인_상품_생성_요청은_실패하고_상품은_생성되지_않는다() {
        // Given: 카테고리 생성 (API를 통한 데이터 준비)
        long categoryId = 카테고리_생성("음료");

        // When: 가격 0으로 상품 생성
        RestAssured.given()
                .contentType(ContentType.JSON)
                .body(Map.of(
                        "name", "아메리카노",
                        "price", 0,
                        "imageUrl", "http://image.png",
                        "categoryId", categoryId
                ))
                .when()
                .post("/api/products")
                .then()
                .statusCode(400);

        // Then: 상품이 생성되지 않았다 (상태 불변)
        RestAssured.given()
                .when()
                .get("/api/products")
                .then()
                .statusCode(200)
                .body("content", hasSize(0));
    }

    private long 카테고리_생성(String name) {
        return RestAssured.given()
                .contentType(ContentType.JSON)
                .body(Map.of(
                        "name", name,
                        "color", "#000000",
                        "imageUrl", "http://image.png"
                ))
                .when()
                .post("/api/categories")
                .then()
                .statusCode(201)
                .extract()
                .jsonPath()
                .getLong("id");
    }
}

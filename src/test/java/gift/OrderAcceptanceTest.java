package gift;

import static org.hamcrest.Matchers.*;

import java.util.Map;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.jdbc.Sql;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;

@Sql("classpath:sql/truncate.sql")
class OrderAcceptanceTest extends BaseAcceptanceTest {

    @Test
    @DisplayName("주문을 생성하면 조회할 수 있다")
    void 주문을_생성하면_조회할_수_있다() {
        String token = 회원가입_토큰("test@test.com", "1234");
        long productId = 상품_생성("아메리카노", 4500);
        long optionId = 옵션_생성(productId, "TALL", 100);
        포인트_충전("test@test.com", 50000);

        RestAssured.given()
                .contentType(ContentType.JSON)
                .header("Authorization", token)
                .body(Map.of("optionId", optionId, "quantity", 2, "message", "선물입니다"))
                .when()
                .post("/api/orders")
                .then()
                .statusCode(201)
                .body("optionId", equalTo((int) optionId))
                .body("quantity", equalTo(2))
                .body("message", equalTo("선물입니다"));

        RestAssured.given()
                .header("Authorization", token)
                .when()
                .get("/api/orders")
                .then()
                .statusCode(200)
                .body("content", hasSize(1))
                .body("content[0].quantity", equalTo(2));
    }

    @Test
    @DisplayName("주문 후 옵션 수량이 차감된다")
    void 주문_후_옵션_수량이_차감된다() {
        String token = 회원가입_토큰("test@test.com", "1234");
        long productId = 상품_생성("아메리카노", 4500);
        long optionId = 옵션_생성(productId, "TALL", 100);
        포인트_충전("test@test.com", 50000);

        RestAssured.given()
                .contentType(ContentType.JSON)
                .header("Authorization", token)
                .body(Map.of("optionId", optionId, "quantity", 3))
                .post("/api/orders");

        RestAssured.given()
                .when()
                .get("/api/products/{productId}/options", productId)
                .then()
                .statusCode(200)
                .body("[0].quantity", equalTo(97));
    }

    @Test
    @DisplayName("토큰 없이 주문하면 실패한다")
    void 토큰_없이_주문하면_실패한다() {
        long productId = 상품_생성("아메리카노", 4500);
        long optionId = 옵션_생성(productId, "TALL", 100);

        RestAssured.given()
                .contentType(ContentType.JSON)
                .body(Map.of("optionId", optionId, "quantity", 1))
                .when()
                .post("/api/orders")
                .then()
                .statusCode(400);
    }

    @Test
    @DisplayName("포인트가 부족하면 주문에 실패한다")
    void 포인트가_부족하면_주문에_실패한다() {
        String token = 회원가입_토큰("test@test.com", "1234");
        long productId = 상품_생성("아메리카노", 4500);
        long optionId = 옵션_생성(productId, "TALL", 100);

        RestAssured.given()
                .contentType(ContentType.JSON)
                .header("Authorization", token)
                .body(Map.of("optionId", optionId, "quantity", 1))
                .when()
                .post("/api/orders")
                .then()
                .statusCode(500);
    }

    @Test
    @DisplayName("포인트가 부족하면 옵션 수량이 차감되지 않는다")
    void 포인트가_부족하면_옵션_수량이_차감되지_않는다() {
        // Given: 포인트 0인 회원, 수량 100인 옵션
        String token = 회원가입_토큰("test@test.com", "1234");
        long productId = 상품_생성("아메리카노", 4500);
        long optionId = 옵션_생성(productId, "TALL", 100);

        // When: 포인트 부족으로 주문 실패
        RestAssured.given()
                .contentType(ContentType.JSON)
                .header("Authorization", token)
                .body(Map.of("optionId", optionId, "quantity", 1))
                .post("/api/orders");

        // Then: 옵션 수량이 100 그대로인지 재조회하여 확인
        RestAssured.given()
                .when()
                .get("/api/products/{productId}/options", productId)
                .then()
                .statusCode(200)
                .body("[0].quantity", equalTo(100));
    }

    @Test
    @DisplayName("재고보다 많은 수량을 주문하면 실패한다")
    void 재고보다_많은_수량을_주문하면_실패한다() {
        String token = 회원가입_토큰("test@test.com", "1234");
        long productId = 상품_생성("아메리카노", 4500);
        long optionId = 옵션_생성(productId, "TALL", 5);
        포인트_충전("test@test.com", 500000);

        RestAssured.given()
                .contentType(ContentType.JSON)
                .header("Authorization", token)
                .body(Map.of("optionId", optionId, "quantity", 6))
                .when()
                .post("/api/orders")
                .then()
                .statusCode(500);
    }

    @Test
    @DisplayName("수량 0으로 주문하면 실패한다")
    void 수량_0으로_주문하면_실패한다() {
        String token = 회원가입_토큰("test@test.com", "1234");
        long productId = 상품_생성("아메리카노", 4500);
        long optionId = 옵션_생성(productId, "TALL", 100);
        포인트_충전("test@test.com", 50000);

        RestAssured.given()
                .contentType(ContentType.JSON)
                .header("Authorization", token)
                .body(Map.of("optionId", optionId, "quantity", 0))
                .when()
                .post("/api/orders")
                .then()
                .statusCode(400);
    }

    private String 회원가입_토큰(String email, String password) {
        return RestAssured.given()
                .contentType(ContentType.JSON)
                .body(Map.of("email", email, "password", password))
                .post("/api/members/register")
                .then().extract().jsonPath().getString("token");
    }

    private long 상품_생성(String name, int price) {
        long categoryId = RestAssured.given()
                .contentType(ContentType.JSON)
                .body(Map.of("name", "음료", "color", "#000000", "imageUrl", "http://image.png"))
                .post("/api/categories")
                .then().extract().jsonPath().getLong("id");

        return RestAssured.given()
                .contentType(ContentType.JSON)
                .body(Map.of("name", name, "price", price, "imageUrl", "http://image.png", "categoryId", categoryId))
                .post("/api/products")
                .then().extract().jsonPath().getLong("id");
    }

    private long 옵션_생성(long productId, String name, int quantity) {
        return RestAssured.given()
                .contentType(ContentType.JSON)
                .body(Map.of("name", name, "quantity", quantity))
                .post("/api/products/{productId}/options", productId)
                .then().extract().jsonPath().getLong("id");
    }

    private void 포인트_충전(String email, int amount) {
        Long memberId = jdbcTemplate.queryForObject(
                "SELECT id FROM member WHERE email = ?", Long.class, email);
        jdbcTemplate.update("UPDATE member SET point = point + ? WHERE id = ?", amount, memberId);
    }
}

package gift;

import static org.hamcrest.Matchers.*;

import java.util.Map;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.jdbc.Sql;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;

@Sql("classpath:sql/truncate.sql")
class WishAcceptanceTest extends BaseAcceptanceTest {

    @Test
    @DisplayName("위시리스트에 추가하면 조회할 수 있다")
    void 위시리스트에_추가하면_조회할_수_있다() {
        String token = 회원가입_토큰("test@test.com", "1234");
        long productId = 상품_생성("아메리카노", 4500);

        RestAssured.given()
                .contentType(ContentType.JSON)
                .header("Authorization", token)
                .body(Map.of("productId", productId))
                .when()
                .post("/api/wishes")
                .then()
                .statusCode(201);

        RestAssured.given()
                .header("Authorization", token)
                .when()
                .get("/api/wishes")
                .then()
                .statusCode(200)
                .body("content", hasSize(1))
                .body("content[0].productId", equalTo((int) productId));
    }

    @Test
    @DisplayName("위시리스트에서 삭제하면 조회되지 않는다")
    void 위시리스트에서_삭제하면_조회되지_않는다() {
        String token = 회원가입_토큰("test@test.com", "1234");
        long productId = 상품_생성("아메리카노", 4500);
        long wishId = RestAssured.given()
                .contentType(ContentType.JSON)
                .header("Authorization", token)
                .body(Map.of("productId", productId))
                .post("/api/wishes")
                .then().extract().jsonPath().getLong("id");

        RestAssured.given()
                .header("Authorization", token)
                .when()
                .delete("/api/wishes/{id}", wishId)
                .then()
                .statusCode(204);

        RestAssured.given()
                .header("Authorization", token)
                .when()
                .get("/api/wishes")
                .then()
                .statusCode(200)
                .body("content", hasSize(0));
    }

    @Test
    @DisplayName("동일 상품을 중복 추가하면 200을 반환한다")
    void 동일_상품을_중복_추가하면_200을_반환한다() {
        String token = 회원가입_토큰("test@test.com", "1234");
        long productId = 상품_생성("아메리카노", 4500);

        RestAssured.given()
                .contentType(ContentType.JSON)
                .header("Authorization", token)
                .body(Map.of("productId", productId))
                .post("/api/wishes")
                .then().statusCode(201);

        RestAssured.given()
                .contentType(ContentType.JSON)
                .header("Authorization", token)
                .body(Map.of("productId", productId))
                .when()
                .post("/api/wishes")
                .then()
                .statusCode(200);

        RestAssured.given()
                .header("Authorization", token)
                .when()
                .get("/api/wishes")
                .then()
                .statusCode(200)
                .body("content", hasSize(1));
    }

    @Test
    @DisplayName("토큰 없이 위시리스트를 조회하면 실패한다")
    void 토큰_없이_위시리스트를_조회하면_실패한다() {
        RestAssured.given()
                .when()
                .get("/api/wishes")
                .then()
                .statusCode(400);
    }

    @Test
    @DisplayName("토큰 없이 위시리스트에 추가하면 실패한다")
    void 토큰_없이_위시리스트에_추가하면_실패한다() {
        long productId = 상품_생성("아메리카노", 4500);

        RestAssured.given()
                .contentType(ContentType.JSON)
                .body(Map.of("productId", productId))
                .when()
                .post("/api/wishes")
                .then()
                .statusCode(400);
    }

    @Test
    @DisplayName("다른 사용자의 위시를 삭제하면 403을 반환한다")
    void 다른_사용자의_위시를_삭제하면_403을_반환한다() {
        String tokenA = 회원가입_토큰("userA@test.com", "1234");
        long productId = 상품_생성("아메리카노", 4500);
        long wishId = RestAssured.given()
                .contentType(ContentType.JSON)
                .header("Authorization", tokenA)
                .body(Map.of("productId", productId))
                .post("/api/wishes")
                .then().extract().jsonPath().getLong("id");

        String tokenB = 회원가입_토큰("userB@test.com", "1234");
        RestAssured.given()
                .header("Authorization", tokenB)
                .when()
                .delete("/api/wishes/{id}", wishId)
                .then()
                .statusCode(403);
    }

    @Test
    @DisplayName("존재하지 않는 위시를 삭제하면 404를 반환한다")
    void 존재하지_않는_위시를_삭제하면_404를_반환한다() {
        String token = 회원가입_토큰("test@test.com", "1234");

        RestAssured.given()
                .header("Authorization", token)
                .when()
                .delete("/api/wishes/{id}", 999999)
                .then()
                .statusCode(404);
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
}

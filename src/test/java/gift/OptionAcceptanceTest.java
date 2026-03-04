package gift;

import static org.hamcrest.Matchers.*;

import java.util.Map;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.jdbc.Sql;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;

@Sql("classpath:sql/truncate.sql")
class OptionAcceptanceTest extends BaseAcceptanceTest {

    @Test
    @DisplayName("옵션을 생성하면 조회할 수 있다")
    void 옵션을_생성하면_조회할_수_있다() {
        long productId = 상품_생성("아메리카노", 4500);

        RestAssured.given()
                .contentType(ContentType.JSON)
                .body(Map.of("name", "TALL", "quantity", 100))
                .when()
                .post("/api/products/{productId}/options", productId)
                .then()
                .statusCode(201)
                .body("name", equalTo("TALL"));

        RestAssured.given()
                .when()
                .get("/api/products/{productId}/options", productId)
                .then()
                .statusCode(200)
                .body("", hasSize(1))
                .body("[0].name", equalTo("TALL"))
                .body("[0].quantity", equalTo(100));
    }

    @Test
    @DisplayName("같은 상품에 여러 옵션을 생성할 수 있다")
    void 같은_상품에_여러_옵션을_생성할_수_있다() {
        long productId = 상품_생성("아메리카노", 4500);

        RestAssured.given().contentType(ContentType.JSON)
                .body(Map.of("name", "TALL", "quantity", 100))
                .post("/api/products/{productId}/options", productId);
        RestAssured.given().contentType(ContentType.JSON)
                .body(Map.of("name", "GRANDE", "quantity", 50))
                .post("/api/products/{productId}/options", productId);

        RestAssured.given()
                .when()
                .get("/api/products/{productId}/options", productId)
                .then()
                .statusCode(200)
                .body("", hasSize(2));
    }

    @Test
    @DisplayName("옵션이 2개일 때 1개를 삭제할 수 있다")
    void 옵션이_2개일_때_1개를_삭제할_수_있다() {
        long productId = 상품_생성("아메리카노", 4500);
        long optionId = 옵션_생성(productId, "TALL", 100);
        옵션_생성(productId, "GRANDE", 50);

        RestAssured.given()
                .when()
                .delete("/api/products/{productId}/options/{optionId}", productId, optionId)
                .then()
                .statusCode(204);

        RestAssured.given()
                .when()
                .get("/api/products/{productId}/options", productId)
                .then()
                .statusCode(200)
                .body("", hasSize(1))
                .body("[0].name", equalTo("GRANDE"));
    }

    @Test
    @DisplayName("옵션 이름이 50자를 초과하면 생성에 실패한다")
    void 옵션_이름이_50자를_초과하면_생성에_실패한다() {
        long productId = 상품_생성("아메리카노", 4500);
        String longName = "a".repeat(51);

        RestAssured.given()
                .contentType(ContentType.JSON)
                .body(Map.of("name", longName, "quantity", 100))
                .when()
                .post("/api/products/{productId}/options", productId)
                .then()
                .statusCode(400);
    }

    @Test
    @DisplayName("허용되지 않는 특수문자가 포함된 옵션 이름은 생성에 실패한다")
    void 허용되지_않는_특수문자가_포함된_옵션_이름은_생성에_실패한다() {
        long productId = 상품_생성("아메리카노", 4500);

        RestAssured.given()
                .contentType(ContentType.JSON)
                .body(Map.of("name", "옵션!@#", "quantity", 100))
                .when()
                .post("/api/products/{productId}/options", productId)
                .then()
                .statusCode(400);
    }

    @Test
    @DisplayName("수량이 0이면 옵션 생성에 실패한다")
    void 수량이_0이면_옵션_생성에_실패한다() {
        long productId = 상품_생성("아메리카노", 4500);

        RestAssured.given()
                .contentType(ContentType.JSON)
                .body(Map.of("name", "TALL", "quantity", 0))
                .when()
                .post("/api/products/{productId}/options", productId)
                .then()
                .statusCode(400);
    }

    @Test
    @DisplayName("수량이 상한을 초과하면 옵션 생성에 실패한다")
    void 수량이_상한을_초과하면_옵션_생성에_실패한다() {
        long productId = 상품_생성("아메리카노", 4500);

        RestAssured.given()
                .contentType(ContentType.JSON)
                .body(Map.of("name", "TALL", "quantity", 100_000_000))
                .when()
                .post("/api/products/{productId}/options", productId)
                .then()
                .statusCode(400);
    }

    @Test
    @DisplayName("마지막 남은 옵션은 삭제할 수 없다")
    void 마지막_남은_옵션은_삭제할_수_없다() {
        long productId = 상품_생성("아메리카노", 4500);
        long optionId = 옵션_생성(productId, "TALL", 100);

        RestAssured.given()
                .when()
                .delete("/api/products/{productId}/options/{optionId}", productId, optionId)
                .then()
                .statusCode(400);
    }

    @Test
    @DisplayName("존재하지 않는 상품에 옵션을 생성하면 실패한다")
    void 존재하지_않는_상품에_옵션을_생성하면_실패한다() {
        RestAssured.given()
                .contentType(ContentType.JSON)
                .body(Map.of("name", "TALL", "quantity", 100))
                .when()
                .post("/api/products/{productId}/options", 999999)
                .then()
                .statusCode(404);
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
}

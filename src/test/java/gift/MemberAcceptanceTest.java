package gift;

import static org.hamcrest.Matchers.*;

import java.util.Map;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.jdbc.Sql;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;

@Sql("classpath:sql/truncate.sql")
class MemberAcceptanceTest extends BaseAcceptanceTest {

    @Test
    @DisplayName("회원가입하면 토큰이 발급된다")
    void 회원가입하면_토큰이_발급된다() {
        RestAssured.given()
                .contentType(ContentType.JSON)
                .body(Map.of("email", "test@test.com", "password", "1234"))
                .when()
                .post("/api/members/register")
                .then()
                .statusCode(201)
                .body("token", notNullValue());
    }

    @Test
    @DisplayName("로그인하면 토큰이 발급된다")
    void 로그인하면_토큰이_발급된다() {
        RestAssured.given()
                .contentType(ContentType.JSON)
                .body(Map.of("email", "test@test.com", "password", "1234"))
                .post("/api/members/register");

        RestAssured.given()
                .contentType(ContentType.JSON)
                .body(Map.of("email", "test@test.com", "password", "1234"))
                .when()
                .post("/api/members/login")
                .then()
                .statusCode(200)
                .body("token", notNullValue());
    }

    @Test
    @DisplayName("중복 이메일로 가입하면 실패한다")
    void 중복_이메일로_가입하면_실패한다() {
        RestAssured.given()
                .contentType(ContentType.JSON)
                .body(Map.of("email", "test@test.com", "password", "1234"))
                .post("/api/members/register");

        RestAssured.given()
                .contentType(ContentType.JSON)
                .body(Map.of("email", "test@test.com", "password", "5678"))
                .when()
                .post("/api/members/register")
                .then()
                .statusCode(400);
    }

    @Test
    @DisplayName("잘못된 이메일 형식이면 가입이 실패한다")
    void 잘못된_이메일_형식이면_가입이_실패한다() {
        RestAssured.given()
                .contentType(ContentType.JSON)
                .body(Map.of("email", "invalid-email", "password", "1234"))
                .when()
                .post("/api/members/register")
                .then()
                .statusCode(400);
    }

    @Test
    @DisplayName("비밀번호가 빈 문자열이면 가입이 실패한다")
    void 비밀번호가_빈_문자열이면_가입이_실패한다() {
        RestAssured.given()
                .contentType(ContentType.JSON)
                .body(Map.of("email", "test@test.com", "password", ""))
                .when()
                .post("/api/members/register")
                .then()
                .statusCode(400);
    }

    @Test
    @DisplayName("존재하지 않는 이메일로 로그인하면 실패한다")
    void 존재하지_않는_이메일로_로그인하면_실패한다() {
        RestAssured.given()
                .contentType(ContentType.JSON)
                .body(Map.of("email", "nobody@test.com", "password", "1234"))
                .when()
                .post("/api/members/login")
                .then()
                .statusCode(400);
    }

    @Test
    @DisplayName("틀린 비밀번호로 로그인하면 실패한다")
    void 틀린_비밀번호로_로그인하면_실패한다() {
        RestAssured.given()
                .contentType(ContentType.JSON)
                .body(Map.of("email", "test@test.com", "password", "1234"))
                .post("/api/members/register");

        RestAssured.given()
                .contentType(ContentType.JSON)
                .body(Map.of("email", "test@test.com", "password", "wrong"))
                .when()
                .post("/api/members/login")
                .then()
                .statusCode(400);
    }
}
package io.quarkus;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.RestAssured;
import org.junit.jupiter.api.Test;

import static org.hamcrest.Matchers.equalTo;

@QuarkusTest
public class GreetingTest
{
    // NOTE: RestAssured is aware of the application.properties quarkus.http.root-path switch

    @Test
    public void testFunqy() {
        RestAssured.when().get("/funqyHello").then()
                .statusCode(200)
                .contentType("application/json")
                .body(equalTo("\"hello funqy\""));
    }
}

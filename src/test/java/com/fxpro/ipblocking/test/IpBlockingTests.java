package com.fxpro.ipblocking.test;

import com.fxpro.ipblocking.helper.Helper;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.filter.log.RequestLoggingFilter;
import io.restassured.filter.log.ResponseLoggingFilter;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.Properties;

import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.assertEquals;


public class IpBlockingTests extends Helper {

    private static RequestSpecification spec;
    public Response response;
    public static String jsonAsString;

    public IpBlockingTests(Properties properties) {
        super(properties);
    }

    @BeforeAll
    public static void initSpec(){
        spec = new RequestSpecBuilder()
                .setContentType(ContentType.JSON)
                .setBaseUri("http://localhost:8090/address")
                .addFilter(new ResponseLoggingFilter())
                .addFilter(new RequestLoggingFilter())
                .build();
    }

    @AfterEach
    void CleanUpBlockedAddresses(){
        System.out.println("Cleaned up addresses");
    }

    @Test
    public void testBlockValidIp(){
        jsonAsString = getBlockingValidIpResponse(BLOCK).asString();
        assertEquals(jsonAsString, getPositiveResponseStringWithIp(VALIDIP, BLOCKED));
    }

    private Response getBlockingValidIpResponse(String block) {
        return response =
                given()
                        .spec(spec)
                        .when()
                        .post(VALIDIP +"/" + block)
                        .then()
                        .statusCode(200)
                        .extract()
                        .response();
    }

    @Test
    public void testBlockInvalidIp(){
        response =
                given()
                        .spec(spec)
                        .when()
                        .post( INVALIDIP +"/block")
                        .then()
                        .statusCode(400)
                        .extract()
                        .response();
        jsonAsString = response.asString();
        assertEquals(jsonAsString, getNegativeResponseStringWithIp(INVALIDIP));
    }

    @Test
    public void testBlockInvalidCommand(){
        response =
                given()
                        .spec(spec)
                        .when()
                        .post( VALIDIP +"/blocking")
                        .then()
                        .statusCode(400)
                        .extract()
                        .response();
        jsonAsString = response.asString();
        assertEquals(jsonAsString, Helper.INVALIDCOMMAND);
    }

    @Test
    public void testRepeatBlockingIp(){
        response = getBlockingValidIpResponse(BLOCK);
        jsonAsString = response.asString();
        assertEquals(jsonAsString, getPositiveResponseStringWithIp(VALIDIP, BLOCKED));
        Response response2 = getBlockingValidIpResponse(BLOCKAGAIN);
        String jsonAsString2 = response2.asString();
        assertEquals(jsonAsString2, getPositiveResponseStringWithIp(VALIDIP, ALREADYBLOCKED));
    }

    @Test
    public void testBlockWhitelistedIp(){
        response =
                given()
                        .spec(spec)
                        .when()
                        .post( WHITELISTEDIP +"/block")
                        .then()
                        .statusCode(400)
                        .extract()
                        .response();
        jsonAsString = response.asString();
        assertEquals(jsonAsString, Helper.WHITELISTEDIPCOMMAND);
    }

}

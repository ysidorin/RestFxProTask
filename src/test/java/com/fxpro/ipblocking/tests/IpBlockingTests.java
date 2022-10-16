package com.fxpro.ipblocking.tests;

import com.fxpro.ipblocking.helper.Helper;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.filter.log.RequestLoggingFilter;
import io.restassured.filter.log.ResponseLoggingFilter;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import org.junit.After;
import org.junit.BeforeClass;
import org.junit.Test;


import static io.restassured.RestAssured.given;
import static org.junit.Assert.assertEquals;


public class IpBlockingTests extends Helper {

    private static RequestSpecification spec;
    public Response response;
    public static String jsonAsString;

    @BeforeClass
    public static void initSpec(){
        spec = new RequestSpecBuilder()
                .setContentType(ContentType.JSON)
                .setBaseUri("http://host.docker.internal:8090/address")
                .addFilter(new ResponseLoggingFilter())
                .addFilter(new RequestLoggingFilter())
                .build();
    }

    @After
    public void CleanUpBlockedAddresses(){
        System.out.println("Cleaned up addresses");
    }

    @Test
    public void testBlockValidIp(){
        jsonAsString = getBlockingIpResponse(BLOCK, VALIDIP).asString();
        assertEquals(jsonAsString, getPositiveResponseStringWithIp(VALIDIP, BLOCKED));
        String jsonAsStringLeft = getBlockingIpResponse(BLOCK, VALIDIPLEFT).asString();
        assertEquals(jsonAsStringLeft, getPositiveResponseStringWithIp(VALIDIPLEFT, BLOCKED));
        String jsonAsStringRight = getBlockingIpResponse(BLOCK, VALIDIPRIGHT).asString();
        assertEquals(jsonAsStringRight, getPositiveResponseStringWithIp(VALIDIPRIGHT, BLOCKED));
    }

    private Response getBlockingIpResponse(String block, String ip) {
        return response =
                given()
                        .spec(spec)
                        .when()
                        .post(ip +"/" + block)
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
        response = getBlockingIpResponse(BLOCK, VALIDIP);
        jsonAsString = response.asString();
        assertEquals(jsonAsString, getPositiveResponseStringWithIp(VALIDIP, BLOCKED));
        Response response2 = getBlockingIpResponse(BLOCKAGAIN, VALIDIP);
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

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
import org.junit.Ignore;
import org.junit.Test;


import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Properties;

import static io.restassured.RestAssured.given;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;


public class IpBlockingTests extends Helper {

    Properties properties;
    private static RequestSpecification spec;
    public Response response;
    public static String jsonAsString;

    public IpBlockingTests() throws IOException {
        this.properties = new Properties();
        properties.load(new FileReader(new File(String.format("src/test/resources/local.properties"))));
    }

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

    @Test
    public void testBlockingWithHeadersInRequest(){
        response =
                given()
                        .spec(spec)
                        .when()
                        .contentType(ContentType.JSON)
                        .accept(ContentType.JSON)
                        .post( VALIDIP +"/block")
                        .then()
                        .statusCode(200)
                        .extract()
                        .response();
        jsonAsString = response.asString();
        assertEquals(jsonAsString, getPositiveResponseStringWithIp(VALIDIP, BLOCKED));
    }

    @Test
    public void testBlockingWithBodyInRequest() {
        response =
                given()
                        .spec(spec)
                        .when()
                        .contentType(ContentType.JSON)
                        .accept(ContentType.JSON)
                        .body(REQUESTBODY)
                        .post(VALIDIP + "/block")
                        .then()
                        .statusCode(200)
                        .extract()
                        .response();
        jsonAsString = response.asString();
        assertEquals(jsonAsString, getPositiveResponseStringWithIp(VALIDIP, BLOCKED));
    }

    @Ignore
    @Test
    public void testBlockingPeriod() throws InterruptedException {
        jsonAsString = getBlockingIpResponse(BLOCK, VALIDIP).asString();
        assertEquals(jsonAsString, getPositiveResponseStringWithIp(VALIDIP, BLOCKED));
        jsonAsString = getBlockingIpResponse(BLOCK, VALIDIP).asString();
        assertNotEquals(jsonAsString, getPositiveResponseStringWithIp(VALIDIP, BLOCKED));

        Thread.sleep(Long.getLong(properties.getProperty("blocking.period.seconds")));

        jsonAsString = getBlockingIpResponse(BLOCK, VALIDIP).asString();
        assertEquals(jsonAsString, getPositiveResponseStringWithIp(VALIDIP, BLOCKED));

    }
}

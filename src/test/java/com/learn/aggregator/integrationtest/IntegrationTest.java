package com.learn.aggregator.integrationtest;

import com.github.tomakehurst.wiremock.junit5.WireMockTest;
import com.learn.aggregator.AggregatorApplication;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.getRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.matchingJsonPath;
import static com.github.tomakehurst.wiremock.client.WireMock.ok;
import static com.github.tomakehurst.wiremock.client.WireMock.okForContentType;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.removeAllMappings;
import static com.github.tomakehurst.wiremock.client.WireMock.resetAllRequests;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.verify;


@SpringBootTest(classes = AggregatorApplication.class)
@WireMockTest(httpPort = 7770)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@TestPropertySource(properties = {
        "junit.jupiter.execution.parallel.enabled=false",
        "integration.serviceA.url=http://localhost:7770/api/v2.1/results",
        "integration.serviceB.url=http://localhost:7770/api/v0.7/status",
        "integration.serviceX.url=http://localhost:7770/api/v1/finalize"})
class IntegrationTest {
    private static final String URL_A = "/api/v2.1/results";
    private static final String URL_B = "/api/v0.7/status";
    private static final String URL_X = "/api/v1/finalize";
    private static final String SUCCESS = "success";
    private static final String FAILURE = "failure";
    private static final String FAILURE_REASON = "there was a failure";
    private static final String SERVICE_A_BODY_TEMPLATE = "{\"id\": \"%s\",\"status\": \"%s\"}";
    public static final String SERVICE_B_BODY_TEMPLATE = "<message id=\"%s\">" +
            "<status>%s</status>" +
            "<reason>%s</reason>" +
            "</message>";

    @AfterAll
    void cleanStubs() {
        removeAllMappings();
        resetAllRequests();
    }

    @Test
    @Order(1)
    void shouldTriggerOutboundRequestsPeriodicaly() throws InterruptedException {
        String id = "OUTBOUND_REQUEST_TRIGGER_TEST_ID";
        //given
        stubFor(get(URL_A)
                .willReturn(okForContentType(
                        "application/json",
                        String.format(SERVICE_A_BODY_TEMPLATE, id, SUCCESS))));
        stubFor(get(URL_B)
                .willReturn(okForContentType(
                        "application/xml",
                        String.format(SERVICE_B_BODY_TEMPLATE, id, FAILURE, FAILURE_REASON))));
        stubFor(post(URL_X)
                .withRequestBody(matchingJsonPath("$.id", equalTo(id)))
                .willReturn(ok()));

        //when
        Thread.sleep(9000);

        //then
        verify(0, getRequestedFor(urlEqualTo(URL_A)));
        verify(0, getRequestedFor(urlEqualTo(URL_B)));
        verify(0, postRequestedFor(urlEqualTo(URL_X)));

        //when
        Thread.sleep(9000);
        verify(1, getRequestedFor(urlEqualTo(URL_A)));
        verify(1, getRequestedFor(urlEqualTo(URL_B)));
    }

    @Test
    void shouldPerformOutboundXSuccessResult() throws InterruptedException {
        String id = "SUCCESS_REASON_TEST_ID";
        //given
        stubFor(get(URL_A)
                .willReturn(okForContentType(
                        "application/json",
                        String.format(SERVICE_A_BODY_TEMPLATE, id, SUCCESS))));
        stubFor(get(URL_B)
                .willReturn(okForContentType(
                        "application/xml",
                        String.format(SERVICE_B_BODY_TEMPLATE, id, SUCCESS, StringUtils.EMPTY))));
        stubFor(post(URL_X)
                .withRequestBody(matchingJsonPath("$.id", equalTo(id)))
                .willReturn(ok()));

        //when
        Thread.sleep(19000);

        //then
        verify(1, postRequestedFor(urlEqualTo(URL_X))
                .withRequestBody(matchingJsonPath("$.id", equalTo(id)))
                .withRequestBody(matchingJsonPath("$.reasons", equalTo(StringUtils.EMPTY))));
    }

    @ParameterizedTest
    @CsvSource({
            "POSITIVE_TEST_ID, POSITIVE_TEST_ID, POSITIVE_TEST_ID, 1",
            "NEGATIVE_TEST_ID_1, NEGATIVE_TEST_ID_2, NEGATIVE_TEST_ID_1, 0"
    })
    @Order(Integer.MAX_VALUE)
    void shouldPerformOutboundRequests(String firstId, String secondId, String aggregationId, int calledTimes) throws InterruptedException {
        //given
        stubFor(get(URL_A)
                .willReturn(okForContentType(
                        "application/json",
                        String.format(SERVICE_A_BODY_TEMPLATE, firstId, SUCCESS))));
        stubFor(get(URL_B)
                .willReturn(okForContentType(
                        "application/xml",
                        String.format(SERVICE_B_BODY_TEMPLATE, secondId, FAILURE, FAILURE_REASON))));
        stubFor(post(URL_X)
                .withRequestBody(matchingJsonPath("$.id", equalTo(aggregationId)))
                .willReturn(ok()));

        //when
        Thread.sleep(19000);

        //then
        verify(calledTimes, postRequestedFor(urlEqualTo(URL_X))
                .withRequestBody(matchingJsonPath("$.id", equalTo(aggregationId)))
                .withRequestBody(matchingJsonPath("$.reasons", equalTo(FAILURE_REASON))));
    }
}

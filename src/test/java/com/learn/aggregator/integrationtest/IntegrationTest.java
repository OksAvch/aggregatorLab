package com.learn.aggregator.integrationtest;

import com.github.tomakehurst.wiremock.junit5.WireMockTest;
import com.github.tomakehurst.wiremock.matching.ContentPattern;
import com.github.tomakehurst.wiremock.matching.RegexPattern;
import com.learn.aggregator.AggregatorApplication;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.getAllServeEvents;
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
@TestPropertySource(properties = {
        "integration.serviceA.url=http://localhost:7770/api/v2.1/results",
        "integration.serviceB.url=http://localhost:7770/api/v0.7/status",
        "integration.serviceX.url=http://localhost:7770/api/v1/finalize"})
class IntegrationTest {
    private static final String URL_A = "/api/v2.1/results";
    private static final String URL_B = "/api/v0.7/status";
    private static final String URL_X = "/api/v1/finalize";

    @AfterAll
    void cleanStubs() {
        removeAllMappings();
        resetAllRequests();
    }

    @Test
    void shouldNotPerformOutboundRequests() throws InterruptedException {
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

    @ParameterizedTest
    @CsvSource({
            "POSITIVE_TEST_ID, POSITIVE_TEST_ID, POSITIVE_TEST_ID, 1",
            "POSITIVE_TEST_ID, NEGATIVE_TEST_ID, NEGATIVE_TEST_ID, 0"
    })
    void shouldPerformOutboundRequests(String firstId, String secondId, String aggregationId, int calledTimes) throws InterruptedException {
        String failureReason = "there was a failure";

        //given
        stubFor(get(URL_A)
                .willReturn(okForContentType(
                        "application/json",
                        "{\"id\": \"" + firstId + "\",\"status\": \"success\"}")));
        stubFor(get(URL_B)
                .willReturn(okForContentType(
                        "application/xml",
                        "<message id=\"" + secondId + "\">" +
                                "<status>failure</status>" +
                                "<reason>" + failureReason + "</reason>" +
                                "</message>")));
        stubFor(post(URL_X).willReturn(ok()));

        //when
        Thread.sleep(19000);

        //then
        verify(calledTimes, postRequestedFor(urlEqualTo(URL_X))
                .withRequestBody(matchingJsonPath("$.id", equalTo(aggregationId)))
                .withRequestBody(matchingJsonPath("$.reasons", equalTo(failureReason))));
    }

    @Test
    void shouldPerformOutboundXSuccessResult() throws InterruptedException {
        String id = "SUCCESS_REASON_TEST_ID";
        //given
        stubFor(get(URL_A)
                .willReturn(okForContentType(
                        "application/json",
                        "{\"id\": \"" + id + "\",\"status\": \"success\"}")));
        stubFor(get(URL_B)
                .willReturn(okForContentType(
                        "application/xml",
                        "<message id=\"" + id + "\">" +
                                "<status>success</status>" +
                                "<reason></reason>" +
                                "</message>")));
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

}

package com.learn.aggregator.integrationtest;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import com.github.tomakehurst.wiremock.junit5.WireMockRuntimeInfo;
import com.github.tomakehurst.wiremock.junit5.WireMockTest;
import com.learn.aggregator.AggregatorApplication;
import com.learn.aggregator.configuration.IntegrationParameters;
import com.learn.aggregator.configuration.MessageGateway;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.integration.test.context.SpringIntegrationTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.event.annotation.BeforeTestMethod;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import org.springframework.test.util.ReflectionTestUtils;

import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.getRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.okForContentType;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.putRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.removeAllMappings;
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

    @BeforeEach
    void setGeneralStub() {
        stubFor(get(URL_A)
                .willReturn(okForContentType(
                        "application/json",
                        "{\"id\": \"186256c2-297d-4a20-bfa9-45f1ad5a639f\",\"status\": \"success\"}")));
        stubFor(get(URL_B)
                .willReturn(okForContentType(
                        "application/xml",
                        "<message id=\"186256c2-297d-4a20-bfa9-45f1ad5a639f\">" +
                                "<status>failure</status>" +
                                "<reason>there was a failure</reason>" +
                                "</message>")));
        stubFor(get(URL_X)
                .willReturn(okForContentType(
                        "application/json",
                        "\"id\": \"186256c2-297d-4a20-bfa9-45f1ad5a639f\", \"status\": \"failure\",\n" +
                                "\"reasons\": [\"there was a failure\"]}")));
    }

    @AfterEach
    void cleanStubs() {
        removeAllMappings();
    }


    @Test
    void shouldPerformOutboundRequests() throws InterruptedException {
        Thread.sleep(20000);

        verify(1, getRequestedFor(urlEqualTo(URL_A)));
        verify(1, getRequestedFor(urlEqualTo(URL_B)));
//        verify(1, postRequestedFor(urlEqualTo(URL_X)));
    }

}

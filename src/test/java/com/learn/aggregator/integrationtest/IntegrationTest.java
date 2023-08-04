package com.learn.aggregator.integrationtest;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import com.github.tomakehurst.wiremock.junit5.WireMockRuntimeInfo;
import com.github.tomakehurst.wiremock.junit5.WireMockTest;
import com.learn.aggregator.AggregatorApplication;
import com.learn.aggregator.configuration.ChannelConfiguration;
import com.learn.aggregator.configuration.IntegrationParameters;
import com.learn.aggregator.configuration.MessageGateway;
import org.junit.jupiter.api.AfterAll;
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
import org.springframework.test.context.TestExecutionListeners;
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
@WireMockTest(httpPort = 50001)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestPropertySource(properties = {
        "integration.serviceA.url=http://localhost:50001/api/v2.1/results",
        "integration.serviceB.url=http://localhost:50001/api/v0.7/status"})
public class IntegrationTest {
    private static final String PATH_A = "/api/v2.1/results";
    private static final String PATH_B = "/api/v0.7/status";

    @BeforeAll
    void setGeneralStub() {
        stubFor(get(PATH_A)
                .willReturn(okForContentType(
                        "application/json",
                        "{\"id\": \"186256c2-297d-4a20-bfa9-45f1ad5a639f\",\"status\": \"success\"}")));
        stubFor(get(PATH_B)
                .willReturn(okForContentType(
                        "application/xml",
                        "<message id=\"186256c2-297d-4a20-bfa9-45f1ad5a639f\">" +
                                "<status>failure</status>" +
                                "<reason>there was a failure</reason>" +
                                "</message>")));
    }

    @AfterAll
    void cleanStubs() {
        removeAllMappings();
    }


    @Test
    void shouldPerformOutboundRequests() throws InterruptedException {
        Thread.sleep(20000);

        verify(1, getRequestedFor(urlEqualTo(PATH_A)));
        verify(1, getRequestedFor(urlEqualTo(PATH_B)));
    }

}

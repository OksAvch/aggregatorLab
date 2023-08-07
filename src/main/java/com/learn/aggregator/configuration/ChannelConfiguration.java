package com.learn.aggregator.configuration;

import com.learn.aggregator.dto.InboundMessageDto;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.expression.common.LiteralExpression;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.http.converter.xml.MappingJackson2XmlHttpMessageConverter;
import org.springframework.integration.annotation.InboundChannelAdapter;
import org.springframework.integration.annotation.Poller;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.integration.channel.QueueChannel;
import org.springframework.integration.core.MessageSource;
import org.springframework.integration.endpoint.ExpressionEvaluatingMessageSource;
import org.springframework.integration.http.outbound.HttpRequestExecutingMessageHandler;
import org.springframework.integration.http.support.DefaultHttpHeaderMapper;
import org.springframework.integration.mapping.HeaderMapper;
import org.springframework.messaging.MessageChannel;

import java.util.List;

@Configuration
@AllArgsConstructor
public class ChannelConfiguration {
    public static final String AGGREGATE_MESSAGES = "aggregateMessages";
    public static final String TRIGGER_SERVICE_A_REQUEST = "triggerServiceARequest";
    public static final String TRIGGER_SERVICE_B_REQUEST = "triggerServiceBRequest";
    public static final String SEND_OUTBOUND_REQUEST_SERVICE_A = "sendOutboundRequestServiceA";
    public static final String SEND_OUTBOUND_REQUEST_SERVICE_B = "sendOutboundRequestServiceB";
    public static final String PREPARE_OUTBOUND_REQUEST_SERVICE_X = "prepareOutboundRequestServiceX";
    public static final String SEND_OUTBOUND_REQUEST_SERVICE_X = "sendOutboundRequestServiceX";
    public static final String STORE_OUTBOUND_RESPONSE_SERVICE_X = "storeOutboundResponseServiceX";


    @Bean
    @Qualifier(TRIGGER_SERVICE_A_REQUEST)
    public MessageChannel triggerServiceARequest() {
        return new QueueChannel();
    }

    @Bean
    @Qualifier(TRIGGER_SERVICE_B_REQUEST)
    public MessageChannel triggerServiceBRequest() {
        return new QueueChannel();
    }

    @Bean(name = SEND_OUTBOUND_REQUEST_SERVICE_A)
    public QueueChannel sendOutboundRequestServiceA() {
        return new QueueChannel();
    }

    @Bean(name = SEND_OUTBOUND_REQUEST_SERVICE_B)
    public QueueChannel sendOutboundRequestServiceB() {
        return new QueueChannel();
    }

    @Bean(name = SEND_OUTBOUND_REQUEST_SERVICE_X)
    public QueueChannel sendOutboundRequestServiceX() {
        return new QueueChannel();
    }

    @Bean(name = AGGREGATE_MESSAGES)
    public QueueChannel sendToAggregator() {
        return new QueueChannel();
    }

    @Bean(name = STORE_OUTBOUND_RESPONSE_SERVICE_X)
    public QueueChannel storeOutboundRequestServiceX() {
        return new QueueChannel();
    }

    @Bean
    @ServiceActivator(inputChannel = TRIGGER_SERVICE_A_REQUEST, poller = @Poller(fixedDelay = "${integration.serviceA.pollerPeriod}"))
    public HttpRequestExecutingMessageHandler outboundRequestServiceAHandler(
            @Qualifier(SEND_OUTBOUND_REQUEST_SERVICE_A) MessageChannel outChannel,
            IntegrationParameters integrationParameters) {

        HttpRequestExecutingMessageHandler handler =
                new HttpRequestExecutingMessageHandler(integrationParameters.getServiceAUrl());
        handler.setHttpMethod(HttpMethod.GET);
        handler.setExpectedResponseType(InboundMessageDto.class);
        handler.setOutputChannel(outChannel);
        return handler;
    }

    @Bean
    @ServiceActivator(inputChannel = TRIGGER_SERVICE_B_REQUEST, poller = @Poller(fixedDelay = "${integration.serviceB.pollerPeriod}"))
    public HttpRequestExecutingMessageHandler outboundRequestServiceBHandler(
            @Qualifier(SEND_OUTBOUND_REQUEST_SERVICE_B) MessageChannel outChannel,
            IntegrationParameters integrationParameters) {

        HttpRequestExecutingMessageHandler handler =
                new HttpRequestExecutingMessageHandler(integrationParameters.getServiceBUrl());
        handler.setHttpMethod(HttpMethod.GET);
        handler.setMessageConverters(List.of(new MappingJackson2XmlHttpMessageConverter()));
        handler.setExpectedResponseType(InboundMessageDto.class);
        handler.setOutputChannel(outChannel);
        return handler;
    }

    @Bean
    @ServiceActivator(inputChannel = SEND_OUTBOUND_REQUEST_SERVICE_X)
    public HttpRequestExecutingMessageHandler outboundRequestServiceXHandler(
            IntegrationParameters integrationParameters,
            @Qualifier(STORE_OUTBOUND_RESPONSE_SERVICE_X) MessageChannel outChannel,
            MappingJackson2HttpMessageConverter toJsonConverter) {

        HttpRequestExecutingMessageHandler handler =
                new HttpRequestExecutingMessageHandler(integrationParameters.getServiceXUrl());
        handler.setHttpMethod(HttpMethod.POST);
        handler.setOutputChannel(outChannel);

        HeaderMapper<HttpHeaders> mapper = new DefaultHttpHeaderMapper();
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.add(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);
        mapper.toHeaders(httpHeaders);
        handler.setHeaderMapper(mapper);


        return handler;
    }

    @Bean
    @InboundChannelAdapter(value = TRIGGER_SERVICE_A_REQUEST, poller = @Poller(fixedDelay = "${integration.serviceA.pollerPeriod}"))
    public MessageSource<String> serviceARequestTrigger() {
        return new ExpressionEvaluatingMessageSource<>(new LiteralExpression(""), String.class);
    }

    @Bean
    @InboundChannelAdapter(value = TRIGGER_SERVICE_B_REQUEST, poller = @Poller(fixedDelay = "${integration.serviceB.pollerPeriod}"))
    public MessageSource<String> serviceBRequestTrigger() {
        return new ExpressionEvaluatingMessageSource<>(new LiteralExpression(""), String.class);
    }
}

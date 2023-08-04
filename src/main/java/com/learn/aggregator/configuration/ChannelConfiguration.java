package com.learn.aggregator.configuration;

import com.learn.aggregator.dto.InboundMessageDto;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.expression.common.LiteralExpression;
import org.springframework.http.HttpMethod;
import org.springframework.http.converter.xml.MappingJackson2XmlHttpMessageConverter;
import org.springframework.integration.aggregator.CorrelationStrategy;
import org.springframework.integration.aggregator.ReleaseStrategy;
import org.springframework.integration.annotation.InboundChannelAdapter;
import org.springframework.integration.annotation.Poller;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.integration.channel.QueueChannel;
import org.springframework.integration.core.MessageSource;
import org.springframework.integration.endpoint.ExpressionEvaluatingMessageSource;
import org.springframework.integration.http.outbound.HttpRequestExecutingMessageHandler;
import org.springframework.integration.store.SimpleMessageStore;
import org.springframework.messaging.MessageChannel;

import java.util.List;

@Configuration
@AllArgsConstructor
public class ChannelConfiguration {
    public static final String AGGREGATE_MESSAGES = "aggregateMessages";
    public static final String SERVICE_A_REQUEST_TRIGGER = "serviceARequestTrigger";
    public static final String SERVICE_B_REQUEST_TRIGGER = "serviceBRequestTrigger";
    public static final String SEND_OUTBOUND_REQUEST_SERVICE_A = "sendOutboundRequestServiceA";
    public static final String SEND_OUTBOUND_REQUEST_SERVICE_B = "sendOutboundRequestServiceB";
    public static final String PREPARE_OUTBOUND_REQUEST_SERVICE_X = "prepareOutboundRequestServiceX";
    public static final String SEND_OUTBOUND_REQUEST_SERVICE_X = "sendOutboundRequestServiceX";


    @Bean
    @Qualifier("serviceARequestChannel")
    public MessageChannel serviceARequestChannel() {
        return new QueueChannel();
    }

    @Bean
    @Qualifier("serviceBRequestChannel")
    public MessageChannel serviceBRequestChannel() {
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

    @Bean(name = AGGREGATE_MESSAGES)
    public QueueChannel sendToAggregator() {
        return new QueueChannel();
    }

    @Bean
    @ServiceActivator(inputChannel = "serviceARequestChannel", poller = @Poller(fixedDelay = "10000"))
    public HttpRequestExecutingMessageHandler outboundRequestServiceAHandler(
//            @Qualifier("taskSchedulerServiceA") ThreadPoolTaskScheduler taskScheduler,
                                                                             @Qualifier(SEND_OUTBOUND_REQUEST_SERVICE_A) MessageChannel outChannel,
                                                                             IntegrationParameters integrationParameters) {
        HttpRequestExecutingMessageHandler handler =
                new HttpRequestExecutingMessageHandler(integrationParameters.getServiceAUrl());
        handler.setHttpMethod(HttpMethod.GET);
        handler.setExpectedResponseType(InboundMessageDto.class);
        //handler.setTaskScheduler(taskScheduler);
        handler.setOutputChannel(outChannel);
        return handler;
    }

    @Bean
    @ServiceActivator(inputChannel = "serviceBRequestChannel", poller = @Poller(fixedDelay = "10000"))
    public HttpRequestExecutingMessageHandler outboundRequestServiceBHandler(
//            @Qualifier("taskSchedulerServiceB") ThreadPoolTaskScheduler taskScheduler,
                                                                             @Qualifier(SEND_OUTBOUND_REQUEST_SERVICE_B) MessageChannel outChannel,
                                                                             IntegrationParameters integrationParameters) {
        HttpRequestExecutingMessageHandler handler =
                new HttpRequestExecutingMessageHandler(integrationParameters.getServiceBUrl());
        handler.setHttpMethod(HttpMethod.GET);
        handler.setMessageConverters(List.of(new MappingJackson2XmlHttpMessageConverter()));
        handler.setExpectedResponseType(InboundMessageDto.class);
        handler.setOutputChannel(outChannel);
        //handler.setTaskScheduler(taskScheduler);
        return handler;
    }

    @Bean(name = SEND_OUTBOUND_REQUEST_SERVICE_X)
    public HttpRequestExecutingMessageHandler sendOutboundRequestServiceX(IntegrationParameters integrationParameters) {
        HttpRequestExecutingMessageHandler handler =
                new HttpRequestExecutingMessageHandler(integrationParameters.getServiceXUrl());
        handler.setHttpMethod(HttpMethod.POST);
        handler.setExpectedResponseType(InboundMessageDto.class);
        return handler;
    }

//    @Bean(name = "taskSchedulerServiceA")
//    public ThreadPoolTaskScheduler taskSchedulerServiceA(IntegrationParameters integrationParameters) {
//        ThreadPoolTaskScheduler taskScheduler = new ThreadPoolTaskScheduler();
//        taskScheduler.setPoolSize(integrationParameters.getPollerPeriodServiceA());
//        return taskScheduler;
//    }
//
//    @Bean(name = "taskSchedulerServiceB")
//    public ThreadPoolTaskScheduler taskSchedulerServiceB(IntegrationParameters integrationParameters) {
//        ThreadPoolTaskScheduler taskScheduler = new ThreadPoolTaskScheduler();
//        taskScheduler.setPoolSize(integrationParameters.getPollerPeriodServiceB());
//        return taskScheduler;
//    }

    @Bean
    public CorrelationStrategy correlationStrategy() {
        return message -> ((InboundMessageDto) message.getPayload()).getId();
    }

    @Bean
    public ReleaseStrategy releaseStrategy(IntegrationParameters integrationParameters) {
        return group -> group.size() == integrationParameters.getGroupLimit();
    }

    @Bean
    public SimpleMessageStore messageStore() {
        return new SimpleMessageStore();
    }

    @Bean
    @InboundChannelAdapter(value = "serviceARequestChannel", poller = @Poller(fixedDelay = "10000"))
    public MessageSource<String> serviceARequestTrigger() {
        return new ExpressionEvaluatingMessageSource<>(new LiteralExpression(""), String.class);
    }

    @Bean
    @InboundChannelAdapter(value = "serviceBRequestChannel", poller = @Poller(fixedDelay = "10000"))
    public MessageSource<String> serviceBRequestTrigger() {
        return new ExpressionEvaluatingMessageSource<>(new LiteralExpression(""), String.class);
    }


}

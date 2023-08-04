package com.learn.aggregator.configuration;

import com.learn.aggregator.processor.AggregatedMessagesProcessor;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.aggregator.CorrelationStrategy;
import org.springframework.integration.aggregator.ReleaseStrategy;
import org.springframework.integration.dsl.IntegrationFlow;
import org.springframework.integration.store.SimpleMessageStore;

@Configuration
@RequiredArgsConstructor
public class IntegrationFlowConfiguration {

    @Bean
    public IntegrationFlow readFromServiceA() {
        return IntegrationFlow
                .from(ChannelConfiguration.SERVICE_A_REQUEST_TRIGGER)
                .log(IntegrationFlowConfiguration.class.getName(), m -> ">>> Outbound A flow: " + m.getPayload())
                .channel(ChannelConfiguration.AGGREGATE_MESSAGES)
                .get();
    }

    @Bean
    public IntegrationFlow readFromServiceB() {
        return IntegrationFlow
                .from(ChannelConfiguration.SEND_OUTBOUND_REQUEST_SERVICE_B)
                .log(IntegrationFlowConfiguration.class.getName(), m -> ">>> Outbound B flow: " + m.getPayload())
                .channel(ChannelConfiguration.AGGREGATE_MESSAGES)
                .get();
    }

    @Bean
    public IntegrationFlow sendToServiceX() {
        return IntegrationFlow
                .from(ChannelConfiguration.PREPARE_OUTBOUND_REQUEST_SERVICE_X)
                .log(IntegrationFlowConfiguration.class.getName(), m -> ">>> Outbound X flow: " + m.getPayload())
                .channel(ChannelConfiguration.SEND_OUTBOUND_REQUEST_SERVICE_X)
                .get();
    }

    @Bean
    public IntegrationFlow aggregationFlow(SimpleMessageStore messageStore,
                                           CorrelationStrategy correlationStrategy,
                                           AggregatedMessagesProcessor aggregatedMessagesProcessor,
                                           ReleaseStrategy releaseStrategy) {
        return IntegrationFlow.from(ChannelConfiguration.AGGREGATE_MESSAGES)
                .log(IntegrationFlowConfiguration.class.getName(), m -> ">>> Aggregation flow: " + m.getPayload())
                .aggregate(a -> a.releaseStrategy(releaseStrategy)
                        .messageStore(messageStore)
                        .correlationStrategy(correlationStrategy)
                        .outputProcessor(aggregatedMessagesProcessor)
                        .expireGroupsUponCompletion(true))
                .channel(ChannelConfiguration.PREPARE_OUTBOUND_REQUEST_SERVICE_X)
                .get();
    }
}

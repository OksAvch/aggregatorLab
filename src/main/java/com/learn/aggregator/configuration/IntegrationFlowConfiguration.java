package com.learn.aggregator.configuration;

import com.learn.aggregator.processor.AggregatedMessagesProcessor;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.aggregator.CorrelationStrategy;
import org.springframework.integration.dsl.IntegrationFlow;
import org.springframework.integration.store.SimpleMessageStore;

@Configuration
@RequiredArgsConstructor
public class IntegrationFlowConfiguration {

    private IntegrationParameters integrationParameters;

    @Bean
    public IntegrationFlow readFromServiceA() {
        return IntegrationFlow
                .from(ChannelConfiguration.SEND_OUTBOUND_REQUEST_SERVICE_A)
                .channel(ChannelConfiguration.AGGREGATE_MESSAGES)
                .get();
    }

    @Bean
    public IntegrationFlow readFromServiceB() {
        return IntegrationFlow
                .from(ChannelConfiguration.SEND_OUTBOUND_REQUEST_SERVICE_B)
                .channel(ChannelConfiguration.AGGREGATE_MESSAGES)
                .get();
    }

    @Bean
    public IntegrationFlow sendToServiceX() {
        return IntegrationFlow
                .from(ChannelConfiguration.PREPARE_OUTBOUND_REQUEST_SERVICE_X)
                .channel(ChannelConfiguration.SEND_OUTBOUND_REQUEST_SERVICE_X)
                .get();
    }

    @Bean
    public IntegrationFlow aggregationFlow(SimpleMessageStore messageStore,
                                           CorrelationStrategy correlationStrategy,
                                           AggregatedMessagesProcessor aggregatedMessagesProcessor) {
        return IntegrationFlow.from(ChannelConfiguration.AGGREGATE_MESSAGES)
                .log(IntegrationFlowConfiguration.class.getName(), m -> ">>> Aggregation flow: " + m.getPayload())
                .aggregate(a -> a.releaseStrategy(g -> g.size() == integrationParameters.getGroupLimit())
                        .messageStore(messageStore)
                        .correlationStrategy(correlationStrategy)
                        .outputProcessor(aggregatedMessagesProcessor)
                        .expireGroupsUponCompletion(true))
                .channel(ChannelConfiguration.PREPARE_OUTBOUND_REQUEST_SERVICE_X)
                .get();
    }
}

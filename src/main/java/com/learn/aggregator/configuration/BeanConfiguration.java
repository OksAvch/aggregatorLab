package com.learn.aggregator.configuration;

import com.learn.aggregator.dto.InboundMessageDto;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.aggregator.CorrelationStrategy;
import org.springframework.integration.aggregator.ReleaseStrategy;
import org.springframework.integration.json.ObjectToJsonTransformer;
import org.springframework.integration.store.SimpleMessageStore;

@Configuration
public class BeanConfiguration {

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
    public ObjectToJsonTransformer jsonTransformer() {
        return new ObjectToJsonTransformer();
    }
}

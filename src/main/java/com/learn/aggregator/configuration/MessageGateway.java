package com.learn.aggregator.configuration;

import com.learn.aggregator.dto.InboundMessageDto;
import org.springframework.integration.annotation.Gateway;
import org.springframework.integration.annotation.MessagingGateway;

@MessagingGateway
public interface MessageGateway {

    @Gateway(requestChannel = ChannelConfiguration.AGGREGATE_MESSAGES)
    void aggregateMessages(InboundMessageDto outboundMessageDto);

    @Gateway(requestChannel = ChannelConfiguration.SERVICE_A_REQUEST_TRIGGER)
    void sendOutboundRequestServiceA(InboundMessageDto outboundMessageDto);
}

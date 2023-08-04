package com.learn.aggregator.configuration;

import com.learn.aggregator.dto.InboundMessageDto;
import org.springframework.integration.annotation.Gateway;
import org.springframework.integration.annotation.MessagingGateway;

@MessagingGateway
public interface MessageGateway {

    @Gateway(requestChannel = ChannelConfiguration.AGGREGATE_MESSAGES)
    void aggregateMessages(InboundMessageDto outboundMessageDto);

    @Gateway(requestChannel = ChannelConfiguration.SEND_OUTBOUND_REQUEST_SERVICE_A)
    void sendOutboundRequestServiceA(InboundMessageDto outboundMessageDto);

    @Gateway(requestChannel = ChannelConfiguration.SEND_OUTBOUND_REQUEST_SERVICE_B)
    void sendOutboundRequestServiceB(InboundMessageDto outboundMessageDto);
}

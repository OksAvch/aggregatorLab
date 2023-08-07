package com.learn.aggregator.processor;

import com.learn.aggregator.dto.InboundMessageDto;
import com.learn.aggregator.dto.OutboundMessageDto;
import org.junit.jupiter.api.Test;
import org.springframework.integration.store.MessageGroup;
import org.springframework.integration.store.SimpleMessageGroup;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.GenericMessage;

import java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThat;


class AggregatedMessagesProcessorTest {
    private static final String ID = "id";
    private static final String SUCCESS_STATUS = "success";
    private static final String FAILURE_STATUS = "failure";
    private static final String REASON_1 = "Reason 1";
    private static final String REASON_2 = "Reason 2";

    AggregatedMessagesProcessor testInstance = new AggregatedMessagesProcessor();

    @Test
    void testSuccessfulMessageProcessing() {
        //given
        String id = "successTest";
        MessageGroup group = new SimpleMessageGroup(Arrays.<Message<?>>asList(
                new GenericMessage<>(new InboundMessageDto(id, SUCCESS_STATUS, null)),
                new GenericMessage<>(new InboundMessageDto(id, SUCCESS_STATUS, null))
        ), id);

        //when
        OutboundMessageDto result = testInstance.processMessageGroup(group);

        //then
        assertThat(result.getId()).isEqualTo(id);
        assertThat(result.getStatus()).isEqualTo(SUCCESS_STATUS);
        assertThat(result.getReasons()).isEmpty();
    }

    @Test
    void testFullFailureMessageProcessing() {
        //given
        MessageGroup group = new SimpleMessageGroup(Arrays.<Message<?>>asList(
                new GenericMessage<>(new InboundMessageDto(ID, FAILURE_STATUS, REASON_1)),
                new GenericMessage<>(new InboundMessageDto(ID, FAILURE_STATUS, REASON_2))
        ), ID);

        //when
        OutboundMessageDto result = testInstance.processMessageGroup(group);

        //then
        assertThat(result.getId()).isEqualTo(ID);
        assertThat(result.getStatus()).isEqualTo(FAILURE_STATUS);
        assertThat(result.getReasons()).containsExactlyInAnyOrder(REASON_1, REASON_2);
    }

    @Test
    void testPartialFailureMessageProcessing() {
        //given
        MessageGroup group = new SimpleMessageGroup(Arrays.<Message<?>>asList(
                new GenericMessage<>(new InboundMessageDto(ID, SUCCESS_STATUS, null)),
                new GenericMessage<>(new InboundMessageDto(ID, FAILURE_STATUS, REASON_2))
        ), ID);

        //when
        OutboundMessageDto result = testInstance.processMessageGroup(group);

        //then
        assertThat(result.getId()).isEqualTo(ID);
        assertThat(result.getStatus()).isEqualTo(FAILURE_STATUS);
        assertThat(result.getReasons()).containsExactlyInAnyOrder(REASON_2);
    }

}
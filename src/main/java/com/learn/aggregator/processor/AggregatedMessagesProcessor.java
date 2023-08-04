package com.learn.aggregator.processor;

import com.learn.aggregator.dto.InboundMessageDto;
import com.learn.aggregator.dto.OutboundMessageDto;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.log4j.Log4j2;
import org.springframework.integration.aggregator.MessageGroupProcessor;
import org.springframework.integration.store.MessageGroup;
import org.springframework.messaging.support.GenericMessage;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Log4j2
@Component
@AllArgsConstructor
public class AggregatedMessagesProcessor implements MessageGroupProcessor {

    private static final String SUCCESS_STATUS = "success";
    private static final String FAILURE_STATUS = "failure";

    @Override
    public Object processMessageGroup(MessageGroup group) {
        log.info("Message group '{}' received, size: {}", group.getGroupId(), group.size());

        List<String> reasons = group.getMessages().stream()
                .map(m -> ((InboundMessageDto) m.getPayload()).getReason())
                .collect(Collectors.toList());
        boolean isSuccessful = group.getMessages().stream()
                .map(m -> ((InboundMessageDto) m.getPayload()))
                .map(InboundMessageDto::getStatus)
                .allMatch(status -> status.equals(SUCCESS_STATUS));
        String status = isSuccessful ? SUCCESS_STATUS : FAILURE_STATUS;

        return new GenericMessage<>(new OutboundMessageDto(group.getGroupId().toString(), status, reasons));
    }
}

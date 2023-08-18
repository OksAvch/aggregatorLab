package com.learn.aggregator.processor;

import com.learn.aggregator.dto.InboundMessageDto;
import com.learn.aggregator.dto.OutboundMessageDto;
import lombok.AllArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.integration.aggregator.MessageGroupProcessor;
import org.springframework.integration.store.MessageGroup;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Objects;

@Log4j2
@Component
@AllArgsConstructor
public class AggregatedMessagesProcessor implements MessageGroupProcessor {

    private static final String SUCCESS_STATUS = "success";
    private static final String FAILURE_STATUS = "failure";

    @Override
    public OutboundMessageDto processMessageGroup(MessageGroup group) {
        log.info("Message group '{}' received, size: {}", group.getGroupId(), group.size());

        String status = getGroupStatus(group);
        List<String> reasons = getFailureReasons(group);

        return new OutboundMessageDto(group.getGroupId().toString(), status, reasons);
    }

    private static String getGroupStatus(MessageGroup group) {
        boolean isSuccessful = group.getMessages().stream()
                .map(m -> ((InboundMessageDto) m.getPayload()))
                .map(InboundMessageDto::getStatus)
                .allMatch(status -> status.equals(SUCCESS_STATUS));
        return isSuccessful ? SUCCESS_STATUS : FAILURE_STATUS;
    }

    private static List<String> getFailureReasons(MessageGroup group) {
        return group.getMessages().stream()
                .map(m -> ((InboundMessageDto) m.getPayload()).getReason())
                .filter(Objects::nonNull)
                .toList();
    }
}

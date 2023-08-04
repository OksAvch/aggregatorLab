package com.learn.aggregator.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class InboundMessageDto {

    private String id;
    private String status;
    private String reason;
}

package com.learn.aggregator.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OutboundMessageDto {

    private String id;
    private String status;
    private List<String> reasons;
}

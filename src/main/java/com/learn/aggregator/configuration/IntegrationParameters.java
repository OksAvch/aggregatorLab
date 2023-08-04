package com.learn.aggregator.configuration;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Getter
@Component
public class IntegrationParameters {

    @Value("${integration.groupLimit}")
    private int groupLimit;
    @Value("${integration.serviceA.url}")
    private String serviceAUrl;
    @Value("${integration.serviceA.pollerPeriod}")
    private int pollerPeriodServiceA;
    @Value("${integration.serviceB.url}")
    private String serviceBUrl;
    @Value("${integration.serviceB.pollerPeriod}")
    private int pollerPeriodServiceB;
}

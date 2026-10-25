package com.hungvers.services.catalog.common.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;
import java.time.Duration;

@Configuration
public class TimeConfig {
    public static final Clock CLOCK = Clock.tick(Clock.systemUTC(), Duration.ofNanos(1000));

    @Bean
    public Clock clock() {
        return CLOCK;
    }
}

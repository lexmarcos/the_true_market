package com.thetruemarket.api.infrastructure.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Configuration to enable Spring scheduling for periodic tasks
 * Required for @Scheduled annotations to work
 */
@Configuration
@EnableScheduling
public class SchedulingConfig {
    // No additional configuration needed
    // @EnableScheduling enables all @Scheduled methods
}

package com.thetruemarket.api.infrastructure.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration properties for price history management
 */
@Configuration
@ConfigurationProperties(prefix = "history.update")
@Getter
@Setter
public class HistoryConfig {
    /**
     * Number of seconds after which price history is considered outdated
     * Default: 30 seconds
     */
    private int expirationSeconds = 30;
}

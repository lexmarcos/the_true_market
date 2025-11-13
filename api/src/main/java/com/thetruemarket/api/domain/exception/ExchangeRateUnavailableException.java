package com.thetruemarket.api.domain.exception;

/**
 * Exception thrown when exchange rate cannot be obtained from the API
 * and the cache is too old or unavailable.
 * Domain exception following Clean Architecture principles.
 */
public class ExchangeRateUnavailableException extends RuntimeException {

    public ExchangeRateUnavailableException(String message) {
        super(message);
    }

    public ExchangeRateUnavailableException(String message, Throwable cause) {
        super(message, cause);
    }
}

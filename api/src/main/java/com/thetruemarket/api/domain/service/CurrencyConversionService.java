package com.thetruemarket.api.domain.service;

/**
 * Service port for currency conversion
 * Interface following Dependency Inversion Principle (SOLID)
 */
public interface CurrencyConversionService {
    /**
     * Converts an amount from BRL to USD
     *
     * @param amountInBrl The amount in Brazilian Reais (cents)
     * @return The amount in US Dollars (cents)
     */
    Long convertBrlToUsd(Long amountInBrl);
}

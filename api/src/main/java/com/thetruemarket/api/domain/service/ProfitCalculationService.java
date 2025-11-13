package com.thetruemarket.api.domain.service;

import com.thetruemarket.api.domain.model.ProfitResult;

/**
 * Service port for profit calculation
 * Interface following Dependency Inversion Principle (SOLID)
 */
public interface ProfitCalculationService {
    /**
     * Calculates profit metrics for a skin based on market and Steam prices
     *
     * @param marketPrice The market price in cents
     * @param currency The currency of the market price (e.g., "USD", "BRL")
     * @param steamPrice The Steam average price in USD cents
     * @return ProfitResult containing discount, profit, and expected gain
     * @throws IllegalArgumentException if prices are invalid
     */
    ProfitResult calculateProfit(Long marketPrice, String currency, Long steamPrice);
}

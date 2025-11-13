package com.thetruemarket.api.domain.service;

import com.thetruemarket.api.domain.model.ProfitResult;

/**
 * Service port for profit calculation
 * Interface following Dependency Inversion Principle (SOLID)
 * All prices are expected to be in USD cents
 */
public interface ProfitCalculationService {
    /**
     * Calculates profit metrics for a skin based on market and Steam prices
     * Both prices must be in USD cents
     *
     * @param marketPriceUsd The market price in USD cents
     * @param steamPriceUsd The Steam average price in USD cents
     * @return ProfitResult containing discount, profit, and expected gain
     * @throws IllegalArgumentException if prices are invalid
     */
    ProfitResult calculateProfit(Long marketPriceUsd, Long steamPriceUsd);
}

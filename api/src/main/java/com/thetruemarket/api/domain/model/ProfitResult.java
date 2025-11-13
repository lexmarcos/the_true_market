package com.thetruemarket.api.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * Domain value object representing profit calculation results
 * Framework-agnostic pure domain model
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProfitResult {
    /**
     * Discount percentage compared to Steam price in basis points (× 100)
     * Formula: ((steamPrice - marketPrice) / steamPrice) × 100 × 100
     * Example: 1451 represents 14.51%
     */
    private Double discountPercentage;

    /**
     * Net profit percentage after Steam's 15% fee in basis points (× 100)
     * Formula: (discountPercentage/100 - 15%) × 100
     * Example: -49 represents -0.49%
     */
    private Double profitPercentage;

    /**
     * Expected gain in USD cents if sold on Steam
     * Formula: (steamPrice × profitPercentage) / 10000
     */
    private Long expectedGainCents;

    /**
     * Market price converted to USD cents
     */
    private Long marketPriceUsd;

    /**
     * Steam average price in USD cents
     */
    private Long steamPriceUsd;
}

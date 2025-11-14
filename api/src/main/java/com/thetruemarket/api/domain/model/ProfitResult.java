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

    /**
     * Last sale price in USD cents
     */
    private Long lastSalePrice;

    /**
     * Lowest buy order price in USD cents
     */
    private Long lowestBuyOrderPrice;

    /**
     * Net profit percentage compared to last sale price after Steam's 15% fee
     * Formula: ((lastSalePrice - marketPrice) / lastSalePrice - 15%) × 100
     * Example: 873 represents 8.73%
     */
    private Double profitPercentageVsLastSale;

    /**
     * Net profit percentage compared to lowest buy order price after Steam's 15% fee
     * Formula: ((lowestBuyOrderPrice - marketPrice) / lowestBuyOrderPrice - 15%) × 100
     * Example: 1250 represents 12.50%
     */
    private Double profitPercentageVsLowestBuyOrder;
}

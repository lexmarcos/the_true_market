package com.thetruemarket.api.application.dto;

import com.thetruemarket.api.domain.valueobject.Wear;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Application DTO representing profit analysis for a skin
 * Used to transfer data between use cases and controllers
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProfitAnalysis {
    /**
     * Unique identifier of the skin
     */
    private String skinId;

    /**
     * Skin name (e.g., "AK-47 | Redline")
     */
    private String skinName;

    /**
     * Wear category
     */
    private Wear wear;

    /**
     * Market price in cents (original currency)
     */
    private Long marketPrice;

    /**
     * Currency of the market price
     */
    private String marketCurrency;

    /**
     * Market source (e.g., "BITSKINS", "DASHSKINS")
     */
    private String marketSource;

    /**
     * Steam average price in USD cents
     */
    private Long steamAveragePrice;

    /**
     * Discount percentage compared to Steam in basis points (× 100)
     * Example: 1451 represents 14.51%
     */
    private Double discountPercentage;

    /**
     * Net profit percentage after Steam's 15% fee in basis points (× 100)
     * Example: -49 represents -0.49%
     */
    private Double profitPercentage;

    /**
     * Expected gain in USD cents
     */
    private Long expectedGainUsd;

    /**
     * Whether Steam price history exists for this skin
     */
    private Boolean hasHistory;

    /**
     * When the skin data was last updated
     */
    private LocalDateTime lastUpdated;
}

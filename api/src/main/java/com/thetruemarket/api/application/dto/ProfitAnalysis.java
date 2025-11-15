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
     * Float value in cents (× 10000)
     * Example: 0.1234 becomes 1234, frontend divides by 10000 to get 0.1234
     */
    private Long floatValueCents;

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
     * Direct link to the item on the marketplace
     */
    private String link;

    /**
     * URL of the skin image
     */
    private String imageUrl;

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
     * Last sale price in USD cents
     */
    private Long lastSalePrice;

    /**
     * Lowest buy order price in USD cents
     */
    private Long lowestBuyOrderPrice;

    /**
     * Net profit percentage compared to last sale price after Steam's 15% fee
     * Example: 873 represents 8.73%
     */
    private Double profitPercentageVsLastSale;

    /**
     * Net profit percentage compared to lowest buy order price after Steam's 15% fee
     * Example: 1250 represents 12.50%
     */
    private Double profitPercentageVsLowestBuyOrder;

    /**
     * Whether Steam price history exists for this skin
     */
    private Boolean hasHistory;

    /**
     * When the skin data was last updated
     */
    private LocalDateTime lastUpdated;
}

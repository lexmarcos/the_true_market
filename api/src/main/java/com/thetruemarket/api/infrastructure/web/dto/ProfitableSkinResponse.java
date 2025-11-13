package com.thetruemarket.api.infrastructure.web.dto;

import com.thetruemarket.api.application.dto.ProfitAnalysis;
import com.thetruemarket.api.domain.valueobject.Wear;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Response DTO for profitable skin information
 * Used in REST API responses for profit analysis endpoints
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProfitableSkinResponse {
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

    /**
     * Creates a response DTO from a ProfitAnalysis application DTO
     *
     * @param profitAnalysis The profit analysis from the use case
     * @return Response DTO ready for REST API
     */
    public static ProfitableSkinResponse fromApplication(ProfitAnalysis profitAnalysis) {
        return ProfitableSkinResponse.builder()
                .skinId(profitAnalysis.getSkinId())
                .skinName(profitAnalysis.getSkinName())
                .wear(profitAnalysis.getWear())
                .marketPrice(profitAnalysis.getMarketPrice())
                .marketCurrency(profitAnalysis.getMarketCurrency())
                .marketSource(profitAnalysis.getMarketSource())
                .steamAveragePrice(profitAnalysis.getSteamAveragePrice())
                .discountPercentage(profitAnalysis.getDiscountPercentage())
                .profitPercentage(profitAnalysis.getProfitPercentage())
                .expectedGainUsd(profitAnalysis.getExpectedGainUsd())
                .hasHistory(profitAnalysis.getHasHistory())
                .lastUpdated(profitAnalysis.getLastUpdated())
                .build();
    }
}

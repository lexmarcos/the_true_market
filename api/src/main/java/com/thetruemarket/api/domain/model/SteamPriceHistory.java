package com.thetruemarket.api.domain.model;

import com.thetruemarket.api.domain.valueobject.Wear;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Domain entity representing Steam price history for a specific skin and wear combination
 * Framework-agnostic pure domain model
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SteamPriceHistory {
    /**
     * Unique identifier
     */
    private Long id;

    /**
     * Reference to the skin ID
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
     * Average price from recent Steam sales in USD (cents)
     */
    private Long averagePrice;

    /**
     * Price of the last sale in USD (cents)
     */
    private Long lastSalePrice;

    /**
     * Price of the lowest buy order in USD (cents)
     */
    private Long lowestBuyOrderPrice;

    /**
     * When this price data was recorded
     */
    private LocalDateTime recordedAt;

    /**
     * When this record was created
     */
    private LocalDateTime createdAt;

    /**
     * Creates a new price history record
     */
    public static SteamPriceHistory create(String skinId, String skinName, Wear wear, Long averagePrice,
                                          Long lastSalePrice, Long lowestBuyOrderPrice) {
        LocalDateTime now = LocalDateTime.now();

        return SteamPriceHistory.builder()
                .skinId(skinId)
                .skinName(skinName)
                .wear(wear)
                .averagePrice(averagePrice)
                .lastSalePrice(lastSalePrice)
                .lowestBuyOrderPrice(lowestBuyOrderPrice)
                .recordedAt(now)
                .createdAt(now)
                .build();
    }
}

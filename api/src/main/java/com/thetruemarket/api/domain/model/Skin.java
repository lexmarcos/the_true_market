package com.thetruemarket.api.domain.model;

import com.thetruemarket.api.domain.valueobject.Wear;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Domain entity representing a CS2 Skin
 * Framework-agnostic pure domain model
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Skin {
    /**
     * Unique identifier from the market source
     */
    private String id;

    /**
     * Skin name (e.g., "AK-47 | Redline")
     */
    private String name;

    /**
     * Asset identifier
     */
    private String assetId;

    /**
     * Float value determining wear condition (0.00 - 1.00)
     * Can be null - wear will be extracted from name in this case
     */
    private Double floatValue;

    /**
     * Wear category calculated from float value or extracted from name
     * Always present to allow linking with price history
     */
    private Wear wear;

    /**
     * Paint seed number
     */
    private Integer paintSeed;

    /**
     * Paint index
     */
    private Integer paintIndex;

    /**
     * Stickers attached to the skin
     */
    private List<Sticker> stickers;

    /**
     * Number of stickers
     */
    private Integer stickerCount;

    /**
     * Market price in cents
     * Can be null if skin came without price information
     */
    private Long price;

    /**
     * Currency of the price (e.g., "USD", "BRL")
     * Can be null if price is null
     */
    private String currency;

    /**
     * Market source where the skin was listed (e.g., "BITSKINS", "DASHSKINS")
     * Can be null if not from a market listing
     */
    private String marketSource;

    /**
     * Direct link to the item on the marketplace
     * Can be null if not applicable
     */
    private String link;

    /**
     * When the skin was first saved
     */
    private LocalDateTime createdAt;

    /**
     * When the skin data was last updated
     */
    private LocalDateTime updatedAt;

    /**
     * Creates a Skin instance and automatically determines the wear category
     * If floatValue is present, wear is calculated from it
     * If floatValue is null, wear is extracted from skin name (e.g., "AK-47 | Redline (Factory New)")
     *
     * @param id The unique identifier
     * @param name The skin name (must contain wear in parentheses if floatValue is null)
     * @param assetId The asset identifier
     * @param floatValue The float value (can be null)
     * @param paintSeed The paint seed
     * @param paintIndex The paint index
     * @param stickers The list of stickers
     * @param stickerCount The number of stickers
     * @param price The market price in cents (can be null)
     * @param currency The currency of the price (can be null)
     * @param marketSource The market source (can be null)
     * @param link The direct link to the item (can be null)
     * @return A new Skin instance with wear determined
     * @throws IllegalArgumentException if wear cannot be determined from either floatValue or name
     */
    public static Skin create(String id, String name, String assetId, Double floatValue,
                              Integer paintSeed, Integer paintIndex,
                              List<Sticker> stickers, Integer stickerCount,
                              Long price, String currency, String marketSource, String link) {
        Wear calculatedWear;

        // If floatValue is present, calculate wear from it
        if (floatValue != null) {
            calculatedWear = Wear.fromFloatValue(floatValue);
        } else {
            // If floatValue is null, extract wear from name
            calculatedWear = Wear.fromSkinName(name);
        }

        LocalDateTime now = LocalDateTime.now();

        return Skin.builder()
                .id(id)
                .name(name)
                .assetId(assetId)
                .floatValue(floatValue)
                .wear(calculatedWear)
                .paintSeed(paintSeed)
                .paintIndex(paintIndex)
                .stickers(stickers)
                .stickerCount(stickerCount)
                .price(price)
                .currency(currency)
                .marketSource(marketSource)
                .link(link)
                .createdAt(now)
                .updatedAt(now)
                .build();
    }
}

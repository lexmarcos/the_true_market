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
     */
    private Double floatValue;

    /**
     * Wear category calculated from float value
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
     * When the skin was first saved
     */
    private LocalDateTime createdAt;

    /**
     * When the skin data was last updated
     */
    private LocalDateTime updatedAt;

    /**
     * Creates a Skin instance and automatically calculates the wear category
     */
    public static Skin create(String id, String name, String assetId, Double floatValue,
                              Integer paintSeed, Integer paintIndex,
                              List<Sticker> stickers, Integer stickerCount) {
        Wear calculatedWear = Wear.fromFloatValue(floatValue);
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
                .createdAt(now)
                .updatedAt(now)
                .build();
    }
}

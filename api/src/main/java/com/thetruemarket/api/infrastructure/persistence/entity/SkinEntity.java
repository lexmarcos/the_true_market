package com.thetruemarket.api.infrastructure.persistence.entity;

import com.thetruemarket.api.domain.valueobject.SkinStatus;
import com.thetruemarket.api.domain.valueobject.Wear;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * JPA Entity for Skin persistence
 * Infrastructure layer implementation
 */
@Entity
@Table(name = "skins")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SkinEntity {
    /**
     * Unique identifier from the market source
     */
    @Id
    @Column(name = "id", nullable = false, length = 255)
    private String id;

    /**
     * Skin name (e.g., "AK-47 | Redline")
     */
    @Column(name = "name", nullable = false, length = 500)
    private String name;

    /**
     * Asset identifier
     */
    @Column(name = "asset_id", length = 255)
    private String assetId;

    /**
     * Float value determining wear condition (0.00 - 1.00)
     * Can be null - wear will be extracted from name in this case
     */
    @Column(name = "float_value", nullable = true)
    private Double floatValue;

    /**
     * Wear category calculated from float value or extracted from name
     * Always present to allow linking with price history
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "wear", nullable = false, length = 50)
    private Wear wear;

    /**
     * Paint seed number
     */
    @Column(name = "paint_seed")
    private Integer paintSeed;

    /**
     * Paint index
     * Can be null when not available
     */
    @Column(name = "paint_index")
    private Integer paintIndex;

    /**
     * Number of stickers (sticker details stored separately if needed)
     */
    @Column(name = "sticker_count")
    private Integer stickerCount;

    /**
     * Market price in cents
     * Can be null if skin came without price information
     */
    @Column(name = "price", nullable = true)
    private Long price;

    /**
     * Currency of the price (e.g., "USD", "BRL")
     * Can be null if price is null
     */
    @Column(name = "currency", nullable = true, length = 3)
    private String currency;

    /**
     * Market source where the skin was listed (e.g., "BITSKINS", "DASHSKINS")
     * Can be null if not from a market listing
     */
    @Column(name = "market_source", nullable = true, length = 50)
    private String marketSource;

    /**
     * Direct link to the item on the marketplace
     * Can be null if not applicable
     */
    @Column(name = "link", nullable = true, length = 1000)
    private String link;

    /**
     * Relationship to skin image (1:1 via skin name)
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "name", referencedColumnName = "skin_name", insertable = false, updatable = false)
    private SkinImageEntity skinImage;

    /**
     * When the skin was first saved
     */
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    /**
     * When the skin data was last updated
     */
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    /**
     * Last time this skin was seen by a bot (heartbeat timestamp)
     * Used to detect when a skin has been sold
     */
    @Column(name = "last_seen_at", nullable = false)
    private LocalDateTime lastSeenAt;

    /**
     * Current availability status of the skin
     * AVAILABLE: Currently listed in marketplace
     * SOLD: No longer available (not seen for configured duration)
     * UNAVAILABLE: Temporarily unavailable
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private SkinStatus status;

    @PrePersist
    protected void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        this.createdAt = now;
        this.updatedAt = now;
        this.lastSeenAt = now;
        if (this.status == null) {
            this.status = SkinStatus.AVAILABLE;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        LocalDateTime now = LocalDateTime.now();
        this.updatedAt = now;
        this.lastSeenAt = now;
    }
}

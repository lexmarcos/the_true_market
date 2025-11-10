package com.thetruemarket.api.infrastructure.persistence.entity;

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
     */
    @Column(name = "float_value", nullable = false)
    private Double floatValue;

    /**
     * Wear category calculated from float value
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
     */
    @Column(name = "paint_index")
    private Integer paintIndex;

    /**
     * Number of stickers (sticker details stored separately if needed)
     */
    @Column(name = "sticker_count")
    private Integer stickerCount;

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

    @PrePersist
    protected void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        this.createdAt = now;
        this.updatedAt = now;
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}

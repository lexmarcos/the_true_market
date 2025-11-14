package com.thetruemarket.api.infrastructure.persistence.entity;

import com.thetruemarket.api.domain.valueobject.Wear;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * JPA Entity for Steam Price History persistence
 * Infrastructure layer implementation
 */
@Entity
@Table(name = "steam_price_history", indexes = {
        @Index(name = "idx_skin_name_wear", columnList = "skin_name, wear"),
        @Index(name = "idx_recorded_at", columnList = "recorded_at")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SteamPriceHistoryEntity {
    /**
     * Auto-generated unique identifier
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    /**
     * Reference to the skin ID
     */
    @Column(name = "skin_id", length = 255)
    private String skinId;

    /**
     * Skin name (denormalized for efficient querying)
     */
    @Column(name = "skin_name", nullable = false, length = 500)
    private String skinName;

    /**
     * Wear category
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "wear", nullable = false, length = 50)
    private Wear wear;

    /**
     * Average price from recent Steam sales (in cents)
     */
    @Column(name = "average_price", nullable = false)
    private Long averagePrice;

    /**
     * Price of the last sale in USD (cents)
     */
    @Column(name = "last_sale_price", nullable = false)
    private Long lastSalePrice;

    /**
     * Price of the lowest buy order in USD (cents)
     */
    @Column(name = "lowest_buy_order_price", nullable = false)
    private Long lowestBuyOrderPrice;

    /**
     * When this price data was recorded
     */
    @Column(name = "recorded_at", nullable = false)
    private LocalDateTime recordedAt;

    /**
     * When this record was created
     */
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        this.createdAt = now;
        if (this.recordedAt == null) {
            this.recordedAt = now;
        }
    }
}

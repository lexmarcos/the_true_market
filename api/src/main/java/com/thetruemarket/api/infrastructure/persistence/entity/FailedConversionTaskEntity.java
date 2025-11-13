package com.thetruemarket.api.infrastructure.persistence.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * JPA Entity for FailedConversionTask persistence
 * Infrastructure layer implementation
 */
@Entity
@Table(name = "failed_conversion_tasks")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FailedConversionTaskEntity {
    /**
     * Unique identifier
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    /**
     * Original skin market data (as JSON) to be reprocessed
     */
    @Column(name = "skin_data_json", nullable = false, columnDefinition = "TEXT")
    private String skinDataJson;

    /**
     * Original price before conversion
     */
    @Column(name = "original_price", nullable = false)
    private Long originalPrice;

    /**
     * Original currency (e.g., "BRL")
     */
    @Column(name = "currency", nullable = false, length = 3)
    private String currency;

    /**
     * Skin ID from the original message
     */
    @Column(name = "skin_id", nullable = false, length = 255)
    private String skinId;

    /**
     * Number of retry attempts made
     */
    @Column(name = "attempt_count", nullable = false)
    private Integer attemptCount;

    /**
     * Last error message encountered
     */
    @Column(name = "last_error", nullable = true, columnDefinition = "TEXT")
    private String lastError;

    /**
     * When the task was created
     */
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    /**
     * When the next retry should be attempted
     */
    @Column(name = "next_retry_at", nullable = true)
    private LocalDateTime nextRetryAt;

    /**
     * Whether the task has permanently failed after max attempts
     */
    @Column(name = "permanently_failed", nullable = false)
    private Boolean permanentlyFailed;

    @PrePersist
    protected void onCreate() {
        if (this.createdAt == null) {
            this.createdAt = LocalDateTime.now();
        }
        if (this.attemptCount == null) {
            this.attemptCount = 0;
        }
        if (this.permanentlyFailed == null) {
            this.permanentlyFailed = false;
        }
    }
}

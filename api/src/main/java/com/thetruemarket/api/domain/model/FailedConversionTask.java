package com.thetruemarket.api.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Domain entity representing a failed currency conversion attempt.
 * Used to track and retry conversions that couldn't be completed due to
 * exchange rate API unavailability.
 * Pure domain model following Clean Architecture principles.
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FailedConversionTask {
    /**
     * Unique identifier
     */
    private Long id;

    /**
     * Original skin market data (as JSON) to be reprocessed
     */
    private String skinDataJson;

    /**
     * Original price before conversion
     */
    private Long originalPrice;

    /**
     * Original currency (e.g., "BRL")
     */
    private String currency;

    /**
     * Skin ID from the original message
     */
    private String skinId;

    /**
     * Number of retry attempts made
     */
    private Integer attemptCount;

    /**
     * Last error message encountered
     */
    private String lastError;

    /**
     * When the task was created
     */
    private LocalDateTime createdAt;

    /**
     * When the next retry should be attempted
     */
    private LocalDateTime nextRetryAt;

    /**
     * Whether the task has permanently failed after max attempts
     */
    private Boolean permanentlyFailed;

    /**
     * Creates a new FailedConversionTask with initial values
     *
     * @param skinDataJson JSON representation of SkinMarketData
     * @param originalPrice Price in original currency
     * @param currency Original currency
     * @param skinId Skin identifier
     * @param errorMessage Error message from failed conversion
     * @param nextRetryDelayMinutes Minutes until next retry
     * @return New FailedConversionTask instance
     */
    public static FailedConversionTask create(String skinDataJson, Long originalPrice,
                                               String currency, String skinId,
                                               String errorMessage, int nextRetryDelayMinutes) {
        LocalDateTime now = LocalDateTime.now();
        return FailedConversionTask.builder()
                .skinDataJson(skinDataJson)
                .originalPrice(originalPrice)
                .currency(currency)
                .skinId(skinId)
                .attemptCount(1)
                .lastError(errorMessage)
                .createdAt(now)
                .nextRetryAt(now.plusMinutes(nextRetryDelayMinutes))
                .permanentlyFailed(false)
                .build();
    }

    /**
     * Increments the attempt count and updates next retry time with exponential backoff
     *
     * @param errorMessage New error message
     * @param baseDelayMinutes Base delay in minutes for calculating backoff
     * @param maxAttempts Maximum attempts before marking as permanently failed
     */
    public void incrementAttempt(String errorMessage, int baseDelayMinutes, int maxAttempts) {
        this.attemptCount++;
        this.lastError = errorMessage;

        if (this.attemptCount >= maxAttempts) {
            this.permanentlyFailed = true;
            this.nextRetryAt = null;
        } else {
            // Exponential backoff: delay * 2^(attemptCount - 1)
            int delayMinutes = (int) (baseDelayMinutes * Math.pow(2, attemptCount - 1));
            this.nextRetryAt = LocalDateTime.now().plusMinutes(delayMinutes);
        }
    }
}

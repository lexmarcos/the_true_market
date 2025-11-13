package com.thetruemarket.api.infrastructure.web.dto;

import com.thetruemarket.api.domain.valueobject.Wear;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for completing a history update task
 * Used in POST /api/v1/history-update-tasks/{taskId}/complete
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CompleteHistoryUpdateRequest {
    /**
     * Skin name (e.g., "AK-47 | Redline")
     * Required field
     */
    private String skinName;

    /**
     * Wear category
     * Required field
     */
    private Wear wear;

    /**
     * Average price from recent Steam sales in BRL (Brazilian Reais, in cents)
     * Will be automatically converted to USD before saving
     * Required field, must be positive
     */
    private Long averagePrice;
}

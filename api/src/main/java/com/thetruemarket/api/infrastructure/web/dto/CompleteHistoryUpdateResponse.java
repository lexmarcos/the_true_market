package com.thetruemarket.api.infrastructure.web.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Response DTO for completing a history update task
 * Used in POST /api/v1/history-update-tasks/{taskId}/complete response
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CompleteHistoryUpdateResponse {
    private String message;
    private Long taskId;
    private String skinName;
    private String wear;

    public static CompleteHistoryUpdateResponse success(Long taskId, String skinName, String wear) {
        return CompleteHistoryUpdateResponse.builder()
                .message("History update task completed successfully")
                .taskId(taskId)
                .skinName(skinName)
                .wear(wear)
                .build();
    }
}

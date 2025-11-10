package com.thetruemarket.api.infrastructure.web.dto;

import com.thetruemarket.api.domain.model.HistoryUpdateTask;
import com.thetruemarket.api.domain.valueobject.TaskStatus;
import com.thetruemarket.api.domain.valueobject.Wear;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Response DTO for History Update Task
 * Used in REST API responses
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HistoryUpdateTaskResponse {
    private Long id;
    private String skinName;
    private Wear wear;
    private TaskStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime finishedAt;

    /**
     * Creates a response DTO from a domain entity
     */
    public static HistoryUpdateTaskResponse fromDomain(HistoryUpdateTask task) {
        return HistoryUpdateTaskResponse.builder()
                .id(task.getId())
                .skinName(task.getSkinName())
                .wear(task.getWear())
                .status(task.getStatus())
                .createdAt(task.getCreatedAt())
                .finishedAt(task.getFinishedAt())
                .build();
    }
}

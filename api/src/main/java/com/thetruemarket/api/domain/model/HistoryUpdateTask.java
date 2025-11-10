package com.thetruemarket.api.domain.model;

import com.thetruemarket.api.domain.valueobject.TaskStatus;
import com.thetruemarket.api.domain.valueobject.Wear;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Domain entity representing a task to update Steam price history
 * Framework-agnostic pure domain model
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HistoryUpdateTask {
    /**
     * Unique identifier
     */
    private Long id;

    /**
     * Skin name (e.g., "AK-47 | Redline")
     */
    private String skinName;

    /**
     * Wear category
     */
    private Wear wear;

    /**
     * Current task status
     */
    private TaskStatus status;

    /**
     * When the task was created
     */
    private LocalDateTime createdAt;

    /**
     * When the task was completed (null if still waiting)
     */
    private LocalDateTime finishedAt;

    /**
     * Creates a new waiting task
     */
    public static HistoryUpdateTask createWaiting(String skinName, Wear wear) {
        return HistoryUpdateTask.builder()
                .skinName(skinName)
                .wear(wear)
                .status(TaskStatus.WAITING)
                .createdAt(LocalDateTime.now())
                .finishedAt(null)
                .build();
    }

    /**
     * Marks the task as completed
     */
    public void complete() {
        this.status = TaskStatus.COMPLETED;
        this.finishedAt = LocalDateTime.now();
    }
}

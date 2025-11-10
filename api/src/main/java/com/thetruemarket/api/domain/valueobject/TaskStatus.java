package com.thetruemarket.api.domain.valueobject;

/**
 * Status of a history update task
 */
public enum TaskStatus {
    /**
     * Task created and waiting to be processed
     */
    WAITING,

    /**
     * Task has been completed successfully
     */
    COMPLETED
}

package com.thetruemarket.api.domain.repository;

import com.thetruemarket.api.domain.model.FailedConversionTask;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Repository interface for FailedConversionTask domain entity.
 * Defines contract for persistence operations following Clean Architecture.
 * Implementation will be provided in the Infrastructure layer.
 */
public interface FailedConversionTaskRepository {
    /**
     * Saves a failed conversion task
     *
     * @param task The task to save
     * @return The saved task with generated ID
     */
    FailedConversionTask save(FailedConversionTask task);

    /**
     * Finds all tasks that are ready for retry
     * (nextRetryAt <= now AND permanentlyFailed = false)
     *
     * @param now Current timestamp
     * @return List of tasks ready for retry
     */
    List<FailedConversionTask> findTasksReadyForRetry(LocalDateTime now);

    /**
     * Deletes a task by ID
     *
     * @param id Task ID
     */
    void deleteById(Long id);

    /**
     * Finds all permanently failed tasks
     *
     * @return List of permanently failed tasks
     */
    List<FailedConversionTask> findPermanentlyFailed();
}

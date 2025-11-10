package com.thetruemarket.api.domain.repository;

import com.thetruemarket.api.domain.model.HistoryUpdateTask;
import com.thetruemarket.api.domain.valueobject.TaskStatus;
import com.thetruemarket.api.domain.valueobject.Wear;

import java.util.List;
import java.util.Optional;

/**
 * Repository port for HistoryUpdateTask domain entity
 * Interface following Dependency Inversion Principle (SOLID)
 */
public interface HistoryUpdateTaskRepository {
    /**
     * Saves a history update task to the repository
     *
     * @param task The task to save
     * @return The saved task
     */
    HistoryUpdateTask save(HistoryUpdateTask task);

    /**
     * Finds a task by its ID
     *
     * @param id The task ID
     * @return Optional containing the task if found
     */
    Optional<HistoryUpdateTask> findById(Long id);

    /**
     * Finds all tasks with a specific status, ordered by creation date (FIFO)
     *
     * @param status The status to filter by
     * @return List of tasks ordered by creation date ascending
     */
    List<HistoryUpdateTask> findByStatusOrderByCreatedAtAsc(TaskStatus status);

    /**
     * Checks if a waiting task already exists for a skin name and wear combination
     *
     * @param skinName The skin name
     * @param wear The wear category
     * @return true if a waiting task exists, false otherwise
     */
    boolean existsBySkinNameAndWearAndStatus(String skinName, Wear wear, TaskStatus status);
}

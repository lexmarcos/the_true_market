package com.thetruemarket.api.infrastructure.persistence.repository;

import com.thetruemarket.api.domain.valueobject.TaskStatus;
import com.thetruemarket.api.domain.valueobject.Wear;
import com.thetruemarket.api.infrastructure.persistence.entity.HistoryUpdateTaskEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Spring Data JPA repository for HistoryUpdateTaskEntity
 */
@Repository
public interface HistoryUpdateTaskJpaRepository extends JpaRepository<HistoryUpdateTaskEntity, Long> {
    /**
     * Finds all tasks with a specific status, ordered by creation date (FIFO)
     *
     * @param status The status to filter by
     * @return List of tasks ordered by creation date ascending
     */
    List<HistoryUpdateTaskEntity> findByStatusOrderByCreatedAtAsc(TaskStatus status);

    /**
     * Checks if a task already exists for a skin name, wear, and status combination
     *
     * @param skinName The skin name
     * @param wear The wear category
     * @param status The task status
     * @return true if exists, false otherwise
     */
    boolean existsBySkinNameAndWearAndStatus(String skinName, Wear wear, TaskStatus status);
}

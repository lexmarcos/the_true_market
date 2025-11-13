package com.thetruemarket.api.infrastructure.persistence.repository;

import com.thetruemarket.api.infrastructure.persistence.entity.FailedConversionTaskEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Spring Data JPA repository for FailedConversionTaskEntity
 */
@Repository
public interface JpaFailedConversionTaskRepository extends JpaRepository<FailedConversionTaskEntity, Long> {
    /**
     * Finds all tasks that are ready for retry
     *
     * @param now Current timestamp
     * @return List of tasks ready for retry
     */
    @Query("SELECT t FROM FailedConversionTaskEntity t WHERE t.nextRetryAt <= :now AND t.permanentlyFailed = false")
    List<FailedConversionTaskEntity> findTasksReadyForRetry(@Param("now") LocalDateTime now);

    /**
     * Finds all permanently failed tasks
     *
     * @return List of permanently failed tasks
     */
    List<FailedConversionTaskEntity> findByPermanentlyFailedTrue();
}

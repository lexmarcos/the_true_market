package com.thetruemarket.api.infrastructure.persistence.mapper;

import com.thetruemarket.api.domain.model.FailedConversionTask;
import com.thetruemarket.api.infrastructure.persistence.entity.FailedConversionTaskEntity;
import org.springframework.stereotype.Component;

/**
 * Mapper between FailedConversionTask domain entity and FailedConversionTaskEntity JPA entity
 * Implements the Adapter pattern for persistence layer
 */
@Component
public class FailedConversionTaskMapper {
    /**
     * Converts a domain FailedConversionTask to JPA FailedConversionTaskEntity
     *
     * @param task Domain entity
     * @return JPA entity
     */
    public FailedConversionTaskEntity toEntity(FailedConversionTask task) {
        if (task == null) {
            return null;
        }

        return FailedConversionTaskEntity.builder()
                .id(task.getId())
                .skinDataJson(task.getSkinDataJson())
                .originalPrice(task.getOriginalPrice())
                .currency(task.getCurrency())
                .skinId(task.getSkinId())
                .attemptCount(task.getAttemptCount())
                .lastError(task.getLastError())
                .createdAt(task.getCreatedAt())
                .nextRetryAt(task.getNextRetryAt())
                .permanentlyFailed(task.getPermanentlyFailed())
                .build();
    }

    /**
     * Converts a JPA FailedConversionTaskEntity to domain FailedConversionTask
     *
     * @param entity JPA entity
     * @return Domain entity
     */
    public FailedConversionTask toDomain(FailedConversionTaskEntity entity) {
        if (entity == null) {
            return null;
        }

        return FailedConversionTask.builder()
                .id(entity.getId())
                .skinDataJson(entity.getSkinDataJson())
                .originalPrice(entity.getOriginalPrice())
                .currency(entity.getCurrency())
                .skinId(entity.getSkinId())
                .attemptCount(entity.getAttemptCount())
                .lastError(entity.getLastError())
                .createdAt(entity.getCreatedAt())
                .nextRetryAt(entity.getNextRetryAt())
                .permanentlyFailed(entity.getPermanentlyFailed())
                .build();
    }
}

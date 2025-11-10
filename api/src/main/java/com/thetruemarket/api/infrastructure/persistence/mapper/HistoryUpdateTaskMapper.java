package com.thetruemarket.api.infrastructure.persistence.mapper;

import com.thetruemarket.api.domain.model.HistoryUpdateTask;
import com.thetruemarket.api.infrastructure.persistence.entity.HistoryUpdateTaskEntity;
import org.springframework.stereotype.Component;

/**
 * Mapper between HistoryUpdateTask domain entity and HistoryUpdateTaskEntity JPA entity
 * Implements the Adapter pattern for persistence layer
 */
@Component
public class HistoryUpdateTaskMapper {
    /**
     * Converts a domain HistoryUpdateTask to JPA HistoryUpdateTaskEntity
     *
     * @param task Domain entity
     * @return JPA entity
     */
    public HistoryUpdateTaskEntity toEntity(HistoryUpdateTask task) {
        if (task == null) {
            return null;
        }

        return HistoryUpdateTaskEntity.builder()
                .id(task.getId())
                .skinName(task.getSkinName())
                .wear(task.getWear())
                .status(task.getStatus())
                .createdAt(task.getCreatedAt())
                .finishedAt(task.getFinishedAt())
                .build();
    }

    /**
     * Converts a JPA HistoryUpdateTaskEntity to domain HistoryUpdateTask
     *
     * @param entity JPA entity
     * @return Domain entity
     */
    public HistoryUpdateTask toDomain(HistoryUpdateTaskEntity entity) {
        if (entity == null) {
            return null;
        }

        return HistoryUpdateTask.builder()
                .id(entity.getId())
                .skinName(entity.getSkinName())
                .wear(entity.getWear())
                .status(entity.getStatus())
                .createdAt(entity.getCreatedAt())
                .finishedAt(entity.getFinishedAt())
                .build();
    }
}

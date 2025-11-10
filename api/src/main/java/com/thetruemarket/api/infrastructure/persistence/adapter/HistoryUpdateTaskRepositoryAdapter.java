package com.thetruemarket.api.infrastructure.persistence.adapter;

import com.thetruemarket.api.domain.model.HistoryUpdateTask;
import com.thetruemarket.api.domain.repository.HistoryUpdateTaskRepository;
import com.thetruemarket.api.domain.valueobject.TaskStatus;
import com.thetruemarket.api.domain.valueobject.Wear;
import com.thetruemarket.api.infrastructure.persistence.entity.HistoryUpdateTaskEntity;
import com.thetruemarket.api.infrastructure.persistence.mapper.HistoryUpdateTaskMapper;
import com.thetruemarket.api.infrastructure.persistence.repository.HistoryUpdateTaskJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Adapter implementation of HistoryUpdateTaskRepository using JPA
 * Implements the Adapter/Port pattern for Clean Architecture
 */
@Component
@RequiredArgsConstructor
public class HistoryUpdateTaskRepositoryAdapter implements HistoryUpdateTaskRepository {
    private final HistoryUpdateTaskJpaRepository jpaRepository;
    private final HistoryUpdateTaskMapper mapper;

    @Override
    public HistoryUpdateTask save(HistoryUpdateTask task) {
        HistoryUpdateTaskEntity entity = mapper.toEntity(task);
        HistoryUpdateTaskEntity savedEntity = jpaRepository.save(entity);
        return mapper.toDomain(savedEntity);
    }

    @Override
    public Optional<HistoryUpdateTask> findById(Long id) {
        return jpaRepository.findById(id)
                .map(mapper::toDomain);
    }

    @Override
    public List<HistoryUpdateTask> findByStatusOrderByCreatedAtAsc(TaskStatus status) {
        return jpaRepository.findByStatusOrderByCreatedAtAsc(status).stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public boolean existsBySkinNameAndWearAndStatus(String skinName, Wear wear, TaskStatus status) {
        return jpaRepository.existsBySkinNameAndWearAndStatus(skinName, wear, status);
    }
}

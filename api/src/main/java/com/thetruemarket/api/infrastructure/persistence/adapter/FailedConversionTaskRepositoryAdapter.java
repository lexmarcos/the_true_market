package com.thetruemarket.api.infrastructure.persistence.adapter;

import com.thetruemarket.api.domain.model.FailedConversionTask;
import com.thetruemarket.api.domain.repository.FailedConversionTaskRepository;
import com.thetruemarket.api.infrastructure.persistence.entity.FailedConversionTaskEntity;
import com.thetruemarket.api.infrastructure.persistence.mapper.FailedConversionTaskMapper;
import com.thetruemarket.api.infrastructure.persistence.repository.JpaFailedConversionTaskRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Adapter implementing FailedConversionTaskRepository using JPA
 * Part of the Infrastructure layer (Interface Adapters)
 */
@Component
@RequiredArgsConstructor
public class FailedConversionTaskRepositoryAdapter implements FailedConversionTaskRepository {
    private final JpaFailedConversionTaskRepository jpaRepository;
    private final FailedConversionTaskMapper mapper;

    @Override
    public FailedConversionTask save(FailedConversionTask task) {
        FailedConversionTaskEntity entity = mapper.toEntity(task);
        FailedConversionTaskEntity saved = jpaRepository.save(entity);
        return mapper.toDomain(saved);
    }

    @Override
    public List<FailedConversionTask> findTasksReadyForRetry(LocalDateTime now) {
        return jpaRepository.findTasksReadyForRetry(now).stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public void deleteById(Long id) {
        jpaRepository.deleteById(id);
    }

    @Override
    public List<FailedConversionTask> findPermanentlyFailed() {
        return jpaRepository.findByPermanentlyFailedTrue().stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }
}

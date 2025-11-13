package com.thetruemarket.api.infrastructure.persistence.adapter;

import com.thetruemarket.api.domain.model.Skin;
import com.thetruemarket.api.domain.repository.SkinRepository;
import com.thetruemarket.api.infrastructure.persistence.entity.SkinEntity;
import com.thetruemarket.api.infrastructure.persistence.mapper.SkinMapper;
import com.thetruemarket.api.infrastructure.persistence.repository.SkinJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Adapter implementation of SkinRepository using JPA
 * Implements the Adapter/Port pattern for Clean Architecture
 */
@Component
@RequiredArgsConstructor
public class SkinRepositoryAdapter implements SkinRepository {
    private final SkinJpaRepository jpaRepository;
    private final SkinMapper mapper;

    @Override
    public Skin save(Skin skin) {
        SkinEntity entity = mapper.toEntity(skin);
        SkinEntity savedEntity = jpaRepository.save(entity);
        return mapper.toDomain(savedEntity);
    }

    @Override
    public Optional<Skin> findById(String id) {
        return jpaRepository.findById(id)
                .map(mapper::toDomain);
    }

    @Override
    public boolean existsById(String id) {
        return jpaRepository.existsById(id);
    }

    @Override
    public List<Skin> findAll() {
        return jpaRepository.findAll().stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }
}

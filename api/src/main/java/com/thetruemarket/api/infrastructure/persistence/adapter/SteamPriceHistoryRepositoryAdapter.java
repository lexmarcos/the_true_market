package com.thetruemarket.api.infrastructure.persistence.adapter;

import com.thetruemarket.api.domain.model.SteamPriceHistory;
import com.thetruemarket.api.domain.repository.SteamPriceHistoryRepository;
import com.thetruemarket.api.domain.valueobject.Wear;
import com.thetruemarket.api.infrastructure.persistence.entity.SteamPriceHistoryEntity;
import com.thetruemarket.api.infrastructure.persistence.mapper.SteamPriceHistoryMapper;
import com.thetruemarket.api.infrastructure.persistence.repository.SteamPriceHistoryJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * Adapter implementation of SteamPriceHistoryRepository using JPA
 * Implements the Adapter/Port pattern for Clean Architecture
 */
@Component
@RequiredArgsConstructor
public class SteamPriceHistoryRepositoryAdapter implements SteamPriceHistoryRepository {
    private final SteamPriceHistoryJpaRepository jpaRepository;
    private final SteamPriceHistoryMapper mapper;

    @Override
    public SteamPriceHistory save(SteamPriceHistory priceHistory) {
        SteamPriceHistoryEntity entity = mapper.toEntity(priceHistory);
        SteamPriceHistoryEntity savedEntity = jpaRepository.save(entity);
        return mapper.toDomain(savedEntity);
    }

    @Override
    public Optional<SteamPriceHistory> findLatestBySkinNameAndWear(String skinName, Wear wear) {
        return jpaRepository.findLatestBySkinNameAndWear(skinName, wear)
                .map(mapper::toDomain);
    }
}

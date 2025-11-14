package com.thetruemarket.api.infrastructure.persistence.mapper;

import com.thetruemarket.api.domain.model.SteamPriceHistory;
import com.thetruemarket.api.infrastructure.persistence.entity.SteamPriceHistoryEntity;
import org.springframework.stereotype.Component;

/**
 * Mapper between SteamPriceHistory domain entity and SteamPriceHistoryEntity JPA entity
 * Implements the Adapter pattern for persistence layer
 */
@Component
public class SteamPriceHistoryMapper {
    /**
     * Converts a domain SteamPriceHistory to JPA SteamPriceHistoryEntity
     *
     * @param priceHistory Domain entity
     * @return JPA entity
     */
    public SteamPriceHistoryEntity toEntity(SteamPriceHistory priceHistory) {
        if (priceHistory == null) {
            return null;
        }

        return SteamPriceHistoryEntity.builder()
                .id(priceHistory.getId())
                .skinId(priceHistory.getSkinId())
                .skinName(priceHistory.getSkinName())
                .wear(priceHistory.getWear())
                .averagePrice(priceHistory.getAveragePrice())
                .lastSalePrice(priceHistory.getLastSalePrice())
                .lowestBuyOrderPrice(priceHistory.getLowestBuyOrderPrice())
                .recordedAt(priceHistory.getRecordedAt())
                .createdAt(priceHistory.getCreatedAt())
                .build();
    }

    /**
     * Converts a JPA SteamPriceHistoryEntity to domain SteamPriceHistory
     *
     * @param entity JPA entity
     * @return Domain entity
     */
    public SteamPriceHistory toDomain(SteamPriceHistoryEntity entity) {
        if (entity == null) {
            return null;
        }

        return SteamPriceHistory.builder()
                .id(entity.getId())
                .skinId(entity.getSkinId())
                .skinName(entity.getSkinName())
                .wear(entity.getWear())
                .averagePrice(entity.getAveragePrice())
                .lastSalePrice(entity.getLastSalePrice())
                .lowestBuyOrderPrice(entity.getLowestBuyOrderPrice())
                .recordedAt(entity.getRecordedAt())
                .createdAt(entity.getCreatedAt())
                .build();
    }
}

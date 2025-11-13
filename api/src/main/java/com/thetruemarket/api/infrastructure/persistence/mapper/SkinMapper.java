package com.thetruemarket.api.infrastructure.persistence.mapper;

import com.thetruemarket.api.domain.model.Skin;
import com.thetruemarket.api.infrastructure.persistence.entity.SkinEntity;
import org.springframework.stereotype.Component;

/**
 * Mapper between Skin domain entity and SkinEntity JPA entity
 * Implements the Adapter pattern for persistence layer
 */
@Component
public class SkinMapper {
    /**
     * Converts a domain Skin to JPA SkinEntity
     *
     * @param skin Domain entity
     * @return JPA entity
     */
    public SkinEntity toEntity(Skin skin) {
        if (skin == null) {
            return null;
        }

        return SkinEntity.builder()
                .id(skin.getId())
                .name(skin.getName())
                .assetId(skin.getAssetId())
                .floatValue(skin.getFloatValue())
                .wear(skin.getWear())
                .paintSeed(skin.getPaintSeed())
                .paintIndex(skin.getPaintIndex())
                .stickerCount(skin.getStickerCount())
                .price(skin.getPrice())
                .currency(skin.getCurrency())
                .marketSource(skin.getMarketSource())
                .link(skin.getLink())
                .createdAt(skin.getCreatedAt())
                .updatedAt(skin.getUpdatedAt())
                .build();
    }

    /**
     * Converts a JPA SkinEntity to domain Skin
     *
     * @param entity JPA entity
     * @return Domain entity
     */
    public Skin toDomain(SkinEntity entity) {
        if (entity == null) {
            return null;
        }

        return Skin.builder()
                .id(entity.getId())
                .name(entity.getName())
                .assetId(entity.getAssetId())
                .floatValue(entity.getFloatValue())
                .wear(entity.getWear())
                .paintSeed(entity.getPaintSeed())
                .paintIndex(entity.getPaintIndex())
                .stickerCount(entity.getStickerCount())
                .price(entity.getPrice())
                .currency(entity.getCurrency())
                .marketSource(entity.getMarketSource())
                .link(entity.getLink())
                .stickers(null) // Stickers not persisted in current implementation
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }
}

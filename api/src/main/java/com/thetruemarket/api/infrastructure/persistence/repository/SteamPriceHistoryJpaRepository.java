package com.thetruemarket.api.infrastructure.persistence.repository;

import com.thetruemarket.api.domain.valueobject.Wear;
import com.thetruemarket.api.infrastructure.persistence.entity.SteamPriceHistoryEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Spring Data JPA repository for SteamPriceHistoryEntity
 */
@Repository
public interface SteamPriceHistoryJpaRepository extends JpaRepository<SteamPriceHistoryEntity, Long> {
    /**
     * Finds the most recent price history for a specific skin name and wear combination
     *
     * @param skinName The skin name
     * @param wear The wear category
     * @return Optional containing the most recent price history
     */
    @Query("SELECT sph FROM SteamPriceHistoryEntity sph " +
           "WHERE sph.skinName = :skinName AND sph.wear = :wear " +
           "ORDER BY sph.recordedAt DESC LIMIT 1")
    Optional<SteamPriceHistoryEntity> findLatestBySkinNameAndWear(
            @Param("skinName") String skinName,
            @Param("wear") Wear wear
    );
}

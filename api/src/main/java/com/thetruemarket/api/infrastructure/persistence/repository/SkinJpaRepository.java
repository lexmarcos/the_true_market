package com.thetruemarket.api.infrastructure.persistence.repository;

import com.thetruemarket.api.domain.valueobject.SkinStatus;
import com.thetruemarket.api.infrastructure.persistence.entity.SkinEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Spring Data JPA repository for SkinEntity
 */
@Repository
public interface SkinJpaRepository extends JpaRepository<SkinEntity, String> {
    /**
     * Finds all skins with specific status that haven't been seen since the cutoff date
     * Spring Data JPA will automatically generate the query based on method name
     *
     * @param status The skin status to filter by
     * @param cutoffDate The last seen cutoff date
     * @return List of skin entities not seen since the cutoff date
     */
    List<SkinEntity> findByStatusAndLastSeenAtBefore(SkinStatus status, LocalDateTime cutoffDate);
}

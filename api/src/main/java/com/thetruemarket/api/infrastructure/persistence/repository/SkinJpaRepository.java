package com.thetruemarket.api.infrastructure.persistence.repository;

import com.thetruemarket.api.infrastructure.persistence.entity.SkinEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Spring Data JPA repository for SkinEntity
 */
@Repository
public interface SkinJpaRepository extends JpaRepository<SkinEntity, String> {
}

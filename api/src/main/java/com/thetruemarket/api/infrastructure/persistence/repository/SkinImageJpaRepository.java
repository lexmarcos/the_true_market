package com.thetruemarket.api.infrastructure.persistence.repository;

import com.thetruemarket.api.infrastructure.persistence.entity.SkinImageEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * JPA Repository for SkinImageEntity.
 * Provides database access for cached skin images.
 * Part of the Infrastructure layer (Interface Adapters).
 */
@Repository
public interface SkinImageJpaRepository extends JpaRepository<SkinImageEntity, String> {

  /**
   * Finds a skin image by skin name.
   * 
   * @param skinName The skin name (format: [Weapon] | [Skin] ([Wear]))
   * @return Optional containing the SkinImageEntity if found
   */
  Optional<SkinImageEntity> findBySkinName(String skinName);
}

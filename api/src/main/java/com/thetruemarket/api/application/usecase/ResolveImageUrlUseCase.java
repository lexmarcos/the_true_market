package com.thetruemarket.api.application.usecase;

import com.thetruemarket.api.domain.exception.SkinImageResolutionException;
import com.thetruemarket.api.infrastructure.external.SteamApiService;
import com.thetruemarket.api.infrastructure.messaging.dto.SkinMarketDataDTO;
import com.thetruemarket.api.infrastructure.persistence.entity.SkinImageEntity;
import com.thetruemarket.api.infrastructure.persistence.repository.SkinImageJpaRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

/**
 * Use case for resolving skin image URLs from various sources.
 * Priority order:
 * 1. Check if image already exists in cache (skins_images table)
 * 2. Use imageUrl from DTO if present
 * 3. Build URL from iconUrl if present
 * 4. Fetch from Steam API using classId if present
 * 
 * Saves resolved images to cache for future lookups.
 * Part of the Application layer (Use Cases).
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ResolveImageUrlUseCase {

  private final SkinImageJpaRepository skinImageRepository;
  private final SteamApiService steamApiService;

  @Value("${steam.api.image-base-url}")
  private String imageBaseUrl;

  /**
   * Resolves the image URL for a skin from available sources.
   * Checks cache first, then resolves from DTO fields, and saves to cache if new.
   * ALWAYS ensures an entry exists in skins_images table (even if URL is null).
   * 
   * @param skinName The name of the skin (format: [Weapon] | [Skin] ([Wear]))
   * @param dto      The market data DTO containing potential image sources
   * @return The resolved image URL, or null if unable to resolve
   */
  @Transactional(propagation = org.springframework.transaction.annotation.Propagation.REQUIRES_NEW)
  public String resolve(String skinName, SkinMarketDataDTO dto) {
    if (skinName == null || skinName.isBlank()) {
      log.warn("Cannot resolve image URL: skin name is null or empty");
      return null;
    }

    // 1. Check if image already exists in cache
    Optional<SkinImageEntity> cachedImage = skinImageRepository.findBySkinName(skinName);
    if (cachedImage.isPresent()) {
      log.debug("Image URL found in cache for skin: {}", skinName);
      return cachedImage.get().getImageUrl();
    }

    // 2. Try to resolve from DTO
    String resolvedImageUrl = resolveFromDto(dto);

    // 3. ALWAYS save to cache - even if URL is null (to satisfy FK constraint)
    // Use a placeholder URL if resolution failed
    if (resolvedImageUrl == null || resolvedImageUrl.isBlank()) {
      resolvedImageUrl = "https://placeholder.image/no-image.png"; // Placeholder
      log.warn("Unable to resolve image URL for skin: {}, using placeholder", skinName);
    }
    
    saveToCacheIfNotExists(skinName, resolvedImageUrl);
    return resolvedImageUrl;
  }

  /**
   * Resolves image URL from DTO fields in priority order.
   */
  private String resolveFromDto(SkinMarketDataDTO dto) {
    if (dto == null) {
      return null;
    }

    // Priority 1: imageUrl directly from DTO
    if (dto.getImageUrl() != null && !dto.getImageUrl().isBlank()) {
      log.debug("Using imageUrl from DTO: {}", dto.getImageUrl());
      return dto.getImageUrl();
    }

    // Priority 2: iconUrl - build full URL
    if (dto.getIconUrl() != null && !dto.getIconUrl().isBlank()) {
      String fullUrl = imageBaseUrl + dto.getIconUrl();
      log.debug("Built image URL from iconUrl: {}", fullUrl);
      return fullUrl;
    }

    // Priority 3: classId - fetch from Steam API
    if (dto.getClassId() != null && !dto.getClassId().isBlank()) {
      try {
        log.info("Attempting to fetch image URL from Steam API for classId: {}", dto.getClassId());
        String imageUrl = steamApiService.getImageUrlByClassId(dto.getClassId());
        log.info("Successfully fetched image URL from Steam API for classId {}: {}", dto.getClassId(), imageUrl);
        return imageUrl;
      } catch (SkinImageResolutionException e) {
        log.error("Failed to fetch image from Steam API for classId {}: {}", dto.getClassId(), e.getMessage(), e);
        return null;
      } catch (Exception e) {
        log.error("Unexpected error fetching image from Steam API for classId {}: {}", dto.getClassId(), e.getMessage(), e);
        return null;
      }
    }

    log.warn("No image source available in DTO (no imageUrl, iconUrl, or classId)");
    return null;
  }

  /**
   * Saves image URL to cache only if it doesn't already exist.
   * Prevents race conditions and duplicate key errors.
   */
  private void saveToCacheIfNotExists(String skinName, String imageUrl) {
    try {
      // Double-check before saving (race condition protection)
      if (skinImageRepository.findBySkinName(skinName).isEmpty()) {
        SkinImageEntity entity = SkinImageEntity.builder()
            .skinName(skinName)
            .imageUrl(imageUrl)
            .build();
        skinImageRepository.saveAndFlush(entity); // Force immediate flush to DB
        log.info("Saved image URL to cache for skin: {} -> {}", skinName, imageUrl);
      }
    } catch (Exception e) {
      // Log error and rethrow - this is critical for FK constraint
      log.error("CRITICAL: Failed to save image URL to cache for skin {}: {}", skinName, e.getMessage(), e);
      throw new RuntimeException("Failed to save skin image to database", e);
    }
  }
}

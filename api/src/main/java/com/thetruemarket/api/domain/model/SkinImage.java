package com.thetruemarket.api.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Domain entity representing a cached skin image URL.
 * Serves as an immutable cache for skin images resolved from various sources.
 * Part of the Domain layer (Entities).
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SkinImage {

  /**
   * Unique identifier: the skin name.
   * Format: [Weapon] | [Skin] ([Wear])
   * Example: AK-47 | Redline (Field-Tested)
   */
  private String skinName;

  /**
   * Full image URL for the skin.
   * Example: https://community.cloudflare.steamstatic.com/economy/image/abc123...
   */
  private String imageUrl;
}

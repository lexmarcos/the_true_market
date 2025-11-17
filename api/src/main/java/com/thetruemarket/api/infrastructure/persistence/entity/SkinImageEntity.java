package com.thetruemarket.api.infrastructure.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * JPA entity for the skins_images table.
 * Stores cached image URLs for skins identified by their name.
 * Part of the Infrastructure layer (Frameworks & Drivers).
 */
@Entity
@Table(name = "skins_images")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SkinImageEntity {

  @Id
  @Column(name = "skin_name", nullable = false, unique = true)
  private String skinName;

  @Column(name = "image_url", nullable = false, columnDefinition = "TEXT")
  private String imageUrl;
}

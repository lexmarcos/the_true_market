package com.thetruemarket.api.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Domain entity representing a sticker attached to a CS2 skin.
 * Pure domain model without framework dependencies.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Sticker {

  private String name;
  private Integer slot;
  private Double wear;
  private Integer skinId;
  private String classId;
}

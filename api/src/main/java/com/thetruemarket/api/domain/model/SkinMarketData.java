package com.thetruemarket.api.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Domain entity representing CS2 skin market data.
 * Contains all relevant information about a skin listing from various market
 * sources.
 * Pure domain model following Clean Architecture principles.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SkinMarketData {

  private Long price;
  private String id;
  private String assetId;
  private Double floatValue;
  private Integer paintSeed;
  private Integer paintIndex;
  private List<Sticker> stickers;
  private Integer stickerCount;
  private String name;
  private String store;
  private String currency;
}

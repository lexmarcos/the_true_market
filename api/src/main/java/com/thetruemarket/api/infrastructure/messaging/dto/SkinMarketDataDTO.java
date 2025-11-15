package com.thetruemarket.api.infrastructure.messaging.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.thetruemarket.api.domain.model.SkinMarketData;
import com.thetruemarket.api.domain.model.Sticker;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Data Transfer Object for receiving skin market data messages from RabbitMQ.
 * Maps JSON fields to Java properties and converts to domain entities.
 * Part of the Infrastructure layer (Interface Adapters).
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SkinMarketDataDTO {

  private Long price;

  private String id;

  @JsonProperty("asset_id")
  private String assetId;

  @JsonProperty("float_value")
  private Double floatValue;

  @JsonProperty("paint_seed")
  private Integer paintSeed;

  @JsonProperty("paint_index")
  private Integer paintIndex;

  private List<StickerDTO> stickers;

  @JsonProperty("sticker_count")
  private Integer stickerCount;

  private String name;

  private String store;

  private String currency;

  private String link;

  @JsonProperty("image_url")
  private String imageUrl;

  /**
   * Converts this DTO to a domain entity.
   *
   * @return SkinMarketData domain entity
   */
  public SkinMarketData toDomain() {
    return SkinMarketData.builder()
        .price(price)
        .id(id)
        .assetId(assetId)
        .floatValue(floatValue)
        .paintSeed(paintSeed)
        .paintIndex(paintIndex)
        .stickers(stickers != null ? stickers.stream()
            .map(StickerDTO::toDomain)
            .collect(Collectors.toList()) : null)
        .stickerCount(stickerCount)
        .name(name)
        .store(store)
        .currency(currency)
        .link(link)
        .imageUrl(imageUrl)
        .build();
  }

  @Data
  @Builder
  @NoArgsConstructor
  @AllArgsConstructor
  public static class StickerDTO {
    private String name;
    private Integer slot;
    private Double wear;

    @JsonProperty("skin_id")
    private Integer skinId;

    @JsonProperty("class_id")
    private String classId;

    public Sticker toDomain() {
      return Sticker.builder()
          .name(name)
          .slot(slot)
          .wear(wear)
          .skinId(skinId)
          .classId(classId)
          .build();
    }
  }
}

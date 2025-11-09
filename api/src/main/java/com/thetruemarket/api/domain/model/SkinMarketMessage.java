package com.thetruemarket.api.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Domain entity representing a message received from RabbitMQ containing skin
 * market data.
 * Wraps the market data with metadata about the message source and timestamp.
 * Pure domain model following Clean Architecture principles.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SkinMarketMessage {

  private SkinMarketData data;
  private MarketSource source;
  private LocalDateTime receivedAt;
  private String routingKey;

  /**
   * Factory method to create a message from market data and routing key.
   * 
   * @param data       The skin market data
   * @param routingKey The RabbitMQ routing key
   * @return A new SkinMarketMessage instance
   */
  public static SkinMarketMessage from(SkinMarketData data, String routingKey) {
    return SkinMarketMessage.builder()
        .data(data)
        .source(MarketSource.fromRoutingKey(routingKey))
        .receivedAt(LocalDateTime.now())
        .routingKey(routingKey)
        .build();
  }
}

package com.thetruemarket.api.domain.model;

/**
 * Enumeration representing the different CS2 skin market sources.
 * These correspond to the routing keys in RabbitMQ.
 */
public enum MarketSource {
  STEAM("steam"),
  BITSKINS("bitskins"),
  DASHSKINS("dashskins");

  private final String routingKey;

  MarketSource(String routingKey) {
    this.routingKey = routingKey;
  }

  public String getRoutingKey() {
    return routingKey;
  }

  public String getFullRoutingKey() {
    return "skin.market." + routingKey;
  }

  /**
   * Converts a routing key string to MarketSource enum.
   * 
   * @param routingKey The routing key (e.g., "skin.market.steam" or "steam")
   * @return The corresponding MarketSource
   */
  public static MarketSource fromRoutingKey(String routingKey) {
    String source = routingKey.replace("skin.market.", "").toLowerCase();
    for (MarketSource ms : MarketSource.values()) {
      if (ms.routingKey.equalsIgnoreCase(source)) {
        return ms;
      }
    }
    throw new IllegalArgumentException("Unknown market source: " + routingKey);
  }
}

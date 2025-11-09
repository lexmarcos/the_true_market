package com.thetruemarket.api.infrastructure.messaging.consumer;

import com.thetruemarket.api.domain.model.SkinMarketData;
import com.thetruemarket.api.domain.model.SkinMarketMessage;
import com.thetruemarket.api.infrastructure.messaging.config.RabbitMQConfig;
import com.thetruemarket.api.infrastructure.messaging.dto.SkinMarketDataDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

/**
 * RabbitMQ consumer for processing CS2 skin market data messages.
 * Receives messages from all routing keys (steam, bitskins, dashskins)
 * and processes them accordingly.
 * Part of the Infrastructure layer implementing message-driven architecture.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class SkinMarketConsumer {

  /**
   * Listens to the skin market queue and processes incoming messages.
   * The routing key is extracted from the message to determine the market source.
   * 
   * @param messageDto The skin market data received from RabbitMQ
   * @param message    The raw AMQP message containing metadata
   */
  @RabbitListener(queues = RabbitMQConfig.QUEUE_NAME)
  public void consumeSkinMarketData(SkinMarketDataDTO messageDto, Message message) {
    try {
      // Extract routing key from message
      String routingKey = message.getMessageProperties().getReceivedRoutingKey();

      log.info("Received message from routing key: {} - Skin: {} - Store: {} - Price: {}",
          routingKey,
          messageDto.getName(),
          messageDto.getStore(),
          messageDto.getPrice());

      // Convert DTO to domain entity
      SkinMarketData marketData = messageDto.toDomain();

      // Create domain message with metadata
      SkinMarketMessage skinMarketMessage = SkinMarketMessage.from(marketData, routingKey);

      // Process the message based on routing key
      processMarketData(skinMarketMessage);

      log.info("Successfully processed message from source: {}", skinMarketMessage.getSource());

    } catch (Exception e) {
      log.error("Error processing skin market message from routing key: {}",
          message.getMessageProperties().getReceivedRoutingKey(), e);
      // Re-throw to trigger retry mechanism or dead letter queue
      throw new RuntimeException("Failed to process skin market message", e);
    }
  }

  /**
   * Processes the skin market data based on the source.
   * This method can be extended to implement specific business logic
   * for different market sources.
   * 
   * @param message The domain message containing market data and metadata
   */
  private void processMarketData(SkinMarketMessage message) {
    // TODO: Implement business logic for processing market data
    // This could involve:
    // - Storing data in database
    // - Performing price analysis
    // - Triggering notifications
    // - Updating market statistics
    // - Calling use cases from the application layer

    log.debug("Processing message from {}: Item ID: {}, Asset ID: {}, Float: {}, Price: {} {}",
        message.getSource(),
        message.getData().getId(),
        message.getData().getAssetId(),
        message.getData().getFloatValue(),
        message.getData().getPrice(),
        message.getData().getCurrency());

    // Example of how to handle different sources
    switch (message.getSource()) {
      case STEAM:
        log.debug("Processing Steam market data");
        break;
      case BITSKINS:
        log.debug("Processing BitSkins market data");
        break;
      case DASHSKINS:
        log.debug("Processing DashSkins market data");
        break;
      default:
        log.warn("Unknown market source: {}", message.getSource());
    }
  }
}

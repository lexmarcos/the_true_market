package com.thetruemarket.api.infrastructure.messaging.consumer;

import com.thetruemarket.api.application.usecase.ProcessSkinMarketDataUseCase;
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
  private final ProcessSkinMarketDataUseCase processSkinMarketDataUseCase;

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

      // Process the message based on routing key (pass DTO for image resolution)
      processMarketData(skinMarketMessage, messageDto);

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
   * Delegates to the use case layer for business logic execution.
   *
   * @param message    The domain message containing market data and metadata
   * @param messageDto The original DTO with image resolution fields
   */
  private void processMarketData(SkinMarketMessage message, SkinMarketDataDTO messageDto) {
    log.debug("Processing message from {}: Item ID: {}, Asset ID: {}, Float: {}, Price: {} {}",
        message.getSource(),
        message.getData().getId(),
        message.getData().getAssetId(),
        message.getData().getFloatValue(),
        message.getData().getPrice(),
        message.getData().getCurrency());

    // Delegate to application layer use case
    // The use case will:
    // - Resolve image URL from cache or various sources
    // - Save the skin if it doesn't exist
    // - Check if price history needs update
    // - Create history update task if needed
    processSkinMarketDataUseCase.execute(message.getData(), messageDto);
  }
}

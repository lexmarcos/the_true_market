package com.thetruemarket.api.infrastructure.messaging.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * RabbitMQ configuration for CS2 skin market data processing.
 * Implements a Topic Exchange architecture with routing keys for different
 * market sources.
 * 
 * Exchange: skin.market.data (Topic)
 * Routing Keys:
 * - skin.market.steam
 * - skin.market.bitskins
 * - skin.market.dashskins
 * - skin.market.* (wildcard for all sources)
 */
@Configuration
public class RabbitMQConfig {

  // Exchange name
  public static final String TOPIC_EXCHANGE_NAME = "skin.market.data";

  // Queue name (single queue for all messages)
  public static final String QUEUE_NAME = "skin.market.queue";

  // Routing keys
  public static final String ROUTING_KEY_STEAM = "skin.market.steam";
  public static final String ROUTING_KEY_BITSKINS = "skin.market.bitskins";
  public static final String ROUTING_KEY_DASHSKINS = "skin.market.dashskins";
  public static final String ROUTING_KEY_ALL = "skin.market.*";

  /**
   * Declares the Topic Exchange for skin market data.
   * Topic exchanges route messages to queues based on routing key patterns.
   */
  @Bean
  public TopicExchange skinMarketExchange() {
    return ExchangeBuilder
        .topicExchange(TOPIC_EXCHANGE_NAME)
        .durable(true)
        .build();
  }

  /**
   * Declares the main queue for processing all skin market messages.
   * This queue receives messages from all routing keys.
   */
  @Bean
  public Queue skinMarketQueue() {
    return QueueBuilder
        .durable(QUEUE_NAME)
        .withArgument("x-dead-letter-exchange", TOPIC_EXCHANGE_NAME + ".dlx")
        .withArgument("x-message-ttl", 86400000) // 24 hours
        .build();
  }

  /**
   * Binds the queue to the exchange with wildcard routing key.
   * This allows the queue to receive messages from all market sources.
   */
  @Bean
  public Binding bindingWildcard(Queue skinMarketQueue, TopicExchange skinMarketExchange) {
    return BindingBuilder
        .bind(skinMarketQueue)
        .to(skinMarketExchange)
        .with(ROUTING_KEY_ALL);
  }

  /**
   * Dead Letter Exchange for failed message processing.
   * Messages that fail processing are routed here for later analysis.
   */
  @Bean
  public TopicExchange deadLetterExchange() {
    return ExchangeBuilder
        .topicExchange(TOPIC_EXCHANGE_NAME + ".dlx")
        .durable(true)
        .build();
  }

  /**
   * Dead Letter Queue for storing failed messages.
   */
  @Bean
  public Queue deadLetterQueue() {
    return QueueBuilder
        .durable(QUEUE_NAME + ".dlq")
        .build();
  }

  /**
   * Binds the dead letter queue to the dead letter exchange.
   */
  @Bean
  public Binding bindingDeadLetter(Queue deadLetterQueue, TopicExchange deadLetterExchange) {
    return BindingBuilder
        .bind(deadLetterQueue)
        .to(deadLetterExchange)
        .with(ROUTING_KEY_ALL);
  }

  /**
   * Configures JSON message converter for serializing/deserializing messages.
   * Uses Jackson for JSON processing.
   */
  @Bean
  public MessageConverter jsonMessageConverter() {
    return new Jackson2JsonMessageConverter();
  }

  /**
   * Configures RabbitTemplate with JSON message converter.
   * RabbitTemplate is used for sending messages to RabbitMQ.
   */
  @Bean
  public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory,
      MessageConverter jsonMessageConverter) {
    RabbitTemplate template = new RabbitTemplate(connectionFactory);
    template.setMessageConverter(jsonMessageConverter);
    return template;
  }
}

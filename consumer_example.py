#!/usr/bin/env python3
"""
Exemplo de consumidor da fila RabbitMQ
Este script lê as mensagens da fila e processa os itens
"""

import pika
import json


def callback(ch, method, properties, body):
    """
    Callback executado quando uma mensagem é recebida
    """
    try:
        # Decodificar JSON
        item = json.loads(body)
        
        # Identificar a fila de origem
        queue_name = method.routing_key
        
        print("\n" + "="*60)
        print(f"📦 NOVO ITEM RECEBIDO DA FILA: {queue_name}")
        print("="*60)
        print(f"🏪 Loja: {item.get('store', 'unknown').upper()}")
        print(f"🎯 Nome: {item.get('name')}")
        
        # Formatar preço com moeda correta
        currency = item.get('currency', 'USD')
        price = item.get('price', 0) / 100
        if currency == 'BRL':
            print(f"💰 Preço: R$ {price:.2f}")
        else:
            print(f"💰 Preço: ${price:.2f}")
        
        print(f"💱 Moeda: {currency}")
        print(f"🆔 ID: {item.get('id')}")
        print(f"📊 Float: {item.get('float_value')}")
        print(f"🎨 Stickers: {item.get('sticker_count', 0)}")
        print(f"🔢 Asset ID: {item.get('asset_id')}")
        print(f"🎨 Paint Seed: {item.get('paint_seed')}")
        print(f"🎨 Paint Index: {item.get('paint_index')}")
        
        # Mostrar stickers se existirem
        stickers = item.get('stickers', [])
        if stickers:
            print("\n🎨 Stickers:")
            for sticker in stickers:
                print(f"  - {sticker.get('name')} (Slot: {sticker.get('slot')})")
        
        print("="*60)
        
        # Acknowledge da mensagem (confirma processamento)
        ch.basic_ack(delivery_tag=method.delivery_tag)
        
        # Aqui você pode adicionar sua lógica de processamento
        # Por exemplo: salvar no banco de dados, enviar notificação, etc.
        
    except Exception as e:
        print(f"❌ Erro ao processar mensagem: {e}")
        # Não fazer ack em caso de erro (mensagem volta para a fila)
        ch.basic_nack(delivery_tag=method.delivery_tag, requeue=True)


def main():
    """
    Conecta ao RabbitMQ e começa a consumir mensagens de múltiplas filas
    """
    # Configurações
    RABBITMQ_HOST = "localhost"
    RABBITMQ_PORT = 5672
    RABBITMQ_USER = "guest"
    RABBITMQ_PASSWORD = "guest"
    RABBITMQ_QUEUES = ["bitskins_items", "dashskins_items"]  # Lista de filas para consumir
    
    try:
        # Conectar ao RabbitMQ
        credentials = pika.PlainCredentials(RABBITMQ_USER, RABBITMQ_PASSWORD)
        parameters = pika.ConnectionParameters(
            host=RABBITMQ_HOST,
            port=RABBITMQ_PORT,
            credentials=credentials,
            heartbeat=600,
            blocked_connection_timeout=300
        )
        
        connection = pika.BlockingConnection(parameters)
        channel = connection.channel()
        
        # Declarar as filas (serão criadas se não existirem)
        for queue_name in RABBITMQ_QUEUES:
            channel.queue_declare(queue=queue_name, durable=True)
        
        # Configurar QoS (processar uma mensagem por vez)
        channel.basic_qos(prefetch_count=1)
        
        # Começar a consumir de todas as filas
        print(f"🎧 Aguardando mensagens das filas: {', '.join(RABBITMQ_QUEUES)}...")
        print("⏸️  Pressione CTRL+C para parar\n")
        
        for queue_name in RABBITMQ_QUEUES:
            channel.basic_consume(
                queue=queue_name,
                on_message_callback=callback,
                auto_ack=False  # Manual acknowledgment
            )
        
        channel.start_consuming()
        
    except KeyboardInterrupt:
        print("\n\n🛑 Consumidor interrompido pelo usuário")
        if connection:
            connection.close()
    except Exception as e:
        print(f"❌ Erro: {e}")


if __name__ == "__main__":
    main()

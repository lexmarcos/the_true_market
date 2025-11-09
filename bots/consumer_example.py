#!/usr/bin/env python3
"""
Exemplo de consumidor da fila RabbitMQ com Topic Exchange
Este script lê as mensagens do exchange usando routing keys
"""

import pika
import json
import sys


def callback(ch, method, properties, body):
    """
    Callback executado quando uma mensagem é recebida
    """
    try:
        # Decodificar JSON
        item = json.loads(body)
        
        # Identificar a routing key (origem do item)
        routing_key = method.routing_key
        store = routing_key.split('.')[-1] if routing_key else 'unknown'
        
        print("\n" + "="*60)
        print(f"📦 NOVO ITEM RECEBIDO")
        print("="*60)
        print(f"🔑 Routing Key: {routing_key}")
        print(f"🏪 Loja: {item.get('store', store).upper()}")
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
    Conecta ao RabbitMQ e começa a consumir mensagens usando Topic Exchange
    """
    # Configurações
    RABBITMQ_HOST = "localhost"
    RABBITMQ_PORT = 5672
    RABBITMQ_USER = "guest"
    RABBITMQ_PASSWORD = "guest"
    RABBITMQ_EXCHANGE = "skin.market.data"
    
    # Routing keys para consumir (pode usar wildcards)
    # Opções:
    # - "skin.market.*" = Recebe de TODAS as lojas
    # - "skin.market.bitskins" = Recebe apenas da BitSkins
    # - "skin.market.csmoney" = Recebe apenas da CSMoney
    # - ["skin.market.bitskins", "skin.market.skinport"] = Múltiplas lojas específicas
    
    # Pegar routing key do argumento ou usar padrão
    if len(sys.argv) > 1:
        ROUTING_KEYS = [sys.argv[1]]
    else:
        ROUTING_KEYS = ["skin.market.*"]  # Padrão: recebe de todas as lojas
    
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
        
        # Declarar o exchange (será criado se não existir)
        channel.exchange_declare(
            exchange=RABBITMQ_EXCHANGE,
            exchange_type='topic',
            durable=True
        )
        
        # Criar uma fila exclusiva temporária para este consumidor
        result = channel.queue_declare(queue='', exclusive=True)
        queue_name = result.method.queue
        
        # Fazer binding das routing keys com a fila
        for routing_key in ROUTING_KEYS:
            channel.queue_bind(
                exchange=RABBITMQ_EXCHANGE,
                queue=queue_name,
                routing_key=routing_key
            )
        
        print(f"📡 Conectado ao Exchange: {RABBITMQ_EXCHANGE}")
        print(f"🔑 Routing Keys: {', '.join(ROUTING_KEYS)}")
        print(f"📬 Fila temporária: {queue_name}")
        print("\n🎧 Aguardando mensagens...")
        print("⏸️  Pressione CTRL+C para parar")
        print("\n💡 Dica: Use argumentos para filtrar por loja:")
        print("   python3 consumer_example.py skin.market.bitskins")
        print("   python3 consumer_example.py skin.market.*")
        print()
        
        # Configurar QoS (processar uma mensagem por vez)
        channel.basic_qos(prefetch_count=1)
        
        # Começar a consumir
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

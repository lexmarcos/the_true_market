import requests
import time
import json
import pika
import re
from typing import List, Dict, Any
from datetime import datetime


class DashSkinsMonitor:
    """
    Bot para monitorar descontos na API da DashSkins e enviar para RabbitMQ
    """
    
    def __init__(
        self,
        min_discount: int = 15,
        rabbitmq_host: str = "localhost",
        rabbitmq_port: int = 5672,
        rabbitmq_user: str = "guest",
        rabbitmq_password: str = "guest",
        rabbitmq_exchange: str = "skin.market.data",
        rabbitmq_routing_key: str = "skin.market.dashskins",
        search_knives: bool = True,
        search_rifles: bool = True
    ):
        self.base_url = "https://dashskins.com.br/api/listing"
        self.min_discount = min_discount
        self.headers = {
            "User-Agent": "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36"
        }

        # Configuração RabbitMQ
        self.rabbitmq_host = rabbitmq_host
        self.rabbitmq_port = rabbitmq_port
        self.rabbitmq_user = rabbitmq_user
        self.rabbitmq_password = rabbitmq_password
        self.rabbitmq_exchange = rabbitmq_exchange
        self.rabbitmq_routing_key = rabbitmq_routing_key
        self.rabbitmq_connection = None
        self.rabbitmq_channel = None

        # Configuração de busca
        self.search_knives = search_knives
        self.search_rifles = search_rifles
    
    def build_params(self, item_type: str = "Rifle", page: int = 1, limit: int = 36) -> Dict[str, Any]:
        """
        Constrói os parâmetros da requisição
        
        Args:
            item_type: "Rifle" ou "Faca"
            page: Página da requisição
            limit: Limite de itens por página
        """
        timestamp = int(time.time() * 1000)
        
        return {
            "search": "",
            "item_type": item_type,
            "rarity": "",
            "itemset": "",
            "exterior": "",
            "weapon": "",
            "has_sticker": "",
            "has_charm": "",
            "has_stattrak": "",
            "is_souvenir": "",
            "is_instant": "",
            "sort_by": "discount",
            "sort_dir": "desc",
            "price_min": "",
            "price_max": "",
            "page": str(page),
            "limit": str(limit),
            "t": str(timestamp)
        }
    
    def search_items(self, item_type: str = "Rifle", page: int = 1) -> Dict[str, Any]:
        """
        Faz a requisição para a API da DashSkins
        """
        try:
            params = self.build_params(item_type=item_type, page=page)
            
            response = requests.get(
                self.base_url,
                params=params,
                headers=self.headers,
                timeout=10
            )
            response.raise_for_status()
            return response.json()
        except requests.exceptions.RequestException as e:
            print(f"❌ Erro na requisição: {e}")
            return None
    
    def connect_rabbitmq(self):
        """
        Conecta ao RabbitMQ, cria o exchange e configura o roteamento
        """
        try:
            credentials = pika.PlainCredentials(self.rabbitmq_user, self.rabbitmq_password)
            parameters = pika.ConnectionParameters(
                host=self.rabbitmq_host,
                port=self.rabbitmq_port,
                credentials=credentials,
                heartbeat=600,
                blocked_connection_timeout=300
            )

            self.rabbitmq_connection = pika.BlockingConnection(parameters)
            self.rabbitmq_channel = self.rabbitmq_connection.channel()

            # Declarar o exchange do tipo topic (será criado se não existir)
            self.rabbitmq_channel.exchange_declare(
                exchange=self.rabbitmq_exchange,
                exchange_type='topic',
                durable=True
            )

            print(f"✅ Conectado ao RabbitMQ em {self.rabbitmq_host}:{self.rabbitmq_port}")
            print(f"📮 Exchange: {self.rabbitmq_exchange} (tipo: topic)")
            print(f"🔑 Routing Key: {self.rabbitmq_routing_key}")
            return True

        except Exception as e:
            print(f"❌ Erro ao conectar ao RabbitMQ: {e}")
            return False
    
    def disconnect_rabbitmq(self):
        """
        Desconecta do RabbitMQ
        """
        try:
            if self.rabbitmq_channel:
                self.rabbitmq_channel.close()
            if self.rabbitmq_connection:
                self.rabbitmq_connection.close()
            print("🔌 Desconectado do RabbitMQ")
        except Exception as e:
            print(f"⚠️  Erro ao desconectar do RabbitMQ: {e}")
    
    def send_to_queue(self, item_data: Dict[str, Any]) -> bool:
        """
        Envia os dados do item para o exchange com a routing key configurada
        """
        try:
            message = json.dumps(item_data, ensure_ascii=False)

            self.rabbitmq_channel.basic_publish(
                exchange=self.rabbitmq_exchange,
                routing_key=self.rabbitmq_routing_key,
                body=message,
                properties=pika.BasicProperties(
                    delivery_mode=2,  # Make message persistent
                    content_type='application/json'
                )
            )

            return True

        except Exception as e:
            print(f"❌ Erro ao enviar para exchange: {e}")
            # Tentar reconectar
            if self.connect_rabbitmq():
                try:
                    message = json.dumps(item_data, ensure_ascii=False)
                    self.rabbitmq_channel.basic_publish(
                        exchange=self.rabbitmq_exchange,
                        routing_key=self.rabbitmq_routing_key,
                        body=message,
                        properties=pika.BasicProperties(
                            delivery_mode=2,
                            content_type='application/json'
                        )
                    )
                    return True
                except:
                    return False
            return False
    
    def filter_high_discount_items(self, items: List[Dict[str, Any]]) -> List[Dict[str, Any]]:
        """
        Filtra itens com desconto acima do mínimo configurado
        """
        return [
            item for item in items
            if item.get("discount", 0) >= self.min_discount
        ]

    def generate_slug(self, market_hash_name: str) -> str:
        """
        Gera o slug a partir do market_hash_name
        Converte para lowercase e substitui espaços e parênteses
        """
        slug = market_hash_name.lower()
        # Substituir parênteses por vazio
        slug = re.sub(r'[()]', '', slug)
        # Substituir espaços por hífen
        slug = re.sub(r'\s+', '-', slug)
        # Remover caracteres especiais extras
        slug = re.sub(r'[^\w\-]', '', slug)
        return slug

    def generate_item_link(self, market_hash_name: str, item_id: str) -> str:
        """
        Gera o link do item no DashSkins
        Formato: https://dashskins.com.br/item/{slug}/{_id}
        """
        slug = self.generate_slug(market_hash_name)
        return f"https://dashskins.com.br/item/{slug}/{item_id}"

    def prepare_item_for_queue(self, item: Dict[str, Any]) -> Dict[str, Any]:
        """
        Prepara os dados do item no formato esperado pela fila
        """
        # Extrair informações básicas
        price_cents = int(item.get("price", 0) * 100)  # Converter para centavos
        item_id = item.get("_id")
        asset_id = item.get("assetid")
        
        # Informações do wear_data
        wear_data = item.get("wear_data", {})
        float_value = wear_data.get("floatvalue")
        paint_seed = wear_data.get("paintseed")
        paint_index = wear_data.get("paintindex")
        
        market_hash_name = item.get("market_hash_name", "Unknown")
        name = market_hash_name
        
        # Processar stickers
        stickers = []
        sticker_list = wear_data.get("stickers", [])
        
        if isinstance(sticker_list, list):
            for idx, sticker in enumerate(sticker_list):
                if isinstance(sticker, dict):
                    sticker_data = {
                        "name": sticker.get("name", ""),
                        "slot": sticker.get("slot", idx),
                        "wear": sticker.get("wear", 0),
                        "skin_id": sticker.get("stickerId"),
                        "class_id": str(sticker.get("classId", ""))
                    }
                    stickers.append(sticker_data)
        
        sticker_count = len(stickers)
        
        # Informações adicionais da DashSkins
        steam_price = item.get("steamPrice", 0)
        discount = item.get("discount", 0)
        exterior = item.get("exterior", "")
        weapon = item.get("weapon", "")
        item_type = item.get("item_type", "")
        rarity = item.get("rarity", "")
        quality = item.get("quality", "")
        
        # Gerar link do item
        item_link = self.generate_item_link(market_hash_name, item_id) if item_id else None

        # Montar objeto final
        queue_data = {
            "price": price_cents,
            "id": item_id,
            "asset_id": asset_id,
            "float_value": float_value,
            "paint_seed": paint_seed,
            "paint_index": paint_index,
            "stickers": stickers,
            "sticker_count": sticker_count,
            "name": name,
            "market_hash_name": market_hash_name,
            # Dados extras da DashSkins
            "steam_price": steam_price,
            "discount": discount,
            "exterior": exterior,
            "weapon": weapon,
            "item_type": item_type,
            "rarity": rarity,
            "quality": quality,
            "source": "dashskins",
            "store": "dashskins",
            "currency": "BRL",
            "link": item_link
        }
        
        return queue_data
    
    def monitor(self, check_interval: int = 60, max_iterations: int = None):
        """
        Monitora continuamente a API em busca de itens com alto desconto

        Args:
            check_interval: Intervalo em segundos entre cada verificação
            max_iterations: Número máximo de iterações (None para infinito)
        """
        # Conectar ao RabbitMQ
        if not self.connect_rabbitmq():
            print("❌ Não foi possível conectar ao RabbitMQ. Encerrando...")
            return

        iteration = 0

        search_types = []
        if self.search_rifles:
            search_types.append("Rifle")
        if self.search_knives:
            search_types.append("Faca")
        
        if not search_types:
            print("❌ Nenhum tipo de item selecionado para busca!")
            return
        
        search_desc = " e ".join([
            "🔫 Rifles" if t == "Rifle" else "🔪 Facas" 
            for t in search_types
        ])
        
        print(f"🤖 DashSkins Monitor iniciado - Descontos acima de {self.min_discount}%")
        print(f"🎯 Buscando: {search_desc}")
        print(f"⏰ Intervalo de verificação: {check_interval} segundos\n")
        
        try:
            while True:
                iteration += 1
                
                if max_iterations and iteration > max_iterations:
                    print(f"✅ Limite de {max_iterations} iterações atingido")
                    break
                
                timestamp = datetime.now().strftime("%Y-%m-%d %H:%M:%S")
                print(f"\n[{timestamp}] 🔍 Verificando itens (Iteração #{iteration})...")

                items_sent = 0

                # Buscar em cada tipo de item
                for item_type in search_types:
                    type_label = "🔫 Rifles" if item_type == "Rifle" else "🔪 Facas"
                    print(f"\n📋 Buscando {type_label}...")
                    
                    # Fazer requisição
                    result = self.search_items(item_type=item_type, page=1)
                    
                    if not result:
                        print(f"⚠️  Nenhum resultado retornado para {type_label}")
                        continue
                    
                    # Extrair lista de itens
                    items = result.get("results", [])
                    total_items = result.get("count", 0)
                    
                    print(f"📦 Total de {type_label} encontrados: {total_items}")
                    
                    # Filtrar itens com alto desconto
                    high_discount_items = self.filter_high_discount_items(items)
                    
                    if high_discount_items:
                        print(f"🔥 {len(high_discount_items)} {type_label} com desconto acima de {self.min_discount}%")
                        
                        # Enviar cada item para a fila
                        for item in high_discount_items:
                            item_name = item.get("market_hash_name", "Unknown")
                            item_id = item.get("_id")
                            discount = item.get("discount", 0)
                            price = item.get("price", 0)
                            
                            # Preparar dados para a fila
                            queue_data = self.prepare_item_for_queue(item)
                            
                            # Enviar para RabbitMQ
                            if self.send_to_queue(queue_data):
                                items_sent += 1
                                print(f"  ✅ Enviado: {item_name} | ${price:.2f} | {discount}% OFF (ID: {item_id})")
                            else:
                                print(f"  ❌ Falha ao enviar: {item_name} (ID: {item_id})")
                    else:
                        print(f"✅ Nenhum(a) {type_label} com desconto acima de {self.min_discount}%")
                
                # Resumo da iteração
                if items_sent > 0:
                    print(f"\n📨 Total de {items_sent} item(s) enviado(s) para a fila nesta iteração")
                else:
                    print(f"\n✅ Nenhum item com desconto acima de {self.min_discount}% encontrado")
                
                # Aguardar próximo check
                if max_iterations is None or iteration < max_iterations:
                    print(f"\n⏳ Aguardando {check_interval} segundos para próxima verificação...")
                    time.sleep(check_interval)
                    
        except KeyboardInterrupt:
            print("\n\n🛑 Bot interrompido pelo usuário")
        except Exception as e:
            print(f"\n❌ Erro inesperado: {e}")
            import traceback
            traceback.print_exc()
        finally:
            self.disconnect_rabbitmq()
    
    def single_check(self):
        """
        Realiza uma única verificação e envia para a fila
        """
        # Conectar ao RabbitMQ
        if not self.connect_rabbitmq():
            print("❌ Não foi possível conectar ao RabbitMQ. Encerrando...")
            return

        search_types = []
        if self.search_rifles:
            search_types.append("Rifle")
        if self.search_knives:
            search_types.append("Faca")
        
        if not search_types:
            print("❌ Nenhum tipo de item selecionado para busca!")
            return
        
        search_desc = " e ".join([
            "🔫 Rifles" if t == "Rifle" else "🔪 Facas" 
            for t in search_types
        ])
        
        print(f"🔍 Realizando verificação única...")
        print(f"🎯 Buscando: {search_desc}\n")
        
        items_sent = 0
        
        try:
            for item_type in search_types:
                type_label = "🔫 Rifles" if item_type == "Rifle" else "🔪 Facas"
                print(f"\n📋 Buscando {type_label}...")
                
                result = self.search_items(item_type=item_type, page=1)
                
                if not result:
                    print(f"❌ Falha ao obter resultados para {type_label}")
                    continue
                
                items = result.get("results", [])
                total_items = result.get("count", 0)
                
                print(f"📦 Total de {type_label} encontrados: {total_items}")
                
                high_discount_items = self.filter_high_discount_items(items)
                
                if high_discount_items:
                    print(f"🔥 {len(high_discount_items)} {type_label} com desconto acima de {self.min_discount}%")
                    
                    # Enviar cada item para a fila
                    for item in high_discount_items:
                        item_name = item.get("market_hash_name", "Unknown")
                        item_id = item.get("_id")
                        discount = item.get("discount", 0)
                        price = item.get("price", 0)
                        
                        # Preparar dados para a fila
                        queue_data = self.prepare_item_for_queue(item)
                        
                        # Enviar para RabbitMQ
                        if self.send_to_queue(queue_data):
                            items_sent += 1
                            print(f"  ✅ Enviado: {item_name} | ${price:.2f} | {discount}% OFF (ID: {item_id})")
                        else:
                            print(f"  ❌ Falha ao enviar: {item_name} (ID: {item_id})")
                else:
                    print(f"✅ Nenhum(a) {type_label} com desconto acima de {self.min_discount}%")
            
            if items_sent > 0:
                print(f"\n📨 Total de {items_sent} item(s) enviado(s) para a fila")
            else:
                print(f"\n✅ Nenhum item com desconto acima de {self.min_discount}% encontrado")
                
        finally:
            self.disconnect_rabbitmq()


def main():
    """
    Função principal
    """
    # Configurações do RabbitMQ
    RABBITMQ_HOST = "localhost"
    RABBITMQ_PORT = 5672
    RABBITMQ_USER = "guest"
    RABBITMQ_PASSWORD = "guest"
    RABBITMQ_EXCHANGE = "skin.market.data"
    RABBITMQ_ROUTING_KEY = "skin.market.dashskins"

    # Criar instância do monitor
    monitor = DashSkinsMonitor(
        min_discount=30,
        rabbitmq_host=RABBITMQ_HOST,
        rabbitmq_port=RABBITMQ_PORT,
        rabbitmq_user=RABBITMQ_USER,
        rabbitmq_password=RABBITMQ_PASSWORD,
        rabbitmq_exchange=RABBITMQ_EXCHANGE,
        rabbitmq_routing_key=RABBITMQ_ROUTING_KEY,
        search_knives=True,
        search_rifles=True
    )

    # Opção 1: Verificação única
    # monitor.single_check()

    # Opção 2: Monitoramento contínuo (verifica a cada 60 segundos)
    monitor.monitor(check_interval=60)

    # Opção 3: Monitoramento com limite de iterações
    # monitor.monitor(check_interval=60, max_iterations=10)


if __name__ == "__main__":
    main()

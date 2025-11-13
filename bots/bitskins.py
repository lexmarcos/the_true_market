import requests
import time
import json
import pika
from typing import List, Dict, Any, Optional
from datetime import datetime
import urllib.parse


class BitSkinsMonitor:
    """
    Bot para monitorar descontos na API da BitSkins e enviar para RabbitMQ
    """
    
    def __init__(
        self,
        min_discount: int = 55,
        rabbitmq_host: str = "localhost",
        rabbitmq_port: int = 5672,
        rabbitmq_user: str = "guest",
        rabbitmq_password: str = "guest",
        rabbitmq_exchange: str = "skin.market.data",
        rabbitmq_routing_key: str = "skin.market.bitskins",
        search_knives: bool = True,
        search_weapons: bool = True,
        price_from: int = 10000,
        price_to: int = 25000000
    ):
        self.api_url = "https://api.bitskins.com/market/search/730"
        self.steam_market_url = "https://steamcommunity.com/market/priceoverview/"
        self.min_discount = min_discount
        self.headers = {
            "Content-Type": "application/json"
        }
        self.steam_cache = {}  # Cache para evitar muitas requisições à Steam

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
        self.search_weapons = search_weapons
        self.price_from = price_from
        self.price_to = price_to
        
    def build_payload(
        self, 
        offset: int = 0, 
        limit: int = 30,
        price_from: int = 10000,
        price_to: int = 25000000,
        item_type: str = "weapons"  # "weapons" ou "knives"
    ) -> Dict[str, Any]:
        """
        Constrói o payload da requisição
        
        Args:
            item_type: "weapons" para armas (AK-47, M4A1-S, M4A4) ou "knives" para facas
        """
        payload = {
            "order": [
                {
                    "field": "discount",
                    "order": "DESC"
                }
            ],
            "offset": offset,
            "limit": limit,
            "where": {}
        }
        
        if item_type == "knives":
            # Payload para facas
            payload["where"]["type_id"] = [1]
        else:
            # Payload para armas
            payload["where"]["exterior_id"] = [1, 2]  # Factory New (1), Minimal Wear (2)
            payload["where"]["typesub_id"] = [13, 42, 44]  # AK-47 (13), M4A1-S (42), M4A4 (44)
            payload["where"]["price_from"] = price_from
            payload["where"]["price_to"] = price_to
        
        return payload
    
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
            print(f"� Exchange: {self.rabbitmq_exchange} (tipo: topic)")
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
    
    def search_items(self, payload: Dict[str, Any]) -> Dict[str, Any]:
        """
        Faz a requisição para a API da BitSkins
        """
        try:
            response = requests.post(
                self.api_url, 
                json=payload, 
                headers=self.headers,
                timeout=10
            )
            response.raise_for_status()
            return response.json()
        except requests.exceptions.RequestException as e:
            print(f"❌ Erro na requisição: {e}")
            return None
    
    def get_steam_price(self, item_name: str) -> Optional[Dict[str, float]]:
        """
        Busca o preço do item na Steam Market
        Retorna um dicionário com lowest_price e median_price (última venda)
        """
        # Verificar cache primeiro
        if item_name in self.steam_cache:
            return self.steam_cache[item_name]
        
        try:
            # Codificar o nome do item para URL
            encoded_name = urllib.parse.quote(item_name)
            
            params = {
                "appid": 730,  # CS:GO/CS2
                "currency": 1,  # USD
                "market_hash_name": item_name
            }
            
            response = requests.get(
                self.steam_market_url,
                params=params,
                timeout=10
            )
            
            if response.status_code == 200:
                data = response.json()
                
                if data.get("success"):
                    prices = {}
                    
                    # Preço mais baixo disponível
                    lowest_price = data.get("lowest_price", "")
                    if lowest_price:
                        price_str = lowest_price.replace("$", "").replace(",", "").strip()
                        prices["lowest"] = float(price_str)
                    
                    # Preço mediano (última venda realizada)
                    median_price = data.get("median_price", "")
                    if median_price:
                        price_str = median_price.replace("$", "").replace(",", "").strip()
                        prices["median"] = float(price_str)
                    
                    if prices:
                        # Adicionar ao cache
                        self.steam_cache[item_name] = prices
                        return prices
            
            # Se falhar, adicionar None ao cache para não tentar novamente
            self.steam_cache[item_name] = None
            return None
            
        except Exception as e:
            print(f"⚠️  Erro ao buscar preço na Steam para '{item_name}': {e}")
            self.steam_cache[item_name] = None
            return None
    
    def calculate_steam_discount(self, bitskins_price: float, steam_price: float) -> int:
        """
        Calcula o desconto percentual em relação ao preço da Steam
        """
        if steam_price and steam_price > 0:
            discount = ((steam_price - bitskins_price) / steam_price) * 100
            return int(discount)
        return 0
    
    def calculate_steam_seller_receives(self, steam_price: float) -> float:
        """
        Calcula quanto o vendedor receberia na Steam após as taxas
        Steam cobra 15% de taxa total (5% Steam + 10% Publisher)
        """
        if steam_price and steam_price > 0:
            # Taxa de 15% (0.15)
            seller_receives = steam_price * 0.85
            return seller_receives
        return 0
    
    def calculate_resale_profit(self, bitskins_price: float, steam_seller_receives: float) -> tuple:
        """
        Calcula o lucro potencial comprando na BitSkins e vendendo na Steam
        Retorna (lucro_absoluto, lucro_percentual)
        """
        if steam_seller_receives and bitskins_price > 0:
            profit = steam_seller_receives - bitskins_price
            profit_percentage = (profit / bitskins_price) * 100
            return profit, profit_percentage
        return 0, 0
    
    def prepare_item_for_queue(self, item: Dict[str, Any]) -> Dict[str, Any]:
        """
        Prepara os dados do item no formato esperado pela fila
        """
        # Extrair informações básicas
        # BitSkins API retorna preço em milésimos de dólar, precisamos converter para centavos
        price_cents = item.get("price", 0) // 10  # Converter de milésimos para centavos
        item_id = item.get("id")
        asset_id = item.get("asset_id")
        
        # BitSkins não fornece float_value diretamente, apenas float_id
        # Seria necessário fazer uma requisição adicional para obter o float
        float_value = item.get("float_value")  # Geralmente None
        float_id = item.get("float_id")  # ID para buscar float em outra API
        
        # Paint info
        paint_seed = item.get("paint_seed")  # Geralmente não disponível na busca
        paint_index = item.get("paint_id")  # BitSkins usa 'paint_id' ao invés de 'paint_index'
        
        name = item.get("name", "Unknown")
        
        # Processar stickers
        stickers = []
        sticker_list = item.get("stickers", [])
        
        if isinstance(sticker_list, list):
            for sticker in sticker_list:
                sticker_data = {
                    "name": sticker.get("name", ""),
                    "slot": sticker.get("slot", 0),
                    "wear": sticker.get("wear", 0),
                    "skin_id": sticker.get("skin_id"),
                    "class_id": str(sticker.get("class_id", ""))
                }
                stickers.append(sticker_data)
        
        sticker_count = item.get("sticker_counter", len(stickers))
        
        # Montar objeto final
        queue_data = {
            "price": price_cents,
            "id": item_id,
            "asset_id": asset_id,
            "float_value": float_value,
            "float_id": float_id,  # Adicionar float_id para possível busca futura
            "paint_seed": paint_seed,
            "paint_index": paint_index,
            "stickers": stickers,
            "sticker_count": sticker_count,
            "name": name,
            "store": "bitskins",
            "currency": "USD"
        }
        
        return queue_data
    
    def filter_high_discount_items(self, items: List[Dict[str, Any]]) -> List[Dict[str, Any]]:
        """
        Filtra itens com desconto acima do mínimo configurado
        """
        return [
            item for item in items 
            if item.get("discount", 0) >= self.min_discount
        ]
    
    def format_item_info(self, item: Dict[str, Any]) -> str:
        """
        Formata as informações do item para exibição
        """
        name = item.get("name", "Unknown")
        price = item.get("price", 0) / 1000  # Convertendo para dólares
        discount = item.get("discount", 0)
        suggested_price = item.get("suggested_price", 0) / 1000  # Convertendo para dólares
        float_value = item.get("float_value", "N/A")
        sticker_count = item.get("sticker_counter", 0)
        
        # Buscar preço na Steam
        print(f"🔍 Consultando Steam Market para: {name}")
        steam_prices = self.get_steam_price(name)
        
        # Pequeno delay para não sobrecarregar a API da Steam
        time.sleep(0.5)
        
        info = f"""
{'='*60}
🎯 {name}
{'='*60}
💰 Preço BitSkins: ${price:.2f}
🏷️  Preço Sugerido BitSkins: ${suggested_price:.2f}
🔥 Desconto BitSkins: {discount}%"""
        
        if steam_prices:
            # Usar preço mediano (última venda) como referência principal
            median_price = steam_prices.get("median")
            lowest_price = steam_prices.get("lowest")
            
            # Preferir median_price, mas usar lowest_price se median não estiver disponível
            reference_price = median_price if median_price else lowest_price
            
            if reference_price:
                steam_discount = self.calculate_steam_discount(price, reference_price)
                steam_seller_receives = self.calculate_steam_seller_receives(reference_price)
                steam_fee = reference_price - steam_seller_receives
                profit, profit_percentage = self.calculate_resale_profit(price, steam_seller_receives)
                
                info += f"""
🎮 Preços Steam Market:"""
                
                if median_price:
                    info += f"""
   💰 Última Venda (Mediana): ${median_price:.2f}"""
                
                if lowest_price:
                    info += f"""
   💵 Menor Preço Atual: ${lowest_price:.2f}"""
                
                info += f"""
💵 Vendedor recebe na Steam (após 15% fee): ${steam_seller_receives:.2f}
💸 Taxa Steam (15%): ${steam_fee:.2f}
⚡ Desconto vs Steam: {steam_discount}%
💡 Economia vs Steam: ${reference_price - price:.2f}

{'='*60}
📈 ANÁLISE DE REVENDA (baseada na última venda)
{'='*60}"""
                
                if profit > 0:
                    info += f"""
✅ LUCRO POTENCIAL: ${profit:.2f} ({profit_percentage:.1f}%)
💰 Compra na BitSkins: ${price:.2f}
🏪 Vende na Steam por: ${reference_price:.2f}
💵 Você recebe: ${steam_seller_receives:.2f}
🎯 Lucro líquido: ${profit:.2f}"""
                else:
                    info += f"""
❌ SEM LUCRO: ${profit:.2f} ({profit_percentage:.1f}%)
⚠️  Não vale a pena revender na Steam"""
        else:
            info += f"""
🎮 Preço Steam Market: Não disponível"""
        
        info += f"""
📊 Float: {float_value}
🎨 Stickers: {sticker_count}
🆔 Item ID: {item.get("id")}
        """
        
        return info
    
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
        if self.search_weapons:
            search_types.append("weapons")
        if self.search_knives:
            search_types.append("knives")
        
        if not search_types:
            print("❌ Nenhum tipo de item selecionado para busca!")
            return
        
        search_desc = " e ".join([
            "🔫 Armas (AK-47, M4A1-S, M4A4)" if t == "weapons" else "🔪 Facas" 
            for t in search_types
        ])
        
        print(f"🤖 Bot iniciado - Monitorando descontos acima de {self.min_discount}%")
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
                    type_label = "🔫 Armas" if item_type == "weapons" else "🔪 Facas"
                    print(f"\n📋 Buscando {type_label}...")
                    
                    # Construir payload e fazer requisição
                    payload = self.build_payload(
                        item_type=item_type,
                        price_from=self.price_from,
                        price_to=self.price_to
                    )
                    result = self.search_items(payload)
                    
                    if not result:
                        print(f"⚠️  Nenhum resultado retornado para {type_label}")
                        continue
                    
                    # Extrair lista de itens
                    items = result.get("list", [])
                    total_items = result.get("counter", {}).get("filtered", 0)
                    
                    print(f"📦 Total de {type_label} encontrados: {total_items}")
                    
                    # Filtrar itens com alto desconto
                    high_discount_items = self.filter_high_discount_items(items)
                    
                    if high_discount_items:
                        print(f"🔥 {len(high_discount_items)} {type_label} com desconto acima de {self.min_discount}%")
                        
                        # Enviar cada item para a fila
                        for item in high_discount_items:
                            item_name = item.get("name", "Unknown")
                            item_id = item.get("id")

                            # Preparar dados para a fila
                            queue_data = self.prepare_item_for_queue(item)

                            # Enviar para RabbitMQ
                            if self.send_to_queue(queue_data):
                                items_sent += 1
                                print(f"  ✅ Enviado para fila: {item_name} (ID: {item_id})")
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
        finally:
            self.disconnect_rabbitmq()
    
    def single_check(self):
        """
        Realiza uma única verificação
        """
        # Conectar ao RabbitMQ
        if not self.connect_rabbitmq():
            print("❌ Não foi possível conectar ao RabbitMQ. Encerrando...")
            return

        search_types = []
        if self.search_weapons:
            search_types.append("weapons")
        if self.search_knives:
            search_types.append("knives")
        
        if not search_types:
            print("❌ Nenhum tipo de item selecionado para busca!")
            return
        
        search_desc = " e ".join([
            "🔫 Armas (AK-47, M4A1-S, M4A4)" if t == "weapons" else "🔪 Facas" 
            for t in search_types
        ])
        
        print(f"🔍 Realizando verificação única...")
        print(f"🎯 Buscando: {search_desc}\n")
        
        items_sent = 0
        
        try:
            for item_type in search_types:
                type_label = "🔫 Armas" if item_type == "weapons" else "🔪 Facas"
                print(f"\n📋 Buscando {type_label}...")

                payload = self.build_payload(
                    item_type=item_type,
                    price_from=self.price_from,
                    price_to=self.price_to
                )
                result = self.search_items(payload)
                
                if not result:
                    print(f"❌ Falha ao obter resultados para {type_label}")
                    continue
                
                items = result.get("list", [])
                total_items = result.get("counter", {}).get("filtered", 0)
                
                print(f"📦 Total de {type_label} encontrados: {total_items}")
                
                high_discount_items = self.filter_high_discount_items(items)
                
                if high_discount_items:
                    print(f"🔥 {len(high_discount_items)} {type_label} com desconto acima de {self.min_discount}%")
                    
                    # Enviar cada item para a fila
                    for item in high_discount_items:
                        item_name = item.get("name", "Unknown")
                        item_id = item.get("id")
                        
                        # Preparar dados para a fila
                        queue_data = self.prepare_item_for_queue(item)
                        
                        # Enviar para RabbitMQ
                        if self.send_to_queue(queue_data):
                            items_sent += 1
                            print(f"  ✅ Enviado para fila: {item_name} (ID: {item_id})")
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
    # Altere conforme sua configuração
    RABBITMQ_HOST = "localhost"
    RABBITMQ_PORT = 5672
    RABBITMQ_USER = "guest"
    RABBITMQ_PASSWORD = "guest"
    RABBITMQ_EXCHANGE = "skin.market.data"
    RABBITMQ_ROUTING_KEY = "skin.market.bitskins"
    
    # Criar instância do monitor
    monitor = BitSkinsMonitor(
        min_discount=55,
        rabbitmq_host=RABBITMQ_HOST,
        rabbitmq_port=RABBITMQ_PORT,
        rabbitmq_user=RABBITMQ_USER,
        rabbitmq_password=RABBITMQ_PASSWORD,
        rabbitmq_exchange=RABBITMQ_EXCHANGE,
        rabbitmq_routing_key=RABBITMQ_ROUTING_KEY,
        search_knives=True,
        search_weapons=True,
        price_from=10000,
        price_to=25000000
    )

    # Opção 1: Verificação única
    # monitor.single_check()

    # Opção 2: Monitoramento contínuo (verifica a cada 60 segundos)
    monitor.monitor(check_interval=60)

    # Opção 3: Monitoramento com limite de iterações
    # monitor.monitor(check_interval=60, max_iterations=10)


if __name__ == "__main__":
    main()
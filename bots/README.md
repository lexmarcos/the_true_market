# BitSkins Monitor Bot 🤖

Bot para monitorar descontos na API da BitSkins e enviar itens com alto desconto para um sistema de mensageria RabbitMQ usando **Topic Exchange** para roteamento inteligente.

## 🏗️ Arquitetura do Sistema

```
┌─────────────────────────────────────────────────────────────────┐
│                    RabbitMQ Topic Exchange                      │
│                   Exchange: skin.market.data                    │
└─────────────────────────────────────────────────────────────────┘
                              │
                              │
        ┌─────────────────────┼─────────────────────┐
        │                     │                     │
        ▼                     ▼                     ▼
┌───────────────┐    ┌───────────────┐    ┌───────────────┐
│   BitSkins    │    │   CSMoney     │    │   Skinport    │
│     Bot       │    │     Bot       │    │     Bot       │
├───────────────┤    ├───────────────┤    ├───────────────┤
│ Routing Key:  │    │ Routing Key:  │    │ Routing Key:  │
│ skin.market.  │    │ skin.market.  │    │ skin.market.  │
│   bitskins    │    │   csmoney     │    │   skinport    │
└───────────────┘    └───────────────┘    └───────────────┘
                              │
                              ▼
                    ┌───────────────────┐
                    │   Consumidores    │
                    ├───────────────────┤
                    │ • skin.market.*   │ ← Todas lojas
                    │ • skin.market.    │ ← BitSkins only
                    │     bitskins      │
                    │ • Filtros custom  │
                    └───────────────────┘
```

## 📋 Funcionalidades

- ✅ Monitora itens com desconto acima de um limite configurável (padrão: 55%)
- 🔫 Busca armas (AK-47, M4A1-S, M4A4)
- 🔪 Busca facas
- 📨 Envia itens para Exchange RabbitMQ com routing key específica
- 🔄 Monitoramento contínuo com intervalo configurável
- ⚡ Cache de consultas para otimização
- 🎯 **Topic Exchange** para roteamento inteligente por loja
- 🔑 **Routing Keys** para filtragem flexível de mensagens

## 🚀 Instalação

### Modo Rápido (com script auxiliar)

```bash
# Setup completo (primeira vez)
./manage.sh setup

# Iniciar o bot
./manage.sh run

# Em outro terminal, iniciar o consumidor (opcional)
./manage.sh consumer
```

### Modo Manual

### 1. Criar ambiente virtual

```bash
python3 -m venv venv
source venv/bin/activate  # Linux/Mac
# ou
venv\Scripts\activate  # Windows
```

### 2. Instalar dependências

```bash
pip install -r requirements.txt
```

### 3. Configurar RabbitMQ

#### Opção 1: Docker Compose (Recomendado)

```bash
# Subir o RabbitMQ com Docker Compose
docker-compose up -d

# Verificar se está rodando
docker-compose ps

# Ver logs
docker-compose logs -f rabbitmq

# Parar o RabbitMQ
docker-compose down

# Parar e remover volumes (dados)
docker-compose down -v
```

#### Opção 2: Docker direto

```bash
docker run -d --name rabbitmq -p 5672:5672 -p 15672:15672 rabbitmq:3-management
```

#### Opção 3: Instalação nativa (Ubuntu/Debian)

```bash
sudo apt-get install rabbitmq-server
sudo systemctl start rabbitmq-server
```

**Interface Web**: Após iniciar, acesse http://localhost:15672

- Usuário: `guest`
- Senha: `guest`

## ⚙️ Configuração

### Configuração do RabbitMQ Exchange

Este bot utiliza um **Topic Exchange** para roteamento inteligente de mensagens. Isso permite que múltiplos consumidores filtrem mensagens por loja de origem.

**Arquitetura:**
```
Exchange (topic): skin.market.data
├── Routing Key: skin.market.bitskins  ← Este bot
├── Routing Key: skin.market.csmoney
├── Routing Key: skin.market.skinport
├── Routing Key: skin.market.buff163
└── Routing Key: skin.market.*         (wildcard - todas as lojas)
```

### Configurar o Bot

Edite o arquivo `main.py` na função `main()`:

```python
RABBITMQ_HOST = "localhost"           # Host do RabbitMQ
RABBITMQ_PORT = 5672                  # Porta do RabbitMQ
RABBITMQ_USER = "guest"               # Usuário
RABBITMQ_PASSWORD = "guest"           # Senha
RABBITMQ_EXCHANGE = "skin.market.data"       # Nome do exchange
RABBITMQ_ROUTING_KEY = "skin.market.bitskins" # Routing key
```

### Como Funciona o Roteamento

1. **Publisher (Bot)**: Envia mensagens para o exchange `skin.market.data` com a routing key `skin.market.bitskins`
2. **Exchange**: Roteia as mensagens baseado nas routing keys
3. **Consumer**: Cria uma fila temporária e faz binding com as routing keys desejadas

**Exemplos de Consumo:**

```python
# Receber de TODAS as lojas
routing_key = "skin.market.*"

# Receber apenas da BitSkins
routing_key = "skin.market.bitskins"

# Receber de múltiplas lojas específicas
routing_keys = ["skin.market.bitskins", "skin.market.skinport"]
```

## 📊 Formato dos Dados Enviados

Os itens são enviados para a fila no seguinte formato JSON:

```json
{
  "price": 56900,
  "id": "6526102",
  "asset_id": "123456789",
  "float_value": 0.0123,
  "paint_seed": 123,
  "paint_index": 456,
  "stickers": [
    {
      "name": "Sticker | Team Dignitas (Holo) | DreamHack 2014",
      "slot": 0,
      "wear": 0,
      "skin_id": 8165,
      "class_id": "645332746"
    }
  ],
  "sticker_count": 5,
  "name": "AK-47 | Midnight Laminate (Factory New)",
  "store": "bitskins",
  "currency": "USD"
}
```

**Nota**: O preço é enviado em **centavos** (ex: 56900 = $56.90)

### Campos do JSON:

- `price`: Preço do item em centavos
- `id`: ID único do item na BitSkins
- `asset_id`: ID do asset na Steam
- `float_value`: Valor do float (desgaste) do item
- `paint_seed`: Seed do padrão da skin
- `paint_index`: Índice do padrão da skin
- `stickers`: Array de stickers aplicados no item
  - `name`: Nome do sticker
  - `slot`: Posição do sticker (0-4)
  - `wear`: Desgaste do sticker (0-1)
  - `skin_id`: ID da skin do sticker
  - `class_id`: ID da classe do sticker
- `sticker_count`: Número total de stickers
- `name`: Nome completo do item
- `store`: Loja de origem (`"bitskins"`)
- `currency`: Moeda utilizada (`"USD"`)

## 🎮 Uso

### Usando o script de gerenciamento

```bash
# Ver todos os comandos disponíveis
./manage.sh

# Configurar ambiente (primeira vez)
./manage.sh setup

# Iniciar RabbitMQ
./manage.sh start

# Ver status dos serviços
./manage.sh status

# Ver logs do RabbitMQ
./manage.sh logs

# Executar o bot
./manage.sh run

# Executar o consumidor
./manage.sh consumer

# Parar RabbitMQ
./manage.sh stop
```

### Modo Manual

#### Monitoramento Contínuo

```bash
source venv/bin/activate
python3 main.py
```

### Opções de Configuração

No arquivo `main.py`, você pode escolher diferentes modos:

```python
# Verificação única (armas e facas)
monitor.single_check(search_knives=True, search_weapons=True)

# Monitoramento contínuo (a cada 60 segundos)
monitor.monitor(check_interval=60, search_knives=True, search_weapons=True)

# Apenas facas
monitor.monitor(check_interval=60, search_knives=True, search_weapons=False)

# Apenas armas
monitor.monitor(check_interval=60, search_knives=False, search_weapons=True)

# Com limite de iterações
monitor.monitor(check_interval=60, max_iterations=10)
```

## 📦 Estrutura do Projeto

```
bitskins/
├── main.py                # Código principal do bot
├── consumer_example.py    # Exemplo de consumidor da fila
├── manage.sh             # Script de gerenciamento (auxiliar)
├── docker-compose.yml    # Configuração do RabbitMQ
├── requirements.txt      # Dependências Python
├── README.md            # Documentação
├── .gitignore           # Arquivos ignorados pelo Git
└── venv/                # Ambiente virtual (não versionado)
```

## 🔧 Parâmetros do Monitor

```python
BitSkinsMonitor(
    min_discount=55,                           # Desconto mínimo (%)
    rabbitmq_host="localhost",                 # Host RabbitMQ
    rabbitmq_port=5672,                        # Porta RabbitMQ
    rabbitmq_user="guest",                     # Usuário
    rabbitmq_password="guest",                 # Senha
    rabbitmq_exchange="skin.market.data",      # Nome do exchange
    rabbitmq_routing_key="skin.market.bitskins" # Routing key
)
```

## 🔄 Consumindo Mensagens

O arquivo `consumer_example.py` demonstra como consumir mensagens do exchange.

### Uso Básico

```bash
# Receber de TODAS as lojas
python3 consumer_example.py skin.market.*

# Receber apenas da BitSkins
python3 consumer_example.py skin.market.bitskins

# Receber sem argumentos (padrão: todas as lojas)
python3 consumer_example.py
```

### Vantagens do Topic Exchange

✅ **Escalabilidade**: Adicione novos bots sem modificar os existentes  
✅ **Flexibilidade**: Consumidores escolhem quais lojas monitorar  
✅ **Performance**: Roteamento eficiente baseado em padrões  
✅ **Separação**: Cada loja tem sua própria routing key  
✅ **Wildcards**: Use `*` para receber de todas as lojas ou `#` para padrões complexos

## 🐛 Troubleshooting

### Erro ao conectar ao RabbitMQ

- Verifique se o RabbitMQ está rodando: `sudo systemctl status rabbitmq-server`
- Verifique as credenciais no código
- Verifique se a porta 5672 está aberta

### Nenhum item encontrado

- Ajuste o parâmetro `min_discount` para um valor menor
- Verifique se a API da BitSkins está respondendo

## 📝 Licença

Este projeto é de uso livre.

## 🤝 Contribuindo

Contribuições são bem-vindas! Sinta-se à vontade para abrir issues ou pull requests.

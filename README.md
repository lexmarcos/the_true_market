# BitSkins Monitor Bot 🤖

Bot para monitorar descontos na API da BitSkins e enviar itens com alto desconto para uma fila RabbitMQ.

## 📋 Funcionalidades

- ✅ Monitora itens com desconto acima de um limite configurável (padrão: 55%)
- 🔫 Busca armas (AK-47, M4A1-S, M4A4)
- 🔪 Busca facas
- 📨 Envia itens para fila RabbitMQ
- 🔄 Monitoramento contínuo com intervalo configurável
- ⚡ Cache de consultas para otimização

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

Edite o arquivo `main.py` na função `main()`:

```python
RABBITMQ_HOST = "localhost"      # Host do RabbitMQ
RABBITMQ_PORT = 5672             # Porta do RabbitMQ
RABBITMQ_USER = "guest"          # Usuário
RABBITMQ_PASSWORD = "guest"      # Senha
RABBITMQ_QUEUE = "bitskins_items" # Nome da fila
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
  "name": "AK-47 | Midnight Laminate (Factory New)"
}
```

**Nota**: O preço é enviado em **centavos** (ex: 56900 = $56.90)

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
    min_discount=55,              # Desconto mínimo (%)
    rabbitmq_host="localhost",    # Host RabbitMQ
    rabbitmq_port=5672,           # Porta RabbitMQ
    rabbitmq_user="guest",        # Usuário
    rabbitmq_password="guest",    # Senha
    rabbitmq_queue="bitskins_items" # Nome da fila
)
```

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

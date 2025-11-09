# Docker Setup Guide

Este guia explica como subir o PostgreSQL e RabbitMQ usando Docker Compose para o projeto The True Market API.

## Pré-requisitos

- Docker instalado (versão 20.10 ou superior)
- Docker Compose instalado (versão 2.0 ou superior)

## Serviços Incluídos

### 1. PostgreSQL 15
- **Porta**: 5432
- **Database**: thetruemarket
- **Usuário**: postgres
- **Senha**: postgres
- **Container**: thetruemarket-postgres

### 2. RabbitMQ 3 (com Management UI)
- **Porta AMQP**: 5672
- **Porta Management UI**: 15672
- **Usuário**: guest
- **Senha**: guest
- **Container**: thetruemarket-rabbitmq

## Comandos

### Iniciar os serviços
```bash
docker-compose up -d
```

### Parar os serviços
```bash
docker-compose down
```

### Parar e remover volumes (ATENÇÃO: apaga os dados)
```bash
docker-compose down -v
```

### Ver logs dos serviços
```bash
# Todos os serviços
docker-compose logs -f

# Apenas PostgreSQL
docker-compose logs -f postgres

# Apenas RabbitMQ
docker-compose logs -f rabbitmq
```

### Verificar status dos serviços
```bash
docker-compose ps
```

### Reiniciar um serviço específico
```bash
docker-compose restart postgres
docker-compose restart rabbitmq
```

## Acessar os Serviços

### PostgreSQL
Conecte usando qualquer cliente PostgreSQL:
```
Host: localhost
Port: 5432
Database: thetruemarket
Username: postgres
Password: postgres
```

Ou via linha de comando:
```bash
docker exec -it thetruemarket-postgres psql -U postgres -d thetruemarket
```

### RabbitMQ Management UI
Acesse no navegador:
```
URL: http://localhost:15672
Username: guest
Password: guest
```

### RabbitMQ AMQP
A aplicação Spring Boot se conecta em:
```
Host: localhost
Port: 5672
Username: guest
Password: guest
Virtual Host: /
```

## Volumes

Os dados são persistidos em volumes Docker:
- `postgres_data`: Dados do PostgreSQL
- `rabbitmq_data`: Dados do RabbitMQ
- `rabbitmq_log`: Logs do RabbitMQ

## Health Checks

Ambos os serviços possuem health checks configurados:
- PostgreSQL: Verifica conexão a cada 10 segundos
- RabbitMQ: Ping diagnóstico a cada 10 segundos

## Troubleshooting

### Porta já em uso
Se as portas 5432 ou 5672 já estiverem em uso, você pode alterá-las no `docker-compose.yml`:
```yaml
ports:
  - "5433:5432"  # PostgreSQL em porta diferente
  - "5673:5672"  # RabbitMQ em porta diferente
```

Lembre-se de atualizar o `application.properties` também.

### Verificar se os containers estão rodando
```bash
docker ps | grep thetruemarket
```

### Recriar os containers
```bash
docker-compose down
docker-compose up -d --force-recreate
```

## Integração com a Aplicação

Certifique-se de que o `application.properties` está configurado corretamente:

```properties
# PostgreSQL
spring.datasource.url=jdbc:postgresql://localhost:5432/thetruemarket
spring.datasource.username=postgres
spring.datasource.password=postgres

# RabbitMQ
spring.rabbitmq.host=localhost
spring.rabbitmq.port=5672
spring.rabbitmq.username=guest
spring.rabbitmq.password=guest
spring.rabbitmq.virtual-host=/
```

## Produção

Para ambiente de produção, considere:
1. Alterar as senhas padrão
2. Configurar backups automáticos
3. Usar variáveis de ambiente ao invés de valores hardcoded
4. Configurar recursos (CPU/Memória) apropriados
5. Habilitar SSL/TLS para conexões

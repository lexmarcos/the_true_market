# 🚀 Guia de Início Rápido

## Primeira Vez (Setup Completo)

```bash
# 1. Clone ou navegue até o diretório
cd /home/lexmarcos/projects/bots/bitskins

# 2. Execute o setup automático
./manage.sh setup
```

Isso vai:

- ✅ Verificar se Docker está instalado
- ✅ Criar ambiente virtual Python
- ✅ Instalar dependências
- ✅ Iniciar RabbitMQ com Docker Compose
- ✅ Verificar se tudo está funcionando

## Executar o Bot

```bash
# Terminal 1: Executar o bot
./manage.sh run
```

## Monitorar as Mensagens (Opcional)

```bash
# Terminal 2: Executar o consumidor
./manage.sh consumer
```

## Acessar Interface Web do RabbitMQ

Abra no navegador: http://localhost:15672

- **Usuário**: guest
- **Senha**: guest

## Comandos Úteis

```bash
# Ver status dos serviços
./manage.sh status

# Ver logs do RabbitMQ
./manage.sh logs

# Parar RabbitMQ
./manage.sh stop

# Reiniciar RabbitMQ
./manage.sh restart
```

## Personalizar Configurações

Edite o arquivo `main.py` na função `main()`:

```python
# Alterar desconto mínimo
monitor = BitSkinsMonitor(min_discount=40)  # 40% ao invés de 55%

# Alterar intervalo de verificação
monitor.monitor(check_interval=30)  # 30 segundos

# Buscar apenas facas
monitor.monitor(search_knives=True, search_weapons=False)
```

## Troubleshooting

### RabbitMQ não inicia

```bash
# Verificar se a porta está em uso
sudo lsof -i :5672

# Ver logs detalhados
docker-compose logs rabbitmq

# Remover volumes e tentar novamente
docker-compose down -v
./manage.sh start
```

### Erro ao conectar

```bash
# Verificar se RabbitMQ está rodando
./manage.sh status

# Se não estiver, iniciar
./manage.sh start
```

### Ambiente virtual não funciona

```bash
# Remover e recriar
rm -rf venv
python3 -m venv venv
source venv/bin/activate
pip install -r requirements.txt
```

## Próximos Passos

1. ✅ Configurar o bot para rodar como serviço (systemd)
2. ✅ Criar seu próprio consumidor personalizado
3. ✅ Integrar com banco de dados
4. ✅ Adicionar notificações (Discord, Telegram, etc.)
5. ✅ Criar dashboard de monitoramento

## Estrutura das Mensagens

Cada item é enviado como JSON:

```json
{
  "price": 56900,
  "id": "6526102",
  "asset_id": "123456789",
  "float_value": 0.0123,
  "paint_seed": 123,
  "paint_index": 456,
  "stickers": [...],
  "sticker_count": 5,
  "name": "AK-47 | Midnight Laminate (Factory New)"
}
```

**Nota**: Preço está em centavos (56900 = $56.90)

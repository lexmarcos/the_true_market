#!/bin/bash

# Script auxiliar para gerenciar o ambiente do BitSkins Monitor

set -e

GREEN='\033[0;32m'
BLUE='\033[0;34m'
YELLOW='\033[1;33m'
RED='\033[0;31m'
NC='\033[0m' # No Color

echo -e "${BLUE}==================================================${NC}"
echo -e "${BLUE}   BitSkins Monitor - Gerenciador de Ambiente   ${NC}"
echo -e "${BLUE}==================================================${NC}"
echo ""

# Função para verificar se o Docker está instalado
check_docker() {
    if ! command -v docker &> /dev/null; then
        echo -e "${RED}❌ Docker não está instalado${NC}"
        echo "Instale o Docker: https://docs.docker.com/get-docker/"
        exit 1
    fi
    
    # Verificar se docker compose está disponível (versão nova ou antiga)
    if docker compose version &> /dev/null; then
        DOCKER_COMPOSE="docker compose"
    elif command -v docker-compose &> /dev/null; then
        DOCKER_COMPOSE="docker-compose"
    else
        echo -e "${RED}❌ Docker Compose não está instalado${NC}"
        echo "Instale o Docker Compose: https://docs.docker.com/compose/install/"
        exit 1
    fi
    
    echo -e "${GREEN}✅ Docker e Docker Compose encontrados${NC}"
}

# Função para verificar se o venv existe
check_venv() {
    if [ ! -d "venv" ]; then
        echo -e "${YELLOW}⚠️  Ambiente virtual não encontrado${NC}"
        echo -e "${BLUE}📦 Criando ambiente virtual...${NC}"
        python3 -m venv venv
        source venv/bin/activate
        pip install -r requirements.txt
        echo -e "${GREEN}✅ Ambiente virtual criado e dependências instaladas${NC}"
    else
        echo -e "${GREEN}✅ Ambiente virtual encontrado${NC}"
    fi
}

# Função para iniciar o RabbitMQ
start_rabbitmq() {
    echo -e "${BLUE}🐰 Iniciando RabbitMQ...${NC}"
    $DOCKER_COMPOSE up -d
    
    echo -e "${BLUE}⏳ Aguardando RabbitMQ ficar pronto...${NC}"
    sleep 5
    
    if $DOCKER_COMPOSE ps | grep -q "Up"; then
        echo -e "${GREEN}✅ RabbitMQ iniciado com sucesso${NC}"
        echo -e "${BLUE}🌐 Interface Web: http://localhost:15672${NC}"
        echo -e "${BLUE}   Usuário: guest | Senha: guest${NC}"
    else
        echo -e "${RED}❌ Falha ao iniciar RabbitMQ${NC}"
        $DOCKER_COMPOSE logs rabbitmq
        exit 1
    fi
}

# Função para parar o RabbitMQ
stop_rabbitmq() {
    echo -e "${YELLOW}🛑 Parando RabbitMQ...${NC}"
    $DOCKER_COMPOSE down
    echo -e "${GREEN}✅ RabbitMQ parado${NC}"
}

# Função para ver logs do RabbitMQ
logs_rabbitmq() {
    echo -e "${BLUE}📋 Logs do RabbitMQ:${NC}"
    $DOCKER_COMPOSE logs -f rabbitmq
}

# Função para executar o bot
run_bot() {
    check_venv
    echo -e "${BLUE}🤖 Iniciando BitSkins Monitor...${NC}"
    source venv/bin/activate
    python3 main.py
}

# Função para executar o consumidor
run_consumer() {
    check_venv
    echo -e "${BLUE}🎧 Iniciando Consumidor...${NC}"
    source venv/bin/activate
    python3 consumer_example.py
}

# Função para verificar status
status() {
    echo -e "${BLUE}📊 Status dos Serviços:${NC}"
    echo ""
    
    # Status do RabbitMQ
    if $DOCKER_COMPOSE ps 2>/dev/null | grep -q "Up"; then
        echo -e "RabbitMQ: ${GREEN}✅ Rodando${NC}"
    else
        echo -e "RabbitMQ: ${RED}❌ Parado${NC}"
    fi
    
    # Status do ambiente virtual
    if [ -d "venv" ]; then
        echo -e "Venv:     ${GREEN}✅ Criado${NC}"
    else
        echo -e "Venv:     ${YELLOW}⚠️  Não criado${NC}"
    fi
}

# Função de setup completo
setup() {
    echo -e "${BLUE}🔧 Configurando ambiente completo...${NC}"
    echo ""
    
    check_docker
    check_venv
    start_rabbitmq
    
    echo ""
    echo -e "${GREEN}==================================================${NC}"
    echo -e "${GREEN}✅ Ambiente configurado com sucesso!${NC}"
    echo -e "${GREEN}==================================================${NC}"
    echo ""
    echo -e "${BLUE}Próximos passos:${NC}"
    echo -e "  1. Execute: ${YELLOW}./manage.sh run${NC} para iniciar o bot"
    echo -e "  2. Em outro terminal: ${YELLOW}./manage.sh consumer${NC} para ver as mensagens"
    echo -e "  3. Acesse: ${YELLOW}http://localhost:15672${NC} para ver a interface do RabbitMQ"
}

# Menu principal
case "$1" in
    setup)
        setup
        ;;
    start)
        check_docker
        start_rabbitmq
        ;;
    stop)
        stop_rabbitmq
        ;;
    restart)
        stop_rabbitmq
        start_rabbitmq
        ;;
    logs)
        logs_rabbitmq
        ;;
    run)
        run_bot
        ;;
    consumer)
        run_consumer
        ;;
    status)
        status
        ;;
    *)
        echo "Uso: $0 {setup|start|stop|restart|logs|run|consumer|status}"
        echo ""
        echo "Comandos:"
        echo "  setup     - Configura todo o ambiente (primeira vez)"
        echo "  start     - Inicia o RabbitMQ"
        echo "  stop      - Para o RabbitMQ"
        echo "  restart   - Reinicia o RabbitMQ"
        echo "  logs      - Mostra logs do RabbitMQ"
        echo "  run       - Executa o bot"
        echo "  consumer  - Executa o consumidor de exemplo"
        echo "  status    - Mostra status dos serviços"
        exit 1
        ;;
esac

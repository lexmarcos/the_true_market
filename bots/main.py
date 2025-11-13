#!/usr/bin/env python3
"""
Orquestrador de Bots - The True Market
Gerencia e monitora a execução de múltiplos bots de skin markets
"""

import os
import sys
import time
import signal
import threading
from datetime import datetime
from typing import Dict, Any, Optional
from dotenv import load_dotenv
import traceback

# Importar os bots
from bitskins import BitSkinsMonitor
from dashskins_monitor import DashSkinsMonitor


class ColoredLogger:
    """Logger com cores para console"""

    # Cores ANSI
    RESET = "\033[0m"
    BOLD = "\033[1m"

    # Cores de texto
    BLACK = "\033[30m"
    RED = "\033[31m"
    GREEN = "\033[32m"
    YELLOW = "\033[33m"
    BLUE = "\033[34m"
    MAGENTA = "\033[35m"
    CYAN = "\033[36m"
    WHITE = "\033[37m"

    # Cores de fundo
    BG_RED = "\033[41m"
    BG_GREEN = "\033[42m"
    BG_YELLOW = "\033[43m"
    BG_BLUE = "\033[44m"

    @staticmethod
    def _get_timestamp() -> str:
        """Retorna timestamp formatado"""
        return datetime.now().strftime("%Y-%m-%d %H:%M:%S")

    @classmethod
    def info(cls, bot_name: str, message: str):
        """Log de informação"""
        timestamp = cls._get_timestamp()
        print(f"{cls.CYAN}[{timestamp}]{cls.RESET} {cls.BOLD}[{bot_name}]{cls.RESET} {message}")

    @classmethod
    def success(cls, bot_name: str, message: str):
        """Log de sucesso"""
        timestamp = cls._get_timestamp()
        print(f"{cls.GREEN}[{timestamp}]{cls.RESET} {cls.BOLD}[{bot_name}]{cls.RESET} {cls.GREEN}{message}{cls.RESET}")

    @classmethod
    def warning(cls, bot_name: str, message: str):
        """Log de aviso"""
        timestamp = cls._get_timestamp()
        print(f"{cls.YELLOW}[{timestamp}]{cls.RESET} {cls.BOLD}[{bot_name}]{cls.RESET} {cls.YELLOW}{message}{cls.RESET}")

    @classmethod
    def error(cls, bot_name: str, message: str):
        """Log de erro"""
        timestamp = cls._get_timestamp()
        print(f"{cls.RED}[{timestamp}]{cls.RESET} {cls.BOLD}[{bot_name}]{cls.RESET} {cls.RED}{message}{cls.RESET}")

    @classmethod
    def system(cls, message: str):
        """Log do sistema"""
        timestamp = cls._get_timestamp()
        print(f"{cls.MAGENTA}[{timestamp}]{cls.RESET} {cls.BOLD}[ORCHESTRATOR]{cls.RESET} {message}")


class BotRunner:
    """Gerenciador de execução de um bot individual"""

    def __init__(self, name: str, bot_instance: Any, check_interval: int):
        self.name = name
        self.bot_instance = bot_instance
        self.check_interval = check_interval
        self.thread: Optional[threading.Thread] = None
        self.running = False
        self.should_stop = False
        self.last_heartbeat = time.time()
        self.restart_count = 0
        self.max_restarts = 10
        self.restart_delay = 5  # segundos entre restarts

    def run(self):
        """Executa o bot em loop com auto-restart"""
        self.running = True
        ColoredLogger.success(self.name, "Iniciando bot...")

        while not self.should_stop:
            try:
                self.last_heartbeat = time.time()
                ColoredLogger.info(self.name, f"Executando monitoramento contínuo (intervalo: {self.check_interval}s)")

                # Executar o método monitor do bot
                self.bot_instance.monitor(check_interval=self.check_interval)

                # Se chegou aqui, o bot terminou normalmente (não deveria em modo contínuo)
                if not self.should_stop:
                    ColoredLogger.warning(self.name, "Bot terminou inesperadamente. Reiniciando em 5 segundos...")
                    time.sleep(5)

            except KeyboardInterrupt:
                # Ctrl+C - propagar para o orquestrador
                ColoredLogger.info(self.name, "Recebido sinal de interrupção")
                break

            except Exception as e:
                self.restart_count += 1
                ColoredLogger.error(self.name, f"Erro na execução: {e}")
                ColoredLogger.error(self.name, f"Traceback: {traceback.format_exc()}")

                if self.restart_count >= self.max_restarts:
                    ColoredLogger.error(self.name, f"Máximo de {self.max_restarts} restarts atingido. Parando bot.")
                    break

                if not self.should_stop:
                    ColoredLogger.warning(self.name, f"Tentativa {self.restart_count}/{self.max_restarts}: Reiniciando em {self.restart_delay} segundos...")
                    time.sleep(self.restart_delay)

        self.running = False
        ColoredLogger.info(self.name, "Bot finalizado")

    def start(self):
        """Inicia o bot em uma thread separada"""
        if self.thread and self.thread.is_alive():
            ColoredLogger.warning(self.name, "Bot já está rodando")
            return

        self.thread = threading.Thread(target=self.run, name=self.name, daemon=True)
        self.thread.start()

    def stop(self):
        """Para o bot"""
        ColoredLogger.info(self.name, "Solicitando parada do bot...")
        self.should_stop = True

    def is_healthy(self) -> bool:
        """Verifica se o bot está saudável"""
        # Considera saudável se está rodando e teve heartbeat nos últimos 5 minutos
        return self.running and (time.time() - self.last_heartbeat) < 300


class BotOrchestrator:
    """Orquestrador principal que gerencia todos os bots"""

    def __init__(self):
        self.bots: Dict[str, BotRunner] = {}
        self.running = False
        self.health_check_interval = 60  # segundos

        # Configurar handler para Ctrl+C
        signal.signal(signal.SIGINT, self._signal_handler)
        signal.signal(signal.SIGTERM, self._signal_handler)

    def _signal_handler(self, signum, frame):
        """Handler para sinais de interrupção"""
        ColoredLogger.system("🛑 Sinal de interrupção recebido. Finalizando bots...")
        self.stop()

    def add_bot(self, name: str, bot_instance: Any, check_interval: int):
        """Adiciona um bot ao orquestrador"""
        runner = BotRunner(name, bot_instance, check_interval)
        self.bots[name] = runner
        ColoredLogger.system(f"Bot '{name}' adicionado ao orquestrador")

    def start_all(self):
        """Inicia todos os bots"""
        if not self.bots:
            ColoredLogger.error("ORCHESTRATOR", "Nenhum bot configurado para iniciar")
            return

        ColoredLogger.system("=" * 80)
        ColoredLogger.system("🚀 INICIANDO ORQUESTRADOR DE BOTS - THE TRUE MARKET")
        ColoredLogger.system("=" * 80)
        ColoredLogger.system(f"Total de bots configurados: {len(self.bots)}")

        self.running = True

        # Iniciar todos os bots
        for name, runner in self.bots.items():
            runner.start()
            time.sleep(1)  # Pequeno delay entre inicializações

        ColoredLogger.system("✅ Todos os bots foram iniciados")
        ColoredLogger.system("💡 Pressione Ctrl+C para parar todos os bots")
        ColoredLogger.system("=" * 80)

        # Health check loop
        self._health_check_loop()

    def _health_check_loop(self):
        """Loop de health check dos bots"""
        last_check = time.time()

        try:
            while self.running:
                current_time = time.time()

                # Health check a cada intervalo
                if current_time - last_check >= self.health_check_interval:
                    self._perform_health_check()
                    last_check = current_time

                # Verificar se todos os bots ainda estão rodando
                all_stopped = all(not runner.running for runner in self.bots.values())
                if all_stopped and self.running:
                    ColoredLogger.warning("ORCHESTRATOR", "Todos os bots pararam. Finalizando orquestrador...")
                    break

                time.sleep(5)  # Sleep curto para responder rápido ao Ctrl+C

        except KeyboardInterrupt:
            ColoredLogger.system("Interrupção detectada no health check loop")

    def _perform_health_check(self):
        """Realiza health check de todos os bots"""
        ColoredLogger.system("🏥 Realizando health check dos bots...")

        for name, runner in self.bots.items():
            if runner.is_healthy():
                ColoredLogger.success(name, "✓ Bot saudável")
            else:
                ColoredLogger.warning(name, "⚠ Bot pode estar com problemas")

    def stop(self):
        """Para todos os bots"""
        ColoredLogger.system("🛑 Parando todos os bots...")
        self.running = False

        # Parar todos os bots
        for name, runner in self.bots.items():
            runner.stop()

        # Aguardar threads finalizarem (com timeout)
        ColoredLogger.system("⏳ Aguardando finalização das threads...")
        timeout = 10
        start_time = time.time()

        for name, runner in self.bots.items():
            if runner.thread and runner.thread.is_alive():
                remaining_time = timeout - (time.time() - start_time)
                if remaining_time > 0:
                    runner.thread.join(timeout=remaining_time)

                if runner.thread.is_alive():
                    ColoredLogger.warning(name, "Thread não finalizou no timeout")

        ColoredLogger.system("=" * 80)
        ColoredLogger.system("✅ ORQUESTRADOR FINALIZADO")
        ColoredLogger.system("=" * 80)


def str_to_bool(value: str) -> bool:
    """Converte string para boolean"""
    return value.lower() in ('true', '1', 'yes', 'sim', 'y', 's')


def load_config() -> Dict[str, Any]:
    """Carrega configurações do arquivo .env"""
    # Carregar variáveis de ambiente do .env
    load_dotenv()

    config = {
        # RabbitMQ
        'rabbitmq': {
            'host': os.getenv('RABBITMQ_HOST', 'localhost'),
            'port': int(os.getenv('RABBITMQ_PORT', '5672')),
            'user': os.getenv('RABBITMQ_USER', 'guest'),
            'password': os.getenv('RABBITMQ_PASSWORD', 'guest'),
            'exchange': os.getenv('RABBITMQ_EXCHANGE', 'skin.market.data'),
        },

        # BitSkins Bot
        'bitskins': {
            'enabled': str_to_bool(os.getenv('BITSKINS_ENABLED', 'true')),
            'min_discount': int(os.getenv('BITSKINS_MIN_DISCOUNT', '55')),
            'check_interval': int(os.getenv('BITSKINS_CHECK_INTERVAL', '60')),
            'search_knives': str_to_bool(os.getenv('BITSKINS_SEARCH_KNIVES', 'true')),
            'search_weapons': str_to_bool(os.getenv('BITSKINS_SEARCH_WEAPONS', 'true')),
            'price_from': int(os.getenv('BITSKINS_PRICE_FROM', '10000')),
            'price_to': int(os.getenv('BITSKINS_PRICE_TO', '25000000')),
        },

        # DashSkins Bot
        'dashskins': {
            'enabled': str_to_bool(os.getenv('DASHSKINS_ENABLED', 'true')),
            'min_discount': int(os.getenv('DASHSKINS_MIN_DISCOUNT', '30')),
            'check_interval': int(os.getenv('DASHSKINS_CHECK_INTERVAL', '60')),
        },
    }

    return config


def main():
    """Função principal"""
    ColoredLogger.system("🔧 Carregando configurações...")

    try:
        config = load_config()
    except Exception as e:
        ColoredLogger.error("ORCHESTRATOR", f"Erro ao carregar configurações: {e}")
        sys.exit(1)

    # Criar orquestrador
    orchestrator = BotOrchestrator()

    # Configurar e adicionar BitSkins bot
    if config['bitskins']['enabled']:
        try:
            bitskins_bot = BitSkinsMonitor(
                min_discount=config['bitskins']['min_discount'],
                rabbitmq_host=config['rabbitmq']['host'],
                rabbitmq_port=config['rabbitmq']['port'],
                rabbitmq_user=config['rabbitmq']['user'],
                rabbitmq_password=config['rabbitmq']['password'],
                rabbitmq_exchange=config['rabbitmq']['exchange'],
                rabbitmq_routing_key='skin.market.bitskins',
                search_knives=config['bitskins']['search_knives'],
                search_weapons=config['bitskins']['search_weapons'],
                price_from=config['bitskins']['price_from'],
                price_to=config['bitskins']['price_to']
            )
            orchestrator.add_bot(
                "BitSkins",
                bitskins_bot,
                config['bitskins']['check_interval']
            )
            ColoredLogger.success("ORCHESTRATOR", "✓ BitSkins bot configurado")
        except Exception as e:
            ColoredLogger.error("ORCHESTRATOR", f"Erro ao configurar BitSkins bot: {e}")
    else:
        ColoredLogger.info("ORCHESTRATOR", "BitSkins bot desabilitado")

    # Configurar e adicionar DashSkins bot
    if config['dashskins']['enabled']:
        try:
            dashskins_bot = DashSkinsMonitor(
                min_discount=config['dashskins']['min_discount'],
                rabbitmq_host=config['rabbitmq']['host'],
                rabbitmq_port=config['rabbitmq']['port'],
                rabbitmq_user=config['rabbitmq']['user'],
                rabbitmq_password=config['rabbitmq']['password'],
                rabbitmq_exchange=config['rabbitmq']['exchange'],
                rabbitmq_routing_key='skin.market.dashskins',
                search_knives=True,
                search_rifles=True
            )
            orchestrator.add_bot(
                "DashSkins",
                dashskins_bot,
                config['dashskins']['check_interval']
            )
            ColoredLogger.success("ORCHESTRATOR", "✓ DashSkins bot configurado")
        except Exception as e:
            ColoredLogger.error("ORCHESTRATOR", f"Erro ao configurar DashSkins bot: {e}")
    else:
        ColoredLogger.info("ORCHESTRATOR", "DashSkins bot desabilitado")

    # Iniciar todos os bots
    try:
        orchestrator.start_all()
    except KeyboardInterrupt:
        ColoredLogger.system("Interrupção detectada")
    except Exception as e:
        ColoredLogger.error("ORCHESTRATOR", f"Erro fatal: {e}")
        ColoredLogger.error("ORCHESTRATOR", f"Traceback: {traceback.format_exc()}")
    finally:
        orchestrator.stop()


if __name__ == "__main__":
    main()

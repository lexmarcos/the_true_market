# Steam Price Collector

Coletor automático de preços do Steam Market usando Puppeteer com perfil persistente e integração com API.

## Funcionalidades

- ✅ Navegador com perfil persistente (login Steam mantido entre execuções)
- ✅ Autenticação automática com Steam
- ✅ Scraping de preços do Steam Market
- ✅ Integração com API externa (TheTrueMarket)
- ✅ Sistema de logging estruturado com Pino
- ✅ Retry automático com backoff exponencial
- ✅ Rate limiting para evitar bloqueios
- ✅ Graceful shutdown
- ✅ TypeScript com types completos

## Estrutura do Projeto

```
consumer/
├── src/
│   ├── config/
│   │   └── config.ts           # Configurações e variáveis de ambiente
│   ├── services/
│   │   ├── browser.service.ts  # Gerenciamento do Puppeteer
│   │   ├── steam-auth.service.ts # Autenticação Steam
│   │   ├── steam-scraper.service.ts # Scraping de preços
│   │   └── api.service.ts      # Integração com API
│   ├── types/
│   │   └── index.ts            # Tipos TypeScript
│   ├── utils/
│   │   └── logger.ts           # Sistema de logging
│   └── index.ts                # Orquestrador principal
├── browser-data/               # Perfil persistente do Chrome
├── logs/                       # Arquivos de log
├── .env.example                # Template de variáveis de ambiente
├── tsconfig.json               # Configuração TypeScript
└── package.json                # Dependências do projeto
```

## Pré-requisitos

- Node.js 18+ (para suporte a fetch nativo)
- pnpm (ou npm/yarn)
- Conta Steam para login

## Instalação

1. Clone o repositório e navegue para a pasta:
```bash
cd /home/lexmarcos/projects/bots/bitskins/consumer
```

2. Instale as dependências:
```bash
pnpm install
```

3. Configure as variáveis de ambiente:
```bash
cp .env.example .env
```

4. Edite o arquivo `.env` com suas configurações:
```env
# API Configuration
API_URL=https://api.thetruemarket.com
API_KEY=sua_chave_api_aqui

# Browser Configuration
BROWSER_DATA_DIR=./browser-data
HEADLESS=false

# Scraping Configuration
POLL_INTERVAL_MS=60000          # 1 minuto entre coletas
RATE_LIMIT_DELAY_MS=3000        # 3 segundos entre itens

# Logging Configuration
LOG_LEVEL=info
LOG_DIR=./logs

# Steam Configuration
STEAM_LOGIN_TIMEOUT_MS=300000   # 5 minutos para fazer login
```

## Uso

### Desenvolvimento

Execute em modo de desenvolvimento com hot reload:
```bash
pnpm dev
```

### Produção

1. Compile o projeto:
```bash
pnpm build
```

2. Execute a versão compilada:
```bash
pnpm start
```

### Primeiro Uso

Na primeira execução:
1. O navegador Chrome será aberto automaticamente
2. Você será redirecionado para a página de login da Steam
3. Faça login normalmente (pode ser necessário autenticação de 2 fatores)
4. Após o login, o coletor iniciará automaticamente

**Importante**: O login fica salvo no perfil persistente (`browser-data/`), então você só precisa fazer login uma vez!

## Como Funciona

### 1. Inicialização
- Carrega configurações do `.env`
- Inicializa navegador Puppeteer com perfil persistente
- Verifica se usuário está logado na Steam
- Faz health check da API

### 2. Ciclo de Coleta
- Busca itens populares do CS:GO (ou outros jogos configurados)
- Coleta preços de cada item respeitando rate limiting
- Envia dados para a API externa
- Aguarda intervalo configurado antes da próxima coleta

### 3. Tratamento de Erros
- Retry automático em caso de falhas
- Backoff exponencial para evitar sobrecarga
- Logs detalhados de todos os erros
- Continuação automática após erros temporários

## Personalização

### Coletar itens específicos

Edite [src/index.ts:95](src/index.ts#L95):

```typescript
// Ao invés de buscar itens populares
const items = await steamScraperService.getPopularItems(this.page, 730);

// Você pode definir itens específicos
const items = [
  { appId: 730, marketHashName: 'AK-47 | Redline (Field-Tested)' },
  { appId: 730, marketHashName: 'AWP | Dragon Lore (Factory New)' },
  // ... mais itens
];
```

### Buscar itens por nome

```typescript
const items = await steamScraperService.searchItems(this.page, 'AK-47');
```

### Alterar App ID (jogos)

- `730` = CS:GO
- `440` = Team Fortress 2
- `570` = Dota 2

## Arquitetura

### Services

**BrowserService** ([src/services/browser.service.ts](src/services/browser.service.ts))
- Gerencia instância do Puppeteer
- Perfil persistente para manter sessão
- Stealth mode para evitar detecção

**SteamAuthService** ([src/services/steam-auth.service.ts](src/services/steam-auth.service.ts))
- Verifica status de login
- Aguarda login manual do usuário
- Extrai cookies de sessão

**SteamScraperService** ([src/services/steam-scraper.service.ts](src/services/steam-scraper.service.ts))
- Navega para páginas de itens
- Extrai dados (nome, preço, imagem)
- Busca e lista itens

**ApiService** ([src/services/api.service.ts](src/services/api.service.ts))
- Envia dados para API externa
- Retry automático com backoff
- Health checks

## Logs

Os logs são salvos em dois formatos:

1. **Console**: Saída colorida e formatada para desenvolvimento
2. **Arquivo**: `logs/app.log` em formato JSON para produção

Níveis de log disponíveis:
- `trace` - Detalhes extremos
- `debug` - Informações de debug
- `info` - Informações gerais (padrão)
- `warn` - Avisos
- `error` - Erros
- `fatal` - Erros críticos

## Troubleshooting

### Navegador não abre
- Verifique se `HEADLESS=false` no `.env`
- Instale dependências do Chrome: `sudo apt-get install -y chromium-browser`

### Login não persiste
- Verifique permissões da pasta `browser-data/`
- Delete a pasta e tente novamente

### Rate limiting / IP bloqueado
- Aumente `RATE_LIMIT_DELAY_MS` no `.env`
- Reduza frequência de coleta (`POLL_INTERVAL_MS`)

### Erros de API
- Verifique se `API_KEY` está correta
- Confirme que `API_URL` está acessível
- Verifique logs em `logs/app.log`

## Contribuindo

Sinta-se à vontade para abrir issues ou pull requests!

## Licença

MIT

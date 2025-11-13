# Postman Collection Guide

## Arquivos Criados

1. **postman_collection.json** - Coleção com todos os endpoints da API
2. **postman_environment.json** - Environment com variáveis de configuração

## Como Importar no Postman

### Importar a Coleção

1. Abra o Postman
2. Clique em **Import** (canto superior esquerdo)
3. Selecione o arquivo `postman_collection.json`
4. A coleção "CS2 Skin Market Analysis API" será criada

### Importar o Environment

1. No Postman, vá em **Environments** (barra lateral esquerda)
2. Clique em **Import**
3. Selecione o arquivo `postman_environment.json`
4. Selecione o environment "CS2 Skin Market - Local" no dropdown superior direito

## Endpoints Disponíveis

### 1. History Update Tasks

#### GET - Get Pending Tasks
```
GET http://localhost:8080/api/v1/history-update-tasks
```
Retorna todas as tarefas pendentes de atualização de histórico de preços.

**Response Example:**
```json
[
  {
    "id": 1,
    "skinName": "AK-47 | Redline",
    "wear": "FIELD_TESTED",
    "status": "WAITING",
    "createdAt": "2025-11-10T10:00:00",
    "finishedAt": null
  }
]
```

#### POST - Complete History Update Task
```
POST http://localhost:8080/api/v1/history-update-tasks/{taskId}/complete
```

**Request Body:**
```json
{
  "skinName": "AK-47 | Redline",
  "wear": "FIELD_TESTED",
  "averagePrice": 2500
}
```

**Notas:**
- `averagePrice` deve estar em **centavos de BRL**
- A API converterá automaticamente para USD
- `wear` aceita: `FACTORY_NEW`, `MINIMAL_WEAR`, `FIELD_TESTED`, `WELL_WORN`, `BATTLE_SCARRED`

---

### 2. Profitable Skins

#### GET - Get All Profitable Skins
```
GET http://localhost:8080/api/v1/skins/profitable
```
Retorna todas as skins com análise de lucro, ordenadas por lucro (maior primeiro).

**Response Example:**
```json
[
  {
    "skinId": "12345",
    "skinName": "AK-47 | Redline",
    "wear": "FIELD_TESTED",
    "marketPrice": 1500,
    "marketCurrency": "BRL",
    "marketSource": "BITSKINS",
    "steamAveragePrice": 850,
    "discountPercentage": 25.5,
    "profitPercentage": 10.5,
    "expectedGainUsd": 89,
    "hasHistory": true,
    "lastUpdated": "2025-11-10T10:30:00"
  }
]
```

#### GET - Get Profitable Skins with Filters
```
GET http://localhost:8080/api/v1/skins/profitable?minProfit=20&maxResults=10
```

**Query Parameters:**

| Parâmetro | Tipo | Descrição | Exemplo |
|-----------|------|-----------|---------|
| `minProfit` | Double | Lucro mínimo em % | `20` (20%) |
| `maxResults` | Integer | Limite de resultados | `10` |
| `sortBy` | String | Ordenar por: `profit`, `discount`, `gain` | `profit` |
| `order` | String | Ordem: `asc` ou `desc` | `desc` |

**Exemplos de Uso:**

1. **Top 10 com pelo menos 20% de lucro:**
```
GET /api/v1/skins/profitable?minProfit=20&maxResults=10
```

2. **Ordenar por desconto (maior desconto primeiro):**
```
GET /api/v1/skins/profitable?sortBy=discount&order=desc
```

3. **Top 20 com maior ganho esperado:**
```
GET /api/v1/skins/profitable?sortBy=gain&order=desc&maxResults=20
```

4. **Todos com lucro acima de 15%, ordenado ascendente:**
```
GET /api/v1/skins/profitable?minProfit=15&sortBy=profit&order=asc
```

## Campos da Response

### ProfitableSkinResponse

- **skinId**: ID único da skin
- **skinName**: Nome da skin (ex: "AK-47 | Redline")
- **wear**: Categoria de desgaste
- **marketPrice**: Preço no mercado (centavos, moeda original)
- **marketCurrency**: Moeda do preço (USD, BRL, etc)
- **marketSource**: Origem (BITSKINS, DASHSKINS, etc)
- **steamAveragePrice**: Preço médio Steam em USD (centavos)
- **discountPercentage**: % de desconto vs Steam
- **profitPercentage**: % de lucro líquido (após taxa de 15% da Steam)
- **expectedGainUsd**: Ganho esperado em centavos USD
- **hasHistory**: Se existe histórico de preços Steam
- **lastUpdated**: Última atualização dos dados

## Fórmulas de Cálculo

### Desconto
```
discountPercentage = ((steamPrice - marketPrice) / steamPrice) × 100
```

### Lucro (após taxa de 15% da Steam)
```
profitPercentage = discountPercentage - 15%
```

### Ganho Esperado
```
expectedGainUsd = (steamPrice × profitPercentage) / 100
```

## Configuração do Base URL

Se sua API estiver rodando em outra porta ou host:

1. Vá em **Environments** no Postman
2. Selecione "CS2 Skin Market - Local"
3. Edite a variável `base_url`
4. Exemplos:
   - Local: `http://localhost:8080`
   - Docker: `http://localhost:8080`
   - Produção: `https://api.exemplo.com`

## Troubleshooting

### Erro 404 - Not Found
- Verifique se a API está rodando
- Confirme que o `base_url` está correto
- Verifique se o contexto path está correto

### Erro 500 - Internal Server Error
- Verifique os logs da API
- Confirme que o banco de dados está acessível
- Verifique se há dados de skins salvos

### Nenhum resultado retornado
- Certifique-se de que há skins salvas no banco
- Verifique se há histórico de preços Steam cadastrado
- Tente sem filtros primeiro: `GET /api/v1/skins/profitable`

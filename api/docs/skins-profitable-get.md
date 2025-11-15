# GET /api/v1/skins/profitable

## Finalidade
Retorna uma lista de skins com análise de lucratividade, combinando preços de mercado e histórico da Steam. Ideal para bots que precisam decidir compras baseadas em margem e desconto.

## Método e URL
- **Método:** GET
- **URL:** `/api/v1/skins/profitable`

## Parâmetros de query
- `minProfit` (number, opcional): filtra resultados com `profitPercentage` maior ou igual ao valor informado (em pontos percentuais, por exemplo `12.5`).
- `maxResults` (integer, opcional): limita o total de itens retornados; precisa ser > 0.
- `sortBy` (string, opcional): campo de ordenação; aceita `profit` (padrão), `discount`, `gain`.
- `order` (string, opcional): direção da ordenação; `desc` (padrão) ou `asc`.

## Cabeçalhos esperados
- `Accept: application/json`

## Processamento interno
1. `GetProfitableSkinsUseCase` busca todas as skins persistidas.
2. Ignora skins sem preço configurado.
3. Para cada skin, tenta obter o histórico mais recente (`SteamPriceHistory`) com o mesmo nome e `wear`.
4. Se houver histórico, calcula desconto e lucro líquido via `ProfitCalculationService` aplicando a taxa da Steam (15%).
5. Aplica filtro `minProfit`, ordenação e limite conforme parâmetros.

## Resposta de sucesso (200)
Lista JSON com objetos `ProfitableSkinResponse`. Campos principais:
- `skinId` (string): identificador interno da skin.
- `skinName` (string): nome completo.
- `wear` (string): enum `Wear`.
- `floatValueCents` (number|null): valor do float em cents (× 10000). Exemplo: `0.1234` vira `1234`; no frontend, dividir por 10000 para obter `0.1234`.
- `marketPrice` (number): preço de venda listado (centavos na `marketCurrency`).
- `marketCurrency` (string): moeda do preço de mercado (ex.: `USD`, `BRL`).
- `marketSource` (string): marketplace de origem.
- `steamAveragePrice` (number|null): preço médio da Steam em centavos USD.
- `discountPercentage` (number|null): desconto relativo em pontos percentuais (ex.: `14.51`).
- `profitPercentage` (number|null): lucro esperado após taxas baseado no preço médio da Steam, em pontos percentuais (ex.: `8.73`).
- `expectedGainUsd` (number|null): ganho líquido previsto em centavos USD.
- `lastSalePrice` (number|null): preço da última venda na Steam em centavos USD.
- `lowestBuyOrderPrice` (number|null): preço da buy order mais barata na Steam em centavos USD.
- `profitPercentageVsLastSale` (number|null): lucro esperado após taxas baseado no preço da última venda, em pontos percentuais (ex.: `10.25`).
- `profitPercentageVsLowestBuyOrder` (number|null): lucro esperado após taxas baseado no preço da buy order mais barata, em pontos percentuais (ex.: `15.50`).
- `hasHistory` (boolean): indica se havia histórico na Steam.
- `lastUpdated` (string|null, ISO-8601): data da última atualização da skin no marketplace.

### Exemplo
```json
[
  {
    "skinId": "skin-001",
    "skinName": "AK-47 | Redline",
    "wear": "FIELD_TESTED",
    "floatValueCents": 2567,
    "marketPrice": 157500,
    "marketCurrency": "BRL",
    "marketSource": "BITSKINS",
    "link": "https://bitskins.com/item/123",
    "steamAveragePrice": 28999,
    "lastSalePrice": 27500,
    "lowestBuyOrderPrice": 26000,
    "discountPercentage": 45.62,
    "profitPercentage": 18.37,
    "profitPercentageVsLastSale": 12.25,
    "profitPercentageVsLowestBuyOrder": 8.50,
    "expectedGainUsd": 530,
    "hasHistory": true,
    "lastUpdated": "2025-11-11T20:14:55"
  },
  {
    "skinId": "skin-002",
    "skinName": "AWP | Asiimov",
    "wear": "BATTLE_SCARRED",
    "floatValueCents": null,
    "marketPrice": 9550,
    "marketCurrency": "USD",
    "marketSource": "DASHSKINS",
    "link": "https://dashskins.com/item/456",
    "steamAveragePrice": null,
    "lastSalePrice": null,
    "lowestBuyOrderPrice": null,
    "discountPercentage": null,
    "profitPercentage": null,
    "profitPercentageVsLastSale": null,
    "profitPercentageVsLowestBuyOrder": null,
    "expectedGainUsd": null,
    "hasHistory": false,
    "lastUpdated": "2025-11-10T09:02:17"
  }
]
```

## Considerações
- Quando não existe histórico na Steam, os campos de lucro retornam `null`, mas a skin ainda aparece para que o bot registre a ausência.
- Valores de porcentagem são pontos percentuais (ex.: `14.51` = 14,51%).
- Os campos `profitPercentageVsLastSale` e `profitPercentageVsLowestBuyOrder` podem ser `null` mesmo quando há histórico, caso esses preços específicos não estejam disponíveis.
- Todos os preços (`steamAveragePrice`, `lastSalePrice`, `lowestBuyOrderPrice`) são retornados em centavos USD.
- As porcentagens de profit já consideram a taxa de 15% da Steam.
- O campo `floatValueCents` é `null` quando o float value não está disponível. Quando presente, divide por 10000 para obter o float original (ex.: `2567 ÷ 10000 = 0.2567`).
- Erros inesperados no cálculo são propagados; monitore logs caso receba 5xx.

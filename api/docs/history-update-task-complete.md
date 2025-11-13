# POST /api/v1/history-update-tasks/{taskId}/complete

## Finalidade
Conclui uma tarefa de atualização de histórico inserindo o preço médio recente da Steam (enviado em centavos de BRL) e marcando a tarefa como `COMPLETED`. Use quando o bot já extraiu os dados necessários e deseja liberar a tarefa.

## Método e URL
- **Método:** POST
- **URL:** `/api/v1/history-update-tasks/{taskId}/complete`
- **Path parameter:** `taskId` (number) — ID retornado pelo endpoint de tarefas pendentes.

## Cabeçalhos esperados
- `Content-Type: application/json`
- `Accept: application/json`

## Corpo da requisição
```json
{
  "skinName": "AK-47 | Redline",
  "wear": "FIELD_TESTED",
  "averagePrice": 18990
}
```
Campos:
- `skinName` (string, obrigatório): deve coincidir com o nome salvo na tarefa.
- `wear` (string, obrigatório): enum `Wear`; use valores UPPERCASE (`FACTORY_NEW`, `FIELD_TESTED`, etc.).
- `averagePrice` (number, obrigatório): preço médio em centavos de BRL; precisa ser positivo.

## Processamento interno
1. O caso de uso `CompleteHistoryUpdateTaskUseCase` busca a tarefa pelo ID.
2. Valida se `skinName` e `wear` conferem com a tarefa; caso contrário, falha com `400`.
3. Converte `averagePrice` de BRL para USD usando `CurrencyConversionService`.
4. Persiste o histórico via `SteamPriceHistoryRepository`.
5. Atualiza o status da tarefa para `COMPLETED` e registra `finishedAt`.

## Respostas
### 200 — Sucesso
```json
{
  "message": "History update task completed successfully",
  "taskId": 42,
  "skinName": "AK-47 | Redline",
  "wear": "Field-Tested"
}
```
Notas:
- `wear` é devolvido com o display name amigável.

### 400 — Requisição inválida
Ocorrências comuns:
- Tarefa não encontrada.
- `skinName` ou `wear` diferentes do esperado.
- `averagePrice` nulo ou menor que zero (validado nas camadas inferiores).

Exemplo:
```json
{
  "message": "Error: Skin name mismatch. Task has 'AK-47 | Redline' but received 'AWP | Asiimov'",
  "taskId": 42
}
```

### 500 — Erro inesperado
Retorna mensagem `Internal server error: ...` com o ID da tarefa para diagnóstico.

## Recomendações
- Sempre recupere a tarefa mais recente antes de enviar a conclusão para evitar conflito de dados.
- Garanta idempotência: se a integração falhar após salvar o histórico, uma nova chamada para o mesmo `taskId` resultará em erro, pois a tarefa já estará `COMPLETED` (futuro comportamento a ser especificado).

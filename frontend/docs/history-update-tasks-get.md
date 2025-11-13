# GET /api/v1/history-update-tasks

## Finalidade
Retorna todas as tarefas de atualização de histórico que ainda estão na fila com status `WAITING`, ordenadas por data de criação (FIFO). Use este recurso para distribuir o trabalho entre bots que precisam buscar e processar dados de histórico de preço da Steam.

## Método e URL
- **Método:** GET
- **URL:** `/api/v1/history-update-tasks`

## Cabeçalhos esperados
- `Accept: application/json`

## Lógica de seleção
1. O caso de uso `GetPendingTasksUseCase` consulta o repositório de tarefas filtrando por `TaskStatus.WAITING` e ordenando por `createdAt`.
2. Cada tarefa é convertida para `HistoryUpdateTaskResponse` antes de ser retornada.

## Resposta de sucesso (200)
Lista JSON. Cada item contém:
- `id` (number): identificador único da tarefa.
- `skinName` (string): nome completo da skin.
- `wear` (string): enum `Wear` (`FACTORY_NEW`, `MINIMAL_WEAR`, etc.).
- `status` (string): sempre `WAITING` para esta rota.
- `createdAt` (string, ISO-8601): data e hora de criação.
- `finishedAt` (string|null, ISO-8601): permanece `null` enquanto a tarefa estiver pendente.

### Exemplo
```json
[
  {
    "id": 42,
    "skinName": "AK-47 | Redline",
    "wear": "FIELD_TESTED",
    "status": "WAITING",
    "createdAt": "2025-11-11T17:21:33.412",
    "finishedAt": null
  },
  {
    "id": 43,
    "skinName": "AWP | Asiimov",
    "wear": "BATTLE_SCARRED",
    "status": "WAITING",
    "createdAt": "2025-11-11T18:02:07.089",
    "finishedAt": null
  }
]
```

## Considerações
- Se não houver tarefas pendentes, a resposta é uma lista vazia (`[]`).
- Não há paginação configurada; caso seja necessário, implemente-a em nível de cliente.
- Atualmente a API não exige autenticação, mas pode ser adicionada posteriormente.

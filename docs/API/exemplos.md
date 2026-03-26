# Exemplo de Payloads e Respostas

## POST /avaliacao
### Request
```json
{
  "descricao": "A aula foi excelente, mas o áudio estava baixo.",
  "nota": 7
}
```

### Response
```json
{
  "id": "uuid",
  "descricao": "A aula foi excelente, mas o áudio estava baixo.",
  "nota": 7,
  "dataEnvio": "2026-03-25T14:00:00Z",
  "urgencia": "normal"
}
```

## Notificação de Urgência
```json
{
  "descricao": "Problema grave na plataforma.",
  "urgencia": "alta",
  "dataEnvio": "2026-03-25T14:00:00Z"
}
```

## Relatório Semanal
```json
{
  "descricao": "Resumo semanal de feedbacks.",
  "urgencia": "alta",
  "dataEnvio": "2026-03-25T14:00:00Z",
  "qtdAvaliacoesPorDia": {
    "2026-03-24": 5,
    "2026-03-25": 8
  },
  "qtdAvaliacoesPorUrgencia": {
    "alta": 2,
    "normal": 11
  }
}
```

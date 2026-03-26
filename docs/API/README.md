# Documentação da API — Plataforma de Feedback

## Endpoint: POST /avaliacao
Recebe um feedback do aluno.

### Exemplo de Request
```json
{
  "descricao": "A aula foi excelente, mas o áudio estava baixo.",
  "nota": 7
}
```

### Exemplo de Response (201)
```json
{
  "id": "uuid",
  "descricao": "A aula foi excelente, mas o áudio estava baixo.",
  "nota": 7,
  "dataEnvio": "2026-03-25T14:00:00Z",
  "urgencia": "normal"
}
```

## Dados para Notificação de Urgência
```json
{
  "descricao": "Problema grave na plataforma.",
  "urgencia": "alta",
  "dataEnvio": "2026-03-25T14:00:00Z"
}
```

## Dados para Relatório Semanal
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

> Atualize os exemplos conforme a implementação e mantenha a coleção de testes neste diretório.

# API Local

## POST /avaliacoes

Payload de exemplo disponível em [avaliacao-request.json](./avaliacao-request.json).

Resposta esperada:

```json
{
  "id": "uuid",
  "message": "Avaliação registrada com sucesso."
}
```

## Mensagens SQS

- [email-avaliacao-criada.json](./email-avaliacao-criada.json)
- [email-relatorio-gerado.json](./email-relatorio-gerado.json)

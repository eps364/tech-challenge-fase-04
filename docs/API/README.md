# API Local

## POST /avaliacao

Endpoint principal do Tech Challenge para registrar feedbacks.

Payload aceito:

```json
{
  "descricao": "A aula travou varias vezes e nao consegui acompanhar o conteudo.",
  "nota": 3
}
```

`nota` deve ser um numero inteiro entre `0` e `10`.

`POST /avaliacoes` continua disponivel como rota de compatibilidade.

Resposta esperada:

```json
{
  "id": "uuid",
  "message": "Avaliacao registrada com sucesso."
}
```

## Regra de urgencia

- `0` a `4`: `CRITICA`, dispara e-mail automatico aos administradores.
- `5` a `6`: `ALTA`.
- `7` a `8`: `MEDIA`.
- `9` a `10`: `BAIXA`.

## Mensagens SQS

- [email-avaliacao-critica.json](./email-avaliacao-critica.json)
- [email-relatorio-gerado.json](./email-relatorio-gerado.json)

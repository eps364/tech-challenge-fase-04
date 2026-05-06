# API Local

## POST /avaliacao

Endpoint principal do Tech Challenge para registrar feedbacks.

Payload minimo:

```json
{
  "descricao": "A aula travou varias vezes e nao consegui acompanhar o conteudo.",
  "nota": 3
}
```

Campos opcionais aceitos para enriquecer a demonstracao e enviar confirmacao ao estudante:

```json
{
  "descricao": "Excelente entrega do projeto.",
  "nota": 9,
  "nomeAluno": "Luiz Silva",
  "emailAluno": "luiz@email.com",
  "disciplina": "Arquitetura Java Serverless"
}
```

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

- [email-avaliacao-criada.json](./email-avaliacao-criada.json)
- [email-avaliacao-critica.json](./email-avaliacao-critica.json)
- [email-relatorio-gerado.json](./email-relatorio-gerado.json)

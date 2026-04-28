# Exemplos de Payloads

## Avaliação

```json
{
  "nomeAluno": "Luiz Silva",
  "emailAluno": "luiz@email.com",
  "disciplina": "Arquitetura Java Serverless",
  "nota": 9.5,
  "comentario": "Excelente entrega do projeto."
}
```

## E-mail de confirmação

```json
{
  "type": "AVALIACAO_CRIADA",
  "to": "luiz@email.com",
  "subject": "Avaliação registrada",
  "template": "avaliacao-criada",
  "payload": {
    "nomeAluno": "Luiz Silva",
    "disciplina": "Arquitetura Java Serverless",
    "nota": 9.5
  }
}
```

## E-mail de relatório

```json
{
  "type": "RELATORIO_GERADO",
  "to": "admin@example.com",
  "subject": "Relatório de avaliações",
  "template": "relatorio-avaliacoes",
  "payload": {
    "totalAvaliacoes": 10,
    "mediaNotas": 8.7,
    "maiorNota": 10,
    "menorNota": 6,
    "dataGeracao": "2026-04-28T10:00:00Z"
  }
}
```

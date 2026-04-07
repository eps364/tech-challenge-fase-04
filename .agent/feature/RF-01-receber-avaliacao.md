# RF-01 - Receber avaliacao via endpoint HTTP

## Objetivo
Disponibilizar o endpoint `POST /avaliacao` para receber feedback do aluno com os campos obrigatorios `descricao` e `nota`.

## Escopo funcional
- Expor endpoint HTTP para criacao de avaliacao.
- Receber payload JSON com os campos definidos no contrato da API.
- Encaminhar para validacao e persistencia.
- Retornar resposta padronizada de sucesso ou erro.

## O que sera realizado
1. Definir contrato de entrada (`descricao`, `nota`) em documentacao e exemplos.
2. Implementar fluxo de recebimento HTTP.
3. Garantir retorno `201 Created` em caso de sucesso.
4. Garantir retorno de erro estruturado para payload invalido.

## Entradas e saidas
### Entrada
```json
{
  "descricao": "A aula foi excelente, mas o audio estava baixo.",
  "nota": 7
}
```

### Saida esperada (sucesso)
```json
{
  "id": "uuid",
  "descricao": "A aula foi excelente, mas o audio estava baixo.",
  "nota": 7,
  "dataEnvio": "2026-03-25T14:00:00Z",
  "urgencia": "normal"
}
```

## Regras de negocio relacionadas
- A avaliacao deve conter texto descritivo.
- A nota deve estar no intervalo permitido pelo desafio (0 a 10).

## Criterios de aceite
- Endpoint responde no caminho correto (`POST /avaliacao`).
- Payload valido gera registro e resposta `201`.
- Payload invalido gera erro claro e rastreavel.
- Contrato documentado em `docs/API/README.md` e `docs/API/exemplos.md`.

## Evidencias esperadas
- Exemplo de request/response atualizado na documentacao.
- Registro do fluxo em diagrama de sequencia (`docs/diagrams/flows.mmd`).

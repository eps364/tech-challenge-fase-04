# RF-02 - Validar dados de entrada

## Objetivo
Validar tipos, campos obrigatorios e faixas de valores da requisicao de avaliacao para impedir dados inconsistentes.

## Escopo funcional
- Validar presenca de `descricao`.
- Validar presenca e faixa de `nota` (0 a 10).
- Validar tipo dos campos e formato do payload.
- Retornar mensagens de erro claras para o cliente.

## O que sera realizado
1. Definir regras de validacao por campo.
2. Padronizar mensagens de erro para facilitar depuracao.
3. Garantir bloqueio de persistencia quando houver invalidacao.
4. Documentar cenarios validos e invalidos.

## Regras de validacao
- `descricao`:
  - obrigatoria;
  - nao pode ser vazia.
- `nota`:
  - obrigatoria;
  - tipo numerico inteiro;
  - intervalo de 0 a 10.

## Cenarios de erro esperados
- Campo ausente.
- Tipo invalido.
- Nota fora da faixa.
- Payload malformado.

## Criterios de aceite
- Toda requisicao invalida deve retornar erro sem persistencia.
- Mensagens de erro devem informar o campo e a regra violada.
- Casos de validacao devem estar cobertos em testes.

## Evidencias esperadas
- Casos de teste para payload invalido.
- Exemplos de erro em `docs/API/exemplos.md`.

# RF-04 - Classificar urgencia e acionar notificacao

## Objetivo
Classificar automaticamente o nivel de urgencia da avaliacao e acionar notificacao para casos criticos.

## Escopo funcional
- Definir regra de classificacao de urgencia.
- Marcar cada feedback com nivel de urgencia.
- Disparar notificacao quando a classificacao for critica/alta.
- Registrar envio de notificacao para auditoria.

## O que sera realizado
1. Definir criterios de urgencia (ex.: por nota baixa e/ou palavras-chave).
2. Implementar classificacao no fluxo de recebimento.
3. Integrar com servico de notificacao.
4. Garantir resiliencia para falha no envio (log e alerta).

## Regras de negocio (macro)
- Feedback nao critico: persiste e conclui sem alerta urgente.
- Feedback critico: persiste e gera evento/notificacao para administracao.

## Criterios de aceite
- Toda avaliacao deve possuir classificacao de urgencia.
- Casos criticos devem gerar notificacao.
- Falhas de notificacao devem ser rastreaveis por logs/metricas.

## Evidencias esperadas
- Exemplo de payload de notificacao em `docs/API/README.md`.
- Fluxo atualizado em `docs/diagrams/flows.mmd`.

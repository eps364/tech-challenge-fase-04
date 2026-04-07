# RF-05 - Gerar relatorio semanal

## Objetivo
Consolidar periodicamente os feedbacks para analise, exibindo volume por dia e por urgencia.

## Escopo funcional
- Executar rotina agendada semanal.
- Ler feedbacks do periodo alvo.
- Consolidar metricas por dia e por urgencia.
- Produzir objeto de relatorio com dados resumidos.

## O que sera realizado
1. Definir janela de consolidacao semanal.
2. Implementar agregacoes (`qtdAvaliacoesPorDia`, `qtdAvaliacoesPorUrgencia`).
3. Publicar/entregar relatorio para administracao.
4. Registrar execucao da rotina para observabilidade.

## Saida esperada (exemplo)
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

## Criterios de aceite
- Rotina semanal executa sem intervencao manual.
- Relatorio contem consolidacao por dia e por urgencia.
- Resultado permite analise de tendencia e priorizacao.

## Evidencias esperadas
- Exemplo de relatorio em `docs/API/exemplos.md`.
- Descricao do fluxo no diagrama de arquitetura e fluxo.

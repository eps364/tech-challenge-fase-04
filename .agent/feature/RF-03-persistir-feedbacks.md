# RF-03 - Persistir feedbacks em base de dados

## Objetivo
Armazenar cada avaliacao recebida com os atributos necessarios para rastreabilidade e relatorios.

## Escopo funcional
- Persistir os dados validados da avaliacao.
- Garantir geracao/registro de identificador unico.
- Registrar data/hora de envio.
- Preparar dados para classificacao de urgencia e relatorio semanal.

## O que sera realizado
1. Definir modelo de dados minimo para feedback.
2. Implementar operacao de gravacao transacional.
3. Garantir retorno com dados persistidos.
4. Tratar falhas de persistencia com resposta adequada.

## Modelo minimo de dados
- `id`
- `descricao`
- `nota`
- `dataEnvio`
- `urgencia`

## Criterios de aceite
- Feedback valido deve ser persistido com sucesso.
- Em caso de falha de banco, retornar erro controlado.
- Dados gravados devem ser suficientes para o relatorio semanal.

## Evidencias esperadas
- Exemplo de resposta contendo `id` e `dataEnvio`.
- Descricao do armazenamento em documentacao tecnica.

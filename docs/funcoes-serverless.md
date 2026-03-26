# Documentação das Funções Serverless

## Função: Receber Feedback
- Trigger: HTTP (POST /avaliacao)
- Responsabilidade: Receber, validar e armazenar feedbacks
- Ações: Persistir no banco, acionar notificação se urgente

## Função: Notificação de Urgência
- Trigger: Evento (feedback urgente)
- Responsabilidade: Enviar e-mail/alerta para administradores
- Ações: Montar mensagem, enviar via serviço de notificação

## Função: Relatório Semanal
- Trigger: Agendamento (ex: cron semanal)
- Responsabilidade: Gerar relatório consolidado dos feedbacks
- Ações: Consultar banco, calcular médias, enviar relatório

> Detalhe a implementação, triggers e integrações conforme o ambiente cloud utilizado.

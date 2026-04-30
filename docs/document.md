# Qualidade dos cursos on-line - Tech Challenge Fase 04

## Equipe

| Nome                           | RM       |
|--------------------------------|----------|
| Emerson Pereira da Silva       | RM367268 |
| Luiz Octavio Tassinari Saraiva | RM367408 |

## 1. Contexto

O projeto implementa uma plataforma serverless para receber feedbacks de estudantes sobre aulas on-line, detectar avaliacoes criticas e enviar relatorios semanais para administradores.

## 2. Objetivo

Automatizar o recebimento de feedbacks, o envio de notificacoes para problemas criticos e a geracao de relatorios periodicos em ambiente de nuvem AWS.

## 3. Requisitos Funcionais

- Registrar feedbacks pelo endpoint `POST /avaliacao`.
- Validar `descricao` e `nota` entre `0` e `10`.
- Persistir feedbacks no DynamoDB.
- Classificar urgencia a partir da nota.
- Enviar notificacao automatica aos administradores quando a urgencia for `CRITICA`.
- Gerar relatorio semanal com media das notas, feedbacks enviados, quantidade por dia e quantidade por urgencia.

## 4. Requisitos Nao Funcionais

- Usar arquitetura serverless em cloud.
- Separar responsabilidades entre funcoes Lambda.
- Automatizar deploy com Terraform e GitHub Actions.
- Usar filas para desacoplar envio de e-mails.
- Monitorar erros de Lambdas e mensagens na DLQ com CloudWatch Alarms.
- Aplicar governanca basica por IAM, variaveis de ambiente e recursos gerenciados por Terraform.

## 5. Arquitetura

- `API Gateway`: exposicao HTTP do endpoint de entrada.
- `Lambda Avaliador`: valida, classifica urgencia, persiste feedback e publica notificacoes.
- `DynamoDB`: armazenamento gerenciado dos feedbacks.
- `SQS EmailQueue`: fila de envio assincrono de e-mails.
- `Lambda EmailSender`: consome mensagens da fila e envia e-mails via SES ou simulacao local.
- `EventBridge Scheduler`: aciona o relatorio semanal.
- `Lambda ReportsGenerator`: calcula os dados do relatorio semanal.
- `CloudWatch` e `SNS`: logs, dashboard e alarmes de erro.

## 6. Regra de Urgencia

- `0` a `4`: `CRITICA`, dispara alerta aos administradores.
- `5` a `6`: `ALTA`.
- `7` a `8`: `MEDIA`.
- `9` a `10`: `BAIXA`.

## 7. Deploy

```bash
mvn clean package
cd infra/terraform
terraform init
terraform plan -var-file=environments/dev.tfvars
terraform apply -var-file=environments/dev.tfvars
```

## 8. Execucao Local

```bash
cp .env.example .env
docker compose up -d
bash ./scripts/create-local-resources.sh
bash ./scripts/invoke-avaliador-local.sh
bash ./scripts/invoke-reports-local.sh
bash ./scripts/invoke-email-sender-local.sh
```

## 9. Monitoramento

O Terraform cria log groups para as Lambdas, alarmes de erro por funcao, alarme para mensagens visiveis na DLQ e um dashboard CloudWatch. Os alarmes publicam no topico SNS `monitoring-alerts`, com assinatura de e-mail configurada por `admin_alert_email`.

## 10. Referencias

- AWS Lambda
- Amazon API Gateway HTTP API
- Amazon DynamoDB
- Amazon SQS
- Amazon SES
- Amazon EventBridge Scheduler
- Amazon CloudWatch
- Terraform

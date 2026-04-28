# Tech Challenge Fase 04

Projeto acadêmico em Java 17 para AWS Serverless com três Lambdas, API Gateway, DynamoDB, SQS, SES, EventBridge Scheduler, Terraform, LocalStack e GitHub Actions.

## Visão geral

O sistema registra avaliações de alunos, persiste os dados no DynamoDB, solicita e-mails assíncronos via SQS e gera relatórios periódicos enviados por e-mail.

## Arquitetura

```text
API Gateway
   -> Lambda Avaliador
   -> DynamoDB
   -> SQS EmailQueue
   -> Lambda EmailSender
   -> Amazon SES

EventBridge Scheduler
   -> Lambda ReportsGenerator
   -> DynamoDB
   -> SQS EmailQueue
   -> Lambda EmailSender
   -> Amazon SES
```

## Tecnologias

- Java 17
- Maven multi-módulo
- AWS SDK for Java 2.x
- AWS Lambda Java Events/Core
- DynamoDB, SQS, SESv2, EventBridge Scheduler, API Gateway HTTP API
- Terraform
- Docker Compose + LocalStack
- JUnit 5 + Mockito
- SLF4J + Logback

## Estrutura de pastas

```text
.
├── .github/workflows
├── docs/API
├── infra/localstack
├── infra/terraform
├── lambdas/avaliador
├── lambdas/reports-generator
├── lambdas/email-sender
├── scripts
├── shared
├── docker-compose.yml
├── pom.xml
└── README.md
```

## Módulos

- `shared`: DTOs, modelos, serialização JSON, validação, adapters AWS e interfaces comuns.
- `lambdas/avaliador`: recebe `POST /avaliacoes`, valida payload, salva no DynamoDB e publica e-mail na SQS.
- `lambdas/reports-generator`: executa por agendamento, faz scan da tabela e publica relatório na SQS.
- `lambdas/email-sender`: consome a fila, monta e-mails e usa SES real ou simulado.

## Variáveis de ambiente

Use o arquivo `.env.example` como referência.

Variáveis principais:

- `AWS_REGION`
- `DYNAMODB_TABLE_NAME`
- `EMAIL_QUEUE_URL`
- `SES_FROM_EMAIL`
- `REPORT_RECIPIENT_EMAIL`
- `LOCALSTACK_ENDPOINT`

No host local use normalmente `http://localhost:4566`. Em containers pode ser necessário `http://localstack:4566`.

## Como rodar localmente

Pré-requisitos:

- Java 17
- Maven 3.9+
- Docker
- Terraform 1.6+
- `awslocal`
- `jq`

Passos:

```bash
cp .env.example .env
docker compose up -d
./scripts/create-local-resources.sh
./scripts/invoke-avaliador-local.sh
./scripts/invoke-reports-local.sh
./scripts/invoke-email-sender-local.sh
```

Comandos úteis:

```bash
awslocal dynamodb list-tables
awslocal sqs list-queues
awslocal lambda list-functions
awslocal ses list-identities
```

## Fluxo local esperado

1. A Lambda `avaliador` recebe o payload de [docs/API/avaliacao-request.json](./docs/API/avaliacao-request.json).
2. O registro é salvo na tabela DynamoDB local.
3. Uma mensagem é enviada para a fila `email-queue`.
4. A Lambda `email-sender` consome a mensagem e registra envio simulado.
5. A Lambda `reports-generator` consulta a tabela, calcula o resumo e publica novo e-mail na fila.

## Testes e build

```bash
mvn clean test
mvn clean package
```

Artefatos gerados:

- `lambdas/avaliador/target/avaliador.jar`
- `lambdas/reports-generator/target/reports-generator.jar`
- `lambdas/email-sender/target/email-sender.jar`

## Scripts auxiliares

- `scripts/build.sh`
- `scripts/package.sh`
- `scripts/start-localstack.sh`
- `scripts/create-local-resources.sh`
- `scripts/invoke-avaliador-local.sh`
- `scripts/invoke-reports-local.sh`
- `scripts/invoke-email-sender-local.sh`
- `scripts/deploy.sh`

## Infraestrutura Terraform

Arquivos em `infra/terraform` provisionam:

- DynamoDB
- SQS principal e DLQ
- IAM roles e policies mínimas
- 3 Lambdas Java
- HTTP API Gateway com `POST /avaliacoes`
- Event source mapping SQS -> EmailSender
- EventBridge Scheduler -> ReportsGenerator
- SES email identity
- CloudWatch Log Groups

Fluxo:

```bash
cd infra/terraform
terraform init
terraform plan -var-file=environments/dev.tfvars
terraform apply -var-file=environments/dev.tfvars
```

Saídas relevantes:

- `api_gateway_url`
- `dynamodb_table_name`
- `email_queue_url`
- `email_dlq_url`
- `avaliador_lambda_name`
- `reports_generator_lambda_name`
- `email_sender_lambda_name`

## SES

- Defina `SES_FROM_EMAIL` com um remetente verificado.
- Em contas novas, o SES pode estar em sandbox; nesse caso o destinatário também pode precisar estar verificado.
- Se optar por identidade de domínio, a verificação depende de DNS externo e precisa ser concluída manualmente na AWS.
- No LocalStack o envio é simulado por `FakeEmailService`.

## GitHub Actions

### CI

`/.github/workflows/ci.yml` executa:

- `mvn clean test`
- `mvn clean package -DskipTests`
- `terraform fmt -check`
- `terraform init -backend=false`
- `terraform validate`

### Deploy Dev

`/.github/workflows/deploy-dev.yml` executa build e `terraform apply` em `main` ou manualmente.

Secrets esperados:

- `AWS_ACCESS_KEY_ID`
- `AWS_SECRET_ACCESS_KEY`
- `AWS_REGION`
- `SES_FROM_EMAIL`
- `REPORT_RECIPIENT_EMAIL`

Para um cenário mais robusto, recomenda-se trocar access keys por OIDC + role assumida.

## Troubleshooting

- Se `terraform validate` reclamar de artefatos ausentes, rode `mvn clean package` antes.
- Se o `awslocal lambda create-function` falhar, confira se o Docker socket está acessível ao LocalStack.
- Se o SES real não enviar, valide sandbox, identidade verificada e região correta.
- O `ReportsGenerator` usa `scan` por simplicidade acadêmica; em produção o ideal é modelar consultas e índices adequados.

## Próximos passos

- Adicionar testes de integração com LocalStack em pipeline dedicada.
- Evoluir para templates de e-mail externos.
- Atualizar status da avaliação após envio do e-mail.
- Adicionar métricas e alarmes de falha em produção.

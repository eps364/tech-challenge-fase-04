# Qualidade dos cursos on-line - Tech Challenge Fase 04

## Equipe

| Nome                           | RM       |
|--------------------------------|----------|
| Emerson Pereira da Silva       | RM367268 |
| Luiz Octavio Tassinari Saraiva | RM367408 |

## 1. Introdução

O Tech Challenge da fase 04 propõe uma solução em nuvem para apoiar a melhoria contínua de cursos on-line. O problema central é permitir que estudantes registrem feedbacks sobre as aulas e que administradores recebam rapidamente notificações sobre avaliações críticas, além de relatórios periódicos com indicadores consolidados.

Esta entrega implementa uma plataforma serverless em Java 17 sobre AWS, com execução local via LocalStack para desenvolvimento e validação. A solução usa API Gateway, AWS Lambda, DynamoDB, SQS, SES, EventBridge Scheduler, CloudWatch, SNS, Terraform e GitHub Actions.

## 2. Objetivo

Automatizar o recebimento de feedbacks, a classificação de urgência, o envio de notificações administrativas e a geração de relatórios semanais em uma arquitetura serverless, com infraestrutura versionada e deploy automatizado.

Objetivos específicos:

- Receber avaliações pelo endpoint `POST /avaliacao`.
- Validar os campos obrigatórios `descricao` e `nota`.
- Classificar a urgência de acordo com a nota recebida.
- Persistir os feedbacks no DynamoDB.
- Enviar alerta automático quando uma avaliação for crítica.
- Gerar relatório semanal com média, volume por dia, volume por urgência e lista de feedbacks.
- Provisionar infraestrutura por Terraform.
- Automatizar build, testes e validação de infraestrutura por GitHub Actions.
- Disponibilizar collection Postman para testes manuais.

## 3. Requisitos atendidos

| Requisito do desafio | Implementação |
|----------------------|---------------|
| Ambiente de nuvem configurado | Terraform em `infra/terraform` provisiona recursos AWS. |
| Uso obrigatório de serverless | Três funções AWS Lambda: `avaliador`, `reports-generator` e `email-sender`. |
| Mínimo de duas funções serverless | A solução separa responsabilidades em três Lambdas. |
| Configuração de componentes de suporte | DynamoDB, SQS, DLQ, SES, EventBridge Scheduler, CloudWatch e SNS. |
| Deploy automatizado | Workflows em `.github/workflows` e script `scripts/deploy.sh`. |
| Aplicação monitorada | Log groups, alarmes CloudWatch, dashboard e tópico SNS de alertas. |
| Notificações automáticas para problemas críticos | Avaliações com nota menor que 5 publicam alerta na SQS para envio por e-mail. |
| Relatório semanal dos feedbacks | EventBridge aciona a Lambda `reports-generator` semanalmente. |
| Documentação das funções e arquitetura | Este documento, diagramas em `docs/diagrams` e README do projeto. |
| Arquivo para testes de endpoints | `docs/TechChallenge-Fase04.postman_collection.json`. |

## 4. Modelo de cloud escolhido

A solução usa AWS serverless porque o domínio do problema possui carga variável e eventos bem definidos: entrada HTTP, processamento assíncrono de e-mails e execução agendada de relatórios. Esse modelo reduz a necessidade de gerenciar servidores, permite pagar por uso e favorece separação de responsabilidades.

Componentes principais:

- `API Gateway HTTP API`: expõe a entrada HTTP pública da aplicação.
- `Lambda Avaliador`: valida, classifica e persiste avaliações.
- `DynamoDB`: armazena feedbacks com baixa operação administrativa.
- `SQS EmailQueue`: desacopla geração de eventos de e-mail do envio efetivo.
- `SQS DLQ`: recebe mensagens que falharem após tentativas de processamento.
- `Lambda EmailSender`: consome mensagens da fila e envia e-mails pelo SES.
- `SES`: serviço gerenciado de envio de e-mails.
- `EventBridge Scheduler`: agenda a geração semanal de relatórios.
- `Lambda ReportsGenerator`: consolida feedbacks dos últimos sete dias.
- `CloudWatch`: centraliza logs, métricas, alarmes e dashboard.
- `SNS`: envia alertas de monitoramento para administradores.

## 5. Arquitetura

![Arquitetura do Sistema](diagrams/architecture.svg)

A arquitetura evita um serviço monolítico. A entrada HTTP, o cálculo de relatórios e o envio de e-mails ficam em funções independentes, conectadas por recursos gerenciados.

## 6. Fluxos principais

![Fluxos Principais](diagrams/flows.svg)

### 6.1 Registro de avaliação

### 6.1 Registro de avaliação

1. O estudante envia `POST /avaliacao` com `descricao` e `nota`.
2. O API Gateway invoca a Lambda `avaliador`.
3. A Lambda valida o payload.
4. A urgência é calculada pela nota.
5. O feedback é persistido no DynamoDB.
6. Se a urgência for `CRITICA`, uma mensagem é publicada na SQS.
7. A API retorna `201 Created` com o ID da avaliação.

### 6.2 Notificação crítica

1. A mensagem `AVALIACAO_CRITICA` entra na `email-queue`.
2. O event source mapping SQS invoca a Lambda `email-sender`.
3. A Lambda renderiza o template de e-mail com descrição, urgência, data de envio e nota.
4. Em AWS, o envio é feito via SES. Em LocalStack, o envio é simulado por `FakeEmailService`.
5. Falhas de processamento retornam `batchItemFailures`, permitindo nova tentativa pela SQS.
6. Após o limite de tentativas, a mensagem pode ser enviada para a DLQ.

### 6.3 Relatório semanal

1. O EventBridge Scheduler dispara a Lambda `reports-generator` pela expressão `cron(0 12 ? * MON *)`.
2. A Lambda consulta o DynamoDB.
3. São considerados feedbacks criados nos últimos sete dias.
4. O relatório calcula total de avaliações, média das notas, quantidade por dia, quantidade por urgência e lista de feedbacks.
5. Uma mensagem `RELATORIO_GERADO` é publicada na SQS.
6. A Lambda `email-sender` envia o relatório aos administradores.

## 7. Funções serverless criadas

| Função | Handler | Trigger | Responsabilidade |
|--------|---------|---------|------------------|
| `avaliador` | `br.com.fiap.serverless.avaliador.handler.AvaliadorHandler::handleRequest` | API Gateway HTTP API | Receber feedback, validar entrada, classificar urgência, persistir e publicar alerta crítico. |
| `reports-generator` | `br.com.fiap.serverless.reports.handler.ReportsGeneratorHandler::handleRequest` | EventBridge Scheduler ou invocação manual | Gerar relatório semanal e publicar mensagem de e-mail. |
| `email-sender` | `br.com.fiap.serverless.email.handler.EmailSenderHandler::handleRequest` | SQS `email-queue` | Renderizar e enviar e-mails de alerta crítico e relatório semanal. |

Variáveis de ambiente principais:

- `AWS_REGION`
- `DYNAMODB_TABLE_NAME`
- `EMAIL_QUEUE_URL`
- `SES_FROM_EMAIL`
- `REPORT_RECIPIENT_EMAIL`
- `ADMIN_ALERT_EMAIL`
- `LOCALSTACK_ENDPOINT` para execução local

## 8. API de entrada

### `POST /avaliacao`

Registra um feedback de estudante.

Payload:

```json
{
  "descricao": "A aula travou varias vezes e nao consegui acompanhar o conteudo.",
  "nota": 3
}
```

Regras de validação:

- `descricao` é obrigatória e não pode estar em branco.
- `nota` é obrigatória.
- `nota` deve ser um inteiro entre `0` e `10`.

Resposta de sucesso:

```json
{
  "id": "uuid",
  "message": "Avaliacao registrada com sucesso."
}
```

Resposta de erro:

```json
{
  "message": "nota must be between 0 and 10"
}
```

### `POST /avaliacoes`

Rota de compatibilidade que usa o mesmo handler da rota principal.

## 9. Regras de negócio

Classificação de urgência:

| Nota | Urgência | Ação |
|------|----------|------|
| `0` a `4` | `CRITICA` | Persiste feedback e publica alerta administrativo. |
| `5` a `6` | `ALTA` | Persiste feedback sem alerta crítico. |
| `7` a `8` | `MEDIA` | Persiste feedback sem alerta crítico. |
| `9` a `10` | `BAIXA` | Persiste feedback sem alerta crítico. |

Dados enviados no e-mail de alerta crítico:

- Descrição.
- Urgência.
- Data de envio.
- Nota.

Dados enviados no relatório semanal:

- Total de avaliações.
- Média das notas.
- Quantidade de avaliações por dia.
- Quantidade de avaliações por urgência.
- Feedbacks com descrição, urgência e data de envio.
- Data de geração do relatório.

## 10. Persistência e mensageria

### DynamoDB

Tabela: `avaliacoes`

Chave primária:

- `id` (`S`)

Campos persistidos:

- `id`
- `descricao`
- `nota`
- `urgencia`
- `status`
- `createdAt`
- `updatedAt`

O relatório semanal usa `scan` por simplicidade acadêmica. Em produção, o ideal seria modelar chave de partição/sort key ou índices para consultas por período.

### SQS

Filas:

- `email-queue`: fila principal de mensagens de e-mail.
- `email-queue-dlq`: fila de mensagens que excederem o número máximo de tentativas.

Tipos de mensagem:

- `AVALIACAO_CRITICA`
- `RELATORIO_GERADO`

## 11. Segurança e governança

Práticas aplicadas:

- Roles IAM separadas por Lambda.
- Policies com permissões específicas para cada função.
- Secrets e e-mails parametrizados por variáveis de ambiente e variáveis Terraform.
- Infraestrutura declarada em Terraform, evitando configuração manual não rastreável.
- SES exige identidade de e-mail verificada para envio real.
- API Gateway expõe apenas rotas de escrita de avaliação; não há endpoint público para leitura administrativa dos dados.
- CloudWatch e SNS fornecem rastreabilidade operacional e alerta de falhas.

Observação: a API de feedback é pública para permitir envio direto por estudantes no escopo acadêmico. O controle de acesso administrativo fica restrito aos recursos AWS, IAM, monitoramento e pipelines.

## 12. Monitoramento

O Terraform cria:

- Log groups das três Lambdas com retenção de 14 dias.
- Alarme CloudWatch para erros em cada Lambda.
- Alarme CloudWatch para mensagens visíveis na DLQ.
- Dashboard CloudWatch com erros das Lambdas e mensagens na DLQ.
- Tópico SNS `monitoring-alerts` com assinatura de e-mail administrativa.

Falhas monitoradas:

- Erros de execução na Lambda `avaliador`.
- Erros de execução na Lambda `reports-generator`.
- Erros de execução na Lambda `email-sender`.
- Mensagens acumuladas na DLQ de e-mail.

## 13. Deploy

### Build

```bash
mvn clean package
```

Artefatos gerados:

- `lambdas/avaliador/target/avaliador.jar`
- `lambdas/reports-generator/target/reports-generator.jar`
- `lambdas/email-sender/target/email-sender.jar`

### Terraform

```bash
cd infra/terraform
terraform init
terraform plan -var-file=environments/dev.tfvars
terraform apply -var-file=environments/dev.tfvars
```

Outputs relevantes:

- `api_gateway_url`
- `dynamodb_table_name`
- `email_queue_url`
- `email_dlq_url`
- `avaliador_lambda_name`
- `reports_generator_lambda_name`
- `email_sender_lambda_name`

Observacao: no output consolidado `project_summary` (em `infra/terraform/main.tf`), o campo correspondente aparece como `reports_lambda_name`.

### Script de deploy

```bash
./scripts/deploy.sh dev
```

## 14. Execução local

Pré-requisitos:

- Java 17.
- Maven 3.9+.
- Docker.
- Git Bash ou WSL no Windows para os scripts `.sh`.

Passos:

```bash
cp .env.example .env
./scripts/start-localstack.sh
./scripts/create-local-resources.sh
./scripts/validate-localstack.sh
./scripts/invoke-avaliador-local.sh
./scripts/invoke-reports-local.sh
./scripts/invoke-email-sender-local.sh
```

Diagnostico de acesso via browser (WSL/Windows):

```bash
bash ./scripts/diagnose-localstack-browser.sh
```

Interface grafica opcional para visualizar tabelas do DynamoDB:

```bash
docker compose up -d dynamodb-admin
```

Acesse: `http://localhost:8001`

O arquivo `.env.example` define valores padrão para LocalStack, DynamoDB, SQS, SES e e-mails administrativos.

## 15. GitHub Actions e GitFlow

O workflow `.github/workflows/ci.yml` executa:

- `mvn clean test`
- `mvn clean package -DskipTests`
- `terraform fmt -check`
- `terraform init -backend=false`
- `terraform validate`

Também existem workflows de deploy para dev e prod em `.github/workflows/deploy-dev.yml` e `.github/workflows/deploy-prod.yml`.

O fluxo de branches está documentado em `docs/gitflow.md`:

- `develop`: integração das features.
- `main`: branch estável de release.
- Pull Requests são usados para integração e promoção.

## 16. Collection Postman

Arquivo:

```text
docs/TechChallenge-Fase04.postman_collection.json
```

A collection contém:

- Chamadas HTTP de `POST /avaliacao`.
- Chamada de compatibilidade `POST /avaliacoes`.
- Cenários de validação de erro.
- Invocação local das Lambdas pelo LocalStack.
- Consultas auxiliares em DynamoDB e SQS local.

Variáveis principais:

- `base_url`: preencher com o output Terraform `api_gateway_url` para testes na AWS.
- `localstack_url`: padrão `http://localhost.localstack.cloud:4566`.
- `dynamodb_table_name`: padrão `avaliacoes`.
- `email_queue_url`: URL da fila local.
- `email_dlq_url`: URL da DLQ local.
- `avaliador_lambda_name`: padrão `avaliador` para LocalStack.
- `reports_lambda_name`: padrão `reports-generator` para LocalStack (equivale ao output Terraform `reports_generator_lambda_name`).
- `email_sender_lambda_name`: padrão `email-sender` para LocalStack.

## 17. Práticas obedecidas

- Responsabilidade única por função Lambda.
- Maven multi-módulo para separar código compartilhado e funções executáveis.
- Validação explícita de entrada antes da persistência.
- DTOs e modelos imutáveis com `record`.
- Desacoplamento por fila SQS entre geração de eventos e envio de e-mails.
- DLQ para falhas de processamento assíncrono.
- Infraestrutura como código com Terraform.
- Build e validação automatizados no CI.
- Configuração por variáveis de ambiente.
- Observabilidade com logs, alarmes, dashboard e SNS.
- Execução local com LocalStack para reduzir dependência de cloud durante desenvolvimento.
- Documentação versionada em `docs/`.

## 18. Estrutura de arquivos relevante

```text
.
├── docs/
│   ├── TechChallenge-Fase04.postman_collection.json
│   ├── document.md
│   ├── API/
│   └── diagrams/
├── infra/
│   ├── localstack/
│   └── terraform/
├── lambdas/
│   ├── avaliador/
│   ├── email-sender/
│   └── reports-generator/
├── scripts/
├── shared/
├── docker-compose.yml
├── pom.xml
└── README.md
```

## 19. Como gerar PDF da documentação

O projeto já possui configuração do VS Code para exportação com `markdown-pdf-plus`:

- `.vscode/settings.json`
- `.vscode/markdown-pdf-plus-abnt.css`

Para gerar o PDF, abra `docs/document.md` no VS Code e use o comando da extensão Markdown PDF Plus. O arquivo Markdown é a fonte versionada da documentação de entrega.

## 20. Referências

- AWS Lambda.
- Amazon API Gateway HTTP API.
- Amazon DynamoDB.
- Amazon SQS e Dead-Letter Queue.
- Amazon SES.
- Amazon EventBridge Scheduler.
- Amazon CloudWatch.
- Amazon SNS.
- Terraform.
- LocalStack.

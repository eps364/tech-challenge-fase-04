# Tech Challenge Fase 04

Projeto acadêmico em Java 17 para AWS Serverless com três Lambdas, API Gateway, DynamoDB, SQS, SES, EventBridge Scheduler, Terraform, LocalStack e GitHub Actions.

## Visão geral

O sistema registra feedbacks de alunos, classifica a urgencia pela nota, persiste os dados no DynamoDB, envia alertas criticos por e-mail via SQS/SES e gera relatorios semanais para administradores.

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
- `lambdas/avaliador`: recebe `POST /avaliacao` e `POST /avaliacoes`, valida payload, classifica urgencia, salva no DynamoDB e publica e-mails na SQS.
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
- `ADMIN_ALERT_EMAIL`
- `LOCALSTACK_ENDPOINT`

No Windows com Lambda rodando via LocalStack, prefira `http://localhost.localstack.cloud:4566`. O uso de `http://localhost:4566` pode falhar quando a Lambda executa em container separado.

## Como rodar localmente

Pré-requisitos:

- Java 17
- Maven 3.9+
- Docker
- Terraform 1.6+

No Windows, prefira os scripts PowerShell (`.ps1`). Eles usam o `awslocal` de dentro do container do LocalStack, então você não precisa instalar `awslocal` ou `jq` localmente.

Passos:

```bash
cp .env.example .env
docker compose up -d
./scripts/create-local-resources.sh
./scripts/invoke-avaliador-local.sh
./scripts/invoke-reports-local.sh
./scripts/invoke-email-sender-local.sh
```

No Windows, execute estes mesmos scripts pelo Git Bash ou WSL. O repositório não inclui versões `.ps1` desses atalhos locais.

```powershell
Copy-Item .env.example .env
docker compose up -d
bash ./scripts/create-local-resources.sh
bash ./scripts/invoke-avaliador-local.sh
bash ./scripts/invoke-reports-local.sh
bash ./scripts/invoke-email-sender-local.sh
```

Se o Docker responder com `Acesso negado`, abra o PowerShell como administrador ou ajuste a permissão do Docker Desktop para o seu usuário.

Comandos úteis:

```bash
awslocal dynamodb list-tables
awslocal sqs list-queues
awslocal lambda list-functions
awslocal ses list-identities
```

### Consultando o DynamoDB local

A tabela criada localmente por padrÃ£o Ã© `avaliacoes`.

```bash
# listar as tabelas DynamoDB no LocalStack
awslocal dynamodb list-tables

# ver todo o conteudo da tabela
awslocal dynamodb scan --table-name avaliacoes

# ver apenas os itens retornados
awslocal dynamodb scan --table-name avaliacoes --query Items

# buscar um item especifico pela chave primaria id
awslocal dynamodb get-item --table-name avaliacoes --key '{"id":{"S":"SEU-ID-AQUI"}}'
```

Se preferir usar o AWS CLI comum apontando para o LocalStack:

```bash
aws dynamodb scan \
  --table-name avaliacoes \
  --endpoint-url http://localhost:4566 \
  --region us-east-1
```

## Fluxo local esperado

1. A Lambda `avaliador` recebe o payload de [docs/API/avaliacao-request.json](./docs/API/avaliacao-request.json).
2. O feedback e salvo na tabela DynamoDB local com urgencia calculada pela nota.
3. Se a urgencia for `CRITICA`, uma mensagem de alerta administrativo e enviada para a fila `email-queue`.
4. A Lambda `email-sender` consome a mensagem e registra envio simulado.
5. A Lambda `reports-generator` consulta a tabela, calcula o relatorio semanal e publica novo e-mail na fila.

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
- IAM roles e policies minimas separadas por Lambda
- 3 Lambdas Java
- HTTP API Gateway com `POST /avaliacao` e rota legada `POST /avaliacoes`
- Event source mapping SQS -> EmailSender
- EventBridge Scheduler -> ReportsGenerator
- SES email identity
- CloudWatch Log Groups, Alarms, Dashboard e SNS para alertas de monitoramento

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

`/.github/workflows/deploy-dev.yml` executa build e `terraform apply` em `develop` ou manualmente.

Secrets esperados:

- `AWS_ACCESS_KEY_ID`
- `AWS_SECRET_ACCESS_KEY`
- `AWS_REGION`
- `SES_FROM_EMAIL`
- `REPORT_RECIPIENT_EMAIL`
- `ADMIN_ALERT_EMAIL`

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
- Adicionar testes de contrato para API Gateway em pipeline dedicada.

## Setup Real no Windows com Git Bash

Este foi o setup mínimo necessário para executar o projeto localmente no Windows usando Git Bash, sem alterar o código da aplicação para contornar problemas de terminal.

### Dependências necessárias

- Docker Desktop
- Git for Windows
- Java JDK
- Maven
- Python com `pip`
- `awscli` e `awscli-local`
- `jq`

### Instalações usadas

Quando o `winget` estiver funcionando:

```bash
winget install Python.Python.3.13
winget install jqlang.jq
```

O `aws` e o `awslocal` ficaram mais estáveis no Git Bash quando instalados pelo `pip` no mesmo ambiente Python:

```bash
python -m pip install --user --upgrade awscli awscli-local
```

### Validar no Git Bash

```bash
python --version
python -m pip --version
aws --version
awslocal --version
jq --version
docker --version
docker compose version
```

### Descobrir o diretório de scripts do Python

```bash
python -c "import sysconfig; print(sysconfig.get_path('scripts', 'nt_user'))"
cygpath "$(python -c "import sysconfig; print(sysconfig.get_path('scripts', 'nt_user'))")"
```

No ambiente usado durante a configuração, o diretório retornado foi:

```bash
/c/Users/luizs/AppData/Roaming/Python/Python313/Scripts
```

### Adicionar o diretório do `pip --user` ao PATH do Git Bash

```bash
echo 'export PATH="/c/Users/luizs/AppData/Roaming/Python/Python313/Scripts:$PATH"' >> ~/.bashrc
source ~/.bashrc
hash -r
```

### Validar resolução dos executáveis

```bash
type -a aws
type -a awslocal
type -a jq
```

O esperado é que `aws` e `awslocal` apontem para o mesmo diretório de scripts do Python, e que `jq` apareça no PATH do Git Bash.

### Ordem prática para subir o projeto

```bash
cp .env.example .env
./scripts/start-localstack.sh
./scripts/create-local-resources.sh
./scripts/invoke-avaliador-local.sh
./scripts/invoke-reports-local.sh
./scripts/invoke-email-sender-local.sh
```

### Observações importantes

- Se o `winget` não estiver funcionando, repare ou reinstale o App Installer do Windows antes de continuar.
- O `jq` precisa estar visível no Git Bash, porque `invoke-avaliador-local.sh` usa `jq` para montar o payload JSON.
- O `awscli-local` sozinho não basta; no Git Bash ele deve estar alinhado com um `aws` funcional no mesmo ambiente Python.

## GitFlow

Este repositorio adota duas branches fixas:

- `main`: branch estavel para release.
- `develop`: branch de integracao.

Regras:

- novas implementacoes devem sair de `develop`;
- o merge da branch de trabalho deve acontecer por PR para `develop`;
- a promocao para release deve acontecer por PR de `develop` para `main`;
- `main` e `develop` nao devem receber commit direto.

O detalhamento do fluxo esta em [docs/gitflow.md](./docs/gitflow.md).

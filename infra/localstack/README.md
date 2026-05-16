# LocalStack

Este diretório contém o bootstrap local da stack AWS simulada.

## Fluxo recomendado

```bash
cp .env.example .env
./scripts/start-localstack.sh
./scripts/create-local-resources.sh
./scripts/validate-localstack.sh
./scripts/invoke-avaliador-local.sh
./scripts/invoke-reports-local.sh
./scripts/invoke-email-sender-local.sh
```

## Diagnostico browser (WSL/Windows)

Se o LocalStack funcionar no terminal, mas nao conectar no app.localstack.cloud:

```bash
bash ./scripts/diagnose-localstack-browser.sh
```

## Interface grafica para DynamoDB (opcional)

```bash
docker compose up -d dynamodb-admin
```

Abra no browser: [http://localhost:8001](http://localhost:8001)

## Observações

- O `EmailSender` usa `FakeEmailService` quando `LOCALSTACK_ENDPOINT` está definido.
- O SES local pode não reproduzir todas as restrições do serviço real.
- O `awslocal` no host e opcional: os scripts usam fallback para `docker exec ... awslocal` no container `tech-challenge-localstack`.

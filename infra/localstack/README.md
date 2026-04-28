# LocalStack

Este diretório contém o bootstrap local da stack AWS simulada.

## Fluxo recomendado

```bash
docker compose up -d
./scripts/create-local-resources.sh
./scripts/invoke-avaliador-local.sh
./scripts/invoke-reports-local.sh
```

## Observações

- O `EmailSender` usa `FakeEmailService` quando `LOCALSTACK_ENDPOINT` está definido.
- O SES local pode não reproduzir todas as restrições do serviço real.
- O `awslocal` deve estar instalado no ambiente do desenvolvedor.

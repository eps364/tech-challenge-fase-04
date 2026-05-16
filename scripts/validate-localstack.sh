#!/usr/bin/env bash
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(cd "${SCRIPT_DIR}/.." && pwd)"

# shellcheck source=/dev/null
source "${SCRIPT_DIR}/localstack-cli.sh"

cd "${PROJECT_ROOT}"

DYNAMODB_TABLE_NAME="${DYNAMODB_TABLE_NAME:-avaliacoes}"
EMAIL_QUEUE_NAME="${EMAIL_QUEUE_NAME:-email-queue}"
EMAIL_DLQ_NAME="${EMAIL_DLQ_NAME:-email-queue-dlq}"

check_cmd() {
  local name="$1"
  shift
  if "$@" >/dev/null 2>&1; then
    echo "OK: ${name}"
  else
    echo "FALHA: ${name}" >&2
    return 1
  fi
}

echo "Validando instalacao LocalStack..."
require_localstack_running

check_cmd "health endpoint" curl -fsS http://localhost.localstack.cloud:4566/_localstack/health
check_cmd "dynamodb table ${DYNAMODB_TABLE_NAME}" localstack_cli dynamodb describe-table --table-name "${DYNAMODB_TABLE_NAME}"
check_cmd "sqs queue ${EMAIL_QUEUE_NAME}" localstack_cli sqs get-queue-url --queue-name "${EMAIL_QUEUE_NAME}"
check_cmd "sqs queue ${EMAIL_DLQ_NAME}" localstack_cli sqs get-queue-url --queue-name "${EMAIL_DLQ_NAME}"
check_cmd "lambda avaliador" localstack_cli lambda get-function --function-name avaliador
check_cmd "lambda reports-generator" localstack_cli lambda get-function --function-name reports-generator
check_cmd "lambda email-sender" localstack_cli lambda get-function --function-name email-sender

TMP_A="/tmp/avaliador-validate.json"
TMP_R="/tmp/reports-validate.json"

localstack_cli lambda invoke \
  --function-name avaliador \
  --payload '{"body":"{\"descricao\":\"teste local\",\"nota\":3}"}' \
  "${TMP_A}" >/dev/null

localstack_cli lambda invoke \
  --function-name reports-generator \
  --payload '{}' \
  "${TMP_R}" >/dev/null

echo "OK: smoke invoke avaliador"
cat "${TMP_A}" || true
echo "OK: smoke invoke reports-generator"
cat "${TMP_R}" || true

echo "Validacao concluida com sucesso."

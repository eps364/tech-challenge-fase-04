#!/usr/bin/env bash
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(cd "${SCRIPT_DIR}/.." && pwd)"

# shellcheck source=/dev/null
source "${SCRIPT_DIR}/localstack-cli.sh"

cd "${PROJECT_ROOT}"

AWS_REGION="${AWS_REGION:-us-east-1}"
LOCALSTACK_ENDPOINT="${LOCALSTACK_ENDPOINT:-http://localhost.localstack.cloud:4566}"
DYNAMODB_TABLE_NAME="${DYNAMODB_TABLE_NAME:-avaliacoes}"
EMAIL_QUEUE_NAME="${EMAIL_QUEUE_NAME:-email-queue}"
EMAIL_DLQ_NAME="${EMAIL_DLQ_NAME:-email-queue-dlq}"
SES_FROM_EMAIL="${SES_FROM_EMAIL:-noreply@example.com}"
ADMIN_ALERT_EMAIL="${ADMIN_ALERT_EMAIL:-admin@example.com}"

require_localstack_running

localstack_cli dynamodb create-table \
  --table-name "${DYNAMODB_TABLE_NAME}" \
  --attribute-definitions AttributeName=id,AttributeType=S \
  --key-schema AttributeName=id,KeyType=HASH \
  --billing-mode PAY_PER_REQUEST >/dev/null 2>&1 || true

DLQ_URL="$(localstack_cli sqs create-queue --queue-name "${EMAIL_DLQ_NAME}" --query 'QueueUrl' --output text)"
DLQ_ARN="$(localstack_cli sqs get-queue-attributes --queue-url "${DLQ_URL}" --attribute-names QueueArn --query 'Attributes.QueueArn' --output text)"
REDRIVE_POLICY="$(printf '{"deadLetterTargetArn":"%s","maxReceiveCount":"3"}' "${DLQ_ARN}")"

if ! localstack_cli sqs create-queue --queue-name "${EMAIL_QUEUE_NAME}" --attributes "RedrivePolicy=${REDRIVE_POLICY}" >/dev/null 2>&1; then
  echo "Aviso: nao foi possivel aplicar RedrivePolicy no LocalStack; criando a fila principal sem DLQ vinculada."
  localstack_cli sqs create-queue --queue-name "${EMAIL_QUEUE_NAME}" >/dev/null 2>&1 || true
fi

QUEUE_URL="$(localstack_cli sqs get-queue-url --queue-name "${EMAIL_QUEUE_NAME}" --query 'QueueUrl' --output text)"
QUEUE_ARN="$(localstack_cli sqs get-queue-attributes --queue-url "${QUEUE_URL}" --attribute-names QueueArn --query 'Attributes.QueueArn' --output text)"

localstack_cli ses verify-email-identity --email-address "${SES_FROM_EMAIL}" >/dev/null 2>&1 || true

"${SCRIPT_DIR}/package.sh"

localstack_cli lambda create-function \
  --function-name avaliador \
  --runtime java17 \
  --role arn:aws:iam::000000000000:role/lambda-role \
  --handler br.com.fiap.serverless.avaliador.handler.AvaliadorHandler::handleRequest \
  --zip-file fileb://lambdas/avaliador/target/avaliador.jar \
  --environment "Variables={AWS_REGION=${AWS_REGION},DYNAMODB_TABLE_NAME=${DYNAMODB_TABLE_NAME},EMAIL_QUEUE_URL=${QUEUE_URL},ADMIN_ALERT_EMAIL=${ADMIN_ALERT_EMAIL},LOCALSTACK_ENDPOINT=${LOCALSTACK_ENDPOINT}}" >/dev/null 2>&1 || \
localstack_cli lambda update-function-code --function-name avaliador --zip-file fileb://lambdas/avaliador/target/avaliador.jar >/dev/null
localstack_cli lambda update-function-configuration \
  --function-name avaliador \
  --environment "Variables={AWS_REGION=${AWS_REGION},DYNAMODB_TABLE_NAME=${DYNAMODB_TABLE_NAME},EMAIL_QUEUE_URL=${QUEUE_URL},ADMIN_ALERT_EMAIL=${ADMIN_ALERT_EMAIL},LOCALSTACK_ENDPOINT=${LOCALSTACK_ENDPOINT}}" >/dev/null 2>&1 || true

localstack_cli lambda create-function \
  --function-name reports-generator \
  --runtime java17 \
  --role arn:aws:iam::000000000000:role/lambda-role \
  --handler br.com.fiap.serverless.reports.handler.ReportsGeneratorHandler::handleRequest \
  --zip-file fileb://lambdas/reports-generator/target/reports-generator.jar \
  --environment "Variables={AWS_REGION=${AWS_REGION},DYNAMODB_TABLE_NAME=${DYNAMODB_TABLE_NAME},EMAIL_QUEUE_URL=${QUEUE_URL},REPORT_RECIPIENT_EMAIL=${REPORT_RECIPIENT_EMAIL:-admin@example.com},LOCALSTACK_ENDPOINT=${LOCALSTACK_ENDPOINT}}" >/dev/null 2>&1 || \
localstack_cli lambda update-function-code --function-name reports-generator --zip-file fileb://lambdas/reports-generator/target/reports-generator.jar >/dev/null
localstack_cli lambda update-function-configuration \
  --function-name reports-generator \
  --environment "Variables={AWS_REGION=${AWS_REGION},DYNAMODB_TABLE_NAME=${DYNAMODB_TABLE_NAME},EMAIL_QUEUE_URL=${QUEUE_URL},REPORT_RECIPIENT_EMAIL=${REPORT_RECIPIENT_EMAIL:-admin@example.com},LOCALSTACK_ENDPOINT=${LOCALSTACK_ENDPOINT}}" >/dev/null 2>&1 || true

localstack_cli lambda create-function \
  --function-name email-sender \
  --runtime java17 \
  --role arn:aws:iam::000000000000:role/lambda-role \
  --handler br.com.fiap.serverless.email.handler.EmailSenderHandler::handleRequest \
  --zip-file fileb://lambdas/email-sender/target/email-sender.jar \
  --environment "Variables={AWS_REGION=${AWS_REGION},SES_FROM_EMAIL=${SES_FROM_EMAIL},LOCALSTACK_ENDPOINT=${LOCALSTACK_ENDPOINT}}" >/dev/null 2>&1 || \
localstack_cli lambda update-function-code --function-name email-sender --zip-file fileb://lambdas/email-sender/target/email-sender.jar >/dev/null
localstack_cli lambda update-function-configuration \
  --function-name email-sender \
  --environment "Variables={AWS_REGION=${AWS_REGION},SES_FROM_EMAIL=${SES_FROM_EMAIL},LOCALSTACK_ENDPOINT=${LOCALSTACK_ENDPOINT}}" >/dev/null 2>&1 || true

localstack_cli lambda create-event-source-mapping \
  --function-name email-sender \
  --batch-size 10 \
  --event-source-arn "${QUEUE_ARN}" >/dev/null 2>&1 || true

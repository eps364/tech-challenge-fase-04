#!/usr/bin/env bash
set -euo pipefail

AWS_REGION="${AWS_REGION:-us-east-1}"
LOCALSTACK_ENDPOINT="${LOCALSTACK_ENDPOINT:-http://localhost:4566}"
DYNAMODB_TABLE_NAME="${DYNAMODB_TABLE_NAME:-avaliacoes}"
EMAIL_QUEUE_NAME="${EMAIL_QUEUE_NAME:-email-queue}"
EMAIL_DLQ_NAME="${EMAIL_DLQ_NAME:-email-queue-dlq}"
SES_FROM_EMAIL="${SES_FROM_EMAIL:-noreply@example.com}"

awslocal dynamodb create-table \
  --table-name "${DYNAMODB_TABLE_NAME}" \
  --attribute-definitions AttributeName=id,AttributeType=S \
  --key-schema AttributeName=id,KeyType=HASH \
  --billing-mode PAY_PER_REQUEST >/dev/null 2>&1 || true

DLQ_URL="$(awslocal sqs create-queue --queue-name "${EMAIL_DLQ_NAME}" --query 'QueueUrl' --output text)"
DLQ_ARN="$(awslocal sqs get-queue-attributes --queue-url "${DLQ_URL}" --attribute-names QueueArn --query 'Attributes.QueueArn' --output text)"
REDRIVE_POLICY="$(printf '{"deadLetterTargetArn":"%s","maxReceiveCount":"3"}' "${DLQ_ARN}")"
awslocal sqs create-queue --queue-name "${EMAIL_QUEUE_NAME}" --attributes "RedrivePolicy=${REDRIVE_POLICY}" >/dev/null 2>&1 || true
QUEUE_URL="$(awslocal sqs get-queue-url --queue-name "${EMAIL_QUEUE_NAME}" --query 'QueueUrl' --output text)"
QUEUE_ARN="$(awslocal sqs get-queue-attributes --queue-url "${QUEUE_URL}" --attribute-names QueueArn --query 'Attributes.QueueArn' --output text)"

awslocal ses verify-email-identity --email-address "${SES_FROM_EMAIL}" >/dev/null 2>&1 || true

./scripts/package.sh

awslocal lambda create-function \
  --function-name avaliador \
  --runtime java17 \
  --role arn:aws:iam::000000000000:role/lambda-role \
  --handler br.com.fiap.serverless.avaliador.handler.AvaliadorHandler::handleRequest \
  --zip-file fileb://lambdas/avaliador/target/avaliador.jar \
  --environment "Variables={AWS_REGION=${AWS_REGION},DYNAMODB_TABLE_NAME=${DYNAMODB_TABLE_NAME},EMAIL_QUEUE_URL=${QUEUE_URL},LOCALSTACK_ENDPOINT=${LOCALSTACK_ENDPOINT}}" >/dev/null 2>&1 || \
awslocal lambda update-function-code --function-name avaliador --zip-file fileb://lambdas/avaliador/target/avaliador.jar >/dev/null

awslocal lambda create-function \
  --function-name reports-generator \
  --runtime java17 \
  --role arn:aws:iam::000000000000:role/lambda-role \
  --handler br.com.fiap.serverless.reports.handler.ReportsGeneratorHandler::handleRequest \
  --zip-file fileb://lambdas/reports-generator/target/reports-generator.jar \
  --environment "Variables={AWS_REGION=${AWS_REGION},DYNAMODB_TABLE_NAME=${DYNAMODB_TABLE_NAME},EMAIL_QUEUE_URL=${QUEUE_URL},REPORT_RECIPIENT_EMAIL=${REPORT_RECIPIENT_EMAIL:-admin@example.com},LOCALSTACK_ENDPOINT=${LOCALSTACK_ENDPOINT}}" >/dev/null 2>&1 || \
awslocal lambda update-function-code --function-name reports-generator --zip-file fileb://lambdas/reports-generator/target/reports-generator.jar >/dev/null

awslocal lambda create-function \
  --function-name email-sender \
  --runtime java17 \
  --role arn:aws:iam::000000000000:role/lambda-role \
  --handler br.com.fiap.serverless.email.handler.EmailSenderHandler::handleRequest \
  --zip-file fileb://lambdas/email-sender/target/email-sender.jar \
  --environment "Variables={AWS_REGION=${AWS_REGION},SES_FROM_EMAIL=${SES_FROM_EMAIL},LOCALSTACK_ENDPOINT=${LOCALSTACK_ENDPOINT}}" >/dev/null 2>&1 || \
awslocal lambda update-function-code --function-name email-sender --zip-file fileb://lambdas/email-sender/target/email-sender.jar >/dev/null

awslocal lambda create-event-source-mapping \
  --function-name email-sender \
  --batch-size 10 \
  --event-source-arn "${QUEUE_ARN}" >/dev/null 2>&1 || true

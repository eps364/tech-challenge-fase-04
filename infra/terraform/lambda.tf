resource "aws_cloudwatch_log_group" "avaliador" {
  name              = "/aws/lambda/${local.resource_prefix}-avaliador"
  retention_in_days = 14
  tags              = local.common_tags
}

resource "aws_cloudwatch_log_group" "reports_generator" {
  name              = "/aws/lambda/${local.resource_prefix}-reports-generator"
  retention_in_days = 14
  tags              = local.common_tags
}

resource "aws_cloudwatch_log_group" "email_sender" {
  name              = "/aws/lambda/${local.resource_prefix}-email-sender"
  retention_in_days = 14
  tags              = local.common_tags
}

resource "aws_lambda_function" "avaliador" {
  function_name    = "${local.resource_prefix}-avaliador"
  role             = aws_iam_role.avaliador_lambda_role.arn
  runtime          = var.lambda_runtime
  handler          = "br.com.fiap.serverless.avaliador.handler.AvaliadorHandler::handleRequest"
  filename         = "${path.root}/../../lambdas/avaliador/target/avaliador.jar"
  memory_size      = var.lambda_memory_size
  timeout          = var.lambda_timeout
  source_code_hash = filebase64sha256("${path.root}/../../lambdas/avaliador/target/avaliador.jar")

  environment {
    variables = {
      ADMIN_ALERT_EMAIL   = var.admin_alert_email
      DYNAMODB_TABLE_NAME = aws_dynamodb_table.avaliacoes.name
      EMAIL_QUEUE_URL     = aws_sqs_queue.email_queue.id
    }
  }

  depends_on = [aws_cloudwatch_log_group.avaliador]
  tags       = local.common_tags
}

resource "aws_lambda_function" "reports_generator" {
  function_name    = "${local.resource_prefix}-reports-generator"
  role             = aws_iam_role.reports_generator_lambda_role.arn
  runtime          = var.lambda_runtime
  handler          = "br.com.fiap.serverless.reports.handler.ReportsGeneratorHandler::handleRequest"
  filename         = "${path.root}/../../lambdas/reports-generator/target/reports-generator.jar"
  memory_size      = var.lambda_memory_size
  timeout          = var.lambda_timeout
  source_code_hash = filebase64sha256("${path.root}/../../lambdas/reports-generator/target/reports-generator.jar")

  environment {
    variables = {
      DYNAMODB_TABLE_NAME    = aws_dynamodb_table.avaliacoes.name
      EMAIL_QUEUE_URL        = aws_sqs_queue.email_queue.id
      REPORT_RECIPIENT_EMAIL = var.report_recipient_email
    }
  }

  depends_on = [aws_cloudwatch_log_group.reports_generator]
  tags       = local.common_tags
}

resource "aws_lambda_function" "email_sender" {
  function_name    = "${local.resource_prefix}-email-sender"
  role             = aws_iam_role.email_sender_lambda_role.arn
  runtime          = var.lambda_runtime
  handler          = "br.com.fiap.serverless.email.handler.EmailSenderHandler::handleRequest"
  filename         = "${path.root}/../../lambdas/email-sender/target/email-sender.jar"
  memory_size      = var.lambda_memory_size
  timeout          = var.lambda_timeout
  source_code_hash = filebase64sha256("${path.root}/../../lambdas/email-sender/target/email-sender.jar")

  environment {
    variables = {
      SES_FROM_EMAIL = var.ses_from_email
    }
  }

  depends_on = [aws_cloudwatch_log_group.email_sender]
  tags       = local.common_tags
}

resource "aws_lambda_event_source_mapping" "email_queue" {
  event_source_arn = aws_sqs_queue.email_queue.arn
  function_name    = aws_lambda_function.email_sender.arn
  batch_size       = 10
}

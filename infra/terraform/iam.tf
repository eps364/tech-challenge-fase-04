data "aws_caller_identity" "current" {}

data "aws_iam_policy_document" "lambda_assume_role" {
  statement {
    actions = ["sts:AssumeRole"]

    principals {
      type        = "Service"
      identifiers = ["lambda.amazonaws.com"]
    }
  }
}

locals {
  lambda_log_group_arns = {
    avaliador         = "arn:aws:logs:${var.aws_region}:${data.aws_caller_identity.current.account_id}:log-group:/aws/lambda/${local.resource_prefix}-avaliador:*"
    reports_generator = "arn:aws:logs:${var.aws_region}:${data.aws_caller_identity.current.account_id}:log-group:/aws/lambda/${local.resource_prefix}-reports-generator:*"
    email_sender      = "arn:aws:logs:${var.aws_region}:${data.aws_caller_identity.current.account_id}:log-group:/aws/lambda/${local.resource_prefix}-email-sender:*"
  }
}

resource "aws_iam_role" "avaliador_lambda_role" {
  name               = "${local.resource_prefix}-avaliador-role"
  assume_role_policy = data.aws_iam_policy_document.lambda_assume_role.json
  tags               = local.common_tags
}

resource "aws_iam_role" "reports_generator_lambda_role" {
  name               = "${local.resource_prefix}-reports-generator-role"
  assume_role_policy = data.aws_iam_policy_document.lambda_assume_role.json
  tags               = local.common_tags
}

resource "aws_iam_role" "email_sender_lambda_role" {
  name               = "${local.resource_prefix}-email-sender-role"
  assume_role_policy = data.aws_iam_policy_document.lambda_assume_role.json
  tags               = local.common_tags
}

data "aws_iam_policy_document" "avaliador_lambda_policy" {
  statement {
    actions   = ["logs:CreateLogGroup"]
    resources = ["arn:aws:logs:${var.aws_region}:${data.aws_caller_identity.current.account_id}:*"]
  }

  statement {
    actions   = ["logs:CreateLogStream", "logs:PutLogEvents"]
    resources = [local.lambda_log_group_arns.avaliador]
  }

  statement {
    actions   = ["dynamodb:PutItem"]
    resources = [aws_dynamodb_table.avaliacoes.arn]
  }

  statement {
    actions   = ["sqs:SendMessage"]
    resources = [aws_sqs_queue.email_queue.arn]
  }
}

resource "aws_iam_role_policy" "avaliador_lambda_policy" {
  name   = "${local.resource_prefix}-avaliador-policy"
  role   = aws_iam_role.avaliador_lambda_role.id
  policy = data.aws_iam_policy_document.avaliador_lambda_policy.json
}

data "aws_iam_policy_document" "reports_generator_lambda_policy" {
  statement {
    actions   = ["logs:CreateLogGroup"]
    resources = ["arn:aws:logs:${var.aws_region}:${data.aws_caller_identity.current.account_id}:*"]
  }

  statement {
    actions   = ["logs:CreateLogStream", "logs:PutLogEvents"]
    resources = [local.lambda_log_group_arns.reports_generator]
  }

  statement {
    actions   = ["dynamodb:Scan"]
    resources = [aws_dynamodb_table.avaliacoes.arn]
  }

  statement {
    actions   = ["sqs:SendMessage"]
    resources = [aws_sqs_queue.email_queue.arn]
  }
}

resource "aws_iam_role_policy" "reports_generator_lambda_policy" {
  name   = "${local.resource_prefix}-reports-generator-policy"
  role   = aws_iam_role.reports_generator_lambda_role.id
  policy = data.aws_iam_policy_document.reports_generator_lambda_policy.json
}

data "aws_iam_policy_document" "email_sender_lambda_policy" {
  statement {
    actions   = ["logs:CreateLogGroup"]
    resources = ["arn:aws:logs:${var.aws_region}:${data.aws_caller_identity.current.account_id}:*"]
  }

  statement {
    actions   = ["logs:CreateLogStream", "logs:PutLogEvents"]
    resources = [local.lambda_log_group_arns.email_sender]
  }

  statement {
    actions = [
      "sqs:ChangeMessageVisibility",
      "sqs:DeleteMessage",
      "sqs:GetQueueAttributes",
      "sqs:ReceiveMessage"
    ]
    resources = [aws_sqs_queue.email_queue.arn]
  }

  statement {
    actions   = ["ses:SendEmail", "ses:SendRawEmail"]
    resources = [aws_ses_email_identity.sender.arn]
  }
}

resource "aws_iam_role_policy" "email_sender_lambda_policy" {
  name   = "${local.resource_prefix}-email-sender-policy"
  role   = aws_iam_role.email_sender_lambda_role.id
  policy = data.aws_iam_policy_document.email_sender_lambda_policy.json
}

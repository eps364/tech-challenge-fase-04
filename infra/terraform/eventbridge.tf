resource "aws_scheduler_schedule" "reports_generator" {
  name                = "${local.resource_prefix}-reports-schedule"
  group_name          = "default"
  schedule_expression = var.report_schedule_expression
  flexible_time_window {
    mode = "OFF"
  }

  target {
    arn      = aws_lambda_function.reports_generator.arn
    role_arn = aws_iam_role.scheduler_role.arn
  }
}

data "aws_iam_policy_document" "scheduler_assume_role" {
  statement {
    actions = ["sts:AssumeRole"]
    principals {
      type        = "Service"
      identifiers = ["scheduler.amazonaws.com"]
    }
  }
}

resource "aws_iam_role" "scheduler_role" {
  name               = "${local.resource_prefix}-scheduler-role"
  assume_role_policy = data.aws_iam_policy_document.scheduler_assume_role.json
  tags               = local.common_tags
}

data "aws_iam_policy_document" "scheduler_invoke_lambda" {
  statement {
    actions   = ["lambda:InvokeFunction"]
    resources = [aws_lambda_function.reports_generator.arn]
  }
}

resource "aws_iam_role_policy" "scheduler_invoke_lambda" {
  name   = "${local.resource_prefix}-scheduler-invoke-lambda"
  role   = aws_iam_role.scheduler_role.id
  policy = data.aws_iam_policy_document.scheduler_invoke_lambda.json
}

locals {
  monitored_lambda_functions = {
    avaliador         = aws_lambda_function.avaliador.function_name
    email_sender      = aws_lambda_function.email_sender.function_name
    reports_generator = aws_lambda_function.reports_generator.function_name
  }
}

resource "aws_sns_topic" "monitoring_alerts" {
  name = "${local.resource_prefix}-monitoring-alerts"
  tags = local.common_tags
}

resource "aws_sns_topic_subscription" "monitoring_email" {
  topic_arn = aws_sns_topic.monitoring_alerts.arn
  protocol  = "email"
  endpoint  = var.admin_alert_email
}

resource "aws_cloudwatch_metric_alarm" "lambda_errors" {
  for_each = local.monitored_lambda_functions

  alarm_name          = "${each.value}-errors"
  alarm_description   = "Alerta quando a Lambda ${each.value} registra erro."
  comparison_operator = "GreaterThanOrEqualToThreshold"
  evaluation_periods  = 1
  metric_name         = "Errors"
  namespace           = "AWS/Lambda"
  period              = 300
  statistic           = "Sum"
  threshold           = 1
  treat_missing_data  = "notBreaching"
  alarm_actions       = [aws_sns_topic.monitoring_alerts.arn]

  dimensions = {
    FunctionName = each.value
  }

  tags = local.common_tags
}

resource "aws_cloudwatch_metric_alarm" "email_dlq_messages" {
  alarm_name          = "${local.resource_prefix}-email-dlq-messages"
  alarm_description   = "Alerta quando mensagens chegam na DLQ de e-mail."
  comparison_operator = "GreaterThanOrEqualToThreshold"
  evaluation_periods  = 1
  metric_name         = "ApproximateNumberOfMessagesVisible"
  namespace           = "AWS/SQS"
  period              = 300
  statistic           = "Sum"
  threshold           = 1
  treat_missing_data  = "notBreaching"
  alarm_actions       = [aws_sns_topic.monitoring_alerts.arn]

  dimensions = {
    QueueName = aws_sqs_queue.email_dlq.name
  }

  tags = local.common_tags
}

resource "aws_cloudwatch_dashboard" "main" {
  dashboard_name = "${local.resource_prefix}-dashboard"
  dashboard_body = jsonencode({
    widgets = [
      {
        type   = "metric"
        x      = 0
        y      = 0
        width  = 12
        height = 6
        properties = {
          title   = "Lambda errors"
          region  = var.aws_region
          view    = "timeSeries"
          stacked = false
          metrics = [
            for function_name in values(local.monitored_lambda_functions) :
            ["AWS/Lambda", "Errors", "FunctionName", function_name]
          ]
        }
      },
      {
        type   = "metric"
        x      = 12
        y      = 0
        width  = 12
        height = 6
        properties = {
          title   = "Email DLQ visible messages"
          region  = var.aws_region
          view    = "timeSeries"
          stacked = false
          metrics = [
            ["AWS/SQS", "ApproximateNumberOfMessagesVisible", "QueueName", aws_sqs_queue.email_dlq.name]
          ]
        }
      }
    ]
  })
}

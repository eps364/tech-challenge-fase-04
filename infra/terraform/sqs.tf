resource "aws_sqs_queue" "email_dlq" {
  name = "${local.resource_prefix}-${var.email_dlq_name}"
  tags = local.common_tags
}

resource "aws_sqs_queue" "email_queue" {
  name = "${local.resource_prefix}-${var.email_queue_name}"

  redrive_policy = jsonencode({
    deadLetterTargetArn = aws_sqs_queue.email_dlq.arn
    maxReceiveCount     = 3
  })

  tags = local.common_tags
}

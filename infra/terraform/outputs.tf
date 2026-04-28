output "api_gateway_url" {
  value = aws_apigatewayv2_api.http_api.api_endpoint
}

output "dynamodb_table_name" {
  value = aws_dynamodb_table.avaliacoes.name
}

output "email_queue_url" {
  value = aws_sqs_queue.email_queue.id
}

output "email_dlq_url" {
  value = aws_sqs_queue.email_dlq.id
}

output "avaliador_lambda_name" {
  value = aws_lambda_function.avaliador.function_name
}

output "reports_generator_lambda_name" {
  value = aws_lambda_function.reports_generator.function_name
}

output "email_sender_lambda_name" {
  value = aws_lambda_function.email_sender.function_name
}

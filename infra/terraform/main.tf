output "project_summary" {
  value = {
    api_gateway_url          = aws_apigatewayv2_api.http_api.api_endpoint
    dynamodb_table_name      = aws_dynamodb_table.avaliacoes.name
    email_queue_url          = aws_sqs_queue.email_queue.id
    email_dlq_url            = aws_sqs_queue.email_dlq.id
    avaliador_lambda_name    = aws_lambda_function.avaliador.function_name
    reports_lambda_name      = aws_lambda_function.reports_generator.function_name
    email_sender_lambda_name = aws_lambda_function.email_sender.function_name
  }
}

resource "aws_apigatewayv2_api" "http_api" {
  name          = "${local.resource_prefix}-api"
  protocol_type = "HTTP"

  cors_configuration {
    allow_headers = ["content-type"]
    allow_methods = ["POST", "OPTIONS"]
    allow_origins = ["*"]
  }

  tags = local.common_tags
}

resource "aws_apigatewayv2_integration" "avaliador" {
  api_id                 = aws_apigatewayv2_api.http_api.id
  integration_type       = "AWS_PROXY"
  integration_uri        = aws_lambda_function.avaliador.invoke_arn
  integration_method     = "POST"
  payload_format_version = "2.0"
}

resource "aws_apigatewayv2_route" "avaliacao_post" {
  api_id    = aws_apigatewayv2_api.http_api.id
  route_key = "POST /avaliacao"
  target    = "integrations/${aws_apigatewayv2_integration.avaliador.id}"
}

resource "aws_apigatewayv2_route" "avaliacoes_post" {
  api_id    = aws_apigatewayv2_api.http_api.id
  route_key = "POST /avaliacoes"
  target    = "integrations/${aws_apigatewayv2_integration.avaliador.id}"
}

resource "aws_apigatewayv2_stage" "default" {
  api_id      = aws_apigatewayv2_api.http_api.id
  name        = "$default"
  auto_deploy = true
  tags        = local.common_tags
}

resource "aws_lambda_permission" "apigateway_invoke" {
  statement_id  = "AllowApiGatewayInvoke"
  action        = "lambda:InvokeFunction"
  function_name = aws_lambda_function.avaliador.function_name
  principal     = "apigateway.amazonaws.com"
  source_arn    = "${aws_apigatewayv2_api.http_api.execution_arn}/*/*"
}

variable "project_name" {
  type        = string
  description = "Nome do projeto."
  default     = "tech-challenge-fase-04"
}

variable "environment" {
  type        = string
  description = "Ambiente de deploy."
}

variable "aws_region" {
  type        = string
  description = "Regiao AWS."
}

variable "dynamodb_table_name" {
  type        = string
  description = "Nome da tabela DynamoDB."
  default     = "avaliacoes"
}

variable "email_queue_name" {
  type        = string
  description = "Nome da fila principal."
  default     = "email-queue"
}

variable "email_dlq_name" {
  type        = string
  description = "Nome da fila DLQ."
  default     = "email-queue-dlq"
}

variable "ses_from_email" {
  type        = string
  description = "Endereco remetente validado no SES."
}

variable "report_recipient_email" {
  type        = string
  description = "Endereco destinatario dos relatorios."
}

variable "report_schedule_expression" {
  type        = string
  description = "Expressao de agendamento do EventBridge Scheduler."
  default     = "rate(1 day)"
}

variable "lambda_runtime" {
  type        = string
  description = "Runtime das Lambdas."
  default     = "java17"
}

variable "lambda_memory_size" {
  type        = number
  description = "Memoria das Lambdas."
  default     = 512
}

variable "lambda_timeout" {
  type        = number
  description = "Timeout em segundos das Lambdas."
  default     = 30
}

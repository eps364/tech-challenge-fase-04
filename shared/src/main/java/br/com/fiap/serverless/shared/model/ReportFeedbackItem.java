package br.com.fiap.serverless.shared.model;

public record ReportFeedbackItem(
        String descricao,
        String urgencia,
        String dataEnvio) {
}

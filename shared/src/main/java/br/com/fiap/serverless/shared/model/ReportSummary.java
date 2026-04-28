package br.com.fiap.serverless.shared.model;

import java.math.BigDecimal;

public record ReportSummary(
        long totalAvaliacoes,
        BigDecimal mediaNotas,
        BigDecimal maiorNota,
        BigDecimal menorNota,
        String dataGeracao) {
}

package br.com.fiap.serverless.shared.model;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public record ReportSummary(
        long totalAvaliacoes,
        BigDecimal mediaNotas,
        Map<String, Long> quantidadeAvaliacoesPorDia,
        Map<String, Long> quantidadeAvaliacoesPorUrgencia,
        List<ReportFeedbackItem> feedbacks,
        String dataGeracao) {
}

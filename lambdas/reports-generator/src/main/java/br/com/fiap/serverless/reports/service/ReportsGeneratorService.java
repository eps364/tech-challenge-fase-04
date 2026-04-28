package br.com.fiap.serverless.reports.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Clock;
import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import br.com.fiap.serverless.shared.model.Avaliacao;
import br.com.fiap.serverless.shared.model.EmailMessage;
import br.com.fiap.serverless.shared.model.EmailType;
import br.com.fiap.serverless.shared.model.ReportSummary;
import br.com.fiap.serverless.shared.queue.EmailQueuePublisher;
import br.com.fiap.serverless.shared.repository.AvaliacaoRepository;

public class ReportsGeneratorService {

    private final AvaliacaoRepository repository;
    private final EmailQueuePublisher queuePublisher;
    private final Clock clock;
    private final String reportRecipientEmail;

    public ReportsGeneratorService(AvaliacaoRepository repository,
                                   EmailQueuePublisher queuePublisher,
                                   Clock clock,
                                   String reportRecipientEmail) {
        this.repository = repository;
        this.queuePublisher = queuePublisher;
        this.clock = clock;
        this.reportRecipientEmail = reportRecipientEmail;
    }

    public ReportSummary generate() {
        List<Avaliacao> avaliacoes = repository.findAll();
        ReportSummary summary = buildSummary(avaliacoes);
        queuePublisher.publish(buildEmailMessage(summary));
        return summary;
    }

    ReportSummary buildSummary(List<Avaliacao> avaliacoes) {
        if (avaliacoes.isEmpty()) {
            return new ReportSummary(0, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, Instant.now(clock).toString());
        }

        BigDecimal total = avaliacoes.stream()
                .map(Avaliacao::nota)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal max = avaliacoes.stream().map(Avaliacao::nota).max(Comparator.naturalOrder()).orElse(BigDecimal.ZERO);
        BigDecimal min = avaliacoes.stream().map(Avaliacao::nota).min(Comparator.naturalOrder()).orElse(BigDecimal.ZERO);
        BigDecimal average = total.divide(BigDecimal.valueOf(avaliacoes.size()), 2, RoundingMode.HALF_UP);

        return new ReportSummary(avaliacoes.size(), average, max, min, Instant.now(clock).toString());
    }

    EmailMessage buildEmailMessage(ReportSummary summary) {
        return new EmailMessage(
                EmailType.RELATORIO_GERADO,
                reportRecipientEmail,
                "Relatório de avaliações",
                "relatorio-avaliacoes",
                Map.of(
                        "totalAvaliacoes", summary.totalAvaliacoes(),
                        "mediaNotas", summary.mediaNotas(),
                        "maiorNota", summary.maiorNota(),
                        "menorNota", summary.menorNota(),
                        "dataGeracao", summary.dataGeracao()));
    }
}

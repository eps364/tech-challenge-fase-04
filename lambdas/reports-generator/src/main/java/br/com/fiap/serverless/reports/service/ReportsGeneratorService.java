package br.com.fiap.serverless.reports.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

import br.com.fiap.serverless.shared.model.Avaliacao;
import br.com.fiap.serverless.shared.model.EmailMessage;
import br.com.fiap.serverless.shared.model.EmailType;
import br.com.fiap.serverless.shared.model.ReportFeedbackItem;
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
        Instant reportDate = Instant.now(clock);
        Instant startDate = reportDate.minus(7, ChronoUnit.DAYS);
        List<Avaliacao> weeklyAvaliacoes = avaliacoes.stream()
                .filter(avaliacao -> isInsideReportWindow(avaliacao, startDate, reportDate))
                .sorted(Comparator.comparing(Avaliacao::createdAt))
                .toList();

        if (weeklyAvaliacoes.isEmpty()) {
            return new ReportSummary(
                    0,
                    BigDecimal.ZERO,
                    Map.of(),
                    Map.of(),
                    List.of(),
                    reportDate.toString());
        }

        BigDecimal total = weeklyAvaliacoes.stream()
                .map(Avaliacao::nota)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal average = total.divide(BigDecimal.valueOf(weeklyAvaliacoes.size()), 2, RoundingMode.HALF_UP);
        Map<String, Long> quantidadePorDia = weeklyAvaliacoes.stream()
                .collect(Collectors.groupingBy(
                        avaliacao -> createdAtDate(avaliacao.createdAt()).toString(),
                        TreeMap::new,
                        Collectors.counting()));
        Map<String, Long> quantidadePorUrgencia = weeklyAvaliacoes.stream()
                .collect(Collectors.groupingBy(
                        avaliacao -> avaliacao.urgencia().name(),
                        TreeMap::new,
                        Collectors.counting()));
        List<ReportFeedbackItem> feedbacks = weeklyAvaliacoes.stream()
                .map(avaliacao -> new ReportFeedbackItem(
                        avaliacao.descricao(),
                        avaliacao.urgencia().name(),
                        avaliacao.dataEnvio()))
                .toList();

        return new ReportSummary(
                weeklyAvaliacoes.size(),
                average,
                quantidadePorDia,
                quantidadePorUrgencia,
                feedbacks,
                reportDate.toString());
    }

    EmailMessage buildEmailMessage(ReportSummary summary) {
        return new EmailMessage(
                EmailType.RELATORIO_GERADO,
                reportRecipientEmail,
                "Relatorio semanal de avaliacoes",
                "relatorio-avaliacoes",
                Map.of(
                        "totalAvaliacoes", summary.totalAvaliacoes(),
                        "mediaNotas", summary.mediaNotas(),
                        "quantidadeAvaliacoesPorDia", summary.quantidadeAvaliacoesPorDia(),
                        "quantidadeAvaliacoesPorUrgencia", summary.quantidadeAvaliacoesPorUrgencia(),
                        "feedbacks", summary.feedbacks(),
                        "dataGeracao", summary.dataGeracao()));
    }

    private boolean isInsideReportWindow(Avaliacao avaliacao, Instant startDate, Instant reportDate) {
        Instant createdAt = parseInstant(avaliacao.createdAt());
        return !createdAt.isBefore(startDate) && !createdAt.isAfter(reportDate);
    }

    private LocalDate createdAtDate(String createdAt) {
        return parseInstant(createdAt).atZone(ZoneOffset.UTC).toLocalDate();
    }

    private Instant parseInstant(String value) {
        return Instant.parse(value);
    }
}

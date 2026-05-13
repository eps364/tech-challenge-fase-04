package br.com.fiap.serverless.reports.service;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import br.com.fiap.serverless.shared.model.Avaliacao;
import br.com.fiap.serverless.shared.model.AvaliacaoStatus;
import br.com.fiap.serverless.shared.model.EmailMessage;
import br.com.fiap.serverless.shared.model.ReportSummary;
import br.com.fiap.serverless.shared.model.Urgencia;
import br.com.fiap.serverless.shared.queue.EmailQueuePublisher;
import br.com.fiap.serverless.shared.repository.AvaliacaoRepository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ReportsGeneratorServiceTest {

    private final AvaliacaoRepository repository = mock(AvaliacaoRepository.class);
    private final EmailQueuePublisher publisher = mock(EmailQueuePublisher.class);
    private final Clock clock = Clock.fixed(Instant.parse("2026-04-28T10:00:00Z"), ZoneOffset.UTC);
    private final ReportsGeneratorService service = new ReportsGeneratorService(repository, publisher, clock, "admin@example.com");

    @Test
    void shouldGenerateFallbackReportWhenThereAreNoAvaliacoes() {
        when(repository.findAll()).thenReturn(List.of());

        ReportSummary summary = service.generate();

        assertEquals(0, summary.totalAvaliacoes());
        assertEquals(BigDecimal.ZERO, summary.mediaNotas());
        assertEquals(0, summary.quantidadeAvaliacoesPorDia().size());
        assertEquals(0, summary.quantidadeAvaliacoesPorUrgencia().size());
    }

    @Test
    void shouldGenerateWeeklyReportWithAverageAndRequiredAggregations() {
        when(repository.findAll()).thenReturn(List.of(
                avaliacao("1", 10, "2026-04-28T10:00:00Z"),
                avaliacao("2", 8, "2026-04-28T09:00:00Z"),
                avaliacao("3", 3, "2026-04-27T10:00:00Z"),
                avaliacao("4", 6, "2026-04-18T10:00:00Z")
        ));

        ReportSummary summary = service.generate();

        assertEquals(3, summary.totalAvaliacoes());
        assertEquals(new BigDecimal("7.00"), summary.mediaNotas());
        assertEquals(2L, summary.quantidadeAvaliacoesPorDia().get("2026-04-28"));
        assertEquals(1L, summary.quantidadeAvaliacoesPorUrgencia().get("CRITICA"));
        assertEquals(3, summary.feedbacks().size());

        ArgumentCaptor<EmailMessage> captor = ArgumentCaptor.forClass(EmailMessage.class);
        verify(publisher).publish(captor.capture());
        assertEquals("admin@example.com", captor.getValue().to());
        assertEquals(summary.quantidadeAvaliacoesPorUrgencia(), captor.getValue().payload().get("quantidadeAvaliacoesPorUrgencia"));
    }

    private Avaliacao avaliacao(String id, int nota, String createdAt) {
        return new Avaliacao(
                id,
                "Descricao " + id,
                BigDecimal.valueOf(nota),
                Urgencia.fromNota(BigDecimal.valueOf(nota)),
                AvaliacaoStatus.CREATED,
                createdAt,
                createdAt);
    }
}

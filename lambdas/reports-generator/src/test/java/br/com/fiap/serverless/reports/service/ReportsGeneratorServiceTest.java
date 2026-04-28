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
    }

    @Test
    void shouldGenerateReportWithAverageAndPublishMessage() {
        when(repository.findAll()).thenReturn(List.of(
                avaliacao("1", 10),
                avaliacao("2", 8),
                avaliacao("3", 6)
        ));

        ReportSummary summary = service.generate();

        assertEquals(3, summary.totalAvaliacoes());
        assertEquals(new BigDecimal("8.00"), summary.mediaNotas());
        assertEquals(new BigDecimal("10"), summary.maiorNota());
        assertEquals(new BigDecimal("6"), summary.menorNota());

        ArgumentCaptor<EmailMessage> captor = ArgumentCaptor.forClass(EmailMessage.class);
        verify(publisher).publish(captor.capture());
        assertEquals("admin@example.com", captor.getValue().to());
    }

    private Avaliacao avaliacao(String id, int nota) {
        return new Avaliacao(
                id,
                "Aluno",
                "aluno@email.com",
                "Disciplina",
                BigDecimal.valueOf(nota),
                null,
                AvaliacaoStatus.CREATED,
                "2026-04-28T10:00:00Z",
                "2026-04-28T10:00:00Z");
    }
}

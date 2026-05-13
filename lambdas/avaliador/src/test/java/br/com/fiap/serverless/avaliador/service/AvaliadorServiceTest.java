package br.com.fiap.serverless.avaliador.service;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import br.com.fiap.serverless.shared.dto.CreateAvaliacaoRequest;
import br.com.fiap.serverless.shared.exception.ValidationException;
import br.com.fiap.serverless.shared.model.Avaliacao;
import br.com.fiap.serverless.shared.model.AvaliacaoStatus;
import br.com.fiap.serverless.shared.model.EmailMessage;
import br.com.fiap.serverless.shared.model.EmailType;
import br.com.fiap.serverless.shared.model.Urgencia;
import br.com.fiap.serverless.shared.queue.EmailQueuePublisher;
import br.com.fiap.serverless.shared.repository.AvaliacaoRepository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

class AvaliadorServiceTest {

    private final AvaliacaoRepository repository = mock(AvaliacaoRepository.class);
    private final EmailQueuePublisher publisher = mock(EmailQueuePublisher.class);
    private final Clock clock = Clock.fixed(Instant.parse("2026-04-28T10:00:00Z"), ZoneOffset.UTC);
    private final AvaliadorService service = new AvaliadorService(repository, publisher, clock, "admin@example.com");

    @Test
    void shouldCreateAvaliacaoWithoutPublishingEmailWhenScoreIsNotCritical() {
        CreateAvaliacaoRequest request = new CreateAvaliacaoRequest(
                "Excelente entrega",
                9);

        service.process(request);

        ArgumentCaptor<Avaliacao> avaliacaoCaptor = ArgumentCaptor.forClass(Avaliacao.class);
        verify(repository).save(avaliacaoCaptor.capture());
        assertEquals("Excelente entrega", avaliacaoCaptor.getValue().descricao());
        assertEquals(BigDecimal.valueOf(9), avaliacaoCaptor.getValue().nota());
        assertEquals(Urgencia.BAIXA, avaliacaoCaptor.getValue().urgencia());
        assertEquals(AvaliacaoStatus.CREATED, avaliacaoCaptor.getValue().status());

        verify(publisher, never()).publish(any(EmailMessage.class));
    }

    @Test
    void shouldAcceptMinimalPayloadFromChallengeStatement() {
        CreateAvaliacaoRequest request = new CreateAvaliacaoRequest(
                "Conteudo claro e bem conduzido",
                8);

        service.process(request);

        ArgumentCaptor<Avaliacao> avaliacaoCaptor = ArgumentCaptor.forClass(Avaliacao.class);
        verify(repository).save(avaliacaoCaptor.capture());
        assertEquals("Conteudo claro e bem conduzido", avaliacaoCaptor.getValue().descricao());
        assertEquals(Urgencia.MEDIA, avaliacaoCaptor.getValue().urgencia());
    }

    @Test
    void shouldPublishCriticalAlertForLowScore() {
        CreateAvaliacaoRequest request = new CreateAvaliacaoRequest(
                "A aula travou e nao consegui acompanhar",
                3);

        service.process(request);

        ArgumentCaptor<Avaliacao> avaliacaoCaptor = ArgumentCaptor.forClass(Avaliacao.class);
        verify(repository).save(avaliacaoCaptor.capture());
        assertEquals(AvaliacaoStatus.EMAIL_REQUESTED, avaliacaoCaptor.getValue().status());

        ArgumentCaptor<EmailMessage> captor = ArgumentCaptor.forClass(EmailMessage.class);
        verify(publisher).publish(captor.capture());
        assertEquals(EmailType.AVALIACAO_CRITICA, captor.getValue().type());
        assertEquals("admin@example.com", captor.getValue().to());
        assertEquals("CRITICA", captor.getValue().payload().get("urgencia"));
    }

    @Test
    void shouldPublishOnlyCriticalAlertForLowScore() {
        CreateAvaliacaoRequest request = new CreateAvaliacaoRequest(
                "Problema critico",
                2);

        service.process(request);

        verify(publisher, times(1)).publish(any(EmailMessage.class));
    }

    @Test
    void shouldRejectMissingDescription() {
        CreateAvaliacaoRequest request = new CreateAvaliacaoRequest(
                null,
                9);

        assertThrows(ValidationException.class, () -> service.process(request));
    }

    @Test
    void shouldRejectNotaOutOfRange() {
        CreateAvaliacaoRequest request = new CreateAvaliacaoRequest(
                "Nota invalida",
                11);

        assertThrows(ValidationException.class, () -> service.process(request));
    }
}

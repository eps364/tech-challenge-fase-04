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
import br.com.fiap.serverless.shared.model.EmailMessage;
import br.com.fiap.serverless.shared.model.EmailType;
import br.com.fiap.serverless.shared.model.Urgencia;
import br.com.fiap.serverless.shared.queue.EmailQueuePublisher;
import br.com.fiap.serverless.shared.repository.AvaliacaoRepository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

class AvaliadorServiceTest {

    private final AvaliacaoRepository repository = mock(AvaliacaoRepository.class);
    private final EmailQueuePublisher publisher = mock(EmailQueuePublisher.class);
    private final Clock clock = Clock.fixed(Instant.parse("2026-04-28T10:00:00Z"), ZoneOffset.UTC);
    private final AvaliadorService service = new AvaliadorService(repository, publisher, clock, "admin@example.com");

    @Test
    void shouldCreateAvaliacaoAndPublishConfirmationEmailWhenStudentEmailExists() {
        CreateAvaliacaoRequest request = new CreateAvaliacaoRequest(
                "Luiz Silva",
                "luiz@email.com",
                "Arquitetura Java Serverless",
                BigDecimal.valueOf(9),
                "Excelente entrega");

        service.process(request);

        ArgumentCaptor<Avaliacao> avaliacaoCaptor = ArgumentCaptor.forClass(Avaliacao.class);
        verify(repository).save(avaliacaoCaptor.capture());
        assertEquals("Excelente entrega", avaliacaoCaptor.getValue().descricao());
        assertEquals(Urgencia.BAIXA, avaliacaoCaptor.getValue().urgencia());

        ArgumentCaptor<EmailMessage> emailCaptor = ArgumentCaptor.forClass(EmailMessage.class);
        verify(publisher).publish(emailCaptor.capture());
        assertEquals(EmailType.AVALIACAO_CRIADA, emailCaptor.getValue().type());
        assertEquals("luiz@email.com", emailCaptor.getValue().to());
    }

    @Test
    void shouldAcceptMinimalPayloadFromChallengeStatement() {
        CreateAvaliacaoRequest request = new CreateAvaliacaoRequest(
                null,
                null,
                null,
                BigDecimal.valueOf(8),
                "Conteudo claro e bem conduzido");

        service.process(request);

        ArgumentCaptor<Avaliacao> avaliacaoCaptor = ArgumentCaptor.forClass(Avaliacao.class);
        verify(repository).save(avaliacaoCaptor.capture());
        assertEquals("Conteudo claro e bem conduzido", avaliacaoCaptor.getValue().descricao());
        assertEquals(Urgencia.MEDIA, avaliacaoCaptor.getValue().urgencia());
    }

    @Test
    void shouldPublishCriticalAlertForLowScore() {
        CreateAvaliacaoRequest request = new CreateAvaliacaoRequest(
                null,
                null,
                null,
                BigDecimal.valueOf(3),
                "A aula travou e nao consegui acompanhar");

        service.process(request);

        ArgumentCaptor<EmailMessage> captor = ArgumentCaptor.forClass(EmailMessage.class);
        verify(publisher).publish(captor.capture());
        assertEquals(EmailType.AVALIACAO_CRITICA, captor.getValue().type());
        assertEquals("admin@example.com", captor.getValue().to());
        assertEquals("CRITICA", captor.getValue().payload().get("urgencia"));
    }

    @Test
    void shouldPublishConfirmationAndCriticalAlertWhenCriticalFeedbackHasStudentEmail() {
        CreateAvaliacaoRequest request = new CreateAvaliacaoRequest(
                "Luiz Silva",
                "luiz@email.com",
                "Arquitetura Java Serverless",
                BigDecimal.valueOf(2),
                "Problema critico");

        service.process(request);

        verify(publisher, times(2)).publish(org.mockito.ArgumentMatchers.any(EmailMessage.class));
    }

    @Test
    void shouldRejectMissingDescription() {
        CreateAvaliacaoRequest request = new CreateAvaliacaoRequest(
                null,
                null,
                null,
                BigDecimal.valueOf(9),
                null);

        assertThrows(ValidationException.class, () -> service.process(request));
    }

    @Test
    void shouldRejectInvalidEmailWhenProvided() {
        CreateAvaliacaoRequest request = new CreateAvaliacaoRequest(
                "Luiz Silva",
                "invalido",
                "Arquitetura Java Serverless",
                BigDecimal.valueOf(9),
                "Boa aula");

        assertThrows(ValidationException.class, () -> service.process(request));
    }

    @Test
    void shouldRejectNotaOutOfRange() {
        CreateAvaliacaoRequest request = new CreateAvaliacaoRequest(
                null,
                null,
                null,
                BigDecimal.valueOf(11),
                "Nota invalida");

        assertThrows(ValidationException.class, () -> service.process(request));
    }
}

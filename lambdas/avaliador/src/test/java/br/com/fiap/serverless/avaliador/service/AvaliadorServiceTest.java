package br.com.fiap.serverless.avaliador.service;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import br.com.fiap.serverless.shared.dto.CreateAvaliacaoRequest;
import br.com.fiap.serverless.shared.exception.ValidationException;
import br.com.fiap.serverless.shared.model.EmailMessage;
import br.com.fiap.serverless.shared.queue.EmailQueuePublisher;
import br.com.fiap.serverless.shared.repository.AvaliacaoRepository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class AvaliadorServiceTest {

    private final AvaliacaoRepository repository = mock(AvaliacaoRepository.class);
    private final EmailQueuePublisher publisher = mock(EmailQueuePublisher.class);
    private final Clock clock = Clock.fixed(Instant.parse("2026-04-28T10:00:00Z"), ZoneOffset.UTC);
    private final AvaliadorService service = new AvaliadorService(repository, publisher, clock);

    @Test
    void shouldCreateAvaliacaoAndPublishEmail() {
        CreateAvaliacaoRequest request = new CreateAvaliacaoRequest(
                "Luiz Silva",
                "luiz@email.com",
                "Arquitetura Java Serverless",
                BigDecimal.valueOf(9.5),
                "Excelente entrega");

        service.process(request);

        ArgumentCaptor<EmailMessage> captor = ArgumentCaptor.forClass(EmailMessage.class);
        verify(publisher).publish(captor.capture());
        assertEquals("luiz@email.com", captor.getValue().to());
        assertEquals("Arquitetura Java Serverless", captor.getValue().payload().get("disciplina"));
    }

    @Test
    void shouldRejectMissingName() {
        CreateAvaliacaoRequest request = new CreateAvaliacaoRequest(
                null,
                "luiz@email.com",
                "Arquitetura Java Serverless",
                BigDecimal.valueOf(9.5),
                null);

        assertThrows(ValidationException.class, () -> service.process(request));
    }

    @Test
    void shouldRejectInvalidEmail() {
        CreateAvaliacaoRequest request = new CreateAvaliacaoRequest(
                "Luiz Silva",
                "invalido",
                "Arquitetura Java Serverless",
                BigDecimal.valueOf(9.5),
                null);

        assertThrows(ValidationException.class, () -> service.process(request));
    }

    @Test
    void shouldRejectNotaOutOfRange() {
        CreateAvaliacaoRequest request = new CreateAvaliacaoRequest(
                "Luiz Silva",
                "luiz@email.com",
                "Arquitetura Java Serverless",
                BigDecimal.valueOf(11),
                null);

        assertThrows(ValidationException.class, () -> service.process(request));
    }
}

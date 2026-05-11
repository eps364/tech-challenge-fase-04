package br.com.fiap.serverless.avaliador.service;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;

import br.com.fiap.serverless.shared.dto.CreateAvaliacaoRequest;
import br.com.fiap.serverless.shared.dto.CreateAvaliacaoResponse;
import br.com.fiap.serverless.shared.model.Avaliacao;
import br.com.fiap.serverless.shared.model.AvaliacaoStatus;
import br.com.fiap.serverless.shared.model.EmailMessage;
import br.com.fiap.serverless.shared.model.EmailType;
import br.com.fiap.serverless.shared.model.Urgencia;
import br.com.fiap.serverless.shared.queue.EmailQueuePublisher;
import br.com.fiap.serverless.shared.repository.AvaliacaoRepository;
import br.com.fiap.serverless.shared.util.ValidationUtils;

public class AvaliadorService {

    private final AvaliacaoRepository avaliacaoRepository;
    private final EmailQueuePublisher emailQueuePublisher;
    private final Clock clock;
    private final String adminAlertEmail;

    public AvaliadorService(AvaliacaoRepository avaliacaoRepository,
                            EmailQueuePublisher emailQueuePublisher,
                            Clock clock,
                            String adminAlertEmail) {
        this.avaliacaoRepository = avaliacaoRepository;
        this.emailQueuePublisher = emailQueuePublisher;
        this.clock = clock;
        this.adminAlertEmail = adminAlertEmail;
    }

    public CreateAvaliacaoResponse process(CreateAvaliacaoRequest request) {
        ValidationUtils.validate(request);

        String id = UUID.randomUUID().toString();
        String now = Instant.now(clock).toString();
        BigDecimal nota = BigDecimal.valueOf(request.nota());
        Urgencia urgencia = Urgencia.fromNota(nota);

        Avaliacao avaliacao = new Avaliacao(
                id,
                request.descricao(),
                nota,
                urgencia,
                urgencia.isCritica() ? AvaliacaoStatus.EMAIL_REQUESTED : AvaliacaoStatus.CREATED,
                now,
                now);

        avaliacaoRepository.save(avaliacao);
        if (urgencia.isCritica()) {
            emailQueuePublisher.publish(buildCriticalAlertEmailMessage(avaliacao));
        }

        return new CreateAvaliacaoResponse(id, "Avaliacao registrada com sucesso.");
    }

    EmailMessage buildCriticalAlertEmailMessage(Avaliacao avaliacao) {
        return new EmailMessage(
                EmailType.AVALIACAO_CRITICA,
                adminAlertEmail,
                "Alerta de feedback critico",
                "avaliacao-critica",
                Map.of(
                        "descricao", avaliacao.descricao(),
                        "urgencia", avaliacao.urgencia().name(),
                        "dataEnvio", avaliacao.dataEnvio(),
                        "nota", avaliacao.nota()));
    }
}

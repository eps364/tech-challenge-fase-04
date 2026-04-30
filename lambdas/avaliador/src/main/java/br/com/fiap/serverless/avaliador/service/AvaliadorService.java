package br.com.fiap.serverless.avaliador.service;

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
        Urgencia urgencia = Urgencia.fromNota(request.nota());

        Avaliacao avaliacao = new Avaliacao(
                id,
                request.nomeAluno(),
                request.emailAluno(),
                request.disciplina(),
                request.nota(),
                request.descricao(),
                urgencia,
                isBlank(request.emailAluno()) && !urgencia.isCritica()
                        ? AvaliacaoStatus.CREATED
                        : AvaliacaoStatus.EMAIL_REQUESTED,
                now,
                now);

        avaliacaoRepository.save(avaliacao);
        if (!isBlank(request.emailAluno())) {
            emailQueuePublisher.publish(buildConfirmationEmailMessage(request));
        }
        if (urgencia.isCritica()) {
            emailQueuePublisher.publish(buildCriticalAlertEmailMessage(avaliacao));
        }

        return new CreateAvaliacaoResponse(id, "Avaliacao registrada com sucesso.");
    }

    EmailMessage buildConfirmationEmailMessage(CreateAvaliacaoRequest request) {
        return new EmailMessage(
                EmailType.AVALIACAO_CRIADA,
                request.emailAluno(),
                "Avaliacao registrada",
                "avaliacao-criada",
                Map.of(
                        "nomeAluno", defaultString(request.nomeAluno(), "estudante"),
                        "disciplina", defaultString(request.disciplina(), "aula avaliada"),
                        "nota", request.nota()));
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

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    private String defaultString(String value, String fallback) {
        return isBlank(value) ? fallback : value;
    }
}

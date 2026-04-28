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
import br.com.fiap.serverless.shared.queue.EmailQueuePublisher;
import br.com.fiap.serverless.shared.repository.AvaliacaoRepository;
import br.com.fiap.serverless.shared.util.ValidationUtils;

public class AvaliadorService {

    private final AvaliacaoRepository avaliacaoRepository;
    private final EmailQueuePublisher emailQueuePublisher;
    private final Clock clock;

    public AvaliadorService(AvaliacaoRepository avaliacaoRepository,
                            EmailQueuePublisher emailQueuePublisher,
                            Clock clock) {
        this.avaliacaoRepository = avaliacaoRepository;
        this.emailQueuePublisher = emailQueuePublisher;
        this.clock = clock;
    }

    public CreateAvaliacaoResponse process(CreateAvaliacaoRequest request) {
        ValidationUtils.validate(request);

        String id = UUID.randomUUID().toString();
        String now = Instant.now(clock).toString();

        Avaliacao avaliacao = new Avaliacao(
                id,
                request.nomeAluno(),
                request.emailAluno(),
                request.disciplina(),
                request.nota(),
                request.comentario(),
                AvaliacaoStatus.EMAIL_REQUESTED,
                now,
                now);

        avaliacaoRepository.save(avaliacao);
        emailQueuePublisher.publish(buildEmailMessage(request));

        return new CreateAvaliacaoResponse(id, "Avaliação registrada com sucesso.");
    }

    EmailMessage buildEmailMessage(CreateAvaliacaoRequest request) {
        return new EmailMessage(
                EmailType.AVALIACAO_CRIADA,
                request.emailAluno(),
                "Avaliação registrada",
                "avaliacao-criada",
                Map.of(
                        "nomeAluno", request.nomeAluno(),
                        "disciplina", request.disciplina(),
                        "nota", request.nota()));
    }
}

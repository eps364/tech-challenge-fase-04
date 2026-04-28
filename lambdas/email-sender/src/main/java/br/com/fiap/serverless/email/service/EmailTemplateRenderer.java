package br.com.fiap.serverless.email.service;

import java.util.Map;

import br.com.fiap.serverless.shared.email.EmailContent;
import br.com.fiap.serverless.shared.exception.ValidationException;
import br.com.fiap.serverless.shared.model.EmailMessage;
import br.com.fiap.serverless.shared.model.EmailType;

public class EmailTemplateRenderer {

    public EmailContent render(EmailMessage message) {
        if (message.type() == EmailType.AVALIACAO_CRIADA) {
            return renderAvaliacaoCriada(message);
        }
        if (message.type() == EmailType.RELATORIO_GERADO) {
            return renderRelatorio(message);
        }
        throw new ValidationException("Unsupported email type: " + message.type());
    }

    private EmailContent renderAvaliacaoCriada(EmailMessage message) {
        Map<String, Object> payload = message.payload();
        String text = """
                Olá %s,

                Sua avaliação da disciplina %s foi registrada com a nota %s.
                """.formatted(payload.get("nomeAluno"), payload.get("disciplina"), payload.get("nota"));
        String html = """
                <html><body><h1>Avaliação registrada</h1><p>Olá %s,</p><p>Sua avaliação da disciplina <strong>%s</strong> foi registrada com a nota <strong>%s</strong>.</p></body></html>
                """.formatted(payload.get("nomeAluno"), payload.get("disciplina"), payload.get("nota"));
        return new EmailContent(message.subject(), text, html);
    }

    private EmailContent renderRelatorio(EmailMessage message) {
        Map<String, Object> payload = message.payload();
        String text = """
                Relatório de avaliações

                Total de avaliações: %s
                Média das notas: %s
                Maior nota: %s
                Menor nota: %s
                Data de geração: %s
                """.formatted(
                payload.get("totalAvaliacoes"),
                payload.get("mediaNotas"),
                payload.get("maiorNota"),
                payload.get("menorNota"),
                payload.get("dataGeracao"));
        String html = """
                <html><body><h1>Relatório de avaliações</h1><ul><li>Total de avaliações: %s</li><li>Média das notas: %s</li><li>Maior nota: %s</li><li>Menor nota: %s</li><li>Data de geração: %s</li></ul></body></html>
                """.formatted(
                payload.get("totalAvaliacoes"),
                payload.get("mediaNotas"),
                payload.get("maiorNota"),
                payload.get("menorNota"),
                payload.get("dataGeracao"));
        return new EmailContent(message.subject(), text, html);
    }
}

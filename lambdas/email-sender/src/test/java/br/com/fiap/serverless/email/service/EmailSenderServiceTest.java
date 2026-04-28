package br.com.fiap.serverless.email.service;

import org.junit.jupiter.api.Test;

import br.com.fiap.serverless.shared.email.EmailContent;
import br.com.fiap.serverless.shared.email.EmailService;
import br.com.fiap.serverless.shared.exception.ValidationException;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class EmailSenderServiceTest {

    private final EmailService emailService = mock(EmailService.class);
    private final EmailTemplateRenderer renderer = new EmailTemplateRenderer();
    private final EmailSenderService service = new EmailSenderService(emailService, renderer);

    @Test
    void shouldParseAvaliacaoCriadaMessage() {
        String body = """
                {
                  "type":"AVALIACAO_CRIADA",
                  "to":"luiz@email.com",
                  "subject":"Avaliação registrada",
                  "template":"avaliacao-criada",
                  "payload":{"nomeAluno":"Luiz Silva","disciplina":"Arquitetura Java Serverless","nota":9.5}
                }
                """;

        service.process(body);

        verify(emailService).send(eq("luiz@email.com"), org.mockito.ArgumentMatchers.any(EmailContent.class));
    }

    @Test
    void shouldParseRelatorioGeradoMessage() {
        String body = """
                {
                  "type":"RELATORIO_GERADO",
                  "to":"admin@example.com",
                  "subject":"Relatório de avaliações",
                  "template":"relatorio-avaliacoes",
                  "payload":{"totalAvaliacoes":10,"mediaNotas":8.7,"maiorNota":10,"menorNota":6,"dataGeracao":"2026-04-28T10:00:00Z"}
                }
                """;

        service.process(body);

        verify(emailService).send(eq("admin@example.com"), org.mockito.ArgumentMatchers.any(EmailContent.class));
    }

    @Test
    void shouldBuildHtmlAndTextBodies() {
        EmailContent content = renderer.render(br.com.fiap.serverless.shared.json.JsonUtils.fromJson("""
                {
                  "type":"RELATORIO_GERADO",
                  "to":"admin@example.com",
                  "subject":"Relatório de avaliações",
                  "template":"relatorio-avaliacoes",
                  "payload":{"totalAvaliacoes":10,"mediaNotas":8.7,"maiorNota":10,"menorNota":6,"dataGeracao":"2026-04-28T10:00:00Z"}
                }
                """, br.com.fiap.serverless.shared.model.EmailMessage.class));

        org.junit.jupiter.api.Assertions.assertTrue(content.textBody().contains("Total de avaliações: 10"));
        org.junit.jupiter.api.Assertions.assertTrue(content.htmlBody().contains("<h1>Relatório de avaliações</h1>"));
    }

    @Test
    void shouldRejectUnknownType() {
        String body = """
                {
                  "type":"TIPO_DESCONHECIDO",
                  "to":"admin@example.com",
                  "subject":"Teste",
                  "template":"teste",
                  "payload":{}
                }
                """;

        assertThrows(ValidationException.class, () -> service.process(body));
    }
}

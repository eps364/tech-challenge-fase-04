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
    void shouldParseAvaliacaoCriticaMessage() {
        String body = """
                {
                  "type":"AVALIACAO_CRITICA",
                  "to":"admin@example.com",
                  "subject":"Alerta de feedback critico",
                  "template":"avaliacao-critica",
                  "payload":{"descricao":"Aula travando","urgencia":"CRITICA","dataEnvio":"2026-04-28T10:00:00Z","nota":3}
                }
                """;

        service.process(body);

        verify(emailService).send(eq("admin@example.com"), org.mockito.ArgumentMatchers.any(EmailContent.class));
    }

    @Test
    void shouldParseRelatorioGeradoMessage() {
        String body = """
                {
                  "type":"RELATORIO_GERADO",
                  "to":"admin@example.com",
                  "subject":"Relatorio semanal de avaliacoes",
                  "template":"relatorio-avaliacoes",
                  "payload":{
                    "totalAvaliacoes":10,
                    "mediaNotas":8.7,
                    "quantidadeAvaliacoesPorDia":{"2026-04-28":4},
                    "quantidadeAvaliacoesPorUrgencia":{"BAIXA":6,"CRITICA":1},
                    "feedbacks":[{"descricao":"Aula boa","urgencia":"BAIXA","dataEnvio":"2026-04-28T10:00:00Z"}],
                    "dataGeracao":"2026-04-28T10:00:00Z"
                  }
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
                  "subject":"Relatorio semanal de avaliacoes",
                  "template":"relatorio-avaliacoes",
                  "payload":{
                    "totalAvaliacoes":10,
                    "mediaNotas":8.7,
                    "quantidadeAvaliacoesPorDia":{"2026-04-28":4},
                    "quantidadeAvaliacoesPorUrgencia":{"BAIXA":6,"CRITICA":1},
                    "feedbacks":[{"descricao":"Aula boa","urgencia":"BAIXA","dataEnvio":"2026-04-28T10:00:00Z"}],
                    "dataGeracao":"2026-04-28T10:00:00Z"
                  }
                }
                """, br.com.fiap.serverless.shared.model.EmailMessage.class));

        org.junit.jupiter.api.Assertions.assertTrue(content.textBody().contains("Total de avaliacoes: 10"));
        org.junit.jupiter.api.Assertions.assertTrue(content.textBody().contains("Quantidade de avaliacoes por urgencia"));
        org.junit.jupiter.api.Assertions.assertTrue(content.htmlBody().contains("<h1>Relatorio semanal de avaliacoes</h1>"));
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

package br.com.fiap.serverless.email.service;

import java.util.Map;
import java.util.stream.Collectors;

import br.com.fiap.serverless.shared.email.EmailContent;
import br.com.fiap.serverless.shared.exception.ValidationException;
import br.com.fiap.serverless.shared.model.EmailMessage;
import br.com.fiap.serverless.shared.model.EmailType;

public class EmailTemplateRenderer {

    public EmailContent render(EmailMessage message) {
        if (message.type() == EmailType.AVALIACAO_CRITICA) {
            return renderAvaliacaoCritica(message);
        }
        if (message.type() == EmailType.RELATORIO_GERADO) {
            return renderRelatorio(message);
        }
        throw new ValidationException("Unsupported email type: " + message.type());
    }

    private EmailContent renderAvaliacaoCritica(EmailMessage message) {
        Map<String, Object> payload = message.payload();
        String text = """
                Alerta de feedback critico

                Descricao: %s
                Urgencia: %s
                Data de envio: %s
                Nota: %s
                """.formatted(
                payload.get("descricao"),
                payload.get("urgencia"),
                payload.get("dataEnvio"),
                payload.get("nota"));
        String html = """
                <html><body><h1>Alerta de feedback critico</h1><ul><li>Descricao: %s</li><li>Urgencia: %s</li><li>Data de envio: %s</li><li>Nota: %s</li></ul></body></html>
                """.formatted(
                escapeHtml(payload.get("descricao")),
                escapeHtml(payload.get("urgencia")),
                escapeHtml(payload.get("dataEnvio")),
                escapeHtml(payload.get("nota")));
        return new EmailContent(message.subject(), text, html);
    }

    private EmailContent renderRelatorio(EmailMessage message) {
        Map<String, Object> payload = message.payload();
        String text = """
                Relatorio semanal de avaliacoes

                Total de avaliacoes: %s
                Media das notas: %s
                Quantidade de avaliacoes por dia:
                %s

                Quantidade de avaliacoes por urgencia:
                %s

                Feedbacks:
                %s

                Data de geracao: %s
                """.formatted(
                payload.get("totalAvaliacoes"),
                payload.get("mediaNotas"),
                formatMap(payload.get("quantidadeAvaliacoesPorDia")),
                formatMap(payload.get("quantidadeAvaliacoesPorUrgencia")),
                formatFeedbacksText(payload.get("feedbacks")),
                payload.get("dataGeracao"));
        String html = """
                <html><body><h1>Relatorio semanal de avaliacoes</h1><ul><li>Total de avaliacoes: %s</li><li>Media das notas: %s</li><li>Data de geracao: %s</li></ul><h2>Quantidade por dia</h2>%s<h2>Quantidade por urgencia</h2>%s<h2>Feedbacks</h2>%s</body></html>
                """.formatted(
                escapeHtml(payload.get("totalAvaliacoes")),
                escapeHtml(payload.get("mediaNotas")),
                escapeHtml(payload.get("dataGeracao")),
                formatMapHtml(payload.get("quantidadeAvaliacoesPorDia")),
                formatMapHtml(payload.get("quantidadeAvaliacoesPorUrgencia")),
                formatFeedbacksHtml(payload.get("feedbacks")));
        return new EmailContent(message.subject(), text, html);
    }

    private String formatMap(Object value) {
        if (!(value instanceof Map<?, ?> map) || map.isEmpty()) {
            return "- sem dados";
        }
        return map.entrySet()
                .stream()
                .map(entry -> "- %s: %s".formatted(entry.getKey(), entry.getValue()))
                .collect(Collectors.joining("\n"));
    }

    private String formatMapHtml(Object value) {
        if (!(value instanceof Map<?, ?> map) || map.isEmpty()) {
            return "<p>Sem dados</p>";
        }
        String items = map.entrySet()
                .stream()
                .map(entry -> "<li>%s: %s</li>".formatted(escapeHtml(entry.getKey()), escapeHtml(entry.getValue())))
                .collect(Collectors.joining());
        return "<ul>" + items + "</ul>";
    }

    private String formatFeedbacksText(Object value) {
        if (!(value instanceof Iterable<?> feedbacks)) {
            return "- sem dados";
        }
        StringBuilder builder = new StringBuilder();
        for (Object feedback : feedbacks) {
            if (feedback instanceof Map<?, ?> item) {
                builder.append("- ")
                        .append(item.get("descricao"))
                        .append(" | urgencia: ")
                        .append(item.get("urgencia"))
                        .append(" | data de envio: ")
                        .append(item.get("dataEnvio"))
                        .append("\n");
            } else {
                builder.append("- ").append(feedback).append("\n");
            }
        }
        return builder.isEmpty() ? "- sem dados" : builder.toString().stripTrailing();
    }

    private String formatFeedbacksHtml(Object value) {
        if (!(value instanceof Iterable<?> feedbacks)) {
            return "<p>Sem dados</p>";
        }
        StringBuilder builder = new StringBuilder("<ul>");
        int count = 0;
        for (Object feedback : feedbacks) {
            count++;
            if (feedback instanceof Map<?, ?> item) {
                builder.append("<li><strong>")
                        .append(escapeHtml(item.get("urgencia")))
                        .append("</strong> - ")
                        .append(escapeHtml(item.get("descricao")))
                        .append(" <small>")
                        .append(escapeHtml(item.get("dataEnvio")))
                        .append("</small></li>");
            } else {
                builder.append("<li>").append(escapeHtml(feedback)).append("</li>");
            }
        }
        builder.append("</ul>");
        return count == 0 ? "<p>Sem dados</p>" : builder.toString();
    }

    private String escapeHtml(Object value) {
        if (value == null) {
            return "";
        }
        return value.toString()
                .replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#39;");
    }
}

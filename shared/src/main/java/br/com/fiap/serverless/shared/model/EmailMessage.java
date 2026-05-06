package br.com.fiap.serverless.shared.model;

import java.util.Map;

public record EmailMessage(
        EmailType type,
        String to,
        String subject,
        String template,
        Map<String, Object> payload) {
}

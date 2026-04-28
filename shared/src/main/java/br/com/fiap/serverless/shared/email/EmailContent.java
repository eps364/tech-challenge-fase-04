package br.com.fiap.serverless.shared.email;

public record EmailContent(String subject, String textBody, String htmlBody) {
}

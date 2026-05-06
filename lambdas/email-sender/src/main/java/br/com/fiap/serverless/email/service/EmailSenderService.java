package br.com.fiap.serverless.email.service;

import com.fasterxml.jackson.core.type.TypeReference;

import br.com.fiap.serverless.shared.email.EmailContent;
import br.com.fiap.serverless.shared.email.EmailService;
import br.com.fiap.serverless.shared.exception.ProcessingException;
import br.com.fiap.serverless.shared.exception.ValidationException;
import br.com.fiap.serverless.shared.json.JsonUtils;
import br.com.fiap.serverless.shared.model.EmailMessage;

public class EmailSenderService {

    private final EmailService emailService;
    private final EmailTemplateRenderer renderer;

    public EmailSenderService(EmailService emailService, EmailTemplateRenderer renderer) {
        this.emailService = emailService;
        this.renderer = renderer;
    }

    public void process(String body) {
        EmailMessage message;
        try {
            message = JsonUtils.fromJson(body, new TypeReference<>() { });
        } catch (ProcessingException exception) {
            throw new ValidationException("Invalid email message payload");
        }
        if (message.type() == null) {
            throw new ValidationException("Email message type is required");
        }
        EmailContent content = renderer.render(message);
        emailService.send(message.to(), content);
    }
}

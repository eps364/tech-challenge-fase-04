package br.com.fiap.serverless.shared.email;

import software.amazon.awssdk.services.sesv2.SesV2Client;
import software.amazon.awssdk.services.sesv2.model.Body;
import software.amazon.awssdk.services.sesv2.model.Content;
import software.amazon.awssdk.services.sesv2.model.Destination;
import software.amazon.awssdk.services.sesv2.model.EmailContent.Builder;
import software.amazon.awssdk.services.sesv2.model.Message;
import software.amazon.awssdk.services.sesv2.model.SendEmailRequest;

public class SesEmailService implements EmailService {

    private final SesV2Client sesV2Client;
    private final String fromEmail;

    public SesEmailService(SesV2Client sesV2Client, String fromEmail) {
        this.sesV2Client = sesV2Client;
        this.fromEmail = fromEmail;
    }

    @Override
    public void send(String to, EmailContent content) {
        Builder emailContentBuilder = software.amazon.awssdk.services.sesv2.model.EmailContent.builder();
        emailContentBuilder.simple(Message.builder()
                .subject(Content.builder().data(content.subject()).charset("UTF-8").build())
                .body(Body.builder()
                        .text(Content.builder().data(content.textBody()).charset("UTF-8").build())
                        .html(Content.builder().data(content.htmlBody()).charset("UTF-8").build())
                        .build())
                .build());

        sesV2Client.sendEmail(SendEmailRequest.builder()
                .fromEmailAddress(fromEmail)
                .destination(Destination.builder().toAddresses(to).build())
                .content(emailContentBuilder.build())
                .build());
    }
}

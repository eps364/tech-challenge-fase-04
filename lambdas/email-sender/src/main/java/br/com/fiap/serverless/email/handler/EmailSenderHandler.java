package br.com.fiap.serverless.email.handler;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.SQSEvent;
import com.amazonaws.services.lambda.runtime.events.SQSBatchResponse;

import br.com.fiap.serverless.email.service.EmailSenderService;
import br.com.fiap.serverless.email.service.EmailTemplateRenderer;
import br.com.fiap.serverless.shared.config.AwsClientFactory;
import br.com.fiap.serverless.shared.config.EnvironmentVariables;
import br.com.fiap.serverless.shared.email.EmailService;
import br.com.fiap.serverless.shared.email.FakeEmailService;
import br.com.fiap.serverless.shared.email.SesEmailService;
import software.amazon.awssdk.services.sesv2.SesV2Client;

public class EmailSenderHandler implements RequestHandler<SQSEvent, SQSBatchResponse> {

    private static final Logger LOGGER = LoggerFactory.getLogger(EmailSenderHandler.class);

    private final EmailSenderService emailSenderService;

    public EmailSenderHandler() {
        EnvironmentVariables env = new EnvironmentVariables();
        this.emailSenderService = new EmailSenderService(buildEmailService(env), new EmailTemplateRenderer());
    }

    public EmailSenderHandler(EmailSenderService emailSenderService) {
        this.emailSenderService = emailSenderService;
    }

    @Override
    public SQSBatchResponse handleRequest(SQSEvent event, Context context) {
        List<SQSBatchResponse.BatchItemFailure> failures = new ArrayList<>();
        for (SQSEvent.SQSMessage record : event.getRecords()) {
            try {
                LOGGER.info("Processing SQS message id={}", record.getMessageId());
                emailSenderService.process(record.getBody());
            } catch (Exception exception) {
                LOGGER.error("Failed to process message id={}", record.getMessageId(), exception);
                failures.add(new SQSBatchResponse.BatchItemFailure(record.getMessageId()));
            }
        }
        return new SQSBatchResponse(failures);
    }

    private EmailService buildEmailService(EnvironmentVariables env) {
        if (env.getOptional("LOCALSTACK_ENDPOINT") != null) {
            return new FakeEmailService();
        }

        AwsClientFactory clientFactory = new AwsClientFactory(env);
        SesV2Client sesV2Client = clientFactory.sesV2Client();
        return new SesEmailService(sesV2Client, env.require("SES_FROM_EMAIL"));
    }
}

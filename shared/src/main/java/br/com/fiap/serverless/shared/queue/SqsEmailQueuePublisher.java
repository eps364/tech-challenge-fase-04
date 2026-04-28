package br.com.fiap.serverless.shared.queue;

import br.com.fiap.serverless.shared.json.JsonUtils;
import br.com.fiap.serverless.shared.model.EmailMessage;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.SendMessageRequest;

public class SqsEmailQueuePublisher implements EmailQueuePublisher {

    private final SqsClient sqsClient;
    private final String queueUrl;

    public SqsEmailQueuePublisher(SqsClient sqsClient, String queueUrl) {
        this.sqsClient = sqsClient;
        this.queueUrl = queueUrl;
    }

    @Override
    public void publish(EmailMessage message) {
        sqsClient.sendMessage(SendMessageRequest.builder()
                .queueUrl(queueUrl)
                .messageBody(JsonUtils.toJson(message))
                .build());
    }
}

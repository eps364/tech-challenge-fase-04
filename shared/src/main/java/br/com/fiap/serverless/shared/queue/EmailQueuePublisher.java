package br.com.fiap.serverless.shared.queue;

import br.com.fiap.serverless.shared.model.EmailMessage;

public interface EmailQueuePublisher {

    void publish(EmailMessage message);
}

package br.com.fiap.serverless.shared.email;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FakeEmailService implements EmailService {

    private static final Logger LOGGER = LoggerFactory.getLogger(FakeEmailService.class);

    @Override
    public void send(String to, EmailContent content) {
        LOGGER.info("Simulated email send to={} subject={}", to, content.subject());
    }
}

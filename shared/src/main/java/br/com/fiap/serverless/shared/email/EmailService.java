package br.com.fiap.serverless.shared.email;

public interface EmailService {

    void send(String to, EmailContent content);
}

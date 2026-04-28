package br.com.fiap.serverless.shared.util;

import java.math.BigDecimal;

import org.junit.jupiter.api.Test;

import br.com.fiap.serverless.shared.dto.CreateAvaliacaoRequest;
import br.com.fiap.serverless.shared.exception.ValidationException;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ValidationUtilsTest {

    @Test
    void shouldValidateValidPayload() {
        CreateAvaliacaoRequest request = new CreateAvaliacaoRequest(
                "Luiz Silva",
                "luiz@email.com",
                "Arquitetura Java Serverless",
                BigDecimal.valueOf(9.5),
                "Excelente entrega");

        assertDoesNotThrow(() -> ValidationUtils.validate(request));
    }

    @Test
    void shouldRejectInvalidEmail() {
        CreateAvaliacaoRequest request = new CreateAvaliacaoRequest(
                "Luiz Silva",
                "email-invalido",
                "Arquitetura Java Serverless",
                BigDecimal.valueOf(9.5),
                null);

        assertThrows(ValidationException.class, () -> ValidationUtils.validate(request));
    }
}

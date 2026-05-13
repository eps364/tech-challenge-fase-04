package br.com.fiap.serverless.shared.util;

import org.junit.jupiter.api.Test;

import br.com.fiap.serverless.shared.dto.CreateAvaliacaoRequest;
import br.com.fiap.serverless.shared.exception.ValidationException;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ValidationUtilsTest {

    @Test
    void shouldValidateValidPayload() {
        CreateAvaliacaoRequest request = new CreateAvaliacaoRequest(
                "Excelente entrega",
                9);

        assertDoesNotThrow(() -> ValidationUtils.validate(request));
    }

    @Test
    void shouldValidateMinimalPayload() {
        CreateAvaliacaoRequest request = new CreateAvaliacaoRequest(
                "A aula travou",
                3);

        assertDoesNotThrow(() -> ValidationUtils.validate(request));
    }

    @Test
    void shouldRejectMissingNota() {
        CreateAvaliacaoRequest request = new CreateAvaliacaoRequest(
                "Boa aula",
                null);

        assertThrows(ValidationException.class, () -> ValidationUtils.validate(request));
    }
}

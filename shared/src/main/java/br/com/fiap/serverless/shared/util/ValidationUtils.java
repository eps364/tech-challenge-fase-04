package br.com.fiap.serverless.shared.util;

import br.com.fiap.serverless.shared.dto.CreateAvaliacaoRequest;
import br.com.fiap.serverless.shared.exception.ValidationException;

public final class ValidationUtils {

    private ValidationUtils() {
    }

    public static void validate(CreateAvaliacaoRequest request) {
        if (request == null) {
            throw new ValidationException("Request body is required");
        }
        if (isBlank(request.descricao())) {
            throw new ValidationException("descricao is required");
        }
        if (request.nota() == null) {
            throw new ValidationException("nota is required");
        }
        int nota = request.nota();
        if (nota < 0 || nota > 10) {
            throw new ValidationException("nota must be between 0 and 10");
        }
    }

    private static boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}

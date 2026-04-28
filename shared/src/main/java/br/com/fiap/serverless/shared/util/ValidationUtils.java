package br.com.fiap.serverless.shared.util;

import java.math.BigDecimal;
import java.util.regex.Pattern;

import br.com.fiap.serverless.shared.dto.CreateAvaliacaoRequest;
import br.com.fiap.serverless.shared.exception.ValidationException;

public final class ValidationUtils {

    private static final Pattern EMAIL_PATTERN =
            Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");

    private ValidationUtils() {
    }

    public static void validate(CreateAvaliacaoRequest request) {
        if (request == null) {
            throw new ValidationException("Request body is required");
        }
        if (isBlank(request.nomeAluno())) {
            throw new ValidationException("nomeAluno is required");
        }
        if (isBlank(request.emailAluno()) || !EMAIL_PATTERN.matcher(request.emailAluno()).matches()) {
            throw new ValidationException("emailAluno must be a valid email");
        }
        if (isBlank(request.disciplina())) {
            throw new ValidationException("disciplina is required");
        }
        if (request.nota() == null) {
            throw new ValidationException("nota is required");
        }
        BigDecimal nota = request.nota();
        if (nota.compareTo(BigDecimal.ZERO) < 0 || nota.compareTo(BigDecimal.TEN) > 0) {
            throw new ValidationException("nota must be between 0 and 10");
        }
    }

    private static boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}

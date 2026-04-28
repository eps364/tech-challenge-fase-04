package br.com.fiap.serverless.shared.model;

import java.math.BigDecimal;

public record Avaliacao(
        String id,
        String nomeAluno,
        String emailAluno,
        String disciplina,
        BigDecimal nota,
        String comentario,
        AvaliacaoStatus status,
        String createdAt,
        String updatedAt) {
}

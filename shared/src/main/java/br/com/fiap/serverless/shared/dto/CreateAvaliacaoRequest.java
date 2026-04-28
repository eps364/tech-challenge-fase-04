package br.com.fiap.serverless.shared.dto;

import java.math.BigDecimal;

public record CreateAvaliacaoRequest(
        String nomeAluno,
        String emailAluno,
        String disciplina,
        BigDecimal nota,
        String comentario) {
}

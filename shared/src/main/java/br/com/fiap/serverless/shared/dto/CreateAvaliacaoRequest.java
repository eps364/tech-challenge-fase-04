package br.com.fiap.serverless.shared.dto;

import java.math.BigDecimal;

import com.fasterxml.jackson.annotation.JsonAlias;

public record CreateAvaliacaoRequest(
        String nomeAluno,
        String emailAluno,
        String disciplina,
        BigDecimal nota,
        @JsonAlias("descricao") String comentario) {

    public String descricao() {
        return comentario;
    }
}

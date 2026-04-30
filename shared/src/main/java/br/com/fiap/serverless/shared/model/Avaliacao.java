package br.com.fiap.serverless.shared.model;

import java.math.BigDecimal;

public record Avaliacao(
        String id,
        String nomeAluno,
        String emailAluno,
        String disciplina,
        BigDecimal nota,
        String comentario,
        Urgencia urgencia,
        AvaliacaoStatus status,
        String createdAt,
        String updatedAt) {

    public String descricao() {
        return comentario;
    }

    public String dataEnvio() {
        return createdAt;
    }
}

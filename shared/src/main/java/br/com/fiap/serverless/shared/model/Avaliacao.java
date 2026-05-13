package br.com.fiap.serverless.shared.model;

import java.math.BigDecimal;

public record Avaliacao(
        String id,
        String descricao,
        BigDecimal nota,
        Urgencia urgencia,
        AvaliacaoStatus status,
        String createdAt,
        String updatedAt) {

    public String dataEnvio() {
        return createdAt;
    }
}

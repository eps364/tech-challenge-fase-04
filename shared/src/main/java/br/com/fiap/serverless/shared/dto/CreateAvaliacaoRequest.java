package br.com.fiap.serverless.shared.dto;

public record CreateAvaliacaoRequest(
        String descricao,
        Integer nota) {
}

package br.com.fiap.serverless.shared.repository;

import java.util.List;

import br.com.fiap.serverless.shared.model.Avaliacao;

public interface AvaliacaoRepository {

    void save(Avaliacao avaliacao);

    List<Avaliacao> findAll();
}

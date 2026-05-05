package br.com.fiap.serverless.shared.repository;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import br.com.fiap.serverless.shared.model.Avaliacao;
import br.com.fiap.serverless.shared.model.AvaliacaoStatus;
import br.com.fiap.serverless.shared.model.Urgencia;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.services.dynamodb.model.PutItemRequest;
import software.amazon.awssdk.services.dynamodb.model.ScanRequest;

public class DynamoDbAvaliacaoRepository implements AvaliacaoRepository {

    private final DynamoDbClient dynamoDbClient;
    private final String tableName;

    public DynamoDbAvaliacaoRepository(DynamoDbClient dynamoDbClient, String tableName) {
        this.dynamoDbClient = dynamoDbClient;
        this.tableName = tableName;
    }

    @Override
    public void save(Avaliacao avaliacao) {
        Map<String, AttributeValue> item = new HashMap<>();
        item.put("id", AttributeValue.builder().s(avaliacao.id()).build());
        item.put("nomeAluno", AttributeValue.builder().s(defaultString(avaliacao.nomeAluno())).build());
        item.put("emailAluno", AttributeValue.builder().s(defaultString(avaliacao.emailAluno())).build());
        item.put("disciplina", AttributeValue.builder().s(defaultString(avaliacao.disciplina())).build());
        item.put("nota", AttributeValue.builder().n(avaliacao.nota().toPlainString()).build());
        item.put("descricao", AttributeValue.builder().s(defaultString(avaliacao.descricao())).build());
        item.put("urgencia", AttributeValue.builder().s(avaliacao.urgencia().name()).build());
        item.put("status", AttributeValue.builder().s(avaliacao.status().name()).build());
        item.put("createdAt", AttributeValue.builder().s(avaliacao.createdAt()).build());
        item.put("updatedAt", AttributeValue.builder().s(avaliacao.updatedAt()).build());
        item.put("comentario", AttributeValue.builder().s(defaultString(avaliacao.descricao())).build());

        dynamoDbClient.putItem(PutItemRequest.builder()
                .tableName(tableName)
                .item(item)
                .build());
    }

    @Override
    public List<Avaliacao> findAll() {
        return dynamoDbClient.scan(ScanRequest.builder().tableName(tableName).build())
                .items()
                .stream()
                .map(this::mapItem)
                .toList();
    }

    private Avaliacao mapItem(Map<String, AttributeValue> item) {
        BigDecimal nota = new BigDecimal(item.get("nota").n());
        String descricao = stringValue(item, "descricao", stringValue(item, "comentario", ""));

        return new Avaliacao(
                item.get("id").s(),
                stringValue(item, "nomeAluno", ""),
                stringValue(item, "emailAluno", ""),
                stringValue(item, "disciplina", ""),
                nota,
                descricao,
                urgenciaValue(item, nota),
                AvaliacaoStatus.valueOf(item.get("status").s()),
                item.get("createdAt").s(),
                item.get("updatedAt").s());
    }

    private String stringValue(Map<String, AttributeValue> item, String key, String fallback) {
        AttributeValue value = item.get(key);
        if (value == null || value.s() == null) {
            return fallback;
        }
        return value.s();
    }

    private String defaultString(String value) {
        return value == null ? "" : value;
    }

    private Urgencia urgenciaValue(Map<String, AttributeValue> item, BigDecimal nota) {
        AttributeValue value = item.get("urgencia");
        if (value == null || value.s() == null || value.s().isBlank()) {
            return Urgencia.fromNota(nota);
        }
        return Urgencia.valueOf(value.s());
    }
}

package br.com.fiap.serverless.shared.repository;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import br.com.fiap.serverless.shared.model.Avaliacao;
import br.com.fiap.serverless.shared.model.AvaliacaoStatus;
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
        item.put("nomeAluno", AttributeValue.builder().s(avaliacao.nomeAluno()).build());
        item.put("emailAluno", AttributeValue.builder().s(avaliacao.emailAluno()).build());
        item.put("disciplina", AttributeValue.builder().s(avaliacao.disciplina()).build());
        item.put("nota", AttributeValue.builder().n(avaliacao.nota().toPlainString()).build());
        item.put("status", AttributeValue.builder().s(avaliacao.status().name()).build());
        item.put("createdAt", AttributeValue.builder().s(avaliacao.createdAt()).build());
        item.put("updatedAt", AttributeValue.builder().s(avaliacao.updatedAt()).build());
        item.put("comentario", AttributeValue.builder().s(avaliacao.comentario() == null ? "" : avaliacao.comentario()).build());

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
        return new Avaliacao(
                item.get("id").s(),
                item.get("nomeAluno").s(),
                item.get("emailAluno").s(),
                item.get("disciplina").s(),
                new BigDecimal(item.get("nota").n()),
                item.getOrDefault("comentario", AttributeValue.builder().s("").build()).s(),
                AvaliacaoStatus.valueOf(item.get("status").s()),
                item.get("createdAt").s(),
                item.get("updatedAt").s());
    }
}

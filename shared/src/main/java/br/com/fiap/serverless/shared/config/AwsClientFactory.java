package br.com.fiap.serverless.shared.config;

import java.net.URI;

import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.http.apache.ApacheHttpClient;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.DynamoDbClientBuilder;
import software.amazon.awssdk.services.sesv2.SesV2Client;
import software.amazon.awssdk.services.sesv2.SesV2ClientBuilder;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.SqsClientBuilder;

public class AwsClientFactory {

    private final EnvironmentVariables environmentVariables;

    public AwsClientFactory(EnvironmentVariables environmentVariables) {
        this.environmentVariables = environmentVariables;
    }

    public DynamoDbClient dynamoDbClient() {
        DynamoDbClientBuilder builder = DynamoDbClient.builder()
                .region(resolveRegion())
                .httpClientBuilder(ApacheHttpClient.builder());
        applyLocalstack(builder);
        return builder.build();
    }

    public SqsClient sqsClient() {
        SqsClientBuilder builder = SqsClient.builder()
                .region(resolveRegion())
                .httpClientBuilder(ApacheHttpClient.builder());
        applyLocalstack(builder);
        return builder.build();
    }

    public SesV2Client sesV2Client() {
        SesV2ClientBuilder builder = SesV2Client.builder()
                .region(resolveRegion())
                .httpClientBuilder(ApacheHttpClient.builder());
        applyLocalstack(builder);
        return builder.build();
    }

    private Region resolveRegion() {
        return Region.of(environmentVariables.getOrDefault("AWS_REGION", "us-east-1"));
    }

    private void applyLocalstack(DynamoDbClientBuilder builder) {
        String endpoint = environmentVariables.getOptional("LOCALSTACK_ENDPOINT");
        if (endpoint != null) {
            builder.endpointOverride(URI.create(endpoint));
            builder.credentialsProvider(localCredentials());
        }
    }

    private void applyLocalstack(SqsClientBuilder builder) {
        String endpoint = environmentVariables.getOptional("LOCALSTACK_ENDPOINT");
        if (endpoint != null) {
            builder.endpointOverride(URI.create(endpoint));
            builder.credentialsProvider(localCredentials());
        }
    }

    private void applyLocalstack(SesV2ClientBuilder builder) {
        String endpoint = environmentVariables.getOptional("LOCALSTACK_ENDPOINT");
        if (endpoint != null) {
            builder.endpointOverride(URI.create(endpoint));
            builder.credentialsProvider(localCredentials());
        }
    }

    private StaticCredentialsProvider localCredentials() {
        return StaticCredentialsProvider.create(AwsBasicCredentials.create(
                environmentVariables.getOrDefault("AWS_ACCESS_KEY_ID", "test"),
                environmentVariables.getOrDefault("AWS_SECRET_ACCESS_KEY", "test")));
    }
}

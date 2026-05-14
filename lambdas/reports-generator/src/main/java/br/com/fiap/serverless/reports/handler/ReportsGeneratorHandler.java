package br.com.fiap.serverless.reports.handler;

import java.time.Clock;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;

import br.com.fiap.serverless.reports.service.ReportsGeneratorService;
import br.com.fiap.serverless.shared.config.AwsClientFactory;
import br.com.fiap.serverless.shared.config.EnvironmentVariables;
import br.com.fiap.serverless.shared.model.ReportSummary;
import br.com.fiap.serverless.shared.queue.SqsEmailQueuePublisher;
import br.com.fiap.serverless.shared.repository.DynamoDbAvaliacaoRepository;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.sqs.SqsClient;

public class ReportsGeneratorHandler implements RequestHandler<Map<String, Object>, ReportSummary> {

    private static final Logger LOGGER = LoggerFactory.getLogger(ReportsGeneratorHandler.class);

    private final ReportsGeneratorService reportsGeneratorService;

    public ReportsGeneratorHandler() {
        EnvironmentVariables env = new EnvironmentVariables();
        AwsClientFactory clientFactory = new AwsClientFactory(env);
        DynamoDbClient dynamoDbClient = clientFactory.dynamoDbClient();
        SqsClient sqsClient = clientFactory.sqsClient();

        this.reportsGeneratorService = new ReportsGeneratorService(
                new DynamoDbAvaliacaoRepository(dynamoDbClient, env.require("DYNAMODB_TABLE_NAME")),
                new SqsEmailQueuePublisher(sqsClient, env.require("EMAIL_QUEUE_URL")),
                Clock.systemUTC(),
                env.require("REPORT_RECIPIENT_EMAIL"));
    }

    public ReportsGeneratorHandler(ReportsGeneratorService reportsGeneratorService) {
        this.reportsGeneratorService = reportsGeneratorService;
    }

    @Override
    public ReportSummary handleRequest(Map<String, Object> input, Context context) {
        LOGGER.info("Generating report requestId={}", context != null ? context.getAwsRequestId() : "local");
        return reportsGeneratorService.generate();
    }
}

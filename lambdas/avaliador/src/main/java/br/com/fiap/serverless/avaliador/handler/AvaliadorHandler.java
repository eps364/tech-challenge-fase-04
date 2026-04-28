package br.com.fiap.serverless.avaliador.handler;

import java.time.Clock;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;

import br.com.fiap.serverless.avaliador.service.AvaliadorService;
import br.com.fiap.serverless.shared.config.AwsClientFactory;
import br.com.fiap.serverless.shared.config.EnvironmentVariables;
import br.com.fiap.serverless.shared.dto.CreateAvaliacaoRequest;
import br.com.fiap.serverless.shared.exception.ProcessingException;
import br.com.fiap.serverless.shared.exception.ValidationException;
import br.com.fiap.serverless.shared.json.JsonUtils;
import br.com.fiap.serverless.shared.queue.SqsEmailQueuePublisher;
import br.com.fiap.serverless.shared.repository.DynamoDbAvaliacaoRepository;
import br.com.fiap.serverless.shared.util.HttpResponses;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.sqs.SqsClient;

public class AvaliadorHandler implements RequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> {

    private static final Logger LOGGER = LoggerFactory.getLogger(AvaliadorHandler.class);

    private final AvaliadorService avaliadorService;

    public AvaliadorHandler() {
        EnvironmentVariables env = new EnvironmentVariables();
        AwsClientFactory clientFactory = new AwsClientFactory(env);
        DynamoDbClient dynamoDbClient = clientFactory.dynamoDbClient();
        SqsClient sqsClient = clientFactory.sqsClient();

        this.avaliadorService = new AvaliadorService(
                new DynamoDbAvaliacaoRepository(dynamoDbClient, env.require("DYNAMODB_TABLE_NAME")),
                new SqsEmailQueuePublisher(sqsClient, env.require("EMAIL_QUEUE_URL")),
                Clock.systemUTC());
    }

    public AvaliadorHandler(AvaliadorService avaliadorService) {
        this.avaliadorService = avaliadorService;
    }

    @Override
    public APIGatewayProxyResponseEvent handleRequest(APIGatewayProxyRequestEvent input, Context context) {
        try {
            LOGGER.info("Processing POST /avaliacoes request requestId={}", context != null ? context.getAwsRequestId() : "local");
            CreateAvaliacaoRequest request = JsonUtils.fromJson(input.getBody(), CreateAvaliacaoRequest.class);
            return HttpResponses.success(201, avaliadorService.process(request));
        } catch (ProcessingException | ValidationException exception) {
            LOGGER.warn("Client error: {}", exception.getMessage());
            return HttpResponses.error(400, exception.getMessage());
        } catch (Exception exception) {
            LOGGER.error("Unexpected error while processing avaliacao", exception);
            return HttpResponses.error(500, "Internal server error");
        }
    }
}

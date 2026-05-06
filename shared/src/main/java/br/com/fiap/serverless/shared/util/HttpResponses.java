package br.com.fiap.serverless.shared.util;

import java.util.Map;

import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;

import br.com.fiap.serverless.shared.dto.ErrorResponse;
import br.com.fiap.serverless.shared.json.JsonUtils;

public final class HttpResponses {

    private HttpResponses() {
    }

    public static APIGatewayProxyResponseEvent success(int statusCode, Object body) {
        return base(statusCode, JsonUtils.toJson(body));
    }

    public static APIGatewayProxyResponseEvent error(int statusCode, String message) {
        return base(statusCode, JsonUtils.toJson(new ErrorResponse(message)));
    }

    private static APIGatewayProxyResponseEvent base(int statusCode, String body) {
        return new APIGatewayProxyResponseEvent()
                .withStatusCode(statusCode)
                .withHeaders(Map.of(
                        "Content-Type", "application/json",
                        "Access-Control-Allow-Origin", "*",
                        "Access-Control-Allow-Headers", "*",
                        "Access-Control-Allow-Methods", "OPTIONS,POST"))
                .withBody(body);
    }
}

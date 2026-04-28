package br.com.fiap.serverless.avaliador.handler;

import org.junit.jupiter.api.Test;

import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;

import br.com.fiap.serverless.avaliador.service.AvaliadorService;
import br.com.fiap.serverless.shared.dto.CreateAvaliacaoResponse;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class AvaliadorHandlerTest {

    @Test
    void shouldReturnCreatedWhenPayloadIsValid() {
        AvaliadorService service = mock(AvaliadorService.class);
        when(service.process(any())).thenReturn(new CreateAvaliacaoResponse("uuid", "Avaliação registrada com sucesso."));

        AvaliadorHandler handler = new AvaliadorHandler(service);
        APIGatewayProxyRequestEvent request = new APIGatewayProxyRequestEvent()
                .withBody("""
                        {
                          "nomeAluno":"Luiz Silva",
                          "emailAluno":"luiz@email.com",
                          "disciplina":"Arquitetura Java Serverless",
                          "nota":9.5
                        }
                        """);

        assertEquals(201, handler.handleRequest(request, null).getStatusCode());
    }

    @Test
    void shouldReturnBadRequestWhenBodyIsInvalid() {
        AvaliadorService service = mock(AvaliadorService.class);
        AvaliadorHandler handler = new AvaliadorHandler(service);
        APIGatewayProxyRequestEvent request = new APIGatewayProxyRequestEvent().withBody("{");

        assertEquals(400, handler.handleRequest(request, null).getStatusCode());
    }
}

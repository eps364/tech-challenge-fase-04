package br.com.fiap.serverless.avaliador.handler;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;

import br.com.fiap.serverless.avaliador.service.AvaliadorService;
import br.com.fiap.serverless.shared.dto.CreateAvaliacaoRequest;
import br.com.fiap.serverless.shared.dto.CreateAvaliacaoResponse;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
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
                          "descricao":"Aula muito boa",
                          "nota":9
                        }
                        """);

        assertEquals(201, handler.handleRequest(request, null).getStatusCode());

        ArgumentCaptor<CreateAvaliacaoRequest> captor = ArgumentCaptor.forClass(CreateAvaliacaoRequest.class);
        verify(service).process(captor.capture());
        assertEquals("Aula muito boa", captor.getValue().descricao());
        assertEquals(9, captor.getValue().nota());
    }

    @Test
    void shouldReturnBadRequestWhenBodyIsInvalid() {
        AvaliadorService service = mock(AvaliadorService.class);
        AvaliadorHandler handler = new AvaliadorHandler(service);
        APIGatewayProxyRequestEvent request = new APIGatewayProxyRequestEvent().withBody("{");

        assertEquals(400, handler.handleRequest(request, null).getStatusCode());
    }

    @Test
    void shouldReturnBadRequestWhenNotaIsDecimal() {
        AvaliadorService service = mock(AvaliadorService.class);
        AvaliadorHandler handler = new AvaliadorHandler(service);
        APIGatewayProxyRequestEvent request = new APIGatewayProxyRequestEvent()
                .withBody("""
                        {
                          "descricao":"Aula boa",
                          "nota":8.5
                        }
                        """);

        assertEquals(400, handler.handleRequest(request, null).getStatusCode());
        verify(service, never()).process(any());
    }

    @Test
    void shouldReturnBadRequestWhenPayloadHasUnexpectedFields() {
        AvaliadorService service = mock(AvaliadorService.class);
        AvaliadorHandler handler = new AvaliadorHandler(service);
        APIGatewayProxyRequestEvent request = new APIGatewayProxyRequestEvent()
                .withBody("""
                        {
                          "descricao":"Aula boa",
                          "nota":8,
                          "emailAluno":"luiz@email.com"
                        }
                        """);

        assertEquals(400, handler.handleRequest(request, null).getStatusCode());
        verify(service, never()).process(any());
    }
}

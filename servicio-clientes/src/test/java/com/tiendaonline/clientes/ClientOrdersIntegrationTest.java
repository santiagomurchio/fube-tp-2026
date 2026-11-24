package com.tiendaonline.clientes;

import static org.mockserver.model.HttpRequest.request;
import static org.mockserver.model.HttpResponse.response;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockserver.integration.ClientAndServer;
import org.mockserver.model.MediaType;
import org.mockserver.verify.VerificationTimes;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ClientOrdersIntegrationTest {

    static final ClientAndServer pedidos = ClientAndServer.startClientAndServer(0);

    @Autowired MockMvc mockMvc;

    @AfterAll
    static void stopPedidosMock() {
        pedidos.stop();
    }

    @DynamicPropertySource
    static void pedidosUrl(DynamicPropertyRegistry registry) {
        registry.add("pedidos.base-url", () -> "http://localhost:" + pedidos.getPort());
    }

    @BeforeEach
    void reset() {
        pedidos.reset();
    }

    private String registerClient(String email) throws Exception {
        String location = mockMvc.perform(post("/api/v1/clientes")
                        .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Ana\",\"surname\":\"Perez\",\"email\":\"" + email + "\"}"))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getHeader("Location");
        return location.substring(location.lastIndexOf('/') + 1);
    }

    @Test
    void ordersOfClient_areFetchedFromPedidosService() throws Exception {
        String id = registerClient("ana@example.com");
        pedidos.when(request().withMethod("GET").withPath("/api/v1/pedidos")
                        .withQueryStringParameter("clienteId", id))
                .respond(response().withContentType(MediaType.APPLICATION_JSON).withBody(
                        "[{\"id\":7,\"clienteId\":" + id
                                + ",\"estado\":\"CREADO\",\"montoTotal\":150.00,\"campoNuevo\":\"x\",\"items\":[]}]"));

        mockMvc.perform(get("/api/v1/clientes/" + id + "/pedidos"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(7))
                .andExpect(jsonPath("$[0].estado").value("CREADO"));
    }

    @Test
    void ordersOfUnknownClient_returns404WithoutCallingPedidos() throws Exception {
        mockMvc.perform(get("/api/v1/clientes/9999/pedidos"))
                .andExpect(status().isNotFound());
        pedidos.verify(request(), VerificationTimes.exactly(0));
    }

    @Test
    void pedidosServerError_returns503() throws Exception {
        String id = registerClient("bob@example.com");
        pedidos.when(request().withPath("/api/v1/pedidos")).respond(response().withStatusCode(500));

        mockMvc.perform(get("/api/v1/clientes/" + id + "/pedidos"))
                .andExpect(status().isServiceUnavailable());
    }

    @Test
    void duplicatedEmailIgnoringCase_returns409() throws Exception {
        registerClient("carla@example.com");

        mockMvc.perform(post("/api/v1/clientes")
                        .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Carla\",\"surname\":\"Dos\",\"email\":\"CARLA@example.com\"}"))
                .andExpect(status().isConflict());
    }
}

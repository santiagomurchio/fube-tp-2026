package com.tiendaonline.clientes.integration;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import com.tiendaonline.clientes.dto.PedidoDTO;
import com.tiendaonline.clientes.exceptions.PedidosUnavailableException;

@Component
public class RestPedidosClient implements PedidosClient {

    private static final Logger log = LoggerFactory.getLogger(RestPedidosClient.class);

    private final RestClient restClient;

    public RestPedidosClient(RestClient pedidosRestClient) {
        this.restClient = pedidosRestClient;
    }

    @Override
    public List<PedidoDTO> findByClientId(Long clientId) {
        try {
            return restClient.get()
                    .uri("/api/v1/pedidos?clienteId={clienteId}", clientId)
                    .retrieve()
                    .body(new ParameterizedTypeReference<List<PedidoDTO>>() { });
        } catch (RestClientException ex) {
            log.warn("Could not fetch pedidos for clientId={}", clientId, ex);
            throw new PedidosUnavailableException(ex);
        }
    }
}

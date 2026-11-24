package com.tiendaonline.clientes.integration;

import java.util.List;

import com.tiendaonline.clientes.dto.PedidoDTO;
import com.tiendaonline.clientes.exceptions.PedidosUnavailableException;

public interface PedidosClient {

    List<PedidoDTO> findByClientId(Long clientId) throws PedidosUnavailableException;
}

package com.tiendaonline.clientes.services;

import java.util.List;

import com.tiendaonline.clientes.dto.ClientDTO;
import com.tiendaonline.clientes.dto.PedidoDTO;
import com.tiendaonline.clientes.exceptions.ClientNotFoundException;
import com.tiendaonline.clientes.exceptions.DuplicatedEmailException;
import com.tiendaonline.clientes.exceptions.PedidosUnavailableException;

public interface ClientService {
    
    public ClientDTO registerClient(ClientDTO client) throws DuplicatedEmailException;
    public ClientDTO searchById(Long id) throws ClientNotFoundException;
    public List<PedidoDTO> searchOrders(Long id) throws ClientNotFoundException, PedidosUnavailableException;
}

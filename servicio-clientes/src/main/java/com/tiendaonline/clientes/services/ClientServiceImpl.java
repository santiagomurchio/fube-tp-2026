package com.tiendaonline.clientes.services;

import java.util.List;
import java.util.Locale;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import com.tiendaonline.clientes.dto.ClientDTO;
import com.tiendaonline.clientes.dto.PedidoDTO;
import com.tiendaonline.clientes.exceptions.ClientNotFoundException;
import com.tiendaonline.clientes.exceptions.DuplicatedEmailException;
import com.tiendaonline.clientes.exceptions.PedidosUnavailableException;
import com.tiendaonline.clientes.flags.FeatureFlags;
import com.tiendaonline.clientes.integration.PedidosClient;
import com.tiendaonline.clientes.mapper.ClientMapper;
import com.tiendaonline.clientes.repositories.ClientRepository;
import com.tiendaonline.clientes.services.utils.PiiMasker;

@Service
public class ClientServiceImpl implements ClientService {

    private static final Logger log = LoggerFactory.getLogger(ClientServiceImpl.class);

    private ClientRepository clientRepository;
    private FeatureFlags flags;
    private PiiMasker masker;
    private PedidosClient pedidosClient;

    @Autowired
    ClientServiceImpl(ClientRepository repository, FeatureFlags flags, PiiMasker masker,
            PedidosClient pedidosClient) {
        this.clientRepository = repository;
        this.flags = flags;
        this.masker = masker;
        this.pedidosClient = pedidosClient;
    }

    @Override
    public ClientDTO registerClient(ClientDTO client) throws DuplicatedEmailException {
        if (client.getId() != null) throw new IllegalArgumentException("A new client can not have id");
        ClientDTO normalized = client.toBuilder().email(client.getEmail().trim().toLowerCase(Locale.ROOT)).build();
        if (clientRepository.existsByEmail(normalized.getEmail())) throw new DuplicatedEmailException();
        try {
            ClientDTO created = ClientMapper.toDto(clientRepository.save(ClientMapper.toEntity(normalized)));
            log.info("Client registered clientId={}", created.getId());
            return created;
        } catch (DataIntegrityViolationException ex) {
            throw new DuplicatedEmailException();
        }
    }

    @Override
    public ClientDTO searchById(Long id) throws ClientNotFoundException {
        ClientDTO client = clientRepository.findById(id).map(ClientMapper::toDto)
                .orElseThrow(ClientNotFoundException::new);
        if (flags.isMaskPiiEnabled()) client = masker.maskClient(client);
        return client;
    }

    @Override
    public List<PedidoDTO> searchOrders(Long id) throws ClientNotFoundException, PedidosUnavailableException {
        if (!clientRepository.existsById(id)) throw new ClientNotFoundException();
        return pedidosClient.findByClientId(id);
    }

}

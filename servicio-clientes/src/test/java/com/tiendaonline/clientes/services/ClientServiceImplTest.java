package com.tiendaonline.clientes.services;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.dao.DataIntegrityViolationException;

import com.tiendaonline.clientes.dto.ClientDTO;
import com.tiendaonline.clientes.dto.PedidoDTO;
import com.tiendaonline.clientes.exceptions.PedidosUnavailableException;
import com.tiendaonline.clientes.integration.PedidosClient;
import com.tiendaonline.clientes.entities.Client;
import com.tiendaonline.clientes.exceptions.ClientNotFoundException;
import com.tiendaonline.clientes.exceptions.DuplicatedEmailException;
import com.tiendaonline.clientes.flags.FeatureFlags;
import com.tiendaonline.clientes.repositories.ClientRepository;
import com.tiendaonline.clientes.services.utils.PiiMasker;

@ExtendWith(MockitoExtension.class)
public class ClientServiceImplTest {

    @Mock
    FeatureFlags flags;
    @Mock
    PiiMasker masker;
    @Mock
    ClientRepository repository;
    @Mock
    PedidosClient pedidosClient;
    @InjectMocks
    ClientServiceImpl service;

    ClientDTO getUnsavedTestSubjectDTO() {
        return ClientDTO.builder().name("Test").surname("Subject").email("test@test.test").build();
    }

    ClientDTO getSavedTestSubjectDTO() {
        return ClientDTO.builder().name("Test").surname("Subject").email("test@test.test").id(1L).build();
    }

    ClientDTO getMaskedTestSubjectDTO() {
        return ClientDTO.builder().name("T***").surname("S***").email("****@****.test").build();
    }

    Client getSavedTestSubjectEntity() {
        return Client.builder().id(1L).name("Test").surname("Subject").email("test@test.test").build();
    }

    Client getUnsavedTestSubjectEntity() {
        return Client.builder().name("Test").surname("Subject").email("test@test.test").build();
    }

    @Test
    void searchById_throws_NotFound() {
        when(repository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ClientNotFoundException.class, () -> service.searchById(1L));
    }

    @Test
    void searchById_flagInactive_returnsDataUnmasked() {
        when(repository.findById(1L)).thenReturn(Optional.of(this.getSavedTestSubjectEntity()));
        when(flags.isMaskPiiEnabled()).thenReturn(false);

        assertEquals(getSavedTestSubjectDTO(), service.searchById(1L));

    }

    @Test
    void searchById_flagActive_returnsMaskedData() {
        when(repository.findById(1L)).thenReturn(Optional.of(getSavedTestSubjectEntity()));
        when(flags.isMaskPiiEnabled()).thenReturn(true);
        when(masker.maskClient(getSavedTestSubjectDTO())).thenReturn(getMaskedTestSubjectDTO());

        assertEquals(getMaskedTestSubjectDTO(), service.searchById(1L));
    }

    @Test
    void registerClient_throwsDuplicatedEmail() {
        when(repository.existsByEmail("test@test.test")).thenReturn(true);

        assertThrows(DuplicatedEmailException.class,
                () -> service.registerClient(getUnsavedTestSubjectDTO()));
        verify(repository, never()).save(any());
    }

    @Test
    void registerClient_savesAndReturnsDtoUnmasked() {
        when(repository.save(getUnsavedTestSubjectEntity())).thenReturn(getSavedTestSubjectEntity());

        ClientDTO result = service.registerClient(getUnsavedTestSubjectDTO());

        assertEquals(getSavedTestSubjectDTO(), result);
        verify(repository).save(getUnsavedTestSubjectEntity());
    }

    @Test
    void registerClient_normalizesEmailToLowerCase() {
        ClientDTO input = getUnsavedTestSubjectDTO().toBuilder().email("  Test@Test.TEST ").build();
        when(repository.save(getUnsavedTestSubjectEntity())).thenReturn(getSavedTestSubjectEntity());

        service.registerClient(input);

        verify(repository).existsByEmail("test@test.test");
        verify(repository).save(getUnsavedTestSubjectEntity());
    }

    @Test
    void registerClient_uniqueConstraintRace_throwsDuplicatedEmail() {
        when(repository.save(any(Client.class))).thenThrow(new DataIntegrityViolationException("dup"));

        assertThrows(DuplicatedEmailException.class, () -> service.registerClient(getUnsavedTestSubjectDTO()));
    }

    @Test
    void searchOrders_returnsOrdersFromPedidosService() {
        PedidoDTO pedido = new PedidoDTO(10L, 1L, "CREADO", null, null, null, List.of());
        when(repository.existsById(1L)).thenReturn(true);
        when(pedidosClient.findByClientId(1L)).thenReturn(List.of(pedido));

        assertEquals(List.of(pedido), service.searchOrders(1L));
    }

    @Test
    void searchOrders_unknownClient_throwsNotFoundWithoutCallingPedidos() {
        when(repository.existsById(1L)).thenReturn(false);

        assertThrows(ClientNotFoundException.class, () -> service.searchOrders(1L));
        verify(pedidosClient, never()).findByClientId(any());
    }

    @Test
    void searchOrders_pedidosDown_propagatesUnavailable() {
        when(repository.existsById(1L)).thenReturn(true);
        when(pedidosClient.findByClientId(1L)).thenThrow(new PedidosUnavailableException(new RuntimeException()));

        assertThrows(PedidosUnavailableException.class, () -> service.searchOrders(1L));
    }

}

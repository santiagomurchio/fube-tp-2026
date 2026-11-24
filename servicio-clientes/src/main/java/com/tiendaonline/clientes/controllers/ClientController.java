package com.tiendaonline.clientes.controllers;

import java.net.URI;
import java.util.List;

import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import com.tiendaonline.clientes.dto.ClientDTO;
import com.tiendaonline.clientes.dto.PedidoDTO;
import com.tiendaonline.clientes.services.ClientService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/clientes")
@Tag(name = "Clients")
public class ClientController {

    private final ClientService service;

    public ClientController(ClientService clientService) {
        this.service = clientService;
    }

    @PostMapping
    @Operation(summary = "Register a client")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Client Registered"),
            @ApiResponse(responseCode = "400", description = "Invalid Data",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "409", description = "A client with that email already exists",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    })
    public ResponseEntity<ClientDTO> register(@Valid @RequestBody ClientDTO client) {
        ClientDTO created = service.registerClient(client);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}").buildAndExpand(created.getId()).toUri();
        return ResponseEntity.created(location).body(created);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Search a client by Id",
            description = "Name, surname and email are masked according to feature flag mask-pii status")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Client founded"),
            @ApiResponse(responseCode = "404", description = "Non-existent client",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    })
    public ClientDTO findById(@PathVariable Long id) {
        return service.searchById(id);
    }

    @GetMapping("/{id}/pedidos")
    @Operation(summary = "List the orders of a client",
            description = "Queries servicio-pedidos synchronously")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Orders of the client (may be empty)"),
            @ApiResponse(responseCode = "404", description = "Non-existent client",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "503", description = "servicio-pedidos is unavailable",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    })
    public List<PedidoDTO> findOrders(@PathVariable Long id) {
        return service.searchOrders(id);
    }
}

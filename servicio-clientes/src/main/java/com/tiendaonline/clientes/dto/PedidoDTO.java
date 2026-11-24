package com.tiendaonline.clientes.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record PedidoDTO(
        Long id,
        Long clienteId,
        String estado,
        BigDecimal montoTotal,
        LocalDateTime fechaCreacion,
        LocalDateTime fechaActualizacion,
        List<ItemPedidoDTO> items) {

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record ItemPedidoDTO(Long id, Long productoId, int cantidad, BigDecimal precioUnitario,
                                BigDecimal subtotal) {
    }
}

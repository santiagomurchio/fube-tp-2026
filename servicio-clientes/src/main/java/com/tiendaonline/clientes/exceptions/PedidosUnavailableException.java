package com.tiendaonline.clientes.exceptions;

public class PedidosUnavailableException extends RuntimeException {

    public PedidosUnavailableException(Throwable cause) {
        super("servicio-pedidos is unavailable", cause);
    }
}

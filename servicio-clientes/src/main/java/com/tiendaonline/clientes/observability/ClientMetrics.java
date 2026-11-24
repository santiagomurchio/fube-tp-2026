package com.tiendaonline.clientes.observability;

import org.springframework.stereotype.Component;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;

/** Metricas de negocio del servicio (se publican en /actuator/prometheus). */
@Component
public class ClientMetrics {

    private final Counter created;
    private final Counter found;
    private final Counter notFound;

    public ClientMetrics(MeterRegistry registry) {
        this.created = Counter.builder("clientes.creados").description("Clientes registrados").register(registry);
        this.found = Counter.builder("clientes.encontrados").description("Consultas de cliente con resultado")
                .register(registry);
        this.notFound = Counter.builder("clientes.no_encontrados").description("Consultas de cliente sin resultado")
                .register(registry);
    }

    public void created() {
        created.increment();
    }

    public void found() {
        found.increment();
    }

    public void notFound() {
        notFound.increment();
    }
}

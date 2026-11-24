package com.tiendaonline.clientes;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.emptyOrNullString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.actuate.observability.AutoConfigureObservability;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
@AutoConfigureObservability
@ActiveProfiles("test")
class ObservabilityIntegrationTest {

    @Autowired MockMvc mockMvc;

    @Test
    void responses_exposeTraceIdHeader() throws Exception {
        mockMvc.perform(get("/api/v1/clientes/987654"))
                .andExpect(status().isNotFound())
                .andExpect(header().string("X-Trace-Id", not(emptyOrNullString())));
    }

    @Test
    void prometheusEndpoint_publishesBusinessMetrics() throws Exception {
        mockMvc.perform(get("/api/v1/clientes/987655")).andExpect(status().isNotFound());

        mockMvc.perform(get("/actuator/prometheus"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("clientes_no_encontrados_total")));
    }
}

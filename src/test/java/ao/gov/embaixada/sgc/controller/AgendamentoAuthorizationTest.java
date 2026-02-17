package ao.gov.embaixada.sgc.controller;

import ao.gov.embaixada.sgc.dto.*;
import ao.gov.embaixada.sgc.enums.EstadoAgendamento;
import ao.gov.embaixada.sgc.enums.TipoAgendamento;
import ao.gov.embaixada.sgc.service.AgendamentoService;
import ao.gov.embaixada.sgc.service.AgendamentoSlotConfig;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AgendamentoController.class)
class AgendamentoAuthorizationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private AgendamentoService agendamentoService;

    @Autowired
    private AgendamentoSlotConfig slotConfig;

    @TestConfiguration
    @EnableMethodSecurity
    static class TestConfig {
        @Bean
        public AgendamentoService agendamentoService() {
            return mock(AgendamentoService.class);
        }

        @Bean
        public AgendamentoSlotConfig agendamentoSlotConfig() {
            return mock(AgendamentoSlotConfig.class);
        }
    }

    private AgendamentoResponse sampleResponse() {
        return new AgendamentoResponse(
                UUID.randomUUID(), UUID.randomUUID(), "Joao Silva",
                "joao@test.de", TipoAgendamento.PASSAPORTE,
                "SGC-AGD-00001", EstadoAgendamento.PENDENTE,
                LocalDateTime.now().plusDays(7), 30,
                "Embaixada de Angola — Berlim", null,
                null, Instant.now(), Instant.now());
    }

    @Test
    void officerCanCreateAgendamento() throws Exception {
        when(agendamentoService.create(any())).thenReturn(sampleResponse());

        AgendamentoCreateRequest request = new AgendamentoCreateRequest(
                UUID.randomUUID(), TipoAgendamento.PASSAPORTE,
                LocalDateTime.now().plusDays(7), "Test");

        mockMvc.perform(post("/api/v1/agendamentos")
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_OFFICER")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());
    }

    @Test
    void viewerCanListAgendamentos() throws Exception {
        when(agendamentoService.findAll(any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(sampleResponse())));

        mockMvc.perform(get("/api/v1/agendamentos")
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_VIEWER"))))
                .andExpect(status().isOk());
    }

    @Test
    void adminCanDeleteAgendamento() throws Exception {
        mockMvc.perform(delete("/api/v1/agendamentos/{id}", UUID.randomUUID())
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_ADMIN"))))
                .andExpect(status().isNoContent());
    }

    @Test
    void viewerCannotCreateAgendamento() throws Exception {
        AgendamentoCreateRequest request = new AgendamentoCreateRequest(
                UUID.randomUUID(), TipoAgendamento.PASSAPORTE,
                LocalDateTime.now().plusDays(7), "Test");

        mockMvc.perform(post("/api/v1/agendamentos")
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_VIEWER")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    void officerCannotDeleteAgendamento() throws Exception {
        mockMvc.perform(delete("/api/v1/agendamentos/{id}", UUID.randomUUID())
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_OFFICER"))))
                .andExpect(status().isForbidden());
    }

    @Test
    void unauthenticatedGets401() throws Exception {
        mockMvc.perform(get("/api/v1/agendamentos"))
                .andExpect(status().isUnauthorized());
    }
}

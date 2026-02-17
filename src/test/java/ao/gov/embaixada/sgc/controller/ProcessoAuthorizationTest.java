package ao.gov.embaixada.sgc.controller;

import ao.gov.embaixada.sgc.dto.ProcessoCreateRequest;
import ao.gov.embaixada.sgc.dto.ProcessoResponse;
import ao.gov.embaixada.sgc.enums.EstadoProcesso;
import ao.gov.embaixada.sgc.enums.Prioridade;
import ao.gov.embaixada.sgc.enums.TipoProcesso;
import ao.gov.embaixada.sgc.service.ProcessoService;
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

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ProcessoController.class)
class ProcessoAuthorizationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ProcessoService processoService;

    @TestConfiguration
    @EnableMethodSecurity
    static class TestConfig {
        @Bean
        public ProcessoService processoService() {
            return mock(ProcessoService.class);
        }
    }

    private ProcessoResponse sampleResponse() {
        return new ProcessoResponse(
                UUID.randomUUID(), UUID.randomUUID(), "Joao Silva",
                TipoProcesso.PASSAPORTE, "SGC-PAS-00001",
                "Renovacao de passaporte", EstadoProcesso.RASCUNHO,
                Prioridade.NORMAL, "funcionario1",
                new BigDecimal("50.00"), false, null, null,
                0, Instant.now(), Instant.now());
    }

    @Test
    void officerCanCreateProcesso() throws Exception {
        when(processoService.create(any())).thenReturn(sampleResponse());

        ProcessoCreateRequest request = new ProcessoCreateRequest(
                UUID.randomUUID(), TipoProcesso.PASSAPORTE,
                "Renovacao", Prioridade.NORMAL, "func1", new BigDecimal("50.00"));

        mockMvc.perform(post("/api/v1/processos")
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_OFFICER")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());
    }

    @Test
    void viewerCanListProcessos() throws Exception {
        when(processoService.findAll(any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(sampleResponse())));

        mockMvc.perform(get("/api/v1/processos")
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_VIEWER"))))
                .andExpect(status().isOk());
    }

    @Test
    void adminCanDeleteProcesso() throws Exception {
        mockMvc.perform(delete("/api/v1/processos/{id}", UUID.randomUUID())
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_ADMIN"))))
                .andExpect(status().isNoContent());
    }

    @Test
    void viewerCannotCreateProcesso() throws Exception {
        ProcessoCreateRequest request = new ProcessoCreateRequest(
                UUID.randomUUID(), TipoProcesso.VISTO,
                "Test", Prioridade.NORMAL, null, BigDecimal.ZERO);

        mockMvc.perform(post("/api/v1/processos")
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_VIEWER")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    void officerCannotDeleteProcesso() throws Exception {
        mockMvc.perform(delete("/api/v1/processos/{id}", UUID.randomUUID())
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_OFFICER"))))
                .andExpect(status().isForbidden());
    }

    @Test
    void unauthenticatedGets401() throws Exception {
        mockMvc.perform(get("/api/v1/processos"))
                .andExpect(status().isUnauthorized());
    }
}

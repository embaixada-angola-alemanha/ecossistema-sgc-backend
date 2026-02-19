package ao.gov.embaixada.sgc.controller;

import ao.gov.embaixada.sgc.dto.CidadaoCreateRequest;
import ao.gov.embaixada.sgc.dto.CidadaoResponse;
import ao.gov.embaixada.sgc.enums.EstadoCidadao;
import ao.gov.embaixada.sgc.enums.Sexo;
import ao.gov.embaixada.sgc.service.CitizenContextService;
import ao.gov.embaixada.sgc.service.CidadaoService;
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
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(CidadaoController.class)
class CidadaoAuthorizationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private CidadaoService cidadaoService;

    @Autowired
    private CitizenContextService citizenContext;

    @TestConfiguration
    @EnableMethodSecurity
    static class TestConfig {
        @Bean
        public CidadaoService cidadaoService() {
            return mock(CidadaoService.class);
        }

        @Bean
        public CitizenContextService citizenContextService() {
            return mock(CitizenContextService.class);
        }
    }

    private CidadaoResponse sampleResponse() {
        return new CidadaoResponse(
                UUID.randomUUID(), "N1234567", "Joao Silva", LocalDate.of(1990, 1, 15),
                Sexo.MASCULINO, "Angolana", null,
                "joao@email.com", null, null, null,
                EstadoCidadao.ACTIVO, null, 0, 0, Instant.now(), Instant.now());
    }

    @Test
    void officerCanCreateCidadao() throws Exception {
        when(cidadaoService.create(any())).thenReturn(sampleResponse());

        CidadaoCreateRequest request = new CidadaoCreateRequest(
                "N1234567", "Joao Silva", LocalDate.of(1990, 1, 15),
                Sexo.MASCULINO, "Angolana", null, "joao@email.com", null, null, null);

        mockMvc.perform(post("/api/v1/cidadaos")
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_OFFICER")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());
    }

    @Test
    void viewerCanListCidadaos() throws Exception {
        when(cidadaoService.findAll(any(), any(), any(), any(), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(sampleResponse())));

        mockMvc.perform(get("/api/v1/cidadaos")
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_VIEWER"))))
                .andExpect(status().isOk());
    }

    @Test
    void adminCanDeleteCidadao() throws Exception {
        mockMvc.perform(delete("/api/v1/cidadaos/{id}", UUID.randomUUID())
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_ADMIN"))))
                .andExpect(status().isNoContent());
    }

    @Test
    void viewerCannotCreateCidadao() throws Exception {
        CidadaoCreateRequest request = new CidadaoCreateRequest(
                "N9999999", "Test User", null, null, "Angolana", null, null, null, null, null);

        mockMvc.perform(post("/api/v1/cidadaos")
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_VIEWER")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    void officerCannotDeleteCidadao() throws Exception {
        mockMvc.perform(delete("/api/v1/cidadaos/{id}", UUID.randomUUID())
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_OFFICER"))))
                .andExpect(status().isForbidden());
    }

    @Test
    void unauthenticatedGets401() throws Exception {
        mockMvc.perform(get("/api/v1/cidadaos"))
                .andExpect(status().isUnauthorized());
    }
}

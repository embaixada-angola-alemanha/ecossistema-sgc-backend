package ao.gov.embaixada.sgc.controller;

import ao.gov.embaixada.sgc.dto.DocumentoCreateRequest;
import ao.gov.embaixada.sgc.dto.DocumentoResponse;
import ao.gov.embaixada.sgc.enums.EstadoDocumento;
import ao.gov.embaixada.sgc.enums.TipoDocumento;
import ao.gov.embaixada.sgc.service.DocumentoService;
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
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(DocumentoController.class)
class DocumentoAuthorizationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private DocumentoService documentoService;

    @TestConfiguration
    @EnableMethodSecurity
    static class TestConfig {
        @Bean
        public DocumentoService documentoService() {
            return mock(DocumentoService.class);
        }
    }

    private final UUID cidadaoId = UUID.randomUUID();

    private DocumentoResponse sampleResponse() {
        return new DocumentoResponse(
                UUID.randomUUID(), cidadaoId, "Joao Silva",
                TipoDocumento.PASSAPORTE, "P123456",
                LocalDate.of(2024, 1, 1), LocalDate.of(2029, 1, 1),
                null, null, null, null,
                EstadoDocumento.PENDENTE, Instant.now(), Instant.now());
    }

    @Test
    void officerCanCreateDocumento() throws Exception {
        when(documentoService.create(eq(cidadaoId), any())).thenReturn(sampleResponse());

        DocumentoCreateRequest request = new DocumentoCreateRequest(
                TipoDocumento.PASSAPORTE, "P123456",
                LocalDate.of(2024, 1, 1), LocalDate.of(2029, 1, 1),
                null, null, null, null);

        mockMvc.perform(post("/api/v1/cidadaos/{cidadaoId}/documentos", cidadaoId)
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_OFFICER")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());
    }

    @Test
    void viewerCanListDocumentos() throws Exception {
        when(documentoService.findByCidadaoId(eq(cidadaoId), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(sampleResponse())));

        mockMvc.perform(get("/api/v1/cidadaos/{cidadaoId}/documentos", cidadaoId)
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_VIEWER"))))
                .andExpect(status().isOk());
    }

    @Test
    void consulCanDeleteDocumento() throws Exception {
        UUID docId = UUID.randomUUID();
        mockMvc.perform(delete("/api/v1/cidadaos/{cidadaoId}/documentos/{id}", cidadaoId, docId)
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_CONSUL"))))
                .andExpect(status().isNoContent());
    }

    @Test
    void viewerCannotCreateDocumento() throws Exception {
        DocumentoCreateRequest request = new DocumentoCreateRequest(
                TipoDocumento.BILHETE_IDENTIDADE, "BI001",
                null, null, null, null, null, null);

        mockMvc.perform(post("/api/v1/cidadaos/{cidadaoId}/documentos", cidadaoId)
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_VIEWER")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    void officerCannotDeleteDocumento() throws Exception {
        UUID docId = UUID.randomUUID();
        mockMvc.perform(delete("/api/v1/cidadaos/{cidadaoId}/documentos/{id}", cidadaoId, docId)
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_OFFICER"))))
                .andExpect(status().isForbidden());
    }

    @Test
    void unauthenticatedGets401() throws Exception {
        mockMvc.perform(get("/api/v1/cidadaos/{cidadaoId}/documentos", cidadaoId))
                .andExpect(status().isUnauthorized());
    }
}

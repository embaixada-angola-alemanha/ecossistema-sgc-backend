package ao.gov.embaixada.sgc.controller;

import ao.gov.embaixada.sgc.dto.*;
import ao.gov.embaixada.sgc.enums.EstadoVisto;
import ao.gov.embaixada.sgc.enums.TipoVisto;
import ao.gov.embaixada.sgc.service.CitizenContextService;
import ao.gov.embaixada.sgc.service.VisaDocumentChecklistService;
import ao.gov.embaixada.sgc.service.VisaFeeCalculator;
import ao.gov.embaixada.sgc.service.VisaService;
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
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(VisaController.class)
class VisaAuthorizationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private VisaService visaService;

    @Autowired
    private VisaFeeCalculator feeCalculator;

    @Autowired
    private VisaDocumentChecklistService checklistService;

    @Autowired
    private CitizenContextService citizenContext;

    @TestConfiguration
    @EnableMethodSecurity
    static class TestConfig {
        @Bean
        public VisaService visaService() {
            return mock(VisaService.class);
        }

        @Bean
        public VisaFeeCalculator visaFeeCalculator() {
            return mock(VisaFeeCalculator.class);
        }

        @Bean
        public VisaDocumentChecklistService visaDocumentChecklistService() {
            return mock(VisaDocumentChecklistService.class);
        }

        @Bean
        public CitizenContextService citizenContextService() {
            return mock(CitizenContextService.class);
        }
    }

    private VisaResponse sampleResponse() {
        return new VisaResponse(
                UUID.randomUUID(), UUID.randomUUID(), "Joao Silva",
                TipoVisto.TURISTA, "SGC-VIS-00001",
                EstadoVisto.RASCUNHO, "Alemã",
                "Turismo em Luanda",
                LocalDate.now().plusMonths(1), LocalDate.now().plusMonths(2),
                "Hotel Presidente", null, null,
                new BigDecimal("60.00"), false,
                null, null, null, null,
                0, Instant.now(), Instant.now());
    }

    @Test
    void officerCanCreateVisa() throws Exception {
        when(visaService.create(any())).thenReturn(sampleResponse());

        VisaCreateRequest request = new VisaCreateRequest(
                UUID.randomUUID(), TipoVisto.TURISTA, "Alemã",
                "Turismo", LocalDate.now().plusMonths(1),
                LocalDate.now().plusMonths(2), "Hotel", null, null);

        mockMvc.perform(post("/api/v1/visas")
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_OFFICER")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());
    }

    @Test
    void viewerCanListVisas() throws Exception {
        when(visaService.findAll(any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(sampleResponse())));

        mockMvc.perform(get("/api/v1/visas")
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_VIEWER"))))
                .andExpect(status().isOk());
    }

    @Test
    void adminCanDeleteVisa() throws Exception {
        mockMvc.perform(delete("/api/v1/visas/{id}", UUID.randomUUID())
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_ADMIN"))))
                .andExpect(status().isNoContent());
    }

    @Test
    void viewerCannotCreateVisa() throws Exception {
        VisaCreateRequest request = new VisaCreateRequest(
                UUID.randomUUID(), TipoVisto.TURISTA, "Alemã",
                "Test", null, null, null, null, null);

        mockMvc.perform(post("/api/v1/visas")
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_VIEWER")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    void officerCannotDeleteVisa() throws Exception {
        mockMvc.perform(delete("/api/v1/visas/{id}", UUID.randomUUID())
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_OFFICER"))))
                .andExpect(status().isForbidden());
    }

    @Test
    void unauthenticatedGets401() throws Exception {
        mockMvc.perform(get("/api/v1/visas"))
                .andExpect(status().isUnauthorized());
    }
}

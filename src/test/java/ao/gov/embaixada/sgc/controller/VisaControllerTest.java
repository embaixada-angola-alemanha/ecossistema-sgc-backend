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
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(VisaController.class)
@AutoConfigureMockMvc(addFilters = false)
class VisaControllerTest {

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
    void shouldCreateVisa() throws Exception {
        VisaResponse response = sampleResponse();
        when(visaService.create(any())).thenReturn(response);

        VisaCreateRequest request = new VisaCreateRequest(
                UUID.randomUUID(), TipoVisto.TURISTA, "Alemã",
                "Turismo", LocalDate.now().plusMonths(1),
                LocalDate.now().plusMonths(2), "Hotel", null, null);

        mockMvc.perform(post("/api/v1/visas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.numeroVisto").value("SGC-VIS-00001"))
                .andExpect(jsonPath("$.data.estado").value("RASCUNHO"));
    }

    @Test
    void shouldGetVisaById() throws Exception {
        VisaResponse response = sampleResponse();
        when(visaService.findById(response.id())).thenReturn(response);

        mockMvc.perform(get("/api/v1/visas/{id}", response.id()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.tipo").value("TURISTA"));
    }

    @Test
    void shouldListVisas() throws Exception {
        Page<VisaResponse> page = new PageImpl<>(List.of(sampleResponse()));
        when(visaService.findAll(any(Pageable.class))).thenReturn(page);

        mockMvc.perform(get("/api/v1/visas"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content[0].numeroVisto").exists());
    }

    @Test
    void shouldUpdateEstado() throws Exception {
        VisaResponse response = new VisaResponse(
                UUID.randomUUID(), UUID.randomUUID(), "Joao Silva",
                TipoVisto.TURISTA, "SGC-VIS-00001",
                EstadoVisto.SUBMETIDO, "Alemã",
                "Turismo", null, null, null, null, null,
                new BigDecimal("60.00"), false,
                null, null, null, null,
                0, Instant.now(), Instant.now());

        when(visaService.updateEstado(any(), any(), any())).thenReturn(response);

        mockMvc.perform(patch("/api/v1/visas/{id}/estado", response.id())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"estado\":\"SUBMETIDO\",\"comentario\":\"Submissao\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.estado").value("SUBMETIDO"));
    }

    @Test
    void shouldDeleteVisa() throws Exception {
        UUID id = UUID.randomUUID();

        mockMvc.perform(delete("/api/v1/visas/{id}", id))
                .andExpect(status().isNoContent());
    }

    @Test
    void shouldGetFees() throws Exception {
        VisaFeeResponse feeResponse = new VisaFeeResponse(
                TipoVisto.TURISTA, new BigDecimal("60.00"), "EUR", false);
        when(feeCalculator.getFeeResponse(TipoVisto.TURISTA)).thenReturn(feeResponse);

        mockMvc.perform(get("/api/v1/visas/fees")
                        .param("tipo", "TURISTA"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.valor").value(60.00))
                .andExpect(jsonPath("$.data.moeda").value("EUR"))
                .andExpect(jsonPath("$.data.isento").value(false));
    }

    @Test
    void shouldGetChecklist() throws Exception {
        VisaChecklistResponse checklistResponse = new VisaChecklistResponse(
                TipoVisto.TURISTA, List.of("Passaporte válido", "Foto 3x4"));
        when(checklistService.getChecklistResponse(TipoVisto.TURISTA)).thenReturn(checklistResponse);

        mockMvc.perform(get("/api/v1/visas/checklist")
                        .param("tipo", "TURISTA"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.documentosRequeridos[0]").value("Passaporte válido"))
                .andExpect(jsonPath("$.data.documentosRequeridos[1]").value("Foto 3x4"));
    }
}

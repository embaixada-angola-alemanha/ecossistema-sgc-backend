package ao.gov.embaixada.sgc.controller;

import ao.gov.embaixada.sgc.dto.ProcessoCreateRequest;
import ao.gov.embaixada.sgc.dto.ProcessoResponse;
import ao.gov.embaixada.sgc.enums.EstadoProcesso;
import ao.gov.embaixada.sgc.enums.Prioridade;
import ao.gov.embaixada.sgc.enums.TipoProcesso;
import ao.gov.embaixada.sgc.service.CitizenContextService;
import ao.gov.embaixada.sgc.service.ProcessoService;
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
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ProcessoController.class)
@AutoConfigureMockMvc(addFilters = false)
class ProcessoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ProcessoService processoService;

    @Autowired
    private CitizenContextService citizenContext;

    @TestConfiguration
    static class TestConfig {
        @Bean
        public ProcessoService processoService() {
            return mock(ProcessoService.class);
        }

        @Bean
        public CitizenContextService citizenContextService() {
            return mock(CitizenContextService.class);
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
    void shouldCreateProcesso() throws Exception {
        ProcessoResponse response = sampleResponse();
        when(processoService.create(any())).thenReturn(response);

        ProcessoCreateRequest request = new ProcessoCreateRequest(
                UUID.randomUUID(), TipoProcesso.PASSAPORTE,
                "Renovacao", Prioridade.NORMAL, "func1", new BigDecimal("50.00"));

        mockMvc.perform(post("/api/v1/processos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.numeroProcesso").value("SGC-PAS-00001"))
                .andExpect(jsonPath("$.data.estado").value("RASCUNHO"));
    }

    @Test
    void shouldGetProcessoById() throws Exception {
        ProcessoResponse response = sampleResponse();
        when(processoService.findById(response.id())).thenReturn(response);

        mockMvc.perform(get("/api/v1/processos/{id}", response.id()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.tipo").value("PASSAPORTE"));
    }

    @Test
    void shouldListProcessos() throws Exception {
        Page<ProcessoResponse> page = new PageImpl<>(List.of(sampleResponse()));
        when(processoService.findAll(any(Pageable.class))).thenReturn(page);

        mockMvc.perform(get("/api/v1/processos"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content[0].numeroProcesso").exists());
    }

    @Test
    void shouldUpdateEstado() throws Exception {
        ProcessoResponse response = new ProcessoResponse(
                UUID.randomUUID(), UUID.randomUUID(), "Joao Silva",
                TipoProcesso.PASSAPORTE, "SGC-PAS-00001",
                "Test", EstadoProcesso.SUBMETIDO,
                Prioridade.NORMAL, null,
                BigDecimal.ZERO, false, null, null,
                0, Instant.now(), Instant.now());

        when(processoService.updateEstado(any(), any(), any())).thenReturn(response);

        mockMvc.perform(patch("/api/v1/processos/{id}/estado", response.id())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"estado\":\"SUBMETIDO\",\"comentario\":\"Submissao\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.estado").value("SUBMETIDO"));
    }

    @Test
    void shouldDeleteProcesso() throws Exception {
        UUID id = UUID.randomUUID();

        mockMvc.perform(delete("/api/v1/processos/{id}", id))
                .andExpect(status().isNoContent());
    }
}

package ao.gov.embaixada.sgc.controller;

import ao.gov.embaixada.sgc.dto.*;
import ao.gov.embaixada.sgc.enums.EstadoAgendamento;
import ao.gov.embaixada.sgc.enums.TipoAgendamento;
import ao.gov.embaixada.sgc.service.AgendamentoService;
import ao.gov.embaixada.sgc.service.AgendamentoSlotConfig;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AgendamentoController.class)
@AutoConfigureMockMvc(addFilters = false)
class AgendamentoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private AgendamentoService agendamentoService;

    @Autowired
    private AgendamentoSlotConfig slotConfig;

    @TestConfiguration
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
                "Embaixada de Angola — Berlim", "Renovacao",
                null, Instant.now(), Instant.now());
    }

    @Test
    void shouldCreateAgendamento() throws Exception {
        AgendamentoResponse response = sampleResponse();
        when(agendamentoService.create(any())).thenReturn(response);

        AgendamentoCreateRequest request = new AgendamentoCreateRequest(
                UUID.randomUUID(), TipoAgendamento.PASSAPORTE,
                LocalDateTime.now().plusDays(7), "Renovacao");

        mockMvc.perform(post("/api/v1/agendamentos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.numeroAgendamento").value("SGC-AGD-00001"))
                .andExpect(jsonPath("$.data.estado").value("PENDENTE"));
    }

    @Test
    void shouldGetAgendamentoById() throws Exception {
        AgendamentoResponse response = sampleResponse();
        when(agendamentoService.findById(response.id())).thenReturn(response);

        mockMvc.perform(get("/api/v1/agendamentos/{id}", response.id()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.tipo").value("PASSAPORTE"));
    }

    @Test
    void shouldListAgendamentos() throws Exception {
        when(agendamentoService.findAll(any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(sampleResponse())));

        mockMvc.perform(get("/api/v1/agendamentos"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content[0].numeroAgendamento").exists());
    }

    @Test
    void shouldReschedule() throws Exception {
        AgendamentoResponse response = new AgendamentoResponse(
                UUID.randomUUID(), UUID.randomUUID(), "Joao Silva",
                "joao@test.de", TipoAgendamento.PASSAPORTE,
                "SGC-AGD-00001", EstadoAgendamento.REAGENDADO,
                LocalDateTime.now().plusDays(14), 30,
                "Embaixada de Angola — Berlim", "Nova data",
                null, Instant.now(), Instant.now());

        when(agendamentoService.reschedule(any(), any())).thenReturn(response);

        AgendamentoUpdateRequest request = new AgendamentoUpdateRequest(
                LocalDateTime.now().plusDays(14), "Nova data");

        mockMvc.perform(put("/api/v1/agendamentos/{id}", response.id())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.estado").value("REAGENDADO"));
    }

    @Test
    void shouldUpdateEstado() throws Exception {
        AgendamentoResponse response = new AgendamentoResponse(
                UUID.randomUUID(), UUID.randomUUID(), "Joao Silva",
                "joao@test.de", TipoAgendamento.PASSAPORTE,
                "SGC-AGD-00001", EstadoAgendamento.CONFIRMADO,
                LocalDateTime.now().plusDays(7), 30,
                "Embaixada de Angola — Berlim", null,
                null, Instant.now(), Instant.now());

        when(agendamentoService.updateEstado(any(), any(), any())).thenReturn(response);

        mockMvc.perform(patch("/api/v1/agendamentos/{id}/estado", response.id())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"estado\":\"CONFIRMADO\",\"comentario\":\"Confirmado\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.estado").value("CONFIRMADO"));
    }

    @Test
    void shouldDeleteAgendamento() throws Exception {
        UUID id = UUID.randomUUID();

        mockMvc.perform(delete("/api/v1/agendamentos/{id}", id))
                .andExpect(status().isNoContent());
    }

    @Test
    void shouldGetAvailableSlots() throws Exception {
        LocalDate date = LocalDate.now().plusDays(7);
        List<SlotDisponivelResponse> slots = List.of(
                new SlotDisponivelResponse(date.atTime(9, 0), 30, TipoAgendamento.PASSAPORTE),
                new SlotDisponivelResponse(date.atTime(9, 30), 30, TipoAgendamento.PASSAPORTE));

        when(slotConfig.getAvailableSlots(any(LocalDate.class), eq(TipoAgendamento.PASSAPORTE)))
                .thenReturn(slots);

        mockMvc.perform(get("/api/v1/agendamentos/slots")
                        .param("tipo", "PASSAPORTE")
                        .param("data", date.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].duracaoMinutos").value(30))
                .andExpect(jsonPath("$.data[1].duracaoMinutos").value(30));
    }
}

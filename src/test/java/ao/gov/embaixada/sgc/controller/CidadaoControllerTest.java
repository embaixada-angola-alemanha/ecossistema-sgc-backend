package ao.gov.embaixada.sgc.controller;

import ao.gov.embaixada.sgc.dto.CidadaoCreateRequest;
import ao.gov.embaixada.sgc.dto.CidadaoResponse;
import ao.gov.embaixada.sgc.enums.EstadoCidadao;
import ao.gov.embaixada.sgc.enums.Sexo;
import ao.gov.embaixada.sgc.service.CidadaoService;
import ao.gov.embaixada.sgc.service.CitizenContextService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Bean;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(CidadaoController.class)
@AutoConfigureMockMvc(addFilters = false)
class CidadaoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private CidadaoService cidadaoService;

    @Autowired
    private CitizenContextService citizenContext;

    @TestConfiguration
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

    @Test
    void shouldCreateCidadao() throws Exception {
        UUID id = UUID.randomUUID();
        CidadaoResponse response = new CidadaoResponse(
                id, "N1234567", "Joao Silva", LocalDate.of(1990, 1, 15),
                Sexo.MASCULINO, "Angolana", null,
                "joao@email.com", null, null, null,
                EstadoCidadao.ACTIVO, null, 0, 0, Instant.now(), Instant.now());

        when(cidadaoService.create(any())).thenReturn(response);

        CidadaoCreateRequest request = new CidadaoCreateRequest(
                "N1234567", "Joao Silva", LocalDate.of(1990, 1, 15),
                Sexo.MASCULINO, "Angolana", null,
                "joao@email.com", null, null, null);

        mockMvc.perform(post("/api/v1/cidadaos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.numeroPassaporte").value("N1234567"))
                .andExpect(jsonPath("$.data.nomeCompleto").value("Joao Silva"));
    }

    @Test
    void shouldGetCidadaoById() throws Exception {
        UUID id = UUID.randomUUID();
        CidadaoResponse response = new CidadaoResponse(
                id, "N9999999", "Maria Santos", null,
                Sexo.FEMININO, "Angolana", null,
                null, null, null, null,
                EstadoCidadao.ACTIVO, null, 0, 0, Instant.now(), Instant.now());

        when(cidadaoService.findById(id)).thenReturn(response);

        mockMvc.perform(get("/api/v1/cidadaos/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.nomeCompleto").value("Maria Santos"));
    }

    @Test
    void shouldListCidadaos() throws Exception {
        UUID id = UUID.randomUUID();
        CidadaoResponse response = new CidadaoResponse(
                id, "N1111111", "Pedro Costa", null,
                null, "Angolana", null,
                null, null, null, null,
                EstadoCidadao.ACTIVO, null, 0, 0, Instant.now(), Instant.now());

        Page<CidadaoResponse> page = new PageImpl<>(List.of(response));
        when(cidadaoService.findAll(any(), any(), any(), any(), any(Pageable.class))).thenReturn(page);

        mockMvc.perform(get("/api/v1/cidadaos"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content[0].nomeCompleto").value("Pedro Costa"));
    }

    @Test
    void shouldListCidadaosWithFilters() throws Exception {
        UUID id = UUID.randomUUID();
        CidadaoResponse response = new CidadaoResponse(
                id, "N2222222", "Ana Ferreira", null,
                Sexo.FEMININO, "Angolana", null,
                null, null, null, null,
                EstadoCidadao.ACTIVO, null, 0, 0, Instant.now(), Instant.now());

        Page<CidadaoResponse> page = new PageImpl<>(List.of(response));
        when(cidadaoService.findAll(any(), any(), any(), any(), any(Pageable.class))).thenReturn(page);

        mockMvc.perform(get("/api/v1/cidadaos")
                        .param("search", "Ana")
                        .param("estado", "ACTIVO")
                        .param("sexo", "FEMININO"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content[0].nomeCompleto").value("Ana Ferreira"));
    }

    @Test
    void shouldRejectInvalidEmail() throws Exception {
        CidadaoCreateRequest request = new CidadaoCreateRequest(
                "N5555555", "Test User", LocalDate.of(1990, 1, 15),
                null, null, null,
                "not-an-email", null, null, null);

        mockMvc.perform(post("/api/v1/cidadaos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldRejectBlankName() throws Exception {
        CidadaoCreateRequest request = new CidadaoCreateRequest(
                "N6666666", "", null,
                null, null, null,
                null, null, null, null);

        mockMvc.perform(post("/api/v1/cidadaos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }
}

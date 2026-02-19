package ao.gov.embaixada.sgc.controller;

import ao.gov.embaixada.sgc.AbstractIntegrationTest;
import ao.gov.embaixada.sgc.dto.CidadaoCreateRequest;
import ao.gov.embaixada.sgc.service.CidadaoService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.http.*;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Import(AgendamentoApiIntegrationTest.PermitAllSecurityConfig.class)
class AgendamentoApiIntegrationTest extends AbstractIntegrationTest {

    @TestConfiguration
    static class PermitAllSecurityConfig {
        @Bean
        public SecurityFilterChain permitAllFilterChain(HttpSecurity http) throws Exception {
            return http
                    .csrf(AbstractHttpConfigurer::disable)
                    .authorizeHttpRequests(auth -> auth.anyRequest().permitAll())
                    .build();
        }
    }

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private CidadaoService cidadaoService;

    private UUID cidadaoId;

    @BeforeEach
    void setUp() {
        var cidadao = cidadaoService.create(new CidadaoCreateRequest(
                "AGD-API-" + System.nanoTime(), "Agendamento Test Cidadao", null,
                null, "Angolana", null,
                "agd-test@embaixada.de", null, null, null));
        cidadaoId = cidadao.id();
    }

    private Map<String, Object> agendamentoRequest(String time) {
        LocalDate nextMonday = LocalDate.now().with(TemporalAdjusters.next(DayOfWeek.MONDAY));
        return Map.of(
                "cidadaoId", cidadaoId.toString(),
                "tipo", "PASSAPORTE",
                "dataHora", nextMonday + "T" + time,
                "notas", "Teste de integração API"
        );
    }

    @Test
    void shouldCreateAndRetrieveAgendamento() throws Exception {
        ResponseEntity<String> createResp = restTemplate.postForEntity(
                "/api/v1/agendamentos", agendamentoRequest("09:00:00"), String.class);
        assertEquals(HttpStatus.CREATED, createResp.getStatusCode());

        JsonNode body = objectMapper.readTree(createResp.getBody());
        assertTrue(body.get("success").asBoolean());
        String id = body.at("/data/id").asText();
        assertTrue(body.at("/data/numeroAgendamento").asText().startsWith("SGC-AGD-"));
        assertEquals("PENDENTE", body.at("/data/estado").asText());
        assertEquals("PASSAPORTE", body.at("/data/tipo").asText());
        assertEquals(30, body.at("/data/duracaoMinutos").asInt());

        ResponseEntity<String> getResp = restTemplate.getForEntity(
                "/api/v1/agendamentos/" + id, String.class);
        assertEquals(HttpStatus.OK, getResp.getStatusCode());
    }

    @Test
    void shouldDetectConflictingAppointment() throws Exception {
        restTemplate.postForEntity("/api/v1/agendamentos",
                agendamentoRequest("10:00:00"), String.class);

        ResponseEntity<String> conflictResp = restTemplate.postForEntity(
                "/api/v1/agendamentos", agendamentoRequest("10:00:00"), String.class);
        assertEquals(HttpStatus.CONFLICT, conflictResp.getStatusCode());
    }

    @Test
    void shouldTransitionAgendamentoState() throws Exception {
        ResponseEntity<String> createResp = restTemplate.postForEntity(
                "/api/v1/agendamentos", agendamentoRequest("11:00:00"), String.class);
        String id = objectMapper.readTree(createResp.getBody()).at("/data/id").asText();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        // PENDENTE → CONFIRMADO
        HttpEntity<String> confirmEntity = new HttpEntity<>(
                "{\"estado\":\"CONFIRMADO\",\"comentario\":\"Confirmado\"}", headers);
        ResponseEntity<String> patchResp = restTemplate.exchange(
                "/api/v1/agendamentos/" + id + "/estado", HttpMethod.PATCH, confirmEntity, String.class);
        assertEquals(HttpStatus.OK, patchResp.getStatusCode());
        assertEquals("CONFIRMADO", objectMapper.readTree(patchResp.getBody()).at("/data/estado").asText());

        // CONFIRMADO → COMPLETADO
        HttpEntity<String> completeEntity = new HttpEntity<>(
                "{\"estado\":\"COMPLETADO\",\"comentario\":\"Atendido\"}", headers);
        patchResp = restTemplate.exchange(
                "/api/v1/agendamentos/" + id + "/estado", HttpMethod.PATCH, completeEntity, String.class);
        assertEquals("COMPLETADO", objectMapper.readTree(patchResp.getBody()).at("/data/estado").asText());
    }

    @Test
    void shouldCancelAgendamento() throws Exception {
        ResponseEntity<String> createResp = restTemplate.postForEntity(
                "/api/v1/agendamentos", agendamentoRequest("12:00:00"), String.class);
        String id = objectMapper.readTree(createResp.getBody()).at("/data/id").asText();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        // PENDENTE → CONFIRMADO → CANCELADO
        restTemplate.exchange("/api/v1/agendamentos/" + id + "/estado", HttpMethod.PATCH,
                new HttpEntity<>("{\"estado\":\"CONFIRMADO\",\"comentario\":\"Confirmado\"}", headers), String.class);

        HttpEntity<String> cancelEntity = new HttpEntity<>(
                "{\"estado\":\"CANCELADO\",\"comentario\":\"Cidadao cancelou\"}", headers);
        ResponseEntity<String> patchResp = restTemplate.exchange(
                "/api/v1/agendamentos/" + id + "/estado", HttpMethod.PATCH, cancelEntity, String.class);
        assertEquals(HttpStatus.OK, patchResp.getStatusCode());
        assertEquals("CANCELADO", objectMapper.readTree(patchResp.getBody()).at("/data/estado").asText());
        assertEquals("Cidadao cancelou", objectMapper.readTree(patchResp.getBody()).at("/data/motivoCancelamento").asText());
    }

    @Test
    void shouldListAgendamentos() throws Exception {
        restTemplate.postForEntity("/api/v1/agendamentos",
                agendamentoRequest("13:00:00"), String.class);

        ResponseEntity<String> listResp = restTemplate.getForEntity(
                "/api/v1/agendamentos", String.class);
        assertEquals(HttpStatus.OK, listResp.getStatusCode());

        JsonNode body = objectMapper.readTree(listResp.getBody());
        assertTrue(body.at("/data/content").size() >= 1);
    }

    @Test
    void shouldGetAgendamentoHistory() throws Exception {
        ResponseEntity<String> createResp = restTemplate.postForEntity(
                "/api/v1/agendamentos", agendamentoRequest("14:00:00"), String.class);
        String id = objectMapper.readTree(createResp.getBody()).at("/data/id").asText();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        restTemplate.exchange("/api/v1/agendamentos/" + id + "/estado", HttpMethod.PATCH,
                new HttpEntity<>("{\"estado\":\"CONFIRMADO\",\"comentario\":\"Confirmado\"}", headers), String.class);

        ResponseEntity<String> histResp = restTemplate.getForEntity(
                "/api/v1/agendamentos/" + id + "/historico", String.class);
        assertEquals(HttpStatus.OK, histResp.getStatusCode());

        JsonNode body = objectMapper.readTree(histResp.getBody());
        assertTrue(body.at("/data/content").size() >= 2);
    }

    @Test
    void shouldGetAvailableSlots() throws Exception {
        LocalDate nextMonday = LocalDate.now().with(TemporalAdjusters.next(DayOfWeek.MONDAY));
        ResponseEntity<String> response = restTemplate.getForEntity(
                "/api/v1/agendamentos/slots?tipo=PASSAPORTE&data=" + nextMonday, String.class);
        assertEquals(HttpStatus.OK, response.getStatusCode());

        JsonNode body = objectMapper.readTree(response.getBody());
        assertTrue(body.at("/data").isArray());
    }

    @Test
    void shouldDeleteAgendamento() throws Exception {
        ResponseEntity<String> createResp = restTemplate.postForEntity(
                "/api/v1/agendamentos", agendamentoRequest("15:00:00"), String.class);
        assertEquals(HttpStatus.CREATED, createResp.getStatusCode());
        String id = objectMapper.readTree(createResp.getBody()).at("/data/id").asText();

        ResponseEntity<Void> deleteResp = restTemplate.exchange(
                "/api/v1/agendamentos/" + id, HttpMethod.DELETE, null, Void.class);
        assertEquals(HttpStatus.NO_CONTENT, deleteResp.getStatusCode());

        ResponseEntity<String> getResp = restTemplate.getForEntity(
                "/api/v1/agendamentos/" + id, String.class);
        assertEquals(HttpStatus.NOT_FOUND, getResp.getStatusCode());
    }

    @Test
    void shouldRejectAgendamentoForInvalidCidadao() {
        LocalDate nextMonday = LocalDate.now().with(TemporalAdjusters.next(DayOfWeek.MONDAY));
        Map<String, Object> request = Map.of(
                "cidadaoId", UUID.randomUUID().toString(),
                "tipo", "PASSAPORTE",
                "dataHora", nextMonday + "T09:00:00",
                "notas", "Test");

        ResponseEntity<String> response = restTemplate.postForEntity(
                "/api/v1/agendamentos", request, String.class);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }
}

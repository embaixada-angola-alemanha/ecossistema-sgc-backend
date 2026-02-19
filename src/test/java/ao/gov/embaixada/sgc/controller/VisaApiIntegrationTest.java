package ao.gov.embaixada.sgc.controller;

import ao.gov.embaixada.sgc.AbstractIntegrationTest;
import ao.gov.embaixada.sgc.dto.CidadaoCreateRequest;
import ao.gov.embaixada.sgc.enums.TipoVisto;
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

import java.time.LocalDate;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Import(VisaApiIntegrationTest.PermitAllSecurityConfig.class)
class VisaApiIntegrationTest extends AbstractIntegrationTest {

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
                "VISA-API-" + System.nanoTime(), "Visa Test Cidadao", null,
                null, "Angolana", null, null, null, null, null));
        cidadaoId = cidadao.id();
    }

    private Map<String, Object> visaRequest(TipoVisto tipo) {
        return Map.of(
                "cidadaoId", cidadaoId.toString(),
                "tipo", tipo.name(),
                "nacionalidade", "Alemã",
                "motivo", "Teste de integração",
                "dataEntrada", LocalDate.now().plusMonths(1).toString()
        );
    }

    @Test
    void shouldCreateVisaAndRetrieve() throws Exception {
        ResponseEntity<String> response = restTemplate.postForEntity(
                "/api/v1/visas", visaRequest(TipoVisto.TURISTA), String.class);
        assertEquals(HttpStatus.CREATED, response.getStatusCode());

        JsonNode body = objectMapper.readTree(response.getBody());
        assertTrue(body.get("success").asBoolean());
        String id = body.at("/data/id").asText();
        assertTrue(body.at("/data/numeroVisto").asText().startsWith("SGC-VIS-"));
        assertEquals("RASCUNHO", body.at("/data/estado").asText());
        assertEquals(60.0, body.at("/data/valorTaxa").asDouble(), 0.01);

        ResponseEntity<String> getResp = restTemplate.getForEntity("/api/v1/visas/" + id, String.class);
        assertEquals(HttpStatus.OK, getResp.getStatusCode());
        assertEquals("TURISTA", objectMapper.readTree(getResp.getBody()).at("/data/tipo").asText());
    }

    @Test
    void shouldCalculateCorrectFees() throws Exception {
        // Turista = 60€
        ResponseEntity<String> turistaResp = restTemplate.postForEntity(
                "/api/v1/visas", visaRequest(TipoVisto.TURISTA), String.class);
        assertEquals(60.0, objectMapper.readTree(turistaResp.getBody()).at("/data/valorTaxa").asDouble(), 0.01);

        // Trabalho = 150€
        var cidadao2 = cidadaoService.create(new CidadaoCreateRequest(
                "VISA-FEE-" + System.nanoTime(), "Fee Test", null,
                null, "Angolana", null, null, null, null, null));
        Map<String, Object> trabalhoReq = Map.of(
                "cidadaoId", cidadao2.id().toString(),
                "tipo", "TRABALHO", "nacionalidade", "Alemã",
                "motivo", "Trabalho", "dataEntrada", LocalDate.now().plusMonths(1).toString());
        ResponseEntity<String> trabalhoResp = restTemplate.postForEntity(
                "/api/v1/visas", trabalhoReq, String.class);
        assertEquals(150.0, objectMapper.readTree(trabalhoResp.getBody()).at("/data/valorTaxa").asDouble(), 0.01);

        // Diplomatico = 0€
        var cidadao3 = cidadaoService.create(new CidadaoCreateRequest(
                "VISA-DIP-" + System.nanoTime(), "Diplomatic Test", null,
                null, "Angolana", null, null, null, null, null));
        Map<String, Object> diplomaticoReq = Map.of(
                "cidadaoId", cidadao3.id().toString(),
                "tipo", "DIPLOMATICO", "nacionalidade", "Alemã",
                "motivo", "Missão", "dataEntrada", LocalDate.now().plusMonths(1).toString());
        ResponseEntity<String> diplomaticoResp = restTemplate.postForEntity(
                "/api/v1/visas", diplomaticoReq, String.class);
        assertEquals(0.0, objectMapper.readTree(diplomaticoResp.getBody()).at("/data/valorTaxa").asDouble(), 0.01);
    }

    @Test
    void shouldTransitionVisaState() throws Exception {
        ResponseEntity<String> createResp = restTemplate.postForEntity(
                "/api/v1/visas", visaRequest(TipoVisto.TURISTA), String.class);
        String id = objectMapper.readTree(createResp.getBody()).at("/data/id").asText();

        // RASCUNHO → SUBMETIDO
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> submitEntity = new HttpEntity<>(
                "{\"estado\":\"SUBMETIDO\",\"comentario\":\"Submetido via API\"}", headers);
        ResponseEntity<String> patchResp = restTemplate.exchange(
                "/api/v1/visas/" + id + "/estado", HttpMethod.PATCH, submitEntity, String.class);
        assertEquals(HttpStatus.OK, patchResp.getStatusCode());
        assertEquals("SUBMETIDO", objectMapper.readTree(patchResp.getBody()).at("/data/estado").asText());

        // SUBMETIDO → EM_ANALISE
        HttpEntity<String> analyseEntity = new HttpEntity<>(
                "{\"estado\":\"EM_ANALISE\",\"comentario\":\"Em análise\"}", headers);
        patchResp = restTemplate.exchange(
                "/api/v1/visas/" + id + "/estado", HttpMethod.PATCH, analyseEntity, String.class);
        assertEquals("EM_ANALISE", objectMapper.readTree(patchResp.getBody()).at("/data/estado").asText());
    }

    @Test
    void shouldRejectInvalidStateTransition() throws Exception {
        ResponseEntity<String> createResp = restTemplate.postForEntity(
                "/api/v1/visas", visaRequest(TipoVisto.TURISTA), String.class);
        String id = objectMapper.readTree(createResp.getBody()).at("/data/id").asText();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> entity = new HttpEntity<>(
                "{\"estado\":\"APROVADO\",\"comentario\":\"Invalid jump\"}", headers);
        ResponseEntity<String> patchResp = restTemplate.exchange(
                "/api/v1/visas/" + id + "/estado", HttpMethod.PATCH, entity, String.class);
        assertEquals(HttpStatus.CONFLICT, patchResp.getStatusCode());
    }

    @Test
    void shouldListVisas() throws Exception {
        restTemplate.postForEntity("/api/v1/visas", visaRequest(TipoVisto.TURISTA), String.class);

        ResponseEntity<String> response = restTemplate.getForEntity("/api/v1/visas", String.class);
        assertEquals(HttpStatus.OK, response.getStatusCode());

        JsonNode body = objectMapper.readTree(response.getBody());
        assertTrue(body.at("/data/content").size() >= 1);
    }

    @Test
    void shouldDeleteVisa() throws Exception {
        ResponseEntity<String> createResp = restTemplate.postForEntity(
                "/api/v1/visas", visaRequest(TipoVisto.TURISTA), String.class);
        String id = objectMapper.readTree(createResp.getBody()).at("/data/id").asText();

        restTemplate.delete("/api/v1/visas/" + id);

        ResponseEntity<String> getResp = restTemplate.getForEntity("/api/v1/visas/" + id, String.class);
        assertEquals(HttpStatus.NOT_FOUND, getResp.getStatusCode());
    }

    @Test
    void shouldGetVisaHistory() throws Exception {
        ResponseEntity<String> createResp = restTemplate.postForEntity(
                "/api/v1/visas", visaRequest(TipoVisto.TURISTA), String.class);
        String id = objectMapper.readTree(createResp.getBody()).at("/data/id").asText();

        // Create some state transitions
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        restTemplate.exchange("/api/v1/visas/" + id + "/estado", HttpMethod.PATCH,
                new HttpEntity<>("{\"estado\":\"SUBMETIDO\",\"comentario\":\"Submit\"}", headers), String.class);

        ResponseEntity<String> histResp = restTemplate.getForEntity(
                "/api/v1/visas/" + id + "/historico", String.class);
        assertEquals(HttpStatus.OK, histResp.getStatusCode());

        JsonNode body = objectMapper.readTree(histResp.getBody());
        assertTrue(body.at("/data/content").size() >= 2);
    }

    @Test
    void shouldGetFees() throws Exception {
        ResponseEntity<String> response = restTemplate.getForEntity(
                "/api/v1/visas/fees?tipo=TURISTA", String.class);
        assertEquals(HttpStatus.OK, response.getStatusCode());

        JsonNode body = objectMapper.readTree(response.getBody());
        assertEquals(60.0, body.at("/data/valor").asDouble(), 0.01);
        assertEquals("EUR", body.at("/data/moeda").asText());
    }

    @Test
    void shouldGetChecklist() throws Exception {
        ResponseEntity<String> response = restTemplate.getForEntity(
                "/api/v1/visas/checklist?tipo=TURISTA", String.class);
        assertEquals(HttpStatus.OK, response.getStatusCode());

        JsonNode body = objectMapper.readTree(response.getBody());
        assertTrue(body.at("/data/documentosRequeridos").isArray());
        assertTrue(body.at("/data/documentosRequeridos").size() >= 1);
    }

    @Test
    void shouldRejectVisaForInvalidCidadao() {
        Map<String, Object> request = Map.of(
                "cidadaoId", UUID.randomUUID().toString(),
                "tipo", "TURISTA", "nacionalidade", "Alemã",
                "motivo", "Test", "dataEntrada", LocalDate.now().plusMonths(1).toString());

        ResponseEntity<String> response = restTemplate.postForEntity(
                "/api/v1/visas", request, String.class);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }
}

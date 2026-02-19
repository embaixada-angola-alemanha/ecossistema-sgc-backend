package ao.gov.embaixada.sgc.controller;

import ao.gov.embaixada.sgc.AbstractIntegrationTest;
import ao.gov.embaixada.sgc.dto.CidadaoCreateRequest;
import ao.gov.embaixada.sgc.enums.EstadoCivil;
import ao.gov.embaixada.sgc.enums.Sexo;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
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

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Import(CidadaoApiIntegrationTest.PermitAllSecurityConfig.class)
class CidadaoApiIntegrationTest extends AbstractIntegrationTest {

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

    private CidadaoCreateRequest createRequest(String passport) {
        return new CidadaoCreateRequest(
                passport, "Test Cidadao " + passport, LocalDate.of(1990, 5, 15),
                Sexo.MASCULINO, "Angolana", EstadoCivil.SOLTEIRO,
                passport.toLowerCase() + "@test.com", "+49123456789", "Luanda", "Berlin");
    }

    @Test
    void shouldCreateAndRetrieveCidadao() throws Exception {
        CidadaoCreateRequest request = createRequest("API-CID-001");

        ResponseEntity<String> createResponse = restTemplate.postForEntity(
                "/api/v1/cidadaos", request, String.class);
        assertEquals(HttpStatus.CREATED, createResponse.getStatusCode());

        JsonNode body = objectMapper.readTree(createResponse.getBody());
        assertTrue(body.get("success").asBoolean());
        String id = body.at("/data/id").asText();
        assertNotNull(id);
        assertEquals("API-CID-001", body.at("/data/numeroPassaporte").asText());

        ResponseEntity<String> getResponse = restTemplate.getForEntity(
                "/api/v1/cidadaos/" + id, String.class);
        assertEquals(HttpStatus.OK, getResponse.getStatusCode());

        JsonNode getBody = objectMapper.readTree(getResponse.getBody());
        assertEquals("Test Cidadao API-CID-001", getBody.at("/data/nomeCompleto").asText());
        assertEquals("ACTIVO", getBody.at("/data/estado").asText());
    }

    @Test
    void shouldListCidadaosWithPagination() throws Exception {
        for (int i = 1; i <= 3; i++) {
            restTemplate.postForEntity("/api/v1/cidadaos",
                    createRequest("API-LIST-" + System.nanoTime()), String.class);
        }

        ResponseEntity<String> response = restTemplate.getForEntity(
                "/api/v1/cidadaos?size=2&page=0", String.class);
        assertEquals(HttpStatus.OK, response.getStatusCode());

        JsonNode body = objectMapper.readTree(response.getBody());
        assertTrue(body.at("/data/content").isArray());
        assertTrue(body.at("/data/totalElements").asInt() >= 3);
        assertEquals(2, body.at("/data/content").size());
    }

    @Test
    void shouldSearchCidadaosByName() throws Exception {
        restTemplate.postForEntity("/api/v1/cidadaos",
                createRequest("API-SEARCH-" + System.nanoTime()), String.class);

        ResponseEntity<String> response = restTemplate.getForEntity(
                "/api/v1/cidadaos?search=Test+Cidadao", String.class);
        assertEquals(HttpStatus.OK, response.getStatusCode());

        JsonNode body = objectMapper.readTree(response.getBody());
        assertTrue(body.at("/data/content").size() >= 1);
    }

    @Test
    void shouldRejectDuplicatePassport() throws Exception {
        String passport = "API-DUP-" + System.nanoTime();
        restTemplate.postForEntity("/api/v1/cidadaos", createRequest(passport), String.class);

        ResponseEntity<String> response = restTemplate.postForEntity(
                "/api/v1/cidadaos", createRequest(passport), String.class);
        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
    }

    @Test
    void shouldRejectInvalidRequest() {
        CidadaoCreateRequest invalid = new CidadaoCreateRequest(
                "V001", "", null, null, null, null,
                "not-an-email", null, null, null);

        ResponseEntity<String> response = restTemplate.postForEntity(
                "/api/v1/cidadaos", invalid, String.class);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    void shouldUpdateCidadaoEstado() throws Exception {
        CidadaoCreateRequest request = createRequest("API-EST-" + System.nanoTime());
        ResponseEntity<String> createResponse = restTemplate.postForEntity(
                "/api/v1/cidadaos", request, String.class);
        String id = objectMapper.readTree(createResponse.getBody()).at("/data/id").asText();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> entity = new HttpEntity<>(
                "{\"estado\":\"INACTIVO\"}", headers);

        ResponseEntity<String> patchResponse = restTemplate.exchange(
                "/api/v1/cidadaos/" + id + "/estado", HttpMethod.PATCH, entity, String.class);
        assertEquals(HttpStatus.OK, patchResponse.getStatusCode());

        JsonNode body = objectMapper.readTree(patchResponse.getBody());
        assertEquals("INACTIVO", body.at("/data/estado").asText());
    }

    @Test
    void shouldDeleteCidadao() throws Exception {
        CidadaoCreateRequest request = createRequest("API-DEL-" + System.nanoTime());
        ResponseEntity<String> createResponse = restTemplate.postForEntity(
                "/api/v1/cidadaos", request, String.class);
        String id = objectMapper.readTree(createResponse.getBody()).at("/data/id").asText();

        restTemplate.delete("/api/v1/cidadaos/" + id);

        ResponseEntity<String> getResponse = restTemplate.getForEntity(
                "/api/v1/cidadaos/" + id, String.class);
        assertEquals(HttpStatus.NOT_FOUND, getResponse.getStatusCode());
    }

    @Test
    void shouldReturnNotFoundForInvalidId() {
        ResponseEntity<String> response = restTemplate.getForEntity(
                "/api/v1/cidadaos/00000000-0000-0000-0000-000000000000", String.class);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    void shouldReturnApiResponseEnvelope() throws Exception {
        CidadaoCreateRequest request = createRequest("API-ENV-" + System.nanoTime());
        ResponseEntity<String> response = restTemplate.postForEntity(
                "/api/v1/cidadaos", request, String.class);

        JsonNode body = objectMapper.readTree(response.getBody());
        assertTrue(body.has("success"));
        assertTrue(body.has("data"));
        assertTrue(body.get("success").asBoolean());
    }
}

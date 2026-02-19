package ao.gov.embaixada.sgc.controller;

import ao.gov.embaixada.sgc.AbstractIntegrationTest;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.http.ResponseEntity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;

import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Comprehensive API contract test validating OpenAPI specification
 * covers all endpoints, tags, and security schemes for the full SGC system.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Import(FullApiContractTest.PermitAllSecurityConfig.class)
class FullApiContractTest extends AbstractIntegrationTest {

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

    @Test
    void shouldExposeAllEndpointsInOpenApiSpec() throws Exception {
        ResponseEntity<String> response = restTemplate.getForEntity("/v3/api-docs", String.class);
        assertEquals(200, response.getStatusCode().value());

        JsonNode root = objectMapper.readTree(response.getBody());
        JsonNode paths = root.get("paths");
        assertNotNull(paths);

        // Cidadao endpoints
        assertTrue(paths.has("/api/v1/cidadaos"), "Missing: /api/v1/cidadaos");
        assertTrue(paths.has("/api/v1/cidadaos/{id}"), "Missing: /api/v1/cidadaos/{id}");
        assertTrue(paths.get("/api/v1/cidadaos").has("post"), "Missing POST /api/v1/cidadaos");
        assertTrue(paths.get("/api/v1/cidadaos").has("get"), "Missing GET /api/v1/cidadaos");

        // Visa endpoints
        assertTrue(paths.has("/api/v1/visas"), "Missing: /api/v1/visas");
        assertTrue(paths.has("/api/v1/visas/{id}"), "Missing: /api/v1/visas/{id}");
        assertTrue(paths.has("/api/v1/visas/fees"), "Missing: /api/v1/visas/fees");
        assertTrue(paths.has("/api/v1/visas/checklist"), "Missing: /api/v1/visas/checklist");

        // Agendamento endpoints
        assertTrue(paths.has("/api/v1/agendamentos"), "Missing: /api/v1/agendamentos");
        assertTrue(paths.has("/api/v1/agendamentos/{id}"), "Missing: /api/v1/agendamentos/{id}");
        assertTrue(paths.has("/api/v1/agendamentos/slots"), "Missing: /api/v1/agendamentos/slots");

        // Processo endpoints
        assertTrue(paths.has("/api/v1/processos"), "Missing: /api/v1/processos");
        assertTrue(paths.has("/api/v1/processos/{id}"), "Missing: /api/v1/processos/{id}");

        // Registo Civil endpoints
        assertTrue(paths.has("/api/v1/registos-civis"), "Missing: /api/v1/registos-civis");
        assertTrue(paths.has("/api/v1/registos-civis/{id}"), "Missing: /api/v1/registos-civis/{id}");

        // Servico Notarial endpoints
        assertTrue(paths.has("/api/v1/servicos-notariais"), "Missing: /api/v1/servicos-notariais");
        assertTrue(paths.has("/api/v1/servicos-notariais/{id}"), "Missing: /api/v1/servicos-notariais/{id}");

        // Relatorio endpoints
        assertTrue(paths.has("/api/v1/relatorios/dashboard"), "Missing: /api/v1/relatorios/dashboard");
    }

    @Test
    void shouldHaveAllTags() throws Exception {
        ResponseEntity<String> response = restTemplate.getForEntity("/v3/api-docs", String.class);
        JsonNode root = objectMapper.readTree(response.getBody());

        JsonNode tags = root.get("tags");
        assertNotNull(tags);

        Set<String> tagNames = new HashSet<>();
        tags.forEach(tag -> tagNames.add(tag.get("name").asText()));

        assertTrue(tagNames.contains("Cidadaos"), "Missing tag: Cidadaos");
        assertTrue(tagNames.contains("Vistos"), "Missing tag: Vistos");
        assertTrue(tagNames.contains("Agendamentos"), "Missing tag: Agendamentos");
        assertTrue(tagNames.contains("Processos"), "Missing tag: Processos");
        assertTrue(tagNames.contains("Documentos"), "Missing tag: Documentos");
    }

    @Test
    void shouldHaveBearerJwtSecurityScheme() throws Exception {
        ResponseEntity<String> response = restTemplate.getForEntity("/v3/api-docs", String.class);
        JsonNode root = objectMapper.readTree(response.getBody());

        JsonNode securitySchemes = root.at("/components/securitySchemes");
        assertNotNull(securitySchemes);
        assertTrue(securitySchemes.has("bearer-jwt"));
        assertEquals("bearer", securitySchemes.at("/bearer-jwt/scheme").asText());
        assertEquals("JWT", securitySchemes.at("/bearer-jwt/bearerFormat").asText());
    }

    @Test
    void shouldHaveCorrectApiInfo() throws Exception {
        ResponseEntity<String> response = restTemplate.getForEntity("/v3/api-docs", String.class);
        JsonNode root = objectMapper.readTree(response.getBody());

        JsonNode info = root.get("info");
        assertNotNull(info);
        assertNotNull(info.get("title"));
        assertNotNull(info.get("version"));
    }

    @Test
    void shouldExposeDtoSchemasInComponents() throws Exception {
        ResponseEntity<String> response = restTemplate.getForEntity("/v3/api-docs", String.class);
        JsonNode root = objectMapper.readTree(response.getBody());

        JsonNode schemas = root.at("/components/schemas");
        assertNotNull(schemas);

        // Verify key DTOs are exposed
        assertTrue(schemas.has("CidadaoCreateRequest") || schemas.size() > 10,
                "Should have multiple DTO schemas");
    }

    @Test
    void shouldReturnValidJsonOnAllListEndpoints() throws Exception {
        String[] listEndpoints = {
                "/api/v1/cidadaos",
                "/api/v1/visas",
                "/api/v1/agendamentos",
                "/api/v1/processos",
                "/api/v1/registos-civis",
                "/api/v1/servicos-notariais"
        };

        for (String endpoint : listEndpoints) {
            ResponseEntity<String> response = restTemplate.getForEntity(endpoint, String.class);
            assertEquals(200, response.getStatusCode().value(), "Failed for: " + endpoint);

            JsonNode body = objectMapper.readTree(response.getBody());
            assertTrue(body.has("success"), "Missing 'success' field for: " + endpoint);
            assertTrue(body.has("data"), "Missing 'data' field for: " + endpoint);
        }
    }
}

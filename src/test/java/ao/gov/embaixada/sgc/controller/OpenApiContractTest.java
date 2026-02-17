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

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Import(OpenApiContractTest.PermitAllSecurityConfig.class)
class OpenApiContractTest extends AbstractIntegrationTest {

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
    void shouldExposeOpenApiSpec() throws Exception {
        ResponseEntity<String> response = restTemplate.getForEntity("/v3/api-docs", String.class);
        assertEquals(200, response.getStatusCode().value());

        JsonNode root = objectMapper.readTree(response.getBody());

        // Verify info
        JsonNode info = root.get("info");
        assertNotNull(info);
        assertEquals("SGC — Sistema de Gestao Consular API", info.get("title").asText());
        assertEquals("0.1.0", info.get("version").asText());

        // Verify security schemes
        JsonNode securitySchemes = root.at("/components/securitySchemes");
        assertNotNull(securitySchemes);
        assertTrue(securitySchemes.has("bearer-jwt"));
        assertEquals("bearer", securitySchemes.at("/bearer-jwt/scheme").asText());

        // Verify all 5 tags exist
        JsonNode tags = root.get("tags");
        assertNotNull(tags);
        Set<String> tagNames = new HashSet<>();
        tags.forEach(tag -> tagNames.add(tag.get("name").asText()));
        assertTrue(tagNames.contains("Cidadaos"), "Missing tag: Cidadaos");
        assertTrue(tagNames.contains("Documentos"), "Missing tag: Documentos");
        assertTrue(tagNames.contains("Processos"), "Missing tag: Processos");
        assertTrue(tagNames.contains("Vistos"), "Missing tag: Vistos");
        assertTrue(tagNames.contains("Agendamentos"), "Missing tag: Agendamentos");

        // Verify endpoint paths
        JsonNode paths = root.get("paths");
        assertNotNull(paths);
        assertTrue(paths.has("/api/v1/cidadaos"), "Missing path: /api/v1/cidadaos");
        assertTrue(paths.has("/api/v1/cidadaos/{id}"), "Missing path: /api/v1/cidadaos/{id}");
        assertTrue(paths.has("/api/v1/visas"), "Missing path: /api/v1/visas");
        assertTrue(paths.has("/api/v1/visas/{id}"), "Missing path: /api/v1/visas/{id}");
        assertTrue(paths.has("/api/v1/processos"), "Missing path: /api/v1/processos");
        assertTrue(paths.has("/api/v1/processos/{id}"), "Missing path: /api/v1/processos/{id}");
        assertTrue(paths.has("/api/v1/agendamentos"), "Missing path: /api/v1/agendamentos");
        assertTrue(paths.has("/api/v1/agendamentos/{id}"), "Missing path: /api/v1/agendamentos/{id}");
        assertTrue(paths.has("/api/v1/agendamentos/slots"), "Missing path: /api/v1/agendamentos/slots");
    }
}

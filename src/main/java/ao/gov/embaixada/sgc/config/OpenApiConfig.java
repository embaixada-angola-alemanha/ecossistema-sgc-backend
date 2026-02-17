package ao.gov.embaixada.sgc.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI sgcOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("SGC — Sistema de Gestao Consular API")
                        .description("API REST para gestao de cidadaos, documentos e processos consulares")
                        .version("0.1.0")
                        .contact(new Contact()
                                .name("Ecossistema Digital")
                                .email("dev@ecossistema.local")))
                .addSecurityItem(new SecurityRequirement().addList("bearer-jwt"))
                .schemaRequirement("bearer-jwt", new SecurityScheme()
                        .type(SecurityScheme.Type.HTTP)
                        .scheme("bearer")
                        .bearerFormat("JWT")
                        .description("JWT token from Keycloak"));
    }
}

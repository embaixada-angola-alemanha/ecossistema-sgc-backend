package ao.gov.embaixada.sgc;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MinIOContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.containers.RabbitMQContainer;

@SpringBootTest
@ActiveProfiles("integration")
public abstract class AbstractIntegrationTest {

    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine")
            .withDatabaseName("sgc_test")
            .withUsername("test")
            .withPassword("test");

    static MinIOContainer minio = new MinIOContainer("minio/minio:latest")
            .withUserName("testminio")
            .withPassword("testminio123");

    static RabbitMQContainer rabbitmq = new RabbitMQContainer("rabbitmq:3-management-alpine");

    static {
        postgres.start();
        minio.start();
        rabbitmq.start();
    }

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("spring.datasource.driver-class-name", () -> "org.postgresql.Driver");
        registry.add("ecossistema.storage.enabled", () -> "true");
        registry.add("ecossistema.storage.endpoint", minio::getS3URL);
        registry.add("ecossistema.storage.access-key", minio::getUserName);
        registry.add("ecossistema.storage.secret-key", minio::getPassword);
        registry.add("ecossistema.storage.default-bucket", () -> "sgc-test");
        registry.add("spring.rabbitmq.host", rabbitmq::getHost);
        registry.add("spring.rabbitmq.port", rabbitmq::getAmqpPort);
        registry.add("spring.rabbitmq.username", () -> "guest");
        registry.add("spring.rabbitmq.password", () -> "guest");
    }
}

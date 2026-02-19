package ao.gov.embaixada.sgc.integration;

import ao.gov.embaixada.sgc.AbstractIntegrationTest;
import ao.gov.embaixada.sgc.config.RabbitMQConfig;
import ao.gov.embaixada.sgc.dto.CidadaoCreateRequest;
import ao.gov.embaixada.sgc.dto.NotificationMessage;
import ao.gov.embaixada.sgc.entity.NotificationLog;
import ao.gov.embaixada.sgc.enums.EstadoNotificacao;
import ao.gov.embaixada.sgc.repository.NotificationLogRepository;
import ao.gov.embaixada.sgc.service.CidadaoService;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration test for the notification delivery pipeline:
 * RabbitMQ message → NotificationConsumer → NotificationLog in DB.
 * Uses real RabbitMQ via TestContainers.
 */
class NotificationDeliveryIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Autowired
    private NotificationLogRepository notificationLogRepository;

    @Autowired
    private CidadaoService cidadaoService;

    @Test
    void shouldDeliverNotificationViaRabbitMQ() {
        var cidadao = cidadaoService.create(new CidadaoCreateRequest(
                "NOTIF-" + System.nanoTime(), "Notif Test Cidadao", null,
                null, "Angolana", null, "notif@test.com", null, null, null));

        UUID entityId = UUID.randomUUID();
        NotificationMessage message = new NotificationMessage(
                cidadao.id(), "notif@test.com", "Visa Submetido",
                "email/visa-submetido",
                Map.of("nome", "Notif Test Cidadao", "numeroVisto", "SGC-VIS-TEST"),
                "Visa", entityId);

        rabbitTemplate.convertAndSend(
                RabbitMQConfig.NOTIFICATION_EXCHANGE,
                RabbitMQConfig.NOTIFICATION_ROUTING_KEY,
                message);

        await().atMost(10, TimeUnit.SECONDS).untilAsserted(() -> {
            List<NotificationLog> logs = notificationLogRepository.findAll();
            assertTrue(logs.stream().anyMatch(log ->
                    log.getEntityId().equals(entityId) &&
                    log.getToAddress().equals("notif@test.com") &&
                    log.getWorkflowName().equals("Visa")));
        });
    }

    @Test
    void shouldLogNotificationWithCorrectDetails() {
        var cidadao = cidadaoService.create(new CidadaoCreateRequest(
                "NOTIF-DET-" + System.nanoTime(), "Detail Test", null,
                null, "Angolana", null, "detail@test.com", null, null, null));

        UUID entityId = UUID.randomUUID();
        NotificationMessage message = new NotificationMessage(
                cidadao.id(), "detail@test.com", "Agendamento Confirmado",
                "email/agendamento-confirmado",
                Map.of("nome", "Detail Test", "data", "2026-03-01"),
                "Agendamento", entityId);

        rabbitTemplate.convertAndSend(
                RabbitMQConfig.NOTIFICATION_EXCHANGE,
                RabbitMQConfig.NOTIFICATION_ROUTING_KEY,
                message);

        await().atMost(10, TimeUnit.SECONDS).untilAsserted(() -> {
            List<NotificationLog> logs = notificationLogRepository.findAll();
            NotificationLog log = logs.stream()
                    .filter(l -> l.getEntityId().equals(entityId))
                    .findFirst()
                    .orElse(null);
            assertNotNull(log, "Notification log should be created");
            assertEquals("detail@test.com", log.getToAddress());
            assertEquals("Agendamento Confirmado", log.getSubject());
            assertEquals("email/agendamento-confirmado", log.getTemplate());
            assertEquals("Agendamento", log.getWorkflowName());
        });
    }

    @Test
    void shouldHandleMultipleNotificationsSequentially() {
        var cidadao = cidadaoService.create(new CidadaoCreateRequest(
                "NOTIF-MULTI-" + System.nanoTime(), "Multi Test", null,
                null, "Angolana", null, "multi@test.com", null, null, null));

        UUID entityId1 = UUID.randomUUID();
        UUID entityId2 = UUID.randomUUID();

        rabbitTemplate.convertAndSend(
                RabbitMQConfig.NOTIFICATION_EXCHANGE,
                RabbitMQConfig.NOTIFICATION_ROUTING_KEY,
                new NotificationMessage(cidadao.id(), "multi@test.com", "Subject 1",
                        "email/test-1", Map.of("key", "val1"), "Visa", entityId1));

        rabbitTemplate.convertAndSend(
                RabbitMQConfig.NOTIFICATION_EXCHANGE,
                RabbitMQConfig.NOTIFICATION_ROUTING_KEY,
                new NotificationMessage(cidadao.id(), "multi@test.com", "Subject 2",
                        "email/test-2", Map.of("key", "val2"), "Agendamento", entityId2));

        await().atMost(15, TimeUnit.SECONDS).untilAsserted(() -> {
            List<NotificationLog> logs = notificationLogRepository.findAll();
            assertTrue(logs.stream().anyMatch(l -> l.getEntityId().equals(entityId1)));
            assertTrue(logs.stream().anyMatch(l -> l.getEntityId().equals(entityId2)));
        });
    }
}

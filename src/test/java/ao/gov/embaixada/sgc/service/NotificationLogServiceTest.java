package ao.gov.embaixada.sgc.service;

import ao.gov.embaixada.sgc.AbstractIntegrationTest;
import ao.gov.embaixada.sgc.dto.CidadaoCreateRequest;
import ao.gov.embaixada.sgc.dto.CidadaoResponse;
import ao.gov.embaixada.sgc.dto.NotificationLogResponse;
import ao.gov.embaixada.sgc.dto.NotificationMessage;
import ao.gov.embaixada.sgc.entity.NotificationLog;
import ao.gov.embaixada.sgc.enums.EstadoNotificacao;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@Transactional
class NotificationLogServiceTest extends AbstractIntegrationTest {

    @Autowired
    private NotificationLogService logService;

    @Autowired
    private CidadaoService cidadaoService;

    private UUID cidadaoId;

    @BeforeEach
    void setUp() {
        CidadaoCreateRequest req = new CidadaoCreateRequest(
                "NP-LOG-" + System.nanoTime(), "Test Cidadao Log", null,
                null, "Angolana", null,
                null, null, null, null);
        CidadaoResponse cidadao = cidadaoService.create(req);
        cidadaoId = cidadao.id();
    }

    @Test
    void shouldCreatePendingLog() {
        NotificationMessage message = new NotificationMessage(
                cidadaoId, "test@example.com", "Test Subject",
                "email/visa-submetido", Map.of(), "Visa", UUID.randomUUID());

        NotificationLog log = logService.createPendingLog(message);

        assertNotNull(log.getId());
        assertEquals(EstadoNotificacao.PENDENTE, log.getEstado());
        assertEquals("test@example.com", log.getToAddress());
        assertEquals("Test Subject", log.getSubject());
        assertEquals("email/visa-submetido", log.getTemplate());
        assertEquals("Visa", log.getWorkflowName());
    }

    @Test
    void shouldMarkLogAsSent() {
        NotificationMessage message = new NotificationMessage(
                cidadaoId, "test@example.com", "Test Subject",
                "email/visa-submetido", Map.of(), "Visa", UUID.randomUUID());
        NotificationLog log = logService.createPendingLog(message);

        logService.markSent(log.getId());

        Page<NotificationLogResponse> logs = logService.findByCidadaoId(cidadaoId, PageRequest.of(0, 10));
        assertEquals(1, logs.getTotalElements());
        assertEquals(EstadoNotificacao.ENVIADO, logs.getContent().get(0).estado());
        assertNotNull(logs.getContent().get(0).sentAt());
    }

    @Test
    void shouldMarkLogAsFailed() {
        NotificationMessage message = new NotificationMessage(
                cidadaoId, "test@example.com", "Test Subject",
                "email/visa-submetido", Map.of(), "Visa", UUID.randomUUID());
        NotificationLog log = logService.createPendingLog(message);

        logService.markFailed(log.getId(), "SMTP connection failed");

        Page<NotificationLogResponse> logs = logService.findByCidadaoId(cidadaoId, PageRequest.of(0, 10));
        assertEquals(1, logs.getTotalElements());
        assertEquals(EstadoNotificacao.FALHOU, logs.getContent().get(0).estado());
        assertEquals("SMTP connection failed", logs.getContent().get(0).errorMessage());
    }

    @Test
    void shouldFindAllLogs() {
        NotificationMessage message1 = new NotificationMessage(
                cidadaoId, "a@example.com", "Subject 1",
                "email/visa-submetido", Map.of(), "Visa", UUID.randomUUID());
        NotificationMessage message2 = new NotificationMessage(
                cidadaoId, "b@example.com", "Subject 2",
                "email/processo-submetido", Map.of(), "Processo", UUID.randomUUID());
        logService.createPendingLog(message1);
        logService.createPendingLog(message2);

        Page<NotificationLogResponse> all = logService.findAll(PageRequest.of(0, 10));
        assertTrue(all.getTotalElements() >= 2);
    }
}

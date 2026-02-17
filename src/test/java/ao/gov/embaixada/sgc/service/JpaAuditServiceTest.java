package ao.gov.embaixada.sgc.service;

import ao.gov.embaixada.commons.audit.AuditAction;
import ao.gov.embaixada.commons.audit.AuditEvent;
import ao.gov.embaixada.commons.audit.AuditService;
import ao.gov.embaixada.sgc.AbstractIntegrationTest;
import ao.gov.embaixada.sgc.repository.AuditEventRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.junit.jupiter.api.Assertions.*;

class JpaAuditServiceTest extends AbstractIntegrationTest {

    @Autowired
    private AuditService auditService;

    @Autowired
    private AuditEventRepository auditEventRepository;

    @BeforeEach
    void setUp() {
        auditEventRepository.deleteAll();
    }

    @Test
    void shouldBeJpaAuditServiceInstance() {
        assertInstanceOf(JpaAuditService.class, auditService);
    }

    @Test
    void shouldPersistAuditEvent() {
        AuditEvent event = new AuditEvent("user1", "Test User", AuditAction.CREATE,
                "VisaService", "visa-123", "Created visa application");

        auditService.log(event);

        var all = auditEventRepository.findAll();
        assertEquals(1, all.size());

        var persisted = all.get(0);
        assertEquals("CREATE", persisted.getAction());
        assertEquals("VisaService", persisted.getEntityType());
        assertEquals("visa-123", persisted.getEntityId());
        assertEquals("user1", persisted.getUserId());
        assertEquals("Test User", persisted.getUsername());
        assertEquals("Created visa application", persisted.getDetails());
        assertNotNull(persisted.getTimestamp());
    }

    @Test
    void shouldHandleNullAction() {
        AuditEvent event = new AuditEvent("user1", "Test User", null,
                "Unknown", null, null);

        auditService.log(event);

        var all = auditEventRepository.findAll();
        assertEquals(1, all.size());
        assertEquals("UNKNOWN", all.get(0).getAction());
    }

    @Test
    void shouldPersistMultipleEvents() {
        auditService.log(new AuditEvent("u1", "User 1", AuditAction.CREATE, "Visa", "v1", "d1"));
        auditService.log(new AuditEvent("u2", "User 2", AuditAction.UPDATE, "Processo", "p1", "d2"));
        auditService.log(new AuditEvent("u1", "User 1", AuditAction.DELETE, "Visa", "v1", "d3"));

        assertEquals(3, auditEventRepository.count());
        assertEquals(2, auditEventRepository.countByEntityType("Visa"));
        assertEquals(1, auditEventRepository.countByEntityType("Processo"));
    }
}

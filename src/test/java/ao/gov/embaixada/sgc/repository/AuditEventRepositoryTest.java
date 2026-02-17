package ao.gov.embaixada.sgc.repository;

import ao.gov.embaixada.sgc.AbstractIntegrationTest;
import ao.gov.embaixada.sgc.entity.AuditEventEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

import static org.junit.jupiter.api.Assertions.*;

@Transactional
class AuditEventRepositoryTest extends AbstractIntegrationTest {

    @Autowired
    private AuditEventRepository repository;

    @BeforeEach
    void setUp() {
        repository.deleteAll();
    }

    @Test
    void shouldSaveAndFindAuditEvent() {
        AuditEventEntity event = createEvent("CREATE", "VisaService", "visa-123", "user1");
        repository.save(event);

        Page<AuditEventEntity> result = repository.findByEntityType("VisaService", PageRequest.of(0, 10));
        assertEquals(1, result.getTotalElements());
        assertEquals("CREATE", result.getContent().get(0).getAction());
    }

    @Test
    void shouldFindByTimestampBetween() {
        Instant now = Instant.now();
        AuditEventEntity event1 = createEvent("CREATE", "VisaService", "v1", "user1");
        event1.setTimestamp(now.minus(1, ChronoUnit.HOURS));
        AuditEventEntity event2 = createEvent("UPDATE", "ProcessoService", "p1", "user2");
        event2.setTimestamp(now);
        repository.save(event1);
        repository.save(event2);

        Page<AuditEventEntity> result = repository.findByTimestampBetween(
                now.minus(2, ChronoUnit.HOURS), now.plus(1, ChronoUnit.HOURS), PageRequest.of(0, 10));
        assertEquals(2, result.getTotalElements());
    }

    @Test
    void shouldFindByEntityTypeAndTimestampBetween() {
        Instant now = Instant.now();
        AuditEventEntity event1 = createEvent("CREATE", "VisaService", "v1", "user1");
        event1.setTimestamp(now);
        AuditEventEntity event2 = createEvent("CREATE", "ProcessoService", "p1", "user1");
        event2.setTimestamp(now);
        repository.save(event1);
        repository.save(event2);

        Page<AuditEventEntity> result = repository.findByEntityTypeAndTimestampBetween(
                "VisaService", now.minus(1, ChronoUnit.HOURS), now.plus(1, ChronoUnit.HOURS),
                PageRequest.of(0, 10));
        assertEquals(1, result.getTotalElements());
    }

    @Test
    void shouldCountByAction() {
        repository.save(createEvent("CREATE", "VisaService", "v1", "user1"));
        repository.save(createEvent("CREATE", "ProcessoService", "p1", "user1"));
        repository.save(createEvent("DELETE", "VisaService", "v2", "user2"));

        assertEquals(2, repository.countByAction("CREATE"));
        assertEquals(1, repository.countByAction("DELETE"));
    }

    @Test
    void shouldCountByEntityType() {
        repository.save(createEvent("CREATE", "VisaService", "v1", "user1"));
        repository.save(createEvent("UPDATE", "VisaService", "v1", "user1"));
        repository.save(createEvent("CREATE", "ProcessoService", "p1", "user1"));

        assertEquals(2, repository.countByEntityType("VisaService"));
        assertEquals(1, repository.countByEntityType("ProcessoService"));
    }

    @Test
    void shouldFindByUserId() {
        repository.save(createEvent("CREATE", "VisaService", "v1", "user1"));
        repository.save(createEvent("CREATE", "ProcessoService", "p1", "user2"));

        Page<AuditEventEntity> result = repository.findByUserId("user1", PageRequest.of(0, 10));
        assertEquals(1, result.getTotalElements());
    }

    private AuditEventEntity createEvent(String action, String entityType, String entityId, String userId) {
        AuditEventEntity event = new AuditEventEntity();
        event.setAction(action);
        event.setEntityType(entityType);
        event.setEntityId(entityId);
        event.setUserId(userId);
        event.setUsername("User " + userId);
        event.setTimestamp(Instant.now());
        return event;
    }
}

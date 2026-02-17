package ao.gov.embaixada.sgc.service;

import ao.gov.embaixada.sgc.AbstractIntegrationTest;
import ao.gov.embaixada.sgc.dto.*;
import ao.gov.embaixada.sgc.entity.AuditEventEntity;
import ao.gov.embaixada.sgc.repository.AuditEventRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

@Transactional
class RelatorioServiceTest extends AbstractIntegrationTest {

    @Autowired
    private RelatorioService relatorioService;

    @Autowired
    private AuditEventRepository auditEventRepository;

    @BeforeEach
    void setUp() {
        auditEventRepository.deleteAll();
    }

    @Test
    void shouldReturnDashboardResumo() {
        RelatorioFilter filter = new RelatorioFilter(
                LocalDate.now().minusMonths(12), LocalDate.now(), null, null, null);

        DashboardResumoResponse result = relatorioService.getDashboardResumo(filter);

        assertNotNull(result);
        assertNotNull(result.visas());
        assertNotNull(result.processos());
        assertNotNull(result.registosCivis());
        assertNotNull(result.servicosNotariais());
        assertNotNull(result.agendamentos());
        assertTrue(result.totalGeral() >= 0);
    }

    @Test
    void shouldReturnEstatisticasForModule() {
        RelatorioFilter filter = new RelatorioFilter(
                LocalDate.now().minusMonths(12), LocalDate.now(), "Visa", null, null);

        EstatisticasResponse result = relatorioService.getEstatisticas(filter);

        assertNotNull(result);
        assertEquals("Visa", result.modulo());
        assertNotNull(result.porEstado());
        assertNotNull(result.porTipo());
    }

    @Test
    void shouldReturnEstatisticasForAllModules() {
        RelatorioFilter filter = new RelatorioFilter(
                LocalDate.now().minusMonths(12), LocalDate.now(), null, null, null);

        EstatisticasResponse result = relatorioService.getEstatisticas(filter);

        assertNotNull(result);
        assertEquals("Todos", result.modulo());
    }

    @Test
    void shouldReturnAuditEvents() {
        AuditEventEntity event = new AuditEventEntity();
        event.setAction("CREATE");
        event.setEntityType("VisaService");
        event.setEntityId("visa-1");
        event.setUserId("user1");
        event.setUsername("Test User");
        event.setTimestamp(Instant.now());
        auditEventRepository.save(event);

        RelatorioFilter filter = new RelatorioFilter(
                LocalDate.now().minusDays(1), LocalDate.now().plusDays(1), null, null, null);

        Page<AuditEventResponse> result = relatorioService.getAuditEvents(filter, PageRequest.of(0, 10));

        assertEquals(1, result.getTotalElements());
        assertEquals("CREATE", result.getContent().get(0).action());
        assertEquals("VisaService", result.getContent().get(0).entityType());
    }

    @Test
    void shouldFilterAuditEventsByModule() {
        AuditEventEntity event1 = new AuditEventEntity();
        event1.setAction("CREATE");
        event1.setEntityType("VisaService");
        event1.setEntityId("v1");
        event1.setTimestamp(Instant.now());
        auditEventRepository.save(event1);

        AuditEventEntity event2 = new AuditEventEntity();
        event2.setAction("CREATE");
        event2.setEntityType("ProcessoService");
        event2.setEntityId("p1");
        event2.setTimestamp(Instant.now());
        auditEventRepository.save(event2);

        RelatorioFilter filter = new RelatorioFilter(
                LocalDate.now().minusDays(1), LocalDate.now().plusDays(1), "VisaService", null, null);

        Page<AuditEventResponse> result = relatorioService.getAuditEvents(filter, PageRequest.of(0, 10));

        assertEquals(1, result.getTotalElements());
    }
}

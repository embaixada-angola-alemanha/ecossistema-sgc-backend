package ao.gov.embaixada.sgc.service;

import ao.gov.embaixada.sgc.AbstractIntegrationTest;
import ao.gov.embaixada.sgc.dto.CidadaoCreateRequest;
import ao.gov.embaixada.sgc.dto.CidadaoResponse;
import ao.gov.embaixada.sgc.dto.VisaCreateRequest;
import ao.gov.embaixada.sgc.dto.VisaResponse;
import ao.gov.embaixada.sgc.enums.EstadoVisto;
import ao.gov.embaixada.sgc.enums.TipoVisto;
import ao.gov.embaixada.sgc.statemachine.event.WorkflowTransitionEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.event.EventListener;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@Transactional
class WorkflowEventIntegrationTest extends AbstractIntegrationTest {

    @TestConfiguration
    static class EventCaptureConfig {
        @Bean
        public TestWorkflowEventListener testWorkflowEventListener() {
            return new TestWorkflowEventListener();
        }
    }

    static class TestWorkflowEventListener {
        private final List<WorkflowTransitionEvent> events = new ArrayList<>();

        @EventListener
        public void onTransition(WorkflowTransitionEvent event) {
            events.add(event);
        }

        public List<WorkflowTransitionEvent> getEvents() {
            return events;
        }

        public void clear() {
            events.clear();
        }
    }

    @Autowired
    private VisaService visaService;

    @Autowired
    private CidadaoService cidadaoService;

    @Autowired
    private TestWorkflowEventListener eventListener;

    private UUID cidadaoId;

    @BeforeEach
    void setUp() {
        eventListener.clear();
        CidadaoCreateRequest cidadaoReq = new CidadaoCreateRequest(
                "WF-EVT-" + System.nanoTime(), "Test Cidadao Workflow", null,
                null, "Angolana", null,
                null, null, null, null);
        CidadaoResponse cidadao = cidadaoService.create(cidadaoReq);
        cidadaoId = cidadao.id();
    }

    @Test
    void shouldPublishEventOnStateTransition() {
        VisaCreateRequest request = new VisaCreateRequest(
                cidadaoId, TipoVisto.TRABALHO, "PT", null,
                LocalDate.now().plusMonths(1), null, null, null, null);
        VisaResponse visa = visaService.create(request);

        visaService.updateEstado(visa.id(), EstadoVisto.SUBMETIDO, "Submissao teste");

        assertEquals(1, eventListener.getEvents().size());

        WorkflowTransitionEvent event = eventListener.getEvents().get(0);
        assertEquals(visa.id(), event.getEntityId());
        assertEquals("Visa", event.getWorkflowName());
        assertEquals("RASCUNHO", event.getPreviousState());
        assertEquals("SUBMETIDO", event.getNewState());
        assertEquals("Submissao teste", event.getComment());
        assertNotNull(event.getTransitionTime());
    }

    @Test
    void shouldPublishMultipleEventsForMultipleTransitions() {
        VisaCreateRequest request = new VisaCreateRequest(
                cidadaoId, TipoVisto.TURISTA, "DE", null,
                LocalDate.now().plusMonths(2), null, null, null, null);
        VisaResponse visa = visaService.create(request);

        visaService.updateEstado(visa.id(), EstadoVisto.SUBMETIDO, "Submetido");
        visaService.updateEstado(visa.id(), EstadoVisto.EM_ANALISE, "Em analise");

        assertEquals(2, eventListener.getEvents().size());

        WorkflowTransitionEvent first = eventListener.getEvents().get(0);
        assertEquals("RASCUNHO", first.getPreviousState());
        assertEquals("SUBMETIDO", first.getNewState());

        WorkflowTransitionEvent second = eventListener.getEvents().get(1);
        assertEquals("SUBMETIDO", second.getPreviousState());
        assertEquals("EM_ANALISE", second.getNewState());
    }

    @Test
    void shouldNotPublishEventOnInvalidTransition() {
        VisaCreateRequest request = new VisaCreateRequest(
                cidadaoId, TipoVisto.DIPLOMATICO, "FR", null,
                LocalDate.now().plusMonths(3), null, null, null, null);
        VisaResponse visa = visaService.create(request);

        assertThrows(Exception.class, () ->
                visaService.updateEstado(visa.id(), EstadoVisto.APROVADO, "Invalid"));

        assertTrue(eventListener.getEvents().isEmpty());
    }
}

package ao.gov.embaixada.sgc.statemachine.event;

import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class WorkflowAuditListenerTest {

    private final WorkflowAuditListener listener = new WorkflowAuditListener();

    @Test
    void shouldHandleTransitionEventWithoutThrowing() {
        WorkflowTransitionEvent event = new WorkflowTransitionEvent(
                this, UUID.randomUUID(), "Visa",
                "RASCUNHO", "SUBMETIDO", "Test comment");

        assertDoesNotThrow(() -> listener.onTransition(event));
    }

    @Test
    void shouldHandleNullPreviousState() {
        WorkflowTransitionEvent event = new WorkflowTransitionEvent(
                this, UUID.randomUUID(), "RegistoCivil",
                null, "RASCUNHO", "Initial creation");

        assertDoesNotThrow(() -> listener.onTransition(event));
    }

    @Test
    void shouldHandleNullComment() {
        WorkflowTransitionEvent event = new WorkflowTransitionEvent(
                this, UUID.randomUUID(), "Processo",
                "SUBMETIDO", "EM_ANALISE", null);

        assertDoesNotThrow(() -> listener.onTransition(event));
    }

    @Test
    void eventShouldCarryCorrectData() {
        UUID entityId = UUID.randomUUID();
        WorkflowTransitionEvent event = new WorkflowTransitionEvent(
                this, entityId, "Agendamento",
                "PENDENTE", "CONFIRMADO", "Confirmed by consul");

        assertEquals(entityId, event.getEntityId());
        assertEquals("Agendamento", event.getWorkflowName());
        assertEquals("PENDENTE", event.getPreviousState());
        assertEquals("CONFIRMADO", event.getNewState());
        assertEquals("Confirmed by consul", event.getComment());
        assertNotNull(event.getTransitionTime());
    }
}

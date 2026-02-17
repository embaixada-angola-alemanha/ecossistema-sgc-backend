package ao.gov.embaixada.sgc.statemachine;

import ao.gov.embaixada.sgc.enums.EstadoServicoNotarial;
import ao.gov.embaixada.sgc.exception.InvalidStateTransitionException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.*;

class ServicoNotarialStateMachineTest {

    private ServicoNotarialStateMachine stateMachine;

    @BeforeEach
    void setUp() {
        stateMachine = new ServicoNotarialStateMachine();
    }

    @ParameterizedTest
    @CsvSource({
            "RASCUNHO, SUBMETIDO",
            "RASCUNHO, CANCELADO",
            "SUBMETIDO, EM_PROCESSAMENTO",
            "SUBMETIDO, CANCELADO",
            "EM_PROCESSAMENTO, CONCLUIDO",
            "EM_PROCESSAMENTO, REJEITADO"
    })
    void shouldAllowValidTransitions(EstadoServicoNotarial from, EstadoServicoNotarial to) {
        assertTrue(stateMachine.isTransitionAllowed(from, to));
        assertDoesNotThrow(() -> stateMachine.validateTransition(from, to));
    }

    @ParameterizedTest
    @CsvSource({
            "RASCUNHO, CONCLUIDO",
            "RASCUNHO, EM_PROCESSAMENTO",
            "RASCUNHO, REJEITADO",
            "SUBMETIDO, SUBMETIDO",
            "SUBMETIDO, CONCLUIDO",
            "SUBMETIDO, REJEITADO",
            "EM_PROCESSAMENTO, SUBMETIDO",
            "EM_PROCESSAMENTO, RASCUNHO",
            "EM_PROCESSAMENTO, CANCELADO"
    })
    void shouldDenyInvalidTransitions(EstadoServicoNotarial from, EstadoServicoNotarial to) {
        assertFalse(stateMachine.isTransitionAllowed(from, to));
        assertThrows(InvalidStateTransitionException.class,
                () -> stateMachine.validateTransition(from, to));
    }

    @Test
    void terminalStatesHaveNoOutgoingTransitions() {
        assertFalse(stateMachine.isTransitionAllowed(EstadoServicoNotarial.CONCLUIDO, EstadoServicoNotarial.RASCUNHO));
        assertFalse(stateMachine.isTransitionAllowed(EstadoServicoNotarial.REJEITADO, EstadoServicoNotarial.RASCUNHO));
        assertFalse(stateMachine.isTransitionAllowed(EstadoServicoNotarial.CANCELADO, EstadoServicoNotarial.RASCUNHO));
        assertFalse(stateMachine.isTransitionAllowed(EstadoServicoNotarial.CONCLUIDO, EstadoServicoNotarial.CANCELADO));
        assertFalse(stateMachine.isTransitionAllowed(EstadoServicoNotarial.REJEITADO, EstadoServicoNotarial.SUBMETIDO));
        assertFalse(stateMachine.isTransitionAllowed(EstadoServicoNotarial.CANCELADO, EstadoServicoNotarial.SUBMETIDO));
    }
}

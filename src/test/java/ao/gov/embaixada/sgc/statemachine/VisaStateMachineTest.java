package ao.gov.embaixada.sgc.statemachine;

import ao.gov.embaixada.sgc.enums.EstadoVisto;
import ao.gov.embaixada.sgc.exception.InvalidStateTransitionException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.*;

class VisaStateMachineTest {

    private VisaStateMachine stateMachine;

    @BeforeEach
    void setUp() {
        stateMachine = new VisaStateMachine();
    }

    @ParameterizedTest
    @CsvSource({
            "RASCUNHO, SUBMETIDO",
            "RASCUNHO, CANCELADO",
            "SUBMETIDO, EM_ANALISE",
            "SUBMETIDO, CANCELADO",
            "EM_ANALISE, DOCUMENTOS_PENDENTES",
            "EM_ANALISE, APROVADO",
            "EM_ANALISE, REJEITADO",
            "DOCUMENTOS_PENDENTES, EM_ANALISE",
            "DOCUMENTOS_PENDENTES, CANCELADO",
            "APROVADO, EMITIDO",
            "APROVADO, CANCELADO"
    })
    void shouldAllowValidTransitions(EstadoVisto from, EstadoVisto to) {
        assertTrue(stateMachine.isTransitionAllowed(from, to));
        assertDoesNotThrow(() -> stateMachine.validateTransition(from, to));
    }

    @ParameterizedTest
    @CsvSource({
            "RASCUNHO, APROVADO",
            "RASCUNHO, EM_ANALISE",
            "SUBMETIDO, APROVADO",
            "SUBMETIDO, REJEITADO",
            "EM_ANALISE, SUBMETIDO",
            "EM_ANALISE, EMITIDO",
            "DOCUMENTOS_PENDENTES, APROVADO",
            "APROVADO, RASCUNHO",
            "APROVADO, REJEITADO"
    })
    void shouldDenyInvalidTransitions(EstadoVisto from, EstadoVisto to) {
        assertFalse(stateMachine.isTransitionAllowed(from, to));
        assertThrows(InvalidStateTransitionException.class,
                () -> stateMachine.validateTransition(from, to));
    }

    @Test
    void terminalStatesHaveNoOutgoingTransitions() {
        assertFalse(stateMachine.isTransitionAllowed(EstadoVisto.REJEITADO, EstadoVisto.RASCUNHO));
        assertFalse(stateMachine.isTransitionAllowed(EstadoVisto.EMITIDO, EstadoVisto.CANCELADO));
        assertFalse(stateMachine.isTransitionAllowed(EstadoVisto.CANCELADO, EstadoVisto.RASCUNHO));
    }
}

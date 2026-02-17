package ao.gov.embaixada.sgc.statemachine;

import ao.gov.embaixada.sgc.enums.EstadoRegistoCivil;
import ao.gov.embaixada.sgc.exception.InvalidStateTransitionException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.*;

class RegistoCivilStateMachineTest {

    private RegistoCivilStateMachine stateMachine;

    @BeforeEach
    void setUp() {
        stateMachine = new RegistoCivilStateMachine();
    }

    @ParameterizedTest
    @CsvSource({
            "RASCUNHO, SUBMETIDO",
            "RASCUNHO, CANCELADO",
            "SUBMETIDO, EM_VERIFICACAO",
            "SUBMETIDO, CANCELADO",
            "EM_VERIFICACAO, VERIFICADO",
            "EM_VERIFICACAO, REJEITADO",
            "VERIFICADO, CERTIFICADO_EMITIDO",
            "VERIFICADO, CANCELADO"
    })
    void shouldAllowValidTransitions(EstadoRegistoCivil from, EstadoRegistoCivil to) {
        assertTrue(stateMachine.isTransitionAllowed(from, to));
        assertDoesNotThrow(() -> stateMachine.validateTransition(from, to));
    }

    @ParameterizedTest
    @CsvSource({
            "RASCUNHO, VERIFICADO",
            "RASCUNHO, EM_VERIFICACAO",
            "RASCUNHO, CERTIFICADO_EMITIDO",
            "SUBMETIDO, SUBMETIDO",
            "SUBMETIDO, VERIFICADO",
            "SUBMETIDO, CERTIFICADO_EMITIDO",
            "EM_VERIFICACAO, SUBMETIDO",
            "EM_VERIFICACAO, CERTIFICADO_EMITIDO",
            "EM_VERIFICACAO, CANCELADO",
            "VERIFICADO, RASCUNHO",
            "VERIFICADO, REJEITADO"
    })
    void shouldDenyInvalidTransitions(EstadoRegistoCivil from, EstadoRegistoCivil to) {
        assertFalse(stateMachine.isTransitionAllowed(from, to));
        assertThrows(InvalidStateTransitionException.class,
                () -> stateMachine.validateTransition(from, to));
    }

    @Test
    void terminalStatesHaveNoOutgoingTransitions() {
        assertFalse(stateMachine.isTransitionAllowed(EstadoRegistoCivil.CERTIFICADO_EMITIDO, EstadoRegistoCivil.RASCUNHO));
        assertFalse(stateMachine.isTransitionAllowed(EstadoRegistoCivil.REJEITADO, EstadoRegistoCivil.RASCUNHO));
        assertFalse(stateMachine.isTransitionAllowed(EstadoRegistoCivil.CANCELADO, EstadoRegistoCivil.RASCUNHO));
        assertFalse(stateMachine.isTransitionAllowed(EstadoRegistoCivil.CERTIFICADO_EMITIDO, EstadoRegistoCivil.CANCELADO));
        assertFalse(stateMachine.isTransitionAllowed(EstadoRegistoCivil.REJEITADO, EstadoRegistoCivil.SUBMETIDO));
        assertFalse(stateMachine.isTransitionAllowed(EstadoRegistoCivil.CANCELADO, EstadoRegistoCivil.SUBMETIDO));
    }
}

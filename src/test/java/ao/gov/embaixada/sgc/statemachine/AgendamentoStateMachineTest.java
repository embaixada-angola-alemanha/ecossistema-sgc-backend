package ao.gov.embaixada.sgc.statemachine;

import ao.gov.embaixada.sgc.enums.EstadoAgendamento;
import ao.gov.embaixada.sgc.exception.InvalidStateTransitionException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.*;

class AgendamentoStateMachineTest {

    private AgendamentoStateMachine stateMachine;

    @BeforeEach
    void setUp() {
        stateMachine = new AgendamentoStateMachine();
    }

    @ParameterizedTest
    @CsvSource({
            "PENDENTE, CONFIRMADO",
            "PENDENTE, CANCELADO",
            "CONFIRMADO, REAGENDADO",
            "CONFIRMADO, CANCELADO",
            "CONFIRMADO, COMPLETADO",
            "CONFIRMADO, NAO_COMPARECEU",
            "REAGENDADO, CONFIRMADO",
            "REAGENDADO, CANCELADO"
    })
    void shouldAllowValidTransitions(EstadoAgendamento from, EstadoAgendamento to) {
        assertTrue(stateMachine.isTransitionAllowed(from, to));
        assertDoesNotThrow(() -> stateMachine.validateTransition(from, to));
    }

    @ParameterizedTest
    @CsvSource({
            "PENDENTE, COMPLETADO",
            "PENDENTE, NAO_COMPARECEU",
            "PENDENTE, REAGENDADO",
            "CONFIRMADO, PENDENTE",
            "REAGENDADO, COMPLETADO",
            "REAGENDADO, NAO_COMPARECEU",
            "REAGENDADO, PENDENTE"
    })
    void shouldDenyInvalidTransitions(EstadoAgendamento from, EstadoAgendamento to) {
        assertFalse(stateMachine.isTransitionAllowed(from, to));
        assertThrows(InvalidStateTransitionException.class,
                () -> stateMachine.validateTransition(from, to));
    }

    @Test
    void terminalStatesHaveNoOutgoingTransitions() {
        assertFalse(stateMachine.isTransitionAllowed(EstadoAgendamento.CANCELADO, EstadoAgendamento.PENDENTE));
        assertFalse(stateMachine.isTransitionAllowed(EstadoAgendamento.COMPLETADO, EstadoAgendamento.CONFIRMADO));
        assertFalse(stateMachine.isTransitionAllowed(EstadoAgendamento.NAO_COMPARECEU, EstadoAgendamento.CONFIRMADO));
    }

    @Test
    void terminalStatesAreTerminal() {
        assertTrue(stateMachine.isTerminalState(EstadoAgendamento.CANCELADO));
        assertTrue(stateMachine.isTerminalState(EstadoAgendamento.COMPLETADO));
        assertTrue(stateMachine.isTerminalState(EstadoAgendamento.NAO_COMPARECEU));
    }

    @Test
    void nonTerminalStatesAreNotTerminal() {
        assertFalse(stateMachine.isTerminalState(EstadoAgendamento.PENDENTE));
        assertFalse(stateMachine.isTerminalState(EstadoAgendamento.CONFIRMADO));
        assertFalse(stateMachine.isTerminalState(EstadoAgendamento.REAGENDADO));
    }
}

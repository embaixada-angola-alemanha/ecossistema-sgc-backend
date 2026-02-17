package ao.gov.embaixada.sgc.statemachine;

import ao.gov.embaixada.sgc.enums.EstadoProcesso;
import ao.gov.embaixada.sgc.exception.InvalidStateTransitionException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.*;

class ProcessoStateMachineTest {

    private ProcessoStateMachine stateMachine;

    @BeforeEach
    void setUp() {
        stateMachine = new ProcessoStateMachine();
    }

    @ParameterizedTest
    @CsvSource({
            "RASCUNHO, SUBMETIDO",
            "RASCUNHO, CANCELADO",
            "SUBMETIDO, EM_ANALISE",
            "SUBMETIDO, CANCELADO",
            "EM_ANALISE, APROVADO",
            "EM_ANALISE, REJEITADO",
            "EM_ANALISE, CANCELADO",
            "APROVADO, CONCLUIDO",
            "APROVADO, CANCELADO",
            "REJEITADO, RASCUNHO",
            "REJEITADO, CANCELADO"
    })
    void shouldAllowValidTransitions(EstadoProcesso from, EstadoProcesso to) {
        assertTrue(stateMachine.isTransitionAllowed(from, to));
        assertDoesNotThrow(() -> stateMachine.validateTransition(from, to));
    }

    @ParameterizedTest
    @CsvSource({
            "RASCUNHO, CONCLUIDO",
            "RASCUNHO, EM_ANALISE",
            "RASCUNHO, APROVADO",
            "RASCUNHO, REJEITADO",
            "SUBMETIDO, SUBMETIDO",
            "SUBMETIDO, CONCLUIDO",
            "SUBMETIDO, APROVADO",
            "EM_ANALISE, SUBMETIDO",
            "EM_ANALISE, RASCUNHO",
            "EM_ANALISE, CONCLUIDO",
            "APROVADO, RASCUNHO",
            "APROVADO, SUBMETIDO"
    })
    void shouldDenyInvalidTransitions(EstadoProcesso from, EstadoProcesso to) {
        assertFalse(stateMachine.isTransitionAllowed(from, to));
        assertThrows(InvalidStateTransitionException.class,
                () -> stateMachine.validateTransition(from, to));
    }

    @Test
    void terminalStatesHaveNoOutgoingTransitions() {
        assertFalse(stateMachine.isTransitionAllowed(EstadoProcesso.CONCLUIDO, EstadoProcesso.RASCUNHO));
        assertFalse(stateMachine.isTransitionAllowed(EstadoProcesso.CONCLUIDO, EstadoProcesso.CANCELADO));
        assertFalse(stateMachine.isTransitionAllowed(EstadoProcesso.CANCELADO, EstadoProcesso.RASCUNHO));
        assertFalse(stateMachine.isTransitionAllowed(EstadoProcesso.CANCELADO, EstadoProcesso.SUBMETIDO));
    }

    @Test
    void terminalStatesAreTerminal() {
        assertTrue(stateMachine.isTerminalState(EstadoProcesso.CONCLUIDO));
        assertTrue(stateMachine.isTerminalState(EstadoProcesso.CANCELADO));
    }

    @Test
    void nonTerminalStatesAreNotTerminal() {
        assertFalse(stateMachine.isTerminalState(EstadoProcesso.RASCUNHO));
        assertFalse(stateMachine.isTerminalState(EstadoProcesso.SUBMETIDO));
        assertFalse(stateMachine.isTerminalState(EstadoProcesso.EM_ANALISE));
        assertFalse(stateMachine.isTerminalState(EstadoProcesso.APROVADO));
    }

    @Test
    void rejeitadoCanRevertToRascunho() {
        assertTrue(stateMachine.isTransitionAllowed(EstadoProcesso.REJEITADO, EstadoProcesso.RASCUNHO));
        assertFalse(stateMachine.isTerminalState(EstadoProcesso.REJEITADO));
    }
}

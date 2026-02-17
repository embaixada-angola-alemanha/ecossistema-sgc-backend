package ao.gov.embaixada.sgc.statemachine;

import ao.gov.embaixada.sgc.exception.InvalidStateTransitionException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class WorkflowStateMachineTest {

    enum TestState { A, B, C, DONE, FAILED }

    static class TestStateMachine extends WorkflowStateMachine<TestState> {
        TestStateMachine() {
            super("Test", TestState.class);
        }

        @Override
        protected void configureTransitions() {
            addTransition(TestState.A, TestState.B, TestState.C);
            addTransition(TestState.B, TestState.DONE, TestState.FAILED);
            addTransition(TestState.C, TestState.DONE);
            addTerminalState(TestState.DONE);
            // FAILED is implicitly terminal (no addTransition)
        }
    }

    private TestStateMachine stateMachine;

    @BeforeEach
    void setUp() {
        stateMachine = new TestStateMachine();
    }

    @Test
    void shouldAllowValidTransitions() {
        assertTrue(stateMachine.isTransitionAllowed(TestState.A, TestState.B));
        assertTrue(stateMachine.isTransitionAllowed(TestState.A, TestState.C));
        assertTrue(stateMachine.isTransitionAllowed(TestState.B, TestState.DONE));
        assertTrue(stateMachine.isTransitionAllowed(TestState.B, TestState.FAILED));
        assertTrue(stateMachine.isTransitionAllowed(TestState.C, TestState.DONE));
    }

    @Test
    void shouldDenyInvalidTransitions() {
        assertFalse(stateMachine.isTransitionAllowed(TestState.A, TestState.DONE));
        assertFalse(stateMachine.isTransitionAllowed(TestState.A, TestState.FAILED));
        assertFalse(stateMachine.isTransitionAllowed(TestState.B, TestState.A));
        assertFalse(stateMachine.isTransitionAllowed(TestState.C, TestState.B));
    }

    @Test
    void validateTransitionShouldThrowOnInvalid() {
        assertDoesNotThrow(() -> stateMachine.validateTransition(TestState.A, TestState.B));
        assertThrows(InvalidStateTransitionException.class,
                () -> stateMachine.validateTransition(TestState.A, TestState.DONE));
    }

    @Test
    void explicitTerminalStateShouldBeTerminal() {
        assertTrue(stateMachine.isTerminalState(TestState.DONE));
    }

    @Test
    void implicitTerminalStateShouldBeTerminal() {
        assertTrue(stateMachine.isTerminalState(TestState.FAILED));
    }

    @Test
    void nonTerminalStatesShouldNotBeTerminal() {
        assertFalse(stateMachine.isTerminalState(TestState.A));
        assertFalse(stateMachine.isTerminalState(TestState.B));
        assertFalse(stateMachine.isTerminalState(TestState.C));
    }

    @Test
    void getAllowedTransitionsShouldReturnCorrectSet() {
        Set<TestState> fromA = stateMachine.getAllowedTransitions(TestState.A);
        assertEquals(Set.of(TestState.B, TestState.C), fromA);

        Set<TestState> fromDone = stateMachine.getAllowedTransitions(TestState.DONE);
        assertTrue(fromDone.isEmpty());
    }

    @Test
    void workflowNameAndStateTypeShouldBeCorrect() {
        assertEquals("Test", stateMachine.getWorkflowName());
        assertEquals(TestState.class, stateMachine.getStateType());
    }
}

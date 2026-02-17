package ao.gov.embaixada.sgc.statemachine;

import ao.gov.embaixada.sgc.exception.InvalidStateTransitionException;

import java.util.Collections;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;

public abstract class WorkflowStateMachine<E extends Enum<E>> {

    private final Map<E, Set<E>> transitions;
    private final Set<E> terminalStates;
    private final String workflowName;
    private final Class<E> stateType;

    protected WorkflowStateMachine(String workflowName, Class<E> stateType) {
        this.workflowName = workflowName;
        this.stateType = stateType;
        this.transitions = new EnumMap<>(stateType);
        this.terminalStates = EnumSet.noneOf(stateType);
        configureTransitions();
    }

    protected abstract void configureTransitions();

    @SafeVarargs
    protected final void addTransition(E from, E... to) {
        if (to.length == 0) {
            transitions.put(from, Collections.emptySet());
        } else {
            transitions.put(from, EnumSet.of(to[0], to));
        }
    }

    protected final void addTerminalState(E state) {
        terminalStates.add(state);
        transitions.put(state, Collections.emptySet());
    }

    public boolean isTransitionAllowed(E from, E to) {
        Set<E> allowed = transitions.get(from);
        return allowed != null && allowed.contains(to);
    }

    public void validateTransition(E from, E to) {
        if (!isTransitionAllowed(from, to)) {
            throw new InvalidStateTransitionException(workflowName, from.name(), to.name());
        }
    }

    public boolean isTerminalState(E state) {
        if (terminalStates.contains(state)) {
            return true;
        }
        return !transitions.containsKey(state)
                || transitions.getOrDefault(state, Collections.emptySet()).isEmpty();
    }

    public Set<E> getAllowedTransitions(E from) {
        return Collections.unmodifiableSet(
                transitions.getOrDefault(from, Collections.emptySet()));
    }

    public String getWorkflowName() {
        return workflowName;
    }

    public Class<E> getStateType() {
        return stateType;
    }
}

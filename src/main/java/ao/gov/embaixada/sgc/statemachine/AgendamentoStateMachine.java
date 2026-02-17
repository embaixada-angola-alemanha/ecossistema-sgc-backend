package ao.gov.embaixada.sgc.statemachine;

import ao.gov.embaixada.sgc.enums.EstadoAgendamento;
import ao.gov.embaixada.sgc.exception.InvalidStateTransitionException;
import org.springframework.stereotype.Component;

import java.util.EnumMap;
import java.util.Map;
import java.util.Set;

@Component
public class AgendamentoStateMachine {

    private final Map<EstadoAgendamento, Set<EstadoAgendamento>> transitions = new EnumMap<>(EstadoAgendamento.class);

    public AgendamentoStateMachine() {
        transitions.put(EstadoAgendamento.PENDENTE, Set.of(
                EstadoAgendamento.CONFIRMADO, EstadoAgendamento.CANCELADO));
        transitions.put(EstadoAgendamento.CONFIRMADO, Set.of(
                EstadoAgendamento.REAGENDADO, EstadoAgendamento.CANCELADO,
                EstadoAgendamento.COMPLETADO, EstadoAgendamento.NAO_COMPARECEU));
        transitions.put(EstadoAgendamento.REAGENDADO, Set.of(
                EstadoAgendamento.CONFIRMADO, EstadoAgendamento.CANCELADO));
        // Terminal states: CANCELADO, COMPLETADO, NAO_COMPARECEU — no outgoing transitions
    }

    public boolean isTransitionAllowed(EstadoAgendamento from, EstadoAgendamento to) {
        Set<EstadoAgendamento> allowed = transitions.get(from);
        return allowed != null && allowed.contains(to);
    }

    public void validateTransition(EstadoAgendamento from, EstadoAgendamento to) {
        if (!isTransitionAllowed(from, to)) {
            throw new InvalidStateTransitionException("Agendamento", from.name(), to.name());
        }
    }

    public boolean isTerminal(EstadoAgendamento estado) {
        return !transitions.containsKey(estado);
    }
}

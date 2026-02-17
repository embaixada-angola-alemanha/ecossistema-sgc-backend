package ao.gov.embaixada.sgc.statemachine;

import ao.gov.embaixada.sgc.enums.EstadoVisto;
import ao.gov.embaixada.sgc.exception.InvalidStateTransitionException;
import org.springframework.stereotype.Component;

import java.util.EnumMap;
import java.util.Map;
import java.util.Set;

@Component
public class VisaStateMachine {

    private final Map<EstadoVisto, Set<EstadoVisto>> transitions = new EnumMap<>(EstadoVisto.class);

    public VisaStateMachine() {
        transitions.put(EstadoVisto.RASCUNHO, Set.of(
                EstadoVisto.SUBMETIDO, EstadoVisto.CANCELADO));
        transitions.put(EstadoVisto.SUBMETIDO, Set.of(
                EstadoVisto.EM_ANALISE, EstadoVisto.CANCELADO));
        transitions.put(EstadoVisto.EM_ANALISE, Set.of(
                EstadoVisto.DOCUMENTOS_PENDENTES, EstadoVisto.APROVADO, EstadoVisto.REJEITADO));
        transitions.put(EstadoVisto.DOCUMENTOS_PENDENTES, Set.of(
                EstadoVisto.EM_ANALISE, EstadoVisto.CANCELADO));
        transitions.put(EstadoVisto.APROVADO, Set.of(
                EstadoVisto.EMITIDO, EstadoVisto.CANCELADO));
        // Terminal states: REJEITADO, EMITIDO, CANCELADO — no outgoing transitions
    }

    public boolean isTransitionAllowed(EstadoVisto from, EstadoVisto to) {
        Set<EstadoVisto> allowed = transitions.get(from);
        return allowed != null && allowed.contains(to);
    }

    public void validateTransition(EstadoVisto from, EstadoVisto to) {
        if (!isTransitionAllowed(from, to)) {
            throw new InvalidStateTransitionException("Visa", from.name(), to.name());
        }
    }
}

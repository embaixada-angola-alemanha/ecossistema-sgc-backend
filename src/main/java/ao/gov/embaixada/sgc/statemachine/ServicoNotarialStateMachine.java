package ao.gov.embaixada.sgc.statemachine;

import ao.gov.embaixada.sgc.enums.EstadoServicoNotarial;
import ao.gov.embaixada.sgc.exception.InvalidStateTransitionException;
import org.springframework.stereotype.Component;

import java.util.EnumMap;
import java.util.Map;
import java.util.Set;

@Component
public class ServicoNotarialStateMachine {

    private final Map<EstadoServicoNotarial, Set<EstadoServicoNotarial>> transitions = new EnumMap<>(EstadoServicoNotarial.class);

    public ServicoNotarialStateMachine() {
        transitions.put(EstadoServicoNotarial.RASCUNHO, Set.of(
                EstadoServicoNotarial.SUBMETIDO, EstadoServicoNotarial.CANCELADO));
        transitions.put(EstadoServicoNotarial.SUBMETIDO, Set.of(
                EstadoServicoNotarial.EM_PROCESSAMENTO, EstadoServicoNotarial.CANCELADO));
        transitions.put(EstadoServicoNotarial.EM_PROCESSAMENTO, Set.of(
                EstadoServicoNotarial.CONCLUIDO, EstadoServicoNotarial.REJEITADO));
        // Terminal states: CONCLUIDO, REJEITADO, CANCELADO
    }

    public boolean isTransitionAllowed(EstadoServicoNotarial from, EstadoServicoNotarial to) {
        Set<EstadoServicoNotarial> allowed = transitions.get(from);
        return allowed != null && allowed.contains(to);
    }

    public void validateTransition(EstadoServicoNotarial from, EstadoServicoNotarial to) {
        if (!isTransitionAllowed(from, to)) {
            throw new InvalidStateTransitionException("ServicoNotarial", from.name(), to.name());
        }
    }
}

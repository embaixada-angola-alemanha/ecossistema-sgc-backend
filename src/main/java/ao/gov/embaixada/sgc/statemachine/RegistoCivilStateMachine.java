package ao.gov.embaixada.sgc.statemachine;

import ao.gov.embaixada.sgc.enums.EstadoRegistoCivil;
import ao.gov.embaixada.sgc.exception.InvalidStateTransitionException;
import org.springframework.stereotype.Component;

import java.util.EnumMap;
import java.util.Map;
import java.util.Set;

@Component
public class RegistoCivilStateMachine {

    private final Map<EstadoRegistoCivil, Set<EstadoRegistoCivil>> transitions = new EnumMap<>(EstadoRegistoCivil.class);

    public RegistoCivilStateMachine() {
        transitions.put(EstadoRegistoCivil.RASCUNHO, Set.of(
                EstadoRegistoCivil.SUBMETIDO, EstadoRegistoCivil.CANCELADO));
        transitions.put(EstadoRegistoCivil.SUBMETIDO, Set.of(
                EstadoRegistoCivil.EM_VERIFICACAO, EstadoRegistoCivil.CANCELADO));
        transitions.put(EstadoRegistoCivil.EM_VERIFICACAO, Set.of(
                EstadoRegistoCivil.VERIFICADO, EstadoRegistoCivil.REJEITADO));
        transitions.put(EstadoRegistoCivil.VERIFICADO, Set.of(
                EstadoRegistoCivil.CERTIFICADO_EMITIDO, EstadoRegistoCivil.CANCELADO));
        // Terminal states: CERTIFICADO_EMITIDO, REJEITADO, CANCELADO
    }

    public boolean isTransitionAllowed(EstadoRegistoCivil from, EstadoRegistoCivil to) {
        Set<EstadoRegistoCivil> allowed = transitions.get(from);
        return allowed != null && allowed.contains(to);
    }

    public void validateTransition(EstadoRegistoCivil from, EstadoRegistoCivil to) {
        if (!isTransitionAllowed(from, to)) {
            throw new InvalidStateTransitionException("RegistoCivil", from.name(), to.name());
        }
    }
}

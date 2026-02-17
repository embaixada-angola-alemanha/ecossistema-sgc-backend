package ao.gov.embaixada.sgc.statemachine;

import ao.gov.embaixada.sgc.enums.EstadoProcesso;
import ao.gov.embaixada.sgc.exception.InvalidStateTransitionException;
import org.springframework.stereotype.Component;

import java.util.EnumMap;
import java.util.Map;
import java.util.Set;

@Component
public class ProcessoStateMachine {

    private final Map<EstadoProcesso, Set<EstadoProcesso>> transitions = new EnumMap<>(EstadoProcesso.class);

    public ProcessoStateMachine() {
        transitions.put(EstadoProcesso.RASCUNHO, Set.of(
                EstadoProcesso.SUBMETIDO, EstadoProcesso.CANCELADO));
        transitions.put(EstadoProcesso.SUBMETIDO, Set.of(
                EstadoProcesso.EM_ANALISE, EstadoProcesso.CANCELADO));
        transitions.put(EstadoProcesso.EM_ANALISE, Set.of(
                EstadoProcesso.APROVADO, EstadoProcesso.REJEITADO, EstadoProcesso.CANCELADO));
        transitions.put(EstadoProcesso.APROVADO, Set.of(
                EstadoProcesso.CONCLUIDO, EstadoProcesso.CANCELADO));
        transitions.put(EstadoProcesso.REJEITADO, Set.of(
                EstadoProcesso.RASCUNHO, EstadoProcesso.CANCELADO));
        transitions.put(EstadoProcesso.CONCLUIDO, Set.of());
        transitions.put(EstadoProcesso.CANCELADO, Set.of());
    }

    public boolean isTransitionAllowed(EstadoProcesso from, EstadoProcesso to) {
        Set<EstadoProcesso> allowed = transitions.get(from);
        return allowed != null && allowed.contains(to);
    }

    public void validateTransition(EstadoProcesso from, EstadoProcesso to) {
        if (!isTransitionAllowed(from, to)) {
            throw new InvalidStateTransitionException("Processo", from.name(), to.name());
        }
    }
}

package ao.gov.embaixada.sgc.statemachine;

import ao.gov.embaixada.sgc.enums.EstadoServicoNotarial;
import org.springframework.stereotype.Component;

@Component
public class ServicoNotarialStateMachine extends WorkflowStateMachine<EstadoServicoNotarial> {

    public ServicoNotarialStateMachine() {
        super("ServicoNotarial", EstadoServicoNotarial.class);
    }

    @Override
    protected void configureTransitions() {
        addTransition(EstadoServicoNotarial.RASCUNHO, EstadoServicoNotarial.SUBMETIDO, EstadoServicoNotarial.CANCELADO);
        addTransition(EstadoServicoNotarial.SUBMETIDO, EstadoServicoNotarial.EM_PROCESSAMENTO, EstadoServicoNotarial.CANCELADO);
        addTransition(EstadoServicoNotarial.EM_PROCESSAMENTO, EstadoServicoNotarial.CONCLUIDO, EstadoServicoNotarial.REJEITADO);
        // Terminal states: CONCLUIDO, REJEITADO, CANCELADO
    }
}

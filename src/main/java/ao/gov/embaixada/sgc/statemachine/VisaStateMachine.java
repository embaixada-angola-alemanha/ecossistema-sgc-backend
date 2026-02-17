package ao.gov.embaixada.sgc.statemachine;

import ao.gov.embaixada.sgc.enums.EstadoVisto;
import org.springframework.stereotype.Component;

@Component
public class VisaStateMachine extends WorkflowStateMachine<EstadoVisto> {

    public VisaStateMachine() {
        super("Visa", EstadoVisto.class);
    }

    @Override
    protected void configureTransitions() {
        addTransition(EstadoVisto.RASCUNHO, EstadoVisto.SUBMETIDO, EstadoVisto.CANCELADO);
        addTransition(EstadoVisto.SUBMETIDO, EstadoVisto.EM_ANALISE, EstadoVisto.CANCELADO);
        addTransition(EstadoVisto.EM_ANALISE, EstadoVisto.DOCUMENTOS_PENDENTES, EstadoVisto.APROVADO, EstadoVisto.REJEITADO);
        addTransition(EstadoVisto.DOCUMENTOS_PENDENTES, EstadoVisto.EM_ANALISE, EstadoVisto.CANCELADO);
        addTransition(EstadoVisto.APROVADO, EstadoVisto.EMITIDO, EstadoVisto.CANCELADO);
        // Terminal states: REJEITADO, EMITIDO, CANCELADO — no outgoing transitions
    }
}

package ao.gov.embaixada.sgc.statemachine;

import ao.gov.embaixada.sgc.enums.EstadoProcesso;
import org.springframework.stereotype.Component;

@Component
public class ProcessoStateMachine extends WorkflowStateMachine<EstadoProcesso> {

    public ProcessoStateMachine() {
        super("Processo", EstadoProcesso.class);
    }

    @Override
    protected void configureTransitions() {
        addTransition(EstadoProcesso.RASCUNHO, EstadoProcesso.SUBMETIDO, EstadoProcesso.CANCELADO);
        addTransition(EstadoProcesso.SUBMETIDO, EstadoProcesso.EM_ANALISE, EstadoProcesso.CANCELADO);
        addTransition(EstadoProcesso.EM_ANALISE, EstadoProcesso.APROVADO, EstadoProcesso.REJEITADO, EstadoProcesso.CANCELADO);
        addTransition(EstadoProcesso.APROVADO, EstadoProcesso.CONCLUIDO, EstadoProcesso.CANCELADO);
        addTransition(EstadoProcesso.REJEITADO, EstadoProcesso.RASCUNHO, EstadoProcesso.CANCELADO);
        addTerminalState(EstadoProcesso.CONCLUIDO);
        addTerminalState(EstadoProcesso.CANCELADO);
    }
}

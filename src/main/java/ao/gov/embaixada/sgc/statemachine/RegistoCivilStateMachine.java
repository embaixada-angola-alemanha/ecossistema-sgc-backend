package ao.gov.embaixada.sgc.statemachine;

import ao.gov.embaixada.sgc.enums.EstadoRegistoCivil;
import org.springframework.stereotype.Component;

@Component
public class RegistoCivilStateMachine extends WorkflowStateMachine<EstadoRegistoCivil> {

    public RegistoCivilStateMachine() {
        super("RegistoCivil", EstadoRegistoCivil.class);
    }

    @Override
    protected void configureTransitions() {
        addTransition(EstadoRegistoCivil.RASCUNHO, EstadoRegistoCivil.SUBMETIDO, EstadoRegistoCivil.CANCELADO);
        addTransition(EstadoRegistoCivil.SUBMETIDO, EstadoRegistoCivil.EM_VERIFICACAO, EstadoRegistoCivil.CANCELADO);
        addTransition(EstadoRegistoCivil.EM_VERIFICACAO, EstadoRegistoCivil.VERIFICADO, EstadoRegistoCivil.REJEITADO);
        addTransition(EstadoRegistoCivil.VERIFICADO, EstadoRegistoCivil.CERTIFICADO_EMITIDO, EstadoRegistoCivil.CANCELADO);
        // Terminal states: CERTIFICADO_EMITIDO, REJEITADO, CANCELADO
    }
}

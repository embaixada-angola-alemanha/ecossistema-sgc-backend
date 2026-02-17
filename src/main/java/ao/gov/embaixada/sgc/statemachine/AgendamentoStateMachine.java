package ao.gov.embaixada.sgc.statemachine;

import ao.gov.embaixada.sgc.enums.EstadoAgendamento;
import org.springframework.stereotype.Component;

@Component
public class AgendamentoStateMachine extends WorkflowStateMachine<EstadoAgendamento> {

    public AgendamentoStateMachine() {
        super("Agendamento", EstadoAgendamento.class);
    }

    @Override
    protected void configureTransitions() {
        addTransition(EstadoAgendamento.PENDENTE, EstadoAgendamento.CONFIRMADO, EstadoAgendamento.CANCELADO);
        addTransition(EstadoAgendamento.CONFIRMADO, EstadoAgendamento.REAGENDADO, EstadoAgendamento.CANCELADO,
                EstadoAgendamento.COMPLETADO, EstadoAgendamento.NAO_COMPARECEU);
        addTransition(EstadoAgendamento.REAGENDADO, EstadoAgendamento.CONFIRMADO, EstadoAgendamento.CANCELADO);
        // Terminal states: CANCELADO, COMPLETADO, NAO_COMPARECEU
    }
}

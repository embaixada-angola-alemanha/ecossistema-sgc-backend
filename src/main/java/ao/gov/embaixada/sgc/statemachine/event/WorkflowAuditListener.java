package ao.gov.embaixada.sgc.statemachine.event;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
public class WorkflowAuditListener {

    private static final Logger log = LoggerFactory.getLogger(WorkflowAuditListener.class);

    @EventListener
    public void onTransition(WorkflowTransitionEvent event) {
        log.info("Workflow [{}] entity={} transition: {} -> {} comment='{}'",
                event.getWorkflowName(),
                event.getEntityId(),
                event.getPreviousState(),
                event.getNewState(),
                event.getComment());
    }
}

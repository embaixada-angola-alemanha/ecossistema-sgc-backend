package ao.gov.embaixada.sgc.statemachine.event;

import org.springframework.context.ApplicationEvent;

import java.time.Instant;
import java.util.UUID;

public class WorkflowTransitionEvent extends ApplicationEvent {

    private final UUID entityId;
    private final String workflowName;
    private final String previousState;
    private final String newState;
    private final String comment;
    private final Instant transitionTime;

    public WorkflowTransitionEvent(Object source, UUID entityId,
                                   String workflowName,
                                   String previousState, String newState,
                                   String comment) {
        super(source);
        this.entityId = entityId;
        this.workflowName = workflowName;
        this.previousState = previousState;
        this.newState = newState;
        this.comment = comment;
        this.transitionTime = Instant.now();
    }

    public UUID getEntityId() {
        return entityId;
    }

    public String getWorkflowName() {
        return workflowName;
    }

    public String getPreviousState() {
        return previousState;
    }

    public String getNewState() {
        return newState;
    }

    public String getComment() {
        return comment;
    }

    public Instant getTransitionTime() {
        return transitionTime;
    }
}

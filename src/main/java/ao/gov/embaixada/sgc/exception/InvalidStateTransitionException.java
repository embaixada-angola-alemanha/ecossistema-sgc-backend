package ao.gov.embaixada.sgc.exception;

public class InvalidStateTransitionException extends RuntimeException {

    public InvalidStateTransitionException(String entityType, String fromStatus, String toStatus) {
        super("Invalid " + entityType + " state transition: " + fromStatus + " -> " + toStatus);
    }
}

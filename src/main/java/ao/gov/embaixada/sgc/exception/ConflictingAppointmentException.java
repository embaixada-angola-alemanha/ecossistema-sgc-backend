package ao.gov.embaixada.sgc.exception;

public class ConflictingAppointmentException extends RuntimeException {

    public ConflictingAppointmentException(String message) {
        super(message);
    }
}

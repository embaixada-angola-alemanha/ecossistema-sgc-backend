package ao.gov.embaixada.sgc.exception;

public class DuplicateResourceException extends RuntimeException {

    public DuplicateResourceException(String resourceType, String field, String value) {
        super(resourceType + " already exists with " + field + ": " + value);
    }
}

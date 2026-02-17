package ao.gov.embaixada.sgc.exception;

import java.util.UUID;

public class ResourceNotFoundException extends RuntimeException {

    public ResourceNotFoundException(String resourceType, UUID id) {
        super(resourceType + " not found with id: " + id);
    }

    public ResourceNotFoundException(String resourceType, String field, String value) {
        super(resourceType + " not found with " + field + ": " + value);
    }
}

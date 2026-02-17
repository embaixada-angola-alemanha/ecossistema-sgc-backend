package ao.gov.embaixada.sgc.dto;

import java.time.Instant;
import java.util.UUID;

public record AuditEventResponse(
        UUID id,
        String action,
        String entityType,
        String entityId,
        String userId,
        String username,
        String details,
        String ipAddress,
        Instant timestamp
) {}

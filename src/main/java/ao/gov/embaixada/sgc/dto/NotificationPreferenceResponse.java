package ao.gov.embaixada.sgc.dto;

import java.util.UUID;

public record NotificationPreferenceResponse(
        UUID id,
        UUID cidadaoId,
        String workflowName,
        boolean emailEnabled
) {}

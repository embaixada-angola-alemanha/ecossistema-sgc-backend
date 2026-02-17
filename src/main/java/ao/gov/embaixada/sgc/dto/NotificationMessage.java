package ao.gov.embaixada.sgc.dto;

import java.io.Serializable;
import java.util.Map;
import java.util.UUID;

public record NotificationMessage(
        UUID cidadaoId,
        String toAddress,
        String subject,
        String template,
        Map<String, Object> variables,
        String workflowName,
        UUID entityId
) implements Serializable {}

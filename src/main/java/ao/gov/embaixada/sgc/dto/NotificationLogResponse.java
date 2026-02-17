package ao.gov.embaixada.sgc.dto;

import ao.gov.embaixada.sgc.enums.EstadoNotificacao;

import java.time.Instant;
import java.util.UUID;

public record NotificationLogResponse(
        UUID id,
        UUID cidadaoId,
        String toAddress,
        String subject,
        String template,
        String workflowName,
        UUID entityId,
        EstadoNotificacao estado,
        String errorMessage,
        Instant sentAt,
        Instant createdAt
) {}

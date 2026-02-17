package ao.gov.embaixada.sgc.dto;

import ao.gov.embaixada.sgc.enums.EstadoAgendamento;
import ao.gov.embaixada.sgc.enums.TipoAgendamento;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.UUID;

public record AgendamentoResponse(
        UUID id,
        UUID cidadaoId,
        String cidadaoNome,
        String cidadaoEmail,
        TipoAgendamento tipo,
        String numeroAgendamento,
        EstadoAgendamento estado,
        LocalDateTime dataHora,
        int duracaoMinutos,
        String local,
        String notas,
        String motivoCancelamento,
        Instant createdAt,
        Instant updatedAt
) {}

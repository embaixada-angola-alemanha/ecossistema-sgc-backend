package ao.gov.embaixada.sgc.dto;

import ao.gov.embaixada.sgc.enums.EstadoAgendamento;

import java.time.Instant;
import java.util.UUID;

public record AgendamentoHistoricoResponse(
        UUID id,
        UUID agendamentoId,
        EstadoAgendamento estadoAnterior,
        EstadoAgendamento estadoNovo,
        String comentario,
        String alteradoPor,
        Instant createdAt
) {}

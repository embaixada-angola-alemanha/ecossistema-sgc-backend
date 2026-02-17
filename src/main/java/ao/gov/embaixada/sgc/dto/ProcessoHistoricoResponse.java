package ao.gov.embaixada.sgc.dto;

import ao.gov.embaixada.sgc.enums.EstadoProcesso;

import java.time.Instant;
import java.util.UUID;

public record ProcessoHistoricoResponse(
        UUID id,
        UUID processoId,
        EstadoProcesso estadoAnterior,
        EstadoProcesso estadoNovo,
        String comentario,
        String alteradoPor,
        Instant createdAt
) {}

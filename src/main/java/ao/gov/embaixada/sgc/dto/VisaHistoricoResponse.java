package ao.gov.embaixada.sgc.dto;

import ao.gov.embaixada.sgc.enums.EstadoVisto;

import java.time.Instant;
import java.util.UUID;

public record VisaHistoricoResponse(
        UUID id,
        UUID visaId,
        EstadoVisto estadoAnterior,
        EstadoVisto estadoNovo,
        String comentario,
        String alteradoPor,
        Instant createdAt
) {}

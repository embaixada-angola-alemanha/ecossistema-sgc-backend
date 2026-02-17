package ao.gov.embaixada.sgc.dto;

import ao.gov.embaixada.sgc.enums.EstadoServicoNotarial;

import java.time.Instant;
import java.util.UUID;

public record ServicoNotarialHistoricoResponse(
        UUID id,
        UUID servicoNotarialId,
        EstadoServicoNotarial estadoAnterior,
        EstadoServicoNotarial estadoNovo,
        String comentario,
        String alteradoPor,
        Instant createdAt
) {}

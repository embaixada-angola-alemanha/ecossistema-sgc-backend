package ao.gov.embaixada.sgc.dto;

import ao.gov.embaixada.sgc.enums.EstadoRegistoCivil;

import java.time.Instant;
import java.util.UUID;

public record RegistoCivilHistoricoResponse(
        UUID id,
        UUID registoCivilId,
        EstadoRegistoCivil estadoAnterior,
        EstadoRegistoCivil estadoNovo,
        String comentario,
        String alteradoPor,
        Instant createdAt
) {}

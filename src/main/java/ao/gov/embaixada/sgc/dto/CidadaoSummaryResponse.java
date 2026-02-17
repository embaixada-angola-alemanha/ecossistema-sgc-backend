package ao.gov.embaixada.sgc.dto;

import ao.gov.embaixada.sgc.enums.EstadoCidadao;

import java.util.UUID;

public record CidadaoSummaryResponse(
        UUID id,
        String numeroPassaporte,
        String nomeCompleto,
        EstadoCidadao estado
) {}

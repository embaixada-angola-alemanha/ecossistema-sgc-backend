package ao.gov.embaixada.sgc.dto;

import ao.gov.embaixada.sgc.enums.EstadoDocumento;

import java.time.Instant;
import java.util.UUID;

public record DocumentoVersionResponse(
        UUID id,
        Integer versao,
        String ficheiroNome,
        Long ficheiroTamanho,
        String ficheiroTipo,
        EstadoDocumento estado,
        String createdBy,
        Instant createdAt
) {}

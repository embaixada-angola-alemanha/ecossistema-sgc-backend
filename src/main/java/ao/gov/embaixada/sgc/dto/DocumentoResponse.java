package ao.gov.embaixada.sgc.dto;

import ao.gov.embaixada.sgc.enums.EstadoDocumento;
import ao.gov.embaixada.sgc.enums.TipoDocumento;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public record DocumentoResponse(
        UUID id,
        UUID cidadaoId,
        String cidadaoNome,
        TipoDocumento tipo,
        String numero,
        LocalDate dataEmissao,
        LocalDate dataValidade,
        String ficheiroUrl,
        String ficheiroNome,
        Long ficheiroTamanho,
        String ficheiroTipo,
        EstadoDocumento estado,
        Integer versao,
        UUID documentoOriginalId,
        Instant createdAt,
        Instant updatedAt
) {}

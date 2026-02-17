package ao.gov.embaixada.sgc.dto;

import ao.gov.embaixada.sgc.enums.EstadoVisto;
import ao.gov.embaixada.sgc.enums.TipoVisto;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

public record VisaResponse(
        UUID id,
        UUID cidadaoId,
        String cidadaoNome,
        TipoVisto tipo,
        String numeroVisto,
        EstadoVisto estado,
        String nacionalidadePassaporte,
        String motivoViagem,
        LocalDate dataEntrada,
        LocalDate dataSaida,
        String localAlojamento,
        String entidadeConvite,
        String responsavel,
        BigDecimal valorTaxa,
        boolean taxaPaga,
        LocalDateTime dataSubmissao,
        LocalDateTime dataDecisao,
        String motivoRejeicao,
        String observacoes,
        int documentoCount,
        Instant createdAt,
        Instant updatedAt
) {}

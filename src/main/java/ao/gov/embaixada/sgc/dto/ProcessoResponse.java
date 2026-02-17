package ao.gov.embaixada.sgc.dto;

import ao.gov.embaixada.sgc.enums.EstadoProcesso;
import ao.gov.embaixada.sgc.enums.Prioridade;
import ao.gov.embaixada.sgc.enums.TipoProcesso;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record ProcessoResponse(
        UUID id,
        UUID cidadaoId,
        String cidadaoNome,
        TipoProcesso tipo,
        String numeroProcesso,
        String descricao,
        EstadoProcesso estado,
        Prioridade prioridade,
        String responsavel,
        BigDecimal valorTaxa,
        boolean taxaPaga,
        LocalDateTime dataSubmissao,
        LocalDateTime dataConclusao,
        int documentoCount,
        Instant createdAt,
        Instant updatedAt
) {}

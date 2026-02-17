package ao.gov.embaixada.sgc.dto;

import ao.gov.embaixada.sgc.enums.EstadoServicoNotarial;
import ao.gov.embaixada.sgc.enums.TipoServicoNotarial;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.UUID;

public record ServicoNotarialResponse(
        UUID id,
        UUID cidadaoId,
        String cidadaoNome,
        TipoServicoNotarial tipo,
        String numeroServico,
        EstadoServicoNotarial estado,
        String descricao,
        String observacoes,
        String responsavel,
        String motivoRejeicao,
        BigDecimal valorTaxa,
        boolean taxaPaga,
        LocalDateTime dataSubmissao,
        LocalDateTime dataConclusao,
        // Power of attorney
        String outorgante,
        String outorgado,
        String finalidadeProcuracao,
        // Legalization
        String documentoOrigem,
        String paisOrigem,
        String entidadeEmissora,
        // Apostille
        String documentoApostilado,
        String paisDestino,
        // Certified copy
        String documentoOriginalRef,
        Integer numeroCopias,
        // Certificate
        String certificadoUrl,
        UUID agendamentoId,
        int documentoCount,
        Instant createdAt,
        Instant updatedAt
) {}

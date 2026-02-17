package ao.gov.embaixada.sgc.dto;

import jakarta.validation.constraints.Size;

public record ServicoNotarialUpdateRequest(
        String descricao,
        String observacoes,
        @Size(max = 100) String responsavel,
        // Power of attorney (PROCURACAO)
        @Size(max = 255) String outorgante,
        @Size(max = 255) String outorgado,
        String finalidadeProcuracao,
        // Legalization (LEGALIZACAO)
        @Size(max = 255) String documentoOrigem,
        @Size(max = 100) String paisOrigem,
        @Size(max = 255) String entidadeEmissora,
        // Apostille (APOSTILA)
        @Size(max = 255) String documentoApostilado,
        @Size(max = 100) String paisDestino,
        // Certified copy (COPIA_CERTIFICADA)
        @Size(max = 255) String documentoOriginalRef,
        Integer numeroCopias
) {}

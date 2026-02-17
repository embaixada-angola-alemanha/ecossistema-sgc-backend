package ao.gov.embaixada.sgc.dto;

import ao.gov.embaixada.sgc.enums.TipoServicoNotarial;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.UUID;

public record ServicoNotarialCreateRequest(
        @NotNull UUID cidadaoId,
        @NotNull TipoServicoNotarial tipo,
        String descricao,
        String observacoes,
        UUID agendamentoId,
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

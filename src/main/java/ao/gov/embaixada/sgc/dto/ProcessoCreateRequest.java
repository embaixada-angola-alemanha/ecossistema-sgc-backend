package ao.gov.embaixada.sgc.dto;

import ao.gov.embaixada.sgc.enums.Prioridade;
import ao.gov.embaixada.sgc.enums.TipoProcesso;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.util.UUID;

public record ProcessoCreateRequest(
        @NotNull UUID cidadaoId,
        @NotNull TipoProcesso tipo,
        String descricao,
        Prioridade prioridade,
        String responsavel,
        BigDecimal valorTaxa
) {}

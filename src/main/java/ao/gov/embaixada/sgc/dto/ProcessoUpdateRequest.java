package ao.gov.embaixada.sgc.dto;

import ao.gov.embaixada.sgc.enums.Prioridade;

import java.math.BigDecimal;

public record ProcessoUpdateRequest(
        String descricao,
        Prioridade prioridade,
        String responsavel,
        BigDecimal valorTaxa,
        Boolean taxaPaga
) {}

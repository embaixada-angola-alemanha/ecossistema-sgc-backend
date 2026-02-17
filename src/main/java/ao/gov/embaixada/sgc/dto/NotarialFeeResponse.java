package ao.gov.embaixada.sgc.dto;

import ao.gov.embaixada.sgc.enums.TipoServicoNotarial;

import java.math.BigDecimal;

public record NotarialFeeResponse(
        TipoServicoNotarial tipo,
        BigDecimal valor,
        String moeda,
        boolean isento
) {}

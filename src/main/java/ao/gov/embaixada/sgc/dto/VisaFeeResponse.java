package ao.gov.embaixada.sgc.dto;

import ao.gov.embaixada.sgc.enums.TipoVisto;

import java.math.BigDecimal;

public record VisaFeeResponse(
        TipoVisto tipo,
        BigDecimal valor,
        String moeda,
        boolean isento
) {}

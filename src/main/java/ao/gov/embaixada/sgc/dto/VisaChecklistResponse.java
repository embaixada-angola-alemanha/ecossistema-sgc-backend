package ao.gov.embaixada.sgc.dto;

import ao.gov.embaixada.sgc.enums.TipoVisto;

import java.util.List;

public record VisaChecklistResponse(
        TipoVisto tipo,
        List<String> documentosRequeridos
) {}

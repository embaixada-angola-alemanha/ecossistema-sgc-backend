package ao.gov.embaixada.sgc.dto;

import java.util.Map;

public record EstatisticasResponse(
        String modulo,
        long total,
        Map<String, Long> porEstado,
        Map<String, Long> porTipo,
        String periodo
) {}

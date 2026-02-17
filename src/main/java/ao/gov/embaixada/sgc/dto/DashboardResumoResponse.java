package ao.gov.embaixada.sgc.dto;

import java.util.Map;

public record DashboardResumoResponse(
        ModuloResumo visas,
        ModuloResumo processos,
        ModuloResumo registosCivis,
        ModuloResumo servicosNotariais,
        ModuloResumo agendamentos,
        long totalGeral
) {
    public record ModuloResumo(
            long total,
            Map<String, Long> porEstado,
            Map<String, Long> porTipo
    ) {}
}

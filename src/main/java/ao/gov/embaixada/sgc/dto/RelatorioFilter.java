package ao.gov.embaixada.sgc.dto;

import java.time.LocalDate;

public record RelatorioFilter(
        LocalDate dataInicio,
        LocalDate dataFim,
        String modulo,
        String tipo,
        String estado
) {
    public RelatorioFilter {
        if (dataInicio == null) {
            dataInicio = LocalDate.now().minusMonths(12);
        }
        if (dataFim == null) {
            dataFim = LocalDate.now();
        }
    }
}

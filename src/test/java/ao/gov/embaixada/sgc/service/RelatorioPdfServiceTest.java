package ao.gov.embaixada.sgc.service;

import ao.gov.embaixada.sgc.dto.DashboardResumoResponse;
import ao.gov.embaixada.sgc.dto.DashboardResumoResponse.ModuloResumo;
import ao.gov.embaixada.sgc.dto.EstatisticasResponse;
import ao.gov.embaixada.sgc.dto.RelatorioFilter;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RelatorioPdfServiceTest {

    @Mock
    private RelatorioService relatorioService;

    @InjectMocks
    private RelatorioPdfService pdfService;

    @Test
    void shouldGenerateDashboardPdf() {
        ModuloResumo emptyResumo = new ModuloResumo(0, Map.of(), Map.of());
        ModuloResumo visaResumo = new ModuloResumo(5, Map.of("SUBMETIDO", 3L, "APROVADO", 2L), Map.of("TURISTA", 5L));

        when(relatorioService.getDashboardResumo(any())).thenReturn(
                new DashboardResumoResponse(visaResumo, emptyResumo, emptyResumo, emptyResumo, emptyResumo, 5));

        RelatorioFilter filter = new RelatorioFilter(LocalDate.now().minusMonths(1), LocalDate.now(), null, null, null);
        byte[] pdf = pdfService.generateDashboardPdf(filter);

        assertNotNull(pdf);
        assertTrue(pdf.length > 0);
        // PDF starts with %PDF
        assertEquals('%', (char) pdf[0]);
        assertEquals('P', (char) pdf[1]);
        assertEquals('D', (char) pdf[2]);
        assertEquals('F', (char) pdf[3]);
    }

    @Test
    void shouldGenerateModuleReportPdf() {
        when(relatorioService.getEstatisticas(any())).thenReturn(
                new EstatisticasResponse("Visa", 10,
                        Map.of("SUBMETIDO", 5L, "APROVADO", 3L, "REJEITADO", 2L),
                        Map.of("TURISTA", 6L, "TRABALHO", 4L),
                        "01/01/2026 a 17/02/2026"));

        RelatorioFilter filter = new RelatorioFilter(LocalDate.now().minusMonths(1), LocalDate.now(), "Visa", null, null);
        byte[] pdf = pdfService.generateModuleReportPdf("Visa", filter);

        assertNotNull(pdf);
        assertTrue(pdf.length > 0);
        assertEquals('%', (char) pdf[0]);
    }

    @Test
    void shouldGeneratePdfWithEmptyData() {
        ModuloResumo emptyResumo = new ModuloResumo(0, Map.of(), Map.of());

        when(relatorioService.getDashboardResumo(any())).thenReturn(
                new DashboardResumoResponse(emptyResumo, emptyResumo, emptyResumo, emptyResumo, emptyResumo, 0));

        RelatorioFilter filter = new RelatorioFilter(LocalDate.now().minusMonths(1), LocalDate.now(), null, null, null);
        byte[] pdf = pdfService.generateDashboardPdf(filter);

        assertNotNull(pdf);
        assertTrue(pdf.length > 100);
    }
}

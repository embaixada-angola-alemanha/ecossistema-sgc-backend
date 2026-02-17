package ao.gov.embaixada.sgc.controller;

import ao.gov.embaixada.sgc.dto.*;
import ao.gov.embaixada.sgc.service.CsvExportService;
import ao.gov.embaixada.sgc.service.RelatorioPdfService;
import ao.gov.embaixada.sgc.service.RelatorioService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/v1/relatorios")
public class RelatorioController {

    private final RelatorioService relatorioService;
    private final CsvExportService csvExportService;
    private final RelatorioPdfService pdfService;

    public RelatorioController(RelatorioService relatorioService,
                               CsvExportService csvExportService,
                               RelatorioPdfService pdfService) {
        this.relatorioService = relatorioService;
        this.csvExportService = csvExportService;
        this.pdfService = pdfService;
    }

    @GetMapping("/dashboard")
    @PreAuthorize("hasAnyRole('ADMIN', 'CONSUL')")
    public DashboardResumoResponse getDashboard(
            @RequestParam(required = false) LocalDate dataInicio,
            @RequestParam(required = false) LocalDate dataFim) {
        return relatorioService.getDashboardResumo(
                new RelatorioFilter(dataInicio, dataFim, null, null, null));
    }

    @GetMapping("/estatisticas")
    @PreAuthorize("hasAnyRole('ADMIN', 'CONSUL')")
    public EstatisticasResponse getEstatisticas(
            @RequestParam(required = false) String modulo,
            @RequestParam(required = false) LocalDate dataInicio,
            @RequestParam(required = false) LocalDate dataFim,
            @RequestParam(required = false) String tipo,
            @RequestParam(required = false) String estado) {
        return relatorioService.getEstatisticas(
                new RelatorioFilter(dataInicio, dataFim, modulo, tipo, estado));
    }

    @GetMapping("/audit")
    @PreAuthorize("hasRole('ADMIN')")
    public Page<AuditEventResponse> getAuditEvents(
            @RequestParam(required = false) String modulo,
            @RequestParam(required = false) LocalDate dataInicio,
            @RequestParam(required = false) LocalDate dataFim,
            Pageable pageable) {
        return relatorioService.getAuditEvents(
                new RelatorioFilter(dataInicio, dataFim, modulo, null, null), pageable);
    }

    @GetMapping("/export/csv/{modulo}")
    @PreAuthorize("hasAnyRole('ADMIN', 'CONSUL')")
    public ResponseEntity<StreamingResponseBody> exportCsv(
            @PathVariable String modulo,
            @RequestParam(required = false) LocalDate dataInicio,
            @RequestParam(required = false) LocalDate dataFim) {
        RelatorioFilter filter = new RelatorioFilter(dataInicio, dataFim, modulo, null, null);
        String filename = "relatorio_" + modulo.toLowerCase() + "_"
                + LocalDate.now() + ".csv";

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .contentType(MediaType.parseMediaType("text/csv; charset=UTF-8"))
                .body(csvExportService.export(modulo, filter));
    }

    @GetMapping("/export/pdf/dashboard")
    @PreAuthorize("hasAnyRole('ADMIN', 'CONSUL')")
    public ResponseEntity<byte[]> exportDashboardPdf(
            @RequestParam(required = false) LocalDate dataInicio,
            @RequestParam(required = false) LocalDate dataFim) {
        RelatorioFilter filter = new RelatorioFilter(dataInicio, dataFim, null, null, null);
        byte[] pdf = pdfService.generateDashboardPdf(filter);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"dashboard_" + LocalDate.now() + ".pdf\"")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdf);
    }

    @GetMapping("/export/pdf/{modulo}")
    @PreAuthorize("hasAnyRole('ADMIN', 'CONSUL')")
    public ResponseEntity<byte[]> exportModulePdf(
            @PathVariable String modulo,
            @RequestParam(required = false) LocalDate dataInicio,
            @RequestParam(required = false) LocalDate dataFim) {
        RelatorioFilter filter = new RelatorioFilter(dataInicio, dataFim, modulo, null, null);
        byte[] pdf = pdfService.generateModuleReportPdf(modulo, filter);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"relatorio_" + modulo.toLowerCase() + "_" + LocalDate.now() + ".pdf\"")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdf);
    }
}

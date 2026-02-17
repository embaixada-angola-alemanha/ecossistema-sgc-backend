package ao.gov.embaixada.sgc.service;

import ao.gov.embaixada.sgc.dto.DashboardResumoResponse;
import ao.gov.embaixada.sgc.dto.DashboardResumoResponse.ModuloResumo;
import ao.gov.embaixada.sgc.dto.EstatisticasResponse;
import ao.gov.embaixada.sgc.dto.RelatorioFilter;
import com.lowagie.text.*;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import org.springframework.stereotype.Service;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Map;

@Service
public class RelatorioPdfService {

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final Color EMBASSY_BLUE = new Color(0, 51, 102);
    private static final Font TITLE_FONT = new Font(Font.HELVETICA, 16, Font.BOLD, EMBASSY_BLUE);
    private static final Font SUBTITLE_FONT = new Font(Font.HELVETICA, 12, Font.NORMAL, EMBASSY_BLUE);
    private static final Font SECTION_FONT = new Font(Font.HELVETICA, 12, Font.BOLD);
    private static final Font LABEL_FONT = new Font(Font.HELVETICA, 10, Font.BOLD);
    private static final Font VALUE_FONT = new Font(Font.HELVETICA, 10, Font.NORMAL);
    private static final Font FOOTER_FONT = new Font(Font.HELVETICA, 9, Font.ITALIC, Color.GRAY);

    private final RelatorioService relatorioService;

    public RelatorioPdfService(RelatorioService relatorioService) {
        this.relatorioService = relatorioService;
    }

    public byte[] generateDashboardPdf(RelatorioFilter filter) {
        DashboardResumoResponse resumo = relatorioService.getDashboardResumo(filter);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        Document document = new Document(PageSize.A4, 50, 50, 50, 50);

        try {
            PdfWriter.getInstance(document, baos);
            document.open();

            addHeader(document);
            addReportTitle(document, "RELATORIO GERAL — DASHBOARD",
                    filter.dataInicio(), filter.dataFim());

            addModuloSection(document, "Vistos", resumo.visas());
            addModuloSection(document, "Processos", resumo.processos());
            addModuloSection(document, "Registo Civil", resumo.registosCivis());
            addModuloSection(document, "Servicos Notariais", resumo.servicosNotariais());
            addModuloSection(document, "Agendamentos", resumo.agendamentos());

            addSummaryRow(document, "TOTAL GERAL", resumo.totalGeral());
            addFooter(document);

            document.close();
        } catch (DocumentException e) {
            throw new RuntimeException("Erro ao gerar relatorio PDF", e);
        }

        return baos.toByteArray();
    }

    public byte[] generateModuleReportPdf(String modulo, RelatorioFilter filter) {
        EstatisticasResponse stats = relatorioService.getEstatisticas(
                new RelatorioFilter(filter.dataInicio(), filter.dataFim(), modulo, null, null));
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        Document document = new Document(PageSize.A4, 50, 50, 50, 50);

        try {
            PdfWriter.getInstance(document, baos);
            document.open();

            addHeader(document);
            addReportTitle(document, "RELATORIO — " + modulo.toUpperCase(),
                    filter.dataInicio(), filter.dataFim());

            addSummaryRow(document, "Total de Registos", stats.total());

            if (!stats.porEstado().isEmpty()) {
                addMapSection(document, "Por Estado", stats.porEstado());
            }
            if (!stats.porTipo().isEmpty()) {
                addMapSection(document, "Por Tipo", stats.porTipo());
            }

            addFooter(document);
            document.close();
        } catch (DocumentException e) {
            throw new RuntimeException("Erro ao gerar relatorio PDF", e);
        }

        return baos.toByteArray();
    }

    private void addHeader(Document document) throws DocumentException {
        Paragraph embassy = new Paragraph("REPUBLICA DE ANGOLA", TITLE_FONT);
        embassy.setAlignment(Element.ALIGN_CENTER);
        document.add(embassy);

        Paragraph sub = new Paragraph("Embaixada da Republica de Angola na Alemanha", SUBTITLE_FONT);
        sub.setAlignment(Element.ALIGN_CENTER);
        document.add(sub);

        Paragraph consular = new Paragraph("Seccao Consular", SUBTITLE_FONT);
        consular.setAlignment(Element.ALIGN_CENTER);
        document.add(consular);

        document.add(new Paragraph(" "));
    }

    private void addReportTitle(Document document, String title,
                                 LocalDate dataInicio, LocalDate dataFim) throws DocumentException {
        Paragraph reportTitle = new Paragraph(title, SECTION_FONT);
        reportTitle.setAlignment(Element.ALIGN_CENTER);
        document.add(reportTitle);

        Paragraph periodo = new Paragraph(
                "Periodo: " + dataInicio.format(DATE_FMT) + " a " + dataFim.format(DATE_FMT),
                VALUE_FONT);
        periodo.setAlignment(Element.ALIGN_CENTER);
        document.add(periodo);

        document.add(new Paragraph(" "));
    }

    private void addModuloSection(Document document, String name, ModuloResumo resumo) throws DocumentException {
        Paragraph sectionTitle = new Paragraph(name + " (Total: " + resumo.total() + ")", SECTION_FONT);
        document.add(sectionTitle);

        if (!resumo.porEstado().isEmpty()) {
            PdfPTable table = createTable();
            for (Map.Entry<String, Long> entry : resumo.porEstado().entrySet()) {
                addRow(table, entry.getKey(), String.valueOf(entry.getValue()));
            }
            document.add(table);
        }

        document.add(new Paragraph(" "));
    }

    private void addMapSection(Document document, String title, Map<String, Long> data) throws DocumentException {
        Paragraph sectionTitle = new Paragraph(title, SECTION_FONT);
        document.add(sectionTitle);

        PdfPTable table = createTable();
        for (Map.Entry<String, Long> entry : data.entrySet()) {
            addRow(table, entry.getKey(), String.valueOf(entry.getValue()));
        }
        document.add(table);
        document.add(new Paragraph(" "));
    }

    private void addSummaryRow(Document document, String label, long value) throws DocumentException {
        PdfPTable table = createTable();
        addRow(table, label, String.valueOf(value));
        document.add(table);
        document.add(new Paragraph(" "));
    }

    private void addFooter(Document document) throws DocumentException {
        document.add(new Paragraph(" "));

        Paragraph generated = new Paragraph(
                "Relatorio gerado em " + LocalDate.now().format(DATE_FMT),
                FOOTER_FONT);
        generated.setAlignment(Element.ALIGN_CENTER);
        document.add(generated);

        Paragraph footer = new Paragraph(
                "Documento gerado electronicamente pela Seccao Consular da Embaixada de Angola em Berlim.",
                FOOTER_FONT);
        footer.setAlignment(Element.ALIGN_CENTER);
        document.add(footer);
    }

    private PdfPTable createTable() {
        PdfPTable table = new PdfPTable(2);
        table.setWidthPercentage(100);
        try {
            table.setWidths(new float[]{50f, 50f});
        } catch (DocumentException e) {
            throw new RuntimeException(e);
        }
        return table;
    }

    private void addRow(PdfPTable table, String label, String value) {
        PdfPCell labelCell = new PdfPCell(new Phrase(label, LABEL_FONT));
        labelCell.setBorderColor(Color.LIGHT_GRAY);
        labelCell.setPadding(6);
        labelCell.setBackgroundColor(new Color(240, 240, 240));

        PdfPCell valueCell = new PdfPCell(new Phrase(value, VALUE_FONT));
        valueCell.setBorderColor(Color.LIGHT_GRAY);
        valueCell.setPadding(6);
        valueCell.setHorizontalAlignment(Element.ALIGN_RIGHT);

        table.addCell(labelCell);
        table.addCell(valueCell);
    }
}

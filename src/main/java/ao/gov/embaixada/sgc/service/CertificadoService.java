package ao.gov.embaixada.sgc.service;

import ao.gov.embaixada.commons.storage.StorageService;
import ao.gov.embaixada.sgc.entity.RegistoCivil;
import ao.gov.embaixada.sgc.enums.TipoRegistoCivil;
import com.lowagie.text.*;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.awt.Color;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.time.format.DateTimeFormatter;

@Service
public class CertificadoService {

    private static final Logger log = LoggerFactory.getLogger(CertificadoService.class);
    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private final StorageService storageService;

    public CertificadoService(StorageService storageService) {
        this.storageService = storageService;
    }

    public String generateAndStore(RegistoCivil registo) {
        byte[] pdfBytes = generatePdf(registo);
        String objectKey = buildObjectKey(registo);

        storageService.upload(
                storageService.getDefaultBucket(),
                objectKey,
                new ByteArrayInputStream(pdfBytes),
                pdfBytes.length,
                "application/pdf"
        );

        log.info("Certificate PDF generated and stored: {}", objectKey);
        return objectKey;
    }

    public byte[] generatePdf(RegistoCivil registo) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        Document document = new Document(PageSize.A4, 50, 50, 50, 50);

        try {
            PdfWriter.getInstance(document, baos);
            document.open();

            addHeader(document);

            switch (registo.getTipo()) {
                case NASCIMENTO -> addNascimentoContent(document, registo);
                case CASAMENTO -> addCasamentoContent(document, registo);
                case OBITO -> addObitoContent(document, registo);
            }

            addFooter(document, registo);

            document.close();
        } catch (DocumentException e) {
            throw new RuntimeException("Erro ao gerar certificado PDF", e);
        }

        return baos.toByteArray();
    }

    private void addHeader(Document document) throws DocumentException {
        Font titleFont = new Font(Font.HELVETICA, 16, Font.BOLD, new Color(0, 51, 102));
        Font subtitleFont = new Font(Font.HELVETICA, 12, Font.NORMAL, new Color(0, 51, 102));

        Paragraph embassy = new Paragraph("REPUBLICA DE ANGOLA", titleFont);
        embassy.setAlignment(Element.ALIGN_CENTER);
        document.add(embassy);

        Paragraph sub = new Paragraph("Embaixada da Republica de Angola na Alemanha", subtitleFont);
        sub.setAlignment(Element.ALIGN_CENTER);
        document.add(sub);

        Paragraph consular = new Paragraph("Seccao Consular", subtitleFont);
        consular.setAlignment(Element.ALIGN_CENTER);
        document.add(consular);

        document.add(new Paragraph(" "));
        document.add(new Paragraph(" "));
    }

    private void addNascimentoContent(Document document, RegistoCivil registo) throws DocumentException {
        Font certTitleFont = new Font(Font.HELVETICA, 14, Font.BOLD);

        Paragraph title = new Paragraph("CERTIDAO DE NASCIMENTO", certTitleFont);
        title.setAlignment(Element.ALIGN_CENTER);
        document.add(title);
        document.add(new Paragraph(" "));

        PdfPTable table = createTable();
        addRow(table, "N.o de Registo", registo.getNumeroRegisto());
        addRow(table, "Nome", registo.getCidadao() != null ? registo.getCidadao().getNomeCompleto() : "-");
        addRow(table, "Data de Nascimento", registo.getDataEvento() != null ? registo.getDataEvento().format(DATE_FMT) : "-");
        addRow(table, "Local de Nascimento", nvl(registo.getLocalNascimento()));
        addRow(table, "Nome do Pai", nvl(registo.getNomePai()));
        addRow(table, "Nome da Mae", nvl(registo.getNomeMae()));
        document.add(table);
    }

    private void addCasamentoContent(Document document, RegistoCivil registo) throws DocumentException {
        Font certTitleFont = new Font(Font.HELVETICA, 14, Font.BOLD);

        Paragraph title = new Paragraph("CERTIDAO DE CASAMENTO", certTitleFont);
        title.setAlignment(Element.ALIGN_CENTER);
        document.add(title);
        document.add(new Paragraph(" "));

        PdfPTable table = createTable();
        addRow(table, "N.o de Registo", registo.getNumeroRegisto());
        addRow(table, "Conjuge 1", nvl(registo.getNomeConjuge1()));
        addRow(table, "Conjuge 2", nvl(registo.getNomeConjuge2()));
        addRow(table, "Data do Casamento", registo.getDataEvento() != null ? registo.getDataEvento().format(DATE_FMT) : "-");
        addRow(table, "Local do Casamento", nvl(registo.getLocalEvento()));
        addRow(table, "Regime de Casamento", nvl(registo.getRegimeCasamento()));
        document.add(table);
    }

    private void addObitoContent(Document document, RegistoCivil registo) throws DocumentException {
        Font certTitleFont = new Font(Font.HELVETICA, 14, Font.BOLD);

        Paragraph title = new Paragraph("CERTIDAO DE OBITO", certTitleFont);
        title.setAlignment(Element.ALIGN_CENTER);
        document.add(title);
        document.add(new Paragraph(" "));

        PdfPTable table = createTable();
        addRow(table, "N.o de Registo", registo.getNumeroRegisto());
        addRow(table, "Nome", registo.getCidadao() != null ? registo.getCidadao().getNomeCompleto() : "-");
        addRow(table, "Data de Obito", registo.getDataObito() != null ? registo.getDataObito().format(DATE_FMT) : "-");
        addRow(table, "Local de Obito", nvl(registo.getLocalObito()));
        addRow(table, "Causa de Obito", nvl(registo.getCausaObito()));
        document.add(table);
    }

    private void addFooter(Document document, RegistoCivil registo) throws DocumentException {
        Font footerFont = new Font(Font.HELVETICA, 9, Font.ITALIC, Color.GRAY);

        document.add(new Paragraph(" "));
        document.add(new Paragraph(" "));

        Paragraph obs = new Paragraph(
                "Observacoes: " + nvl(registo.getObservacoes()), footerFont);
        document.add(obs);

        document.add(new Paragraph(" "));

        Paragraph footer = new Paragraph(
                "Documento emitido electronicamente pela Seccao Consular da Embaixada de Angola em Berlim.",
                footerFont);
        footer.setAlignment(Element.ALIGN_CENTER);
        document.add(footer);
    }

    private PdfPTable createTable() {
        PdfPTable table = new PdfPTable(2);
        table.setWidthPercentage(100);
        try {
            table.setWidths(new float[]{35f, 65f});
        } catch (DocumentException e) {
            throw new RuntimeException(e);
        }
        return table;
    }

    private void addRow(PdfPTable table, String label, String value) {
        Font labelFont = new Font(Font.HELVETICA, 10, Font.BOLD);
        Font valueFont = new Font(Font.HELVETICA, 10, Font.NORMAL);

        PdfPCell labelCell = new PdfPCell(new Phrase(label, labelFont));
        labelCell.setBorderColor(Color.LIGHT_GRAY);
        labelCell.setPadding(8);
        labelCell.setBackgroundColor(new Color(240, 240, 240));

        PdfPCell valueCell = new PdfPCell(new Phrase(value, valueFont));
        valueCell.setBorderColor(Color.LIGHT_GRAY);
        valueCell.setPadding(8);

        table.addCell(labelCell);
        table.addCell(valueCell);
    }

    private String buildObjectKey(RegistoCivil registo) {
        String prefix = switch (registo.getTipo()) {
            case NASCIMENTO -> "nascimento";
            case CASAMENTO -> "casamento";
            case OBITO -> "obito";
        };
        return "certificados/" + prefix + "/" + registo.getId() + "/" + registo.getNumeroRegisto() + ".pdf";
    }

    private String nvl(String value) {
        return value != null ? value : "-";
    }
}

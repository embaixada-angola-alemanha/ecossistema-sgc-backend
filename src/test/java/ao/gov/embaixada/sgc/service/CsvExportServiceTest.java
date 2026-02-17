package ao.gov.embaixada.sgc.service;

import ao.gov.embaixada.sgc.dto.RelatorioFilter;
import ao.gov.embaixada.sgc.repository.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CsvExportServiceTest {

    @Mock
    private VisaRepository visaRepository;
    @Mock
    private ProcessoRepository processoRepository;
    @Mock
    private RegistoCivilRepository registoCivilRepository;
    @Mock
    private ServicoNotarialRepository servicoNotarialRepository;
    @Mock
    private AgendamentoRepository agendamentoRepository;
    @Mock
    private AuditEventRepository auditEventRepository;

    @InjectMocks
    private CsvExportService csvExportService;

    @Test
    void shouldProduceUtf8BomAndSemicolonCsv() throws Exception {
        when(visaRepository.findAll()).thenReturn(Collections.emptyList());

        RelatorioFilter filter = new RelatorioFilter(LocalDate.now().minusMonths(1), LocalDate.now(), null, null, null);
        StreamingResponseBody body = csvExportService.export("Visa", filter);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        body.writeTo(baos);
        byte[] output = baos.toByteArray();

        // Check UTF-8 BOM
        assertEquals((byte) 0xEF, output[0]);
        assertEquals((byte) 0xBB, output[1]);
        assertEquals((byte) 0xBF, output[2]);

        // Check header line uses semicolons
        String content = new String(output, 3, output.length - 3, StandardCharsets.UTF_8);
        assertTrue(content.contains(";"));
        assertTrue(content.startsWith("Numero;Tipo;Estado;"));
    }

    @Test
    void shouldExportVisaHeaders() throws Exception {
        when(visaRepository.findAll()).thenReturn(Collections.emptyList());

        RelatorioFilter filter = new RelatorioFilter(LocalDate.now().minusMonths(1), LocalDate.now(), null, null, null);
        StreamingResponseBody body = csvExportService.export("Visa", filter);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        body.writeTo(baos);
        String content = new String(baos.toByteArray(), StandardCharsets.UTF_8);

        assertTrue(content.contains("Numero"));
        assertTrue(content.contains("Tipo"));
        assertTrue(content.contains("Estado"));
        assertTrue(content.contains("Cidadao"));
        assertTrue(content.contains("Data Submissao"));
    }

    @Test
    void shouldExportProcessoHeaders() throws Exception {
        when(processoRepository.findAll()).thenReturn(Collections.emptyList());

        RelatorioFilter filter = new RelatorioFilter(LocalDate.now().minusMonths(1), LocalDate.now(), null, null, null);
        StreamingResponseBody body = csvExportService.export("Processo", filter);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        body.writeTo(baos);
        String content = new String(baos.toByteArray(), StandardCharsets.UTF_8);

        assertTrue(content.contains("Responsavel"));
    }

    @Test
    void shouldExportAgendamentoHeaders() throws Exception {
        when(agendamentoRepository.findAll()).thenReturn(Collections.emptyList());

        RelatorioFilter filter = new RelatorioFilter(LocalDate.now().minusMonths(1), LocalDate.now(), null, null, null);
        StreamingResponseBody body = csvExportService.export("Agendamento", filter);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        body.writeTo(baos);
        String content = new String(baos.toByteArray(), StandardCharsets.UTF_8);

        assertTrue(content.contains("Local"));
        assertTrue(content.contains("Data/Hora"));
    }

    @Test
    void shouldHandleUnknownModule() throws Exception {
        RelatorioFilter filter = new RelatorioFilter(LocalDate.now().minusMonths(1), LocalDate.now(), null, null, null);
        StreamingResponseBody body = csvExportService.export("Unknown", filter);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        body.writeTo(baos);
        String content = new String(baos.toByteArray(), StandardCharsets.UTF_8);

        assertTrue(content.contains("Modulo desconhecido"));
    }
}

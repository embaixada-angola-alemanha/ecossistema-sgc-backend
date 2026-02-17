package ao.gov.embaixada.sgc.service;

import ao.gov.embaixada.sgc.dto.RelatorioFilter;
import ao.gov.embaixada.sgc.entity.AuditEventEntity;
import ao.gov.embaixada.sgc.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;

@Service
@Transactional(readOnly = true)
public class CsvExportService {

    private static final String SEPARATOR = ";";
    private static final byte[] UTF8_BOM = {(byte) 0xEF, (byte) 0xBB, (byte) 0xBF};
    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")
            .withZone(ZoneOffset.UTC);

    private final VisaRepository visaRepository;
    private final ProcessoRepository processoRepository;
    private final RegistoCivilRepository registoCivilRepository;
    private final ServicoNotarialRepository servicoNotarialRepository;
    private final AgendamentoRepository agendamentoRepository;
    private final AuditEventRepository auditEventRepository;

    public CsvExportService(VisaRepository visaRepository,
                            ProcessoRepository processoRepository,
                            RegistoCivilRepository registoCivilRepository,
                            ServicoNotarialRepository servicoNotarialRepository,
                            AgendamentoRepository agendamentoRepository,
                            AuditEventRepository auditEventRepository) {
        this.visaRepository = visaRepository;
        this.processoRepository = processoRepository;
        this.registoCivilRepository = registoCivilRepository;
        this.servicoNotarialRepository = servicoNotarialRepository;
        this.agendamentoRepository = agendamentoRepository;
        this.auditEventRepository = auditEventRepository;
    }

    public StreamingResponseBody export(String modulo, RelatorioFilter filter) {
        return outputStream -> {
            outputStream.write(UTF8_BOM);
            PrintWriter writer = new PrintWriter(new OutputStreamWriter(outputStream, StandardCharsets.UTF_8));

            switch (modulo) {
                case "Visa" -> exportVisas(writer);
                case "Processo" -> exportProcessos(writer);
                case "RegistoCivil" -> exportRegistosCivis(writer);
                case "ServicoNotarial" -> exportServicosNotariais(writer);
                case "Agendamento" -> exportAgendamentos(writer);
                case "Audit" -> exportAuditEvents(writer, filter);
                default -> {
                    writer.println("Modulo desconhecido: " + modulo);
                }
            }

            writer.flush();
        };
    }

    private void exportVisas(PrintWriter writer) {
        writer.println(String.join(SEPARATOR,
                "Numero", "Tipo", "Estado", "Cidadao", "Data Submissao", "Data Decisao", "Criado Em"));

        visaRepository.findAll().forEach(v -> {
            writer.println(String.join(SEPARATOR,
                    nvl(v.getNumeroVisto()),
                    nvl(v.getTipo()),
                    nvl(v.getEstado()),
                    v.getCidadao() != null ? nvl(v.getCidadao().getNomeCompleto()) : "-",
                    v.getDataSubmissao() != null ? v.getDataSubmissao().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) : "-",
                    v.getDataDecisao() != null ? v.getDataDecisao().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) : "-",
                    formatInstant(v.getCreatedAt())
            ));
        });
    }

    private void exportProcessos(PrintWriter writer) {
        writer.println(String.join(SEPARATOR,
                "Numero", "Tipo", "Estado", "Cidadao", "Responsavel", "Criado Em"));

        processoRepository.findAll().forEach(p -> {
            writer.println(String.join(SEPARATOR,
                    nvl(p.getNumeroProcesso()),
                    nvl(p.getTipo()),
                    nvl(p.getEstado()),
                    p.getCidadao() != null ? nvl(p.getCidadao().getNomeCompleto()) : "-",
                    nvl(p.getResponsavel()),
                    formatInstant(p.getCreatedAt())
            ));
        });
    }

    private void exportRegistosCivis(PrintWriter writer) {
        writer.println(String.join(SEPARATOR,
                "Numero", "Tipo", "Estado", "Cidadao", "Data Evento", "Criado Em"));

        registoCivilRepository.findAll().forEach(r -> {
            writer.println(String.join(SEPARATOR,
                    nvl(r.getNumeroRegisto()),
                    nvl(r.getTipo()),
                    nvl(r.getEstado()),
                    r.getCidadao() != null ? nvl(r.getCidadao().getNomeCompleto()) : "-",
                    r.getDataEvento() != null ? r.getDataEvento().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) : "-",
                    formatInstant(r.getCreatedAt())
            ));
        });
    }

    private void exportServicosNotariais(PrintWriter writer) {
        writer.println(String.join(SEPARATOR,
                "Numero", "Tipo", "Estado", "Cidadao", "Criado Em"));

        servicoNotarialRepository.findAll().forEach(s -> {
            writer.println(String.join(SEPARATOR,
                    nvl(s.getNumeroServico()),
                    nvl(s.getTipo()),
                    nvl(s.getEstado()),
                    s.getCidadao() != null ? nvl(s.getCidadao().getNomeCompleto()) : "-",
                    formatInstant(s.getCreatedAt())
            ));
        });
    }

    private void exportAgendamentos(PrintWriter writer) {
        writer.println(String.join(SEPARATOR,
                "Numero", "Tipo", "Estado", "Cidadao", "Data/Hora", "Local", "Criado Em"));

        agendamentoRepository.findAll().forEach(a -> {
            writer.println(String.join(SEPARATOR,
                    nvl(a.getNumeroAgendamento()),
                    nvl(a.getTipo()),
                    nvl(a.getEstado()),
                    a.getCidadao() != null ? nvl(a.getCidadao().getNomeCompleto()) : "-",
                    a.getDataHora() != null ? a.getDataHora().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")) : "-",
                    nvl(a.getLocal()),
                    formatInstant(a.getCreatedAt())
            ));
        });
    }

    private void exportAuditEvents(PrintWriter writer, RelatorioFilter filter) {
        Instant start = toInstant(filter.dataInicio());
        Instant end = toInstant(filter.dataFim().plusDays(1));

        writer.println(String.join(SEPARATOR,
                "Accao", "Tipo Entidade", "ID Entidade", "Utilizador", "Detalhes", "Timestamp"));

        auditEventRepository.findByTimestampBetween(start, end,
                org.springframework.data.domain.Pageable.unpaged()).forEach(e -> {
            writer.println(String.join(SEPARATOR,
                    nvl(e.getAction()),
                    nvl(e.getEntityType()),
                    nvl(e.getEntityId()),
                    nvl(e.getUsername()),
                    nvl(e.getDetails()),
                    formatInstant(e.getTimestamp())
            ));
        });
    }

    private String nvl(Object value) {
        return value != null ? value.toString().replace(";", ",") : "-";
    }

    private String formatInstant(Instant instant) {
        return instant != null ? FMT.format(instant) : "-";
    }

    private Instant toInstant(LocalDate date) {
        return date.atStartOfDay(ZoneOffset.UTC).toInstant();
    }
}

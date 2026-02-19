package ao.gov.embaixada.sgc.service;

import ao.gov.embaixada.sgc.dto.*;
import ao.gov.embaixada.sgc.dto.DashboardResumoResponse.ModuloResumo;
import ao.gov.embaixada.sgc.entity.AuditEventEntity;
import ao.gov.embaixada.sgc.enums.*;
import ao.gov.embaixada.sgc.repository.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class RelatorioService {

    private final VisaRepository visaRepository;
    private final ProcessoRepository processoRepository;
    private final RegistoCivilRepository registoCivilRepository;
    private final ServicoNotarialRepository servicoNotarialRepository;
    private final AgendamentoRepository agendamentoRepository;
    private final AuditEventRepository auditEventRepository;

    public RelatorioService(VisaRepository visaRepository,
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

    public DashboardResumoResponse getCitizenDashboard(UUID cidadaoId) {
        long totalVisas = visaRepository.countByCidadaoId(cidadaoId);
        long pendingVisas = visaRepository.countByCidadaoIdAndEstado(cidadaoId, EstadoVisto.SUBMETIDO)
                + visaRepository.countByCidadaoIdAndEstado(cidadaoId, EstadoVisto.EM_ANALISE);
        Map<String, Long> visaPorEstado = new LinkedHashMap<>();
        if (pendingVisas > 0) visaPorEstado.put("PENDENTES", pendingVisas);
        ModuloResumo visas = new ModuloResumo(totalVisas, visaPorEstado, Map.of());

        long totalAgendamentos = agendamentoRepository.countByCidadaoId(cidadaoId);
        long pendingAgendamentos = agendamentoRepository.countByCidadaoIdAndEstado(cidadaoId, EstadoAgendamento.PENDENTE)
                + agendamentoRepository.countByCidadaoIdAndEstado(cidadaoId, EstadoAgendamento.CONFIRMADO);
        Map<String, Long> agendPorEstado = new LinkedHashMap<>();
        if (pendingAgendamentos > 0) agendPorEstado.put("PENDENTES", pendingAgendamentos);
        ModuloResumo agendamentos = new ModuloResumo(totalAgendamentos, agendPorEstado, Map.of());

        long totalRC = registoCivilRepository.countByCidadaoId(cidadaoId);
        ModuloResumo registosCivis = new ModuloResumo(totalRC, Map.of(), Map.of());

        long totalSN = servicoNotarialRepository.countByCidadaoId(cidadaoId);
        ModuloResumo servicosNotariais = new ModuloResumo(totalSN, Map.of(), Map.of());

        long totalProcessos = processoRepository.countByCidadaoId(cidadaoId);
        ModuloResumo processos = new ModuloResumo(totalProcessos, Map.of(), Map.of());

        long totalGeral = totalVisas + totalAgendamentos + totalRC + totalSN + totalProcessos;

        return new DashboardResumoResponse(visas, processos, registosCivis,
                servicosNotariais, agendamentos, totalGeral);
    }

    public DashboardResumoResponse getDashboardResumo(RelatorioFilter filter) {
        Instant start = toInstant(filter.dataInicio());
        Instant end = toInstant(filter.dataFim().plusDays(1));

        ModuloResumo visas = buildVisaResumo(start, end);
        ModuloResumo processos = buildProcessoResumo(start, end);
        ModuloResumo registosCivis = buildRegistoCivilResumo(start, end);
        ModuloResumo servicosNotariais = buildServicoNotarialResumo(start, end);
        ModuloResumo agendamentos = buildAgendamentoResumo(start, end);

        long totalGeral = visas.total() + processos.total() + registosCivis.total()
                + servicosNotariais.total() + agendamentos.total();

        return new DashboardResumoResponse(visas, processos, registosCivis,
                servicosNotariais, agendamentos, totalGeral);
    }

    public EstatisticasResponse getEstatisticas(RelatorioFilter filter) {
        Instant start = toInstant(filter.dataInicio());
        Instant end = toInstant(filter.dataFim().plusDays(1));
        String modulo = filter.modulo() != null ? filter.modulo() : "Todos";
        String periodo = filter.dataInicio() + " a " + filter.dataFim();

        Map<String, Long> porEstado = new LinkedHashMap<>();
        Map<String, Long> porTipo = new LinkedHashMap<>();
        long total = 0;

        switch (modulo) {
            case "Visa" -> {
                for (EstadoVisto e : EstadoVisto.values()) {
                    long count = visaRepository.countByEstadoAndCreatedAtBetween(e, start, end);
                    if (count > 0) porEstado.put(e.name(), count);
                }
                for (TipoVisto t : TipoVisto.values()) {
                    long count = visaRepository.countByTipoAndCreatedAtBetween(t, start, end);
                    if (count > 0) porTipo.put(t.name(), count);
                }
                total = visaRepository.countByCreatedAtBetween(start, end);
            }
            case "Processo" -> {
                for (EstadoProcesso e : EstadoProcesso.values()) {
                    long count = processoRepository.countByEstadoAndCreatedAtBetween(e, start, end);
                    if (count > 0) porEstado.put(e.name(), count);
                }
                for (TipoProcesso t : TipoProcesso.values()) {
                    long count = processoRepository.countByTipoAndCreatedAtBetween(t, start, end);
                    if (count > 0) porTipo.put(t.name(), count);
                }
                total = processoRepository.countByCreatedAtBetween(start, end);
            }
            case "RegistoCivil" -> {
                for (EstadoRegistoCivil e : EstadoRegistoCivil.values()) {
                    long count = registoCivilRepository.countByEstadoAndCreatedAtBetween(e, start, end);
                    if (count > 0) porEstado.put(e.name(), count);
                }
                for (TipoRegistoCivil t : TipoRegistoCivil.values()) {
                    long count = registoCivilRepository.countByTipoAndCreatedAtBetween(t, start, end);
                    if (count > 0) porTipo.put(t.name(), count);
                }
                total = registoCivilRepository.countByCreatedAtBetween(start, end);
            }
            case "ServicoNotarial" -> {
                for (EstadoServicoNotarial e : EstadoServicoNotarial.values()) {
                    long count = servicoNotarialRepository.countByEstadoAndCreatedAtBetween(e, start, end);
                    if (count > 0) porEstado.put(e.name(), count);
                }
                for (TipoServicoNotarial t : TipoServicoNotarial.values()) {
                    long count = servicoNotarialRepository.countByTipoAndCreatedAtBetween(t, start, end);
                    if (count > 0) porTipo.put(t.name(), count);
                }
                total = servicoNotarialRepository.countByCreatedAtBetween(start, end);
            }
            case "Agendamento" -> {
                for (EstadoAgendamento e : EstadoAgendamento.values()) {
                    long count = agendamentoRepository.countByEstadoAndCreatedAtBetween(e, start, end);
                    if (count > 0) porEstado.put(e.name(), count);
                }
                for (TipoAgendamento t : TipoAgendamento.values()) {
                    long count = agendamentoRepository.countByTipoAndCreatedAtBetween(t, start, end);
                    if (count > 0) porTipo.put(t.name(), count);
                }
                total = agendamentoRepository.countByCreatedAtBetween(start, end);
            }
            default -> {
                total = visaRepository.countByCreatedAtBetween(start, end)
                        + processoRepository.countByCreatedAtBetween(start, end)
                        + registoCivilRepository.countByCreatedAtBetween(start, end)
                        + servicoNotarialRepository.countByCreatedAtBetween(start, end)
                        + agendamentoRepository.countByCreatedAtBetween(start, end);
            }
        }

        return new EstatisticasResponse(modulo, total, porEstado, porTipo, periodo);
    }

    public Page<AuditEventResponse> getAuditEvents(RelatorioFilter filter, Pageable pageable) {
        Instant start = toInstant(filter.dataInicio());
        Instant end = toInstant(filter.dataFim().plusDays(1));

        Page<AuditEventEntity> page;
        if (filter.modulo() != null && !filter.modulo().isBlank()) {
            page = auditEventRepository.findByEntityTypeAndTimestampBetween(
                    filter.modulo(), start, end, pageable);
        } else {
            page = auditEventRepository.findByTimestampBetween(start, end, pageable);
        }

        return page.map(this::toAuditResponse);
    }

    private AuditEventResponse toAuditResponse(AuditEventEntity entity) {
        return new AuditEventResponse(
                entity.getId(),
                entity.getAction(),
                entity.getEntityType(),
                entity.getEntityId(),
                entity.getUserId(),
                entity.getUsername(),
                entity.getDetails(),
                entity.getIpAddress(),
                entity.getTimestamp()
        );
    }

    private ModuloResumo buildVisaResumo(Instant start, Instant end) {
        Map<String, Long> porEstado = new LinkedHashMap<>();
        Map<String, Long> porTipo = new LinkedHashMap<>();
        for (EstadoVisto e : EstadoVisto.values()) {
            long count = visaRepository.countByEstadoAndCreatedAtBetween(e, start, end);
            if (count > 0) porEstado.put(e.name(), count);
        }
        for (TipoVisto t : TipoVisto.values()) {
            long count = visaRepository.countByTipoAndCreatedAtBetween(t, start, end);
            if (count > 0) porTipo.put(t.name(), count);
        }
        long total = visaRepository.countByCreatedAtBetween(start, end);
        return new ModuloResumo(total, porEstado, porTipo);
    }

    private ModuloResumo buildProcessoResumo(Instant start, Instant end) {
        Map<String, Long> porEstado = new LinkedHashMap<>();
        Map<String, Long> porTipo = new LinkedHashMap<>();
        for (EstadoProcesso e : EstadoProcesso.values()) {
            long count = processoRepository.countByEstadoAndCreatedAtBetween(e, start, end);
            if (count > 0) porEstado.put(e.name(), count);
        }
        for (TipoProcesso t : TipoProcesso.values()) {
            long count = processoRepository.countByTipoAndCreatedAtBetween(t, start, end);
            if (count > 0) porTipo.put(t.name(), count);
        }
        long total = processoRepository.countByCreatedAtBetween(start, end);
        return new ModuloResumo(total, porEstado, porTipo);
    }

    private ModuloResumo buildRegistoCivilResumo(Instant start, Instant end) {
        Map<String, Long> porEstado = new LinkedHashMap<>();
        Map<String, Long> porTipo = new LinkedHashMap<>();
        for (EstadoRegistoCivil e : EstadoRegistoCivil.values()) {
            long count = registoCivilRepository.countByEstadoAndCreatedAtBetween(e, start, end);
            if (count > 0) porEstado.put(e.name(), count);
        }
        for (TipoRegistoCivil t : TipoRegistoCivil.values()) {
            long count = registoCivilRepository.countByTipoAndCreatedAtBetween(t, start, end);
            if (count > 0) porTipo.put(t.name(), count);
        }
        long total = registoCivilRepository.countByCreatedAtBetween(start, end);
        return new ModuloResumo(total, porEstado, porTipo);
    }

    private ModuloResumo buildServicoNotarialResumo(Instant start, Instant end) {
        Map<String, Long> porEstado = new LinkedHashMap<>();
        Map<String, Long> porTipo = new LinkedHashMap<>();
        for (EstadoServicoNotarial e : EstadoServicoNotarial.values()) {
            long count = servicoNotarialRepository.countByEstadoAndCreatedAtBetween(e, start, end);
            if (count > 0) porEstado.put(e.name(), count);
        }
        for (TipoServicoNotarial t : TipoServicoNotarial.values()) {
            long count = servicoNotarialRepository.countByTipoAndCreatedAtBetween(t, start, end);
            if (count > 0) porTipo.put(t.name(), count);
        }
        long total = servicoNotarialRepository.countByCreatedAtBetween(start, end);
        return new ModuloResumo(total, porEstado, porTipo);
    }

    private ModuloResumo buildAgendamentoResumo(Instant start, Instant end) {
        Map<String, Long> porEstado = new LinkedHashMap<>();
        Map<String, Long> porTipo = new LinkedHashMap<>();
        for (EstadoAgendamento e : EstadoAgendamento.values()) {
            long count = agendamentoRepository.countByEstadoAndCreatedAtBetween(e, start, end);
            if (count > 0) porEstado.put(e.name(), count);
        }
        for (TipoAgendamento t : TipoAgendamento.values()) {
            long count = agendamentoRepository.countByTipoAndCreatedAtBetween(t, start, end);
            if (count > 0) porTipo.put(t.name(), count);
        }
        long total = agendamentoRepository.countByCreatedAtBetween(start, end);
        return new ModuloResumo(total, porEstado, porTipo);
    }

    private Instant toInstant(LocalDate date) {
        return date.atStartOfDay(ZoneOffset.UTC).toInstant();
    }
}

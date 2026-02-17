package ao.gov.embaixada.sgc.service;

import ao.gov.embaixada.sgc.dto.*;
import ao.gov.embaixada.sgc.entity.Agendamento;
import ao.gov.embaixada.sgc.entity.AgendamentoHistorico;
import ao.gov.embaixada.sgc.entity.Cidadao;
import ao.gov.embaixada.sgc.enums.EstadoAgendamento;
import ao.gov.embaixada.sgc.exception.ConflictingAppointmentException;
import ao.gov.embaixada.sgc.exception.ResourceNotFoundException;
import ao.gov.embaixada.sgc.mapper.AgendamentoMapper;
import ao.gov.embaixada.sgc.repository.AgendamentoHistoricoRepository;
import ao.gov.embaixada.sgc.repository.AgendamentoRepository;
import ao.gov.embaixada.sgc.repository.CidadaoRepository;
import ao.gov.embaixada.sgc.statemachine.AgendamentoStateMachine;
import ao.gov.embaixada.sgc.statemachine.event.WorkflowTransitionEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;

@Service
@Transactional
public class AgendamentoService {

    private static final Logger log = LoggerFactory.getLogger(AgendamentoService.class);
    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private final AgendamentoRepository agendamentoRepository;
    private final AgendamentoHistoricoRepository historicoRepository;
    private final CidadaoRepository cidadaoRepository;
    private final AgendamentoMapper agendamentoMapper;
    private final AgendamentoStateMachine stateMachine;
    private final AgendamentoSlotConfig slotConfig;
    private final ApplicationEventPublisher eventPublisher;

    private final AtomicLong agendamentoCounter = new AtomicLong(System.currentTimeMillis() % 100000);

    public AgendamentoService(AgendamentoRepository agendamentoRepository,
                              AgendamentoHistoricoRepository historicoRepository,
                              CidadaoRepository cidadaoRepository,
                              AgendamentoMapper agendamentoMapper,
                              AgendamentoStateMachine stateMachine,
                              AgendamentoSlotConfig slotConfig,
                              ApplicationEventPublisher eventPublisher) {
        this.agendamentoRepository = agendamentoRepository;
        this.historicoRepository = historicoRepository;
        this.cidadaoRepository = cidadaoRepository;
        this.agendamentoMapper = agendamentoMapper;
        this.stateMachine = stateMachine;
        this.slotConfig = slotConfig;
        this.eventPublisher = eventPublisher;
    }

    public AgendamentoResponse create(AgendamentoCreateRequest request) {
        Cidadao cidadao = cidadaoRepository.findById(request.cidadaoId())
                .orElseThrow(() -> new ResourceNotFoundException("Cidadao", request.cidadaoId()));

        checkConflict(request.dataHora(), request.tipo());

        Agendamento agendamento = agendamentoMapper.toEntity(request);
        agendamento.setCidadao(cidadao);
        agendamento.setEstado(EstadoAgendamento.PENDENTE);
        agendamento.setNumeroAgendamento(generateNumero());
        agendamento.setDuracaoMinutos(slotConfig.getDuracaoMinutos(request.tipo()));

        agendamento = agendamentoRepository.save(agendamento);

        addHistorico(agendamento, null, EstadoAgendamento.PENDENTE, "Agendamento criado");

        eventPublisher.publishEvent(new WorkflowTransitionEvent(
                this, agendamento.getId(), "Agendamento",
                null, EstadoAgendamento.PENDENTE.name(), "Agendamento criado"));

        return agendamentoMapper.toResponse(agendamento);
    }

    @Transactional(readOnly = true)
    public AgendamentoResponse findById(UUID id) {
        Agendamento agendamento = agendamentoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Agendamento", id));
        return agendamentoMapper.toResponse(agendamento);
    }

    @Transactional(readOnly = true)
    public Page<AgendamentoResponse> findAll(Pageable pageable) {
        return agendamentoRepository.findAll(pageable).map(agendamentoMapper::toResponse);
    }

    @Transactional(readOnly = true)
    public Page<AgendamentoResponse> findByCidadaoId(UUID cidadaoId, Pageable pageable) {
        return agendamentoRepository.findByCidadaoId(cidadaoId, pageable).map(agendamentoMapper::toResponse);
    }

    @Transactional(readOnly = true)
    public Page<AgendamentoResponse> findByEstado(EstadoAgendamento estado, Pageable pageable) {
        return agendamentoRepository.findByEstado(estado, pageable).map(agendamentoMapper::toResponse);
    }

    @Transactional(readOnly = true)
    public Page<AgendamentoResponse> findByTipo(ao.gov.embaixada.sgc.enums.TipoAgendamento tipo, Pageable pageable) {
        return agendamentoRepository.findByTipo(tipo, pageable).map(agendamentoMapper::toResponse);
    }

    @Transactional(readOnly = true)
    public Page<AgendamentoResponse> findByDateRange(LocalDateTime start, LocalDateTime end, Pageable pageable) {
        return agendamentoRepository.findByDataHoraBetween(start, end, pageable).map(agendamentoMapper::toResponse);
    }

    @Transactional(readOnly = true)
    public Page<AgendamentoHistoricoResponse> findHistorico(UUID agendamentoId, Pageable pageable) {
        return historicoRepository.findByAgendamentoIdOrderByCreatedAtDesc(agendamentoId, pageable)
                .map(agendamentoMapper::toHistoricoResponse);
    }

    public AgendamentoResponse reschedule(UUID id, AgendamentoUpdateRequest request) {
        Agendamento agendamento = agendamentoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Agendamento", id));

        stateMachine.validateTransition(agendamento.getEstado(), EstadoAgendamento.REAGENDADO);
        checkConflict(request.dataHora(), agendamento.getTipo());

        EstadoAgendamento estadoAnterior = agendamento.getEstado();
        agendamento.setDataHora(request.dataHora());
        if (request.notas() != null) {
            agendamento.setNotas(request.notas());
        }
        agendamento.setEstado(EstadoAgendamento.REAGENDADO);

        agendamento = agendamentoRepository.save(agendamento);

        addHistorico(agendamento, estadoAnterior, EstadoAgendamento.REAGENDADO,
                "Reagendado para " + request.dataHora().format(DATE_FMT));

        eventPublisher.publishEvent(new WorkflowTransitionEvent(
                this, agendamento.getId(), "Agendamento",
                estadoAnterior.name(), EstadoAgendamento.REAGENDADO.name(),
                "Reagendado para " + request.dataHora().format(DATE_FMT)));

        return agendamentoMapper.toResponse(agendamento);
    }

    public AgendamentoResponse updateEstado(UUID id, EstadoAgendamento novoEstado, String comentario) {
        Agendamento agendamento = agendamentoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Agendamento", id));

        EstadoAgendamento estadoAnterior = agendamento.getEstado();
        stateMachine.validateTransition(estadoAnterior, novoEstado);

        agendamento.setEstado(novoEstado);

        if (novoEstado == EstadoAgendamento.CANCELADO && comentario != null) {
            agendamento.setMotivoCancelamento(comentario);
        }

        agendamento = agendamentoRepository.save(agendamento);

        addHistorico(agendamento, estadoAnterior, novoEstado, comentario);

        eventPublisher.publishEvent(new WorkflowTransitionEvent(
                this, agendamento.getId(), "Agendamento",
                estadoAnterior.name(), novoEstado.name(), comentario));

        return agendamentoMapper.toResponse(agendamento);
    }

    public void delete(UUID id) {
        Agendamento agendamento = agendamentoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Agendamento", id));

        EstadoAgendamento estadoAnterior = agendamento.getEstado();
        if (!stateMachine.isTerminalState(estadoAnterior)) {
            stateMachine.validateTransition(estadoAnterior, EstadoAgendamento.CANCELADO);
            agendamento.setEstado(EstadoAgendamento.CANCELADO);
            agendamento.setMotivoCancelamento("Cancelado pelo sistema");
            agendamentoRepository.save(agendamento);
            addHistorico(agendamento, estadoAnterior, EstadoAgendamento.CANCELADO, "Cancelado pelo sistema");

            eventPublisher.publishEvent(new WorkflowTransitionEvent(
                    this, agendamento.getId(), "Agendamento",
                    estadoAnterior.name(), EstadoAgendamento.CANCELADO.name(),
                    "Cancelado pelo sistema"));
        }

        agendamentoRepository.deleteById(id);
    }

    private void checkConflict(LocalDateTime dataHora, ao.gov.embaixada.sgc.enums.TipoAgendamento tipo) {
        List<EstadoAgendamento> activeStates = List.of(
                EstadoAgendamento.PENDENTE, EstadoAgendamento.CONFIRMADO);

        if (agendamentoRepository.existsByDataHoraAndTipoAndEstadoIn(dataHora, tipo, activeStates)) {
            throw new ConflictingAppointmentException(
                    "Ja existe um agendamento para " + tipo.name() + " em " + dataHora.format(DATE_FMT));
        }
    }

    private void addHistorico(Agendamento agendamento, EstadoAgendamento estadoAnterior,
                               EstadoAgendamento estadoNovo, String comentario) {
        AgendamentoHistorico historico = new AgendamentoHistorico();
        historico.setAgendamento(agendamento);
        historico.setEstadoAnterior(estadoAnterior);
        historico.setEstadoNovo(estadoNovo);
        historico.setComentario(comentario);
        historicoRepository.save(historico);
    }

    private String generateNumero() {
        long seq = agendamentoCounter.incrementAndGet();
        return String.format("SGC-AGD-%05d", seq);
    }
}

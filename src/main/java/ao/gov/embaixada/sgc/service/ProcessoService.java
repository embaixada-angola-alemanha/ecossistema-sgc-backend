package ao.gov.embaixada.sgc.service;

import ao.gov.embaixada.sgc.dto.*;
import ao.gov.embaixada.sgc.entity.Cidadao;
import ao.gov.embaixada.sgc.entity.Documento;
import ao.gov.embaixada.sgc.entity.Processo;
import ao.gov.embaixada.sgc.entity.ProcessoHistorico;
import ao.gov.embaixada.sgc.enums.EstadoProcesso;
import ao.gov.embaixada.sgc.exception.DuplicateResourceException;
import ao.gov.embaixada.sgc.exception.ResourceNotFoundException;
import ao.gov.embaixada.sgc.mapper.ProcessoMapper;
import ao.gov.embaixada.sgc.repository.CidadaoRepository;
import ao.gov.embaixada.sgc.repository.DocumentoRepository;
import ao.gov.embaixada.sgc.repository.ProcessoHistoricoRepository;
import ao.gov.embaixada.sgc.repository.ProcessoRepository;
import ao.gov.embaixada.sgc.statemachine.ProcessoStateMachine;
import ao.gov.embaixada.sgc.statemachine.event.WorkflowTransitionEvent;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import ao.gov.embaixada.commons.audit.Auditable;
import ao.gov.embaixada.commons.audit.AuditAction;

import java.time.LocalDateTime;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;

@Service
@Transactional
public class ProcessoService {

    private final ProcessoRepository processoRepository;
    private final CidadaoRepository cidadaoRepository;
    private final DocumentoRepository documentoRepository;
    private final ProcessoHistoricoRepository historicoRepository;
    private final ProcessoMapper processoMapper;
    private final ProcessoStateMachine stateMachine;
    private final ApplicationEventPublisher eventPublisher;

    private final AtomicLong processoCounter = new AtomicLong(System.currentTimeMillis() % 100000);

    public ProcessoService(ProcessoRepository processoRepository,
                           CidadaoRepository cidadaoRepository,
                           DocumentoRepository documentoRepository,
                           ProcessoHistoricoRepository historicoRepository,
                           ProcessoMapper processoMapper,
                           ProcessoStateMachine stateMachine,
                           ApplicationEventPublisher eventPublisher) {
        this.processoRepository = processoRepository;
        this.cidadaoRepository = cidadaoRepository;
        this.documentoRepository = documentoRepository;
        this.historicoRepository = historicoRepository;
        this.processoMapper = processoMapper;
        this.stateMachine = stateMachine;
        this.eventPublisher = eventPublisher;
    }

    @Auditable(action = AuditAction.CREATE)
    public ProcessoResponse create(ProcessoCreateRequest request) {
        Cidadao cidadao = cidadaoRepository.findById(request.cidadaoId())
                .orElseThrow(() -> new ResourceNotFoundException("Cidadao", request.cidadaoId()));

        Processo processo = processoMapper.toEntity(request);
        processo.setCidadao(cidadao);
        processo.setEstado(EstadoProcesso.RASCUNHO);
        processo.setNumeroProcesso(generateNumeroProcesso(request.tipo().name()));

        processo = processoRepository.save(processo);

        addHistorico(processo, null, EstadoProcesso.RASCUNHO, "Processo criado");

        return processoMapper.toResponse(processo);
    }

    @Transactional(readOnly = true)
    public ProcessoResponse findById(UUID id) {
        Processo processo = processoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Processo", id));
        return processoMapper.toResponse(processo);
    }

    @Transactional(readOnly = true)
    public Page<ProcessoResponse> findAll(Pageable pageable) {
        return processoRepository.findAll(pageable).map(processoMapper::toResponse);
    }

    @Transactional(readOnly = true)
    public Page<ProcessoResponse> findByCidadaoId(UUID cidadaoId, Pageable pageable) {
        return processoRepository.findByCidadaoId(cidadaoId, pageable).map(processoMapper::toResponse);
    }

    @Transactional(readOnly = true)
    public Page<ProcessoResponse> findByEstado(EstadoProcesso estado, Pageable pageable) {
        return processoRepository.findByEstado(estado, pageable).map(processoMapper::toResponse);
    }

    @Transactional(readOnly = true)
    public Page<ProcessoResponse> findByTipo(ao.gov.embaixada.sgc.enums.TipoProcesso tipo, Pageable pageable) {
        return processoRepository.findByTipo(tipo, pageable).map(processoMapper::toResponse);
    }

    @Transactional(readOnly = true)
    public Page<ProcessoHistoricoResponse> findHistorico(UUID processoId, Pageable pageable) {
        return historicoRepository.findByProcessoIdOrderByCreatedAtDesc(processoId, pageable)
                .map(processoMapper::toHistoricoResponse);
    }

    @Auditable(action = AuditAction.UPDATE)
    public ProcessoResponse update(UUID id, ProcessoUpdateRequest request) {
        Processo processo = processoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Processo", id));
        processoMapper.updateEntity(request, processo);
        if (request.taxaPaga() != null) {
            processo.setTaxaPaga(request.taxaPaga());
        }
        processo = processoRepository.save(processo);
        return processoMapper.toResponse(processo);
    }

    @Auditable(action = AuditAction.UPDATE)
    public ProcessoResponse updateEstado(UUID id, EstadoProcesso novoEstado, String comentario) {
        Processo processo = processoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Processo", id));

        EstadoProcesso estadoAnterior = processo.getEstado();
        stateMachine.validateTransition(estadoAnterior, novoEstado);

        processo.setEstado(novoEstado);

        if (novoEstado == EstadoProcesso.SUBMETIDO) {
            processo.setDataSubmissao(LocalDateTime.now());
        } else if (novoEstado == EstadoProcesso.CONCLUIDO) {
            processo.setDataConclusao(LocalDateTime.now());
        }

        processo = processoRepository.save(processo);

        addHistorico(processo, estadoAnterior, novoEstado, comentario);

        eventPublisher.publishEvent(new WorkflowTransitionEvent(
                this, processo.getId(), "Processo",
                estadoAnterior.name(), novoEstado.name(), comentario));

        return processoMapper.toResponse(processo);
    }

    public ProcessoResponse addDocumento(UUID processoId, UUID documentoId) {
        Processo processo = processoRepository.findById(processoId)
                .orElseThrow(() -> new ResourceNotFoundException("Processo", processoId));
        Documento documento = documentoRepository.findById(documentoId)
                .orElseThrow(() -> new ResourceNotFoundException("Documento", documentoId));

        processo.getDocumentos().add(documento);
        processo = processoRepository.save(processo);
        return processoMapper.toResponse(processo);
    }

    public ProcessoResponse removeDocumento(UUID processoId, UUID documentoId) {
        Processo processo = processoRepository.findById(processoId)
                .orElseThrow(() -> new ResourceNotFoundException("Processo", processoId));

        processo.getDocumentos().removeIf(d -> d.getId().equals(documentoId));
        processo = processoRepository.save(processo);
        return processoMapper.toResponse(processo);
    }

    @Auditable(action = AuditAction.DELETE)
    public void delete(UUID id) {
        if (!processoRepository.existsById(id)) {
            throw new ResourceNotFoundException("Processo", id);
        }
        processoRepository.deleteById(id);
    }

    private void addHistorico(Processo processo, EstadoProcesso estadoAnterior,
                              EstadoProcesso estadoNovo, String comentario) {
        ProcessoHistorico historico = new ProcessoHistorico();
        historico.setProcesso(processo);
        historico.setEstadoAnterior(estadoAnterior);
        historico.setEstadoNovo(estadoNovo);
        historico.setComentario(comentario);
        historicoRepository.save(historico);
    }

    private String generateNumeroProcesso(String tipo) {
        long seq = processoCounter.incrementAndGet();
        return String.format("SGC-%s-%05d", tipo.substring(0, Math.min(3, tipo.length())), seq);
    }
}

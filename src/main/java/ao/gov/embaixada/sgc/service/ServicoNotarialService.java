package ao.gov.embaixada.sgc.service;

import ao.gov.embaixada.sgc.dto.*;
import ao.gov.embaixada.sgc.entity.Cidadao;
import ao.gov.embaixada.sgc.entity.Documento;
import ao.gov.embaixada.sgc.entity.ServicoNotarial;
import ao.gov.embaixada.sgc.entity.ServicoNotarialHistorico;
import ao.gov.embaixada.sgc.enums.EstadoServicoNotarial;
import ao.gov.embaixada.sgc.enums.TipoServicoNotarial;
import ao.gov.embaixada.sgc.exception.ResourceNotFoundException;
import ao.gov.embaixada.sgc.mapper.ServicoNotarialMapper;
import ao.gov.embaixada.sgc.repository.CidadaoRepository;
import ao.gov.embaixada.sgc.repository.DocumentoRepository;
import ao.gov.embaixada.sgc.repository.ServicoNotarialHistoricoRepository;
import ao.gov.embaixada.sgc.repository.ServicoNotarialRepository;
import ao.gov.embaixada.sgc.statemachine.ServicoNotarialStateMachine;
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
public class ServicoNotarialService {

    private final ServicoNotarialRepository servicoRepository;
    private final CidadaoRepository cidadaoRepository;
    private final DocumentoRepository documentoRepository;
    private final ServicoNotarialHistoricoRepository historicoRepository;
    private final ServicoNotarialMapper mapper;
    private final ServicoNotarialStateMachine stateMachine;
    private final NotarialFeeCalculator feeCalculator;
    private final CertificadoService certificadoService;
    private final ApplicationEventPublisher eventPublisher;

    private final AtomicLong servicoCounter = new AtomicLong(System.currentTimeMillis() % 100000);

    public ServicoNotarialService(ServicoNotarialRepository servicoRepository,
                                  CidadaoRepository cidadaoRepository,
                                  DocumentoRepository documentoRepository,
                                  ServicoNotarialHistoricoRepository historicoRepository,
                                  ServicoNotarialMapper mapper,
                                  ServicoNotarialStateMachine stateMachine,
                                  NotarialFeeCalculator feeCalculator,
                                  CertificadoService certificadoService,
                                  ApplicationEventPublisher eventPublisher) {
        this.servicoRepository = servicoRepository;
        this.cidadaoRepository = cidadaoRepository;
        this.documentoRepository = documentoRepository;
        this.historicoRepository = historicoRepository;
        this.mapper = mapper;
        this.stateMachine = stateMachine;
        this.feeCalculator = feeCalculator;
        this.certificadoService = certificadoService;
        this.eventPublisher = eventPublisher;
    }

    @Auditable(action = AuditAction.CREATE)
    public ServicoNotarialResponse create(ServicoNotarialCreateRequest request) {
        Cidadao cidadao = cidadaoRepository.findById(request.cidadaoId())
                .orElseThrow(() -> new ResourceNotFoundException("Cidadao", request.cidadaoId()));

        ServicoNotarial servico = mapper.toEntity(request);
        servico.setCidadao(cidadao);
        servico.setEstado(EstadoServicoNotarial.RASCUNHO);
        servico.setNumeroServico(generateNumeroServico());
        servico.setValorTaxa(feeCalculator.calculateFee(request.tipo()));

        servico = servicoRepository.save(servico);

        addHistorico(servico, null, EstadoServicoNotarial.RASCUNHO, "Servico notarial criado");

        return mapper.toResponse(servico);
    }

    @Transactional(readOnly = true)
    public ServicoNotarialResponse findById(UUID id) {
        ServicoNotarial servico = servicoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("ServicoNotarial", id));
        return mapper.toResponse(servico);
    }

    @Transactional(readOnly = true)
    public Page<ServicoNotarialResponse> findAll(Pageable pageable) {
        return servicoRepository.findAll(pageable).map(mapper::toResponse);
    }

    @Transactional(readOnly = true)
    public Page<ServicoNotarialResponse> findByCidadaoId(UUID cidadaoId, Pageable pageable) {
        return servicoRepository.findByCidadaoId(cidadaoId, pageable).map(mapper::toResponse);
    }

    @Transactional(readOnly = true)
    public Page<ServicoNotarialResponse> findByEstado(EstadoServicoNotarial estado, Pageable pageable) {
        return servicoRepository.findByEstado(estado, pageable).map(mapper::toResponse);
    }

    @Transactional(readOnly = true)
    public Page<ServicoNotarialResponse> findByTipo(TipoServicoNotarial tipo, Pageable pageable) {
        return servicoRepository.findByTipo(tipo, pageable).map(mapper::toResponse);
    }

    @Transactional(readOnly = true)
    public Page<ServicoNotarialHistoricoResponse> findHistorico(UUID servicoId, Pageable pageable) {
        return historicoRepository.findByServicoNotarialIdOrderByCreatedAtDesc(servicoId, pageable)
                .map(mapper::toHistoricoResponse);
    }

    @Auditable(action = AuditAction.UPDATE)
    public ServicoNotarialResponse update(UUID id, ServicoNotarialUpdateRequest request) {
        ServicoNotarial servico = servicoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("ServicoNotarial", id));
        mapper.updateEntity(request, servico);
        servico = servicoRepository.save(servico);
        return mapper.toResponse(servico);
    }

    @Auditable(action = AuditAction.UPDATE)
    public ServicoNotarialResponse updateEstado(UUID id, EstadoServicoNotarial novoEstado, String comentario) {
        ServicoNotarial servico = servicoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("ServicoNotarial", id));

        EstadoServicoNotarial estadoAnterior = servico.getEstado();
        stateMachine.validateTransition(estadoAnterior, novoEstado);

        servico.setEstado(novoEstado);

        switch (novoEstado) {
            case SUBMETIDO -> servico.setDataSubmissao(LocalDateTime.now());
            case CONCLUIDO -> {
                servico.setDataConclusao(LocalDateTime.now());
                String objectKey = certificadoService.generateAndStoreNotarial(servico);
                servico.setCertificadoObjectKey(objectKey);
                servico.setCertificadoUrl("/api/v1/servicos-notariais/" + servico.getId() + "/certificado");
            }
            case REJEITADO -> servico.setMotivoRejeicao(comentario);
            default -> { }
        }

        servico = servicoRepository.save(servico);

        addHistorico(servico, estadoAnterior, novoEstado, comentario);

        eventPublisher.publishEvent(new WorkflowTransitionEvent(
                this, servico.getId(), "ServicoNotarial",
                estadoAnterior.name(), novoEstado.name(), comentario));

        return mapper.toResponse(servico);
    }

    public ServicoNotarialResponse markTaxaPaga(UUID id) {
        ServicoNotarial servico = servicoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("ServicoNotarial", id));
        servico.setTaxaPaga(true);
        servico = servicoRepository.save(servico);
        return mapper.toResponse(servico);
    }

    public ServicoNotarialResponse addDocumento(UUID servicoId, UUID documentoId) {
        ServicoNotarial servico = servicoRepository.findById(servicoId)
                .orElseThrow(() -> new ResourceNotFoundException("ServicoNotarial", servicoId));
        Documento documento = documentoRepository.findById(documentoId)
                .orElseThrow(() -> new ResourceNotFoundException("Documento", documentoId));

        servico.getDocumentos().add(documento);
        servico = servicoRepository.save(servico);
        return mapper.toResponse(servico);
    }

    public ServicoNotarialResponse removeDocumento(UUID servicoId, UUID documentoId) {
        ServicoNotarial servico = servicoRepository.findById(servicoId)
                .orElseThrow(() -> new ResourceNotFoundException("ServicoNotarial", servicoId));

        servico.getDocumentos().removeIf(d -> d.getId().equals(documentoId));
        servico = servicoRepository.save(servico);
        return mapper.toResponse(servico);
    }

    @Auditable(action = AuditAction.DELETE)
    public void delete(UUID id) {
        if (!servicoRepository.existsById(id)) {
            throw new ResourceNotFoundException("ServicoNotarial", id);
        }
        servicoRepository.deleteById(id);
    }

    private void addHistorico(ServicoNotarial servico, EstadoServicoNotarial estadoAnterior,
                              EstadoServicoNotarial estadoNovo, String comentario) {
        ServicoNotarialHistorico historico = new ServicoNotarialHistorico();
        historico.setServicoNotarial(servico);
        historico.setEstadoAnterior(estadoAnterior);
        historico.setEstadoNovo(estadoNovo);
        historico.setComentario(comentario);
        historicoRepository.save(historico);
    }

    private String generateNumeroServico() {
        long seq = servicoCounter.incrementAndGet();
        return String.format("SGC-NOT-%05d", seq);
    }
}

package ao.gov.embaixada.sgc.service;

import ao.gov.embaixada.sgc.dto.*;
import ao.gov.embaixada.sgc.entity.Cidadao;
import ao.gov.embaixada.sgc.entity.Documento;
import ao.gov.embaixada.sgc.entity.VisaApplication;
import ao.gov.embaixada.sgc.entity.VisaHistorico;
import ao.gov.embaixada.sgc.enums.EstadoVisto;
import ao.gov.embaixada.sgc.exception.ResourceNotFoundException;
import ao.gov.embaixada.sgc.mapper.VisaMapper;
import ao.gov.embaixada.sgc.repository.CidadaoRepository;
import ao.gov.embaixada.sgc.repository.DocumentoRepository;
import ao.gov.embaixada.sgc.repository.VisaHistoricoRepository;
import ao.gov.embaixada.sgc.repository.VisaRepository;
import ao.gov.embaixada.sgc.statemachine.VisaStateMachine;
import ao.gov.embaixada.sgc.statemachine.event.WorkflowTransitionEvent;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;

@Service
@Transactional
public class VisaService {

    private final VisaRepository visaRepository;
    private final CidadaoRepository cidadaoRepository;
    private final DocumentoRepository documentoRepository;
    private final VisaHistoricoRepository historicoRepository;
    private final VisaMapper visaMapper;
    private final VisaStateMachine stateMachine;
    private final VisaFeeCalculator feeCalculator;
    private final ApplicationEventPublisher eventPublisher;

    private final AtomicLong visaCounter = new AtomicLong(System.currentTimeMillis() % 100000);

    public VisaService(VisaRepository visaRepository,
                       CidadaoRepository cidadaoRepository,
                       DocumentoRepository documentoRepository,
                       VisaHistoricoRepository historicoRepository,
                       VisaMapper visaMapper,
                       VisaStateMachine stateMachine,
                       VisaFeeCalculator feeCalculator,
                       ApplicationEventPublisher eventPublisher) {
        this.visaRepository = visaRepository;
        this.cidadaoRepository = cidadaoRepository;
        this.documentoRepository = documentoRepository;
        this.historicoRepository = historicoRepository;
        this.visaMapper = visaMapper;
        this.stateMachine = stateMachine;
        this.feeCalculator = feeCalculator;
        this.eventPublisher = eventPublisher;
    }

    public VisaResponse create(VisaCreateRequest request) {
        Cidadao cidadao = cidadaoRepository.findById(request.cidadaoId())
                .orElseThrow(() -> new ResourceNotFoundException("Cidadao", request.cidadaoId()));

        VisaApplication visa = visaMapper.toEntity(request);
        visa.setCidadao(cidadao);
        visa.setEstado(EstadoVisto.RASCUNHO);
        visa.setNumeroVisto(generateNumeroVisto());
        visa.setValorTaxa(feeCalculator.calculateFee(request.tipo()));

        visa = visaRepository.save(visa);

        addHistorico(visa, null, EstadoVisto.RASCUNHO, "Pedido de visto criado");

        return visaMapper.toResponse(visa);
    }

    @Transactional(readOnly = true)
    public VisaResponse findById(UUID id) {
        VisaApplication visa = visaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Visa", id));
        return visaMapper.toResponse(visa);
    }

    @Transactional(readOnly = true)
    public Page<VisaResponse> findAll(Pageable pageable) {
        return visaRepository.findAll(pageable).map(visaMapper::toResponse);
    }

    @Transactional(readOnly = true)
    public Page<VisaResponse> findByCidadaoId(UUID cidadaoId, Pageable pageable) {
        return visaRepository.findByCidadaoId(cidadaoId, pageable).map(visaMapper::toResponse);
    }

    @Transactional(readOnly = true)
    public Page<VisaResponse> findByEstado(EstadoVisto estado, Pageable pageable) {
        return visaRepository.findByEstado(estado, pageable).map(visaMapper::toResponse);
    }

    @Transactional(readOnly = true)
    public Page<VisaResponse> findByTipo(ao.gov.embaixada.sgc.enums.TipoVisto tipo, Pageable pageable) {
        return visaRepository.findByTipo(tipo, pageable).map(visaMapper::toResponse);
    }

    @Transactional(readOnly = true)
    public Page<VisaHistoricoResponse> findHistorico(UUID visaId, Pageable pageable) {
        return historicoRepository.findByVisaApplicationIdOrderByCreatedAtDesc(visaId, pageable)
                .map(visaMapper::toHistoricoResponse);
    }

    public VisaResponse update(UUID id, VisaUpdateRequest request) {
        VisaApplication visa = visaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Visa", id));
        visaMapper.updateEntity(request, visa);
        visa = visaRepository.save(visa);
        return visaMapper.toResponse(visa);
    }

    public VisaResponse updateEstado(UUID id, EstadoVisto novoEstado, String comentario) {
        VisaApplication visa = visaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Visa", id));

        EstadoVisto estadoAnterior = visa.getEstado();
        stateMachine.validateTransition(estadoAnterior, novoEstado);

        visa.setEstado(novoEstado);

        if (novoEstado == EstadoVisto.SUBMETIDO) {
            visa.setDataSubmissao(LocalDateTime.now());
        } else if (novoEstado == EstadoVisto.APROVADO || novoEstado == EstadoVisto.REJEITADO) {
            visa.setDataDecisao(LocalDateTime.now());
        }

        visa = visaRepository.save(visa);

        addHistorico(visa, estadoAnterior, novoEstado, comentario);

        eventPublisher.publishEvent(new WorkflowTransitionEvent(
                this, visa.getId(), "Visa",
                estadoAnterior.name(), novoEstado.name(), comentario));

        return visaMapper.toResponse(visa);
    }

    public VisaResponse addDocumento(UUID visaId, UUID documentoId) {
        VisaApplication visa = visaRepository.findById(visaId)
                .orElseThrow(() -> new ResourceNotFoundException("Visa", visaId));
        Documento documento = documentoRepository.findById(documentoId)
                .orElseThrow(() -> new ResourceNotFoundException("Documento", documentoId));

        visa.getDocumentos().add(documento);
        visa = visaRepository.save(visa);
        return visaMapper.toResponse(visa);
    }

    public VisaResponse removeDocumento(UUID visaId, UUID documentoId) {
        VisaApplication visa = visaRepository.findById(visaId)
                .orElseThrow(() -> new ResourceNotFoundException("Visa", visaId));

        visa.getDocumentos().removeIf(d -> d.getId().equals(documentoId));
        visa = visaRepository.save(visa);
        return visaMapper.toResponse(visa);
    }

    public void delete(UUID id) {
        if (!visaRepository.existsById(id)) {
            throw new ResourceNotFoundException("Visa", id);
        }
        visaRepository.deleteById(id);
    }

    private void addHistorico(VisaApplication visa, EstadoVisto estadoAnterior,
                              EstadoVisto estadoNovo, String comentario) {
        VisaHistorico historico = new VisaHistorico();
        historico.setVisaApplication(visa);
        historico.setEstadoAnterior(estadoAnterior);
        historico.setEstadoNovo(estadoNovo);
        historico.setComentario(comentario);
        historicoRepository.save(historico);
    }

    private String generateNumeroVisto() {
        long seq = visaCounter.incrementAndGet();
        return String.format("SGC-VIS-%05d", seq);
    }
}

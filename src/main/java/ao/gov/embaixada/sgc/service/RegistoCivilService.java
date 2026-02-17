package ao.gov.embaixada.sgc.service;

import ao.gov.embaixada.sgc.dto.*;
import ao.gov.embaixada.sgc.entity.Cidadao;
import ao.gov.embaixada.sgc.entity.Documento;
import ao.gov.embaixada.sgc.entity.RegistoCivil;
import ao.gov.embaixada.sgc.entity.RegistoCivilHistorico;
import ao.gov.embaixada.sgc.enums.EstadoRegistoCivil;
import ao.gov.embaixada.sgc.enums.TipoRegistoCivil;
import ao.gov.embaixada.sgc.exception.ResourceNotFoundException;
import ao.gov.embaixada.sgc.mapper.RegistoCivilMapper;
import ao.gov.embaixada.sgc.repository.CidadaoRepository;
import ao.gov.embaixada.sgc.repository.DocumentoRepository;
import ao.gov.embaixada.sgc.repository.RegistoCivilHistoricoRepository;
import ao.gov.embaixada.sgc.repository.RegistoCivilRepository;
import ao.gov.embaixada.sgc.statemachine.RegistoCivilStateMachine;
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
public class RegistoCivilService {

    private final RegistoCivilRepository registoRepository;
    private final CidadaoRepository cidadaoRepository;
    private final DocumentoRepository documentoRepository;
    private final RegistoCivilHistoricoRepository historicoRepository;
    private final RegistoCivilMapper mapper;
    private final RegistoCivilStateMachine stateMachine;
    private final CertificadoService certificadoService;
    private final ApplicationEventPublisher eventPublisher;

    private final AtomicLong registoCounter = new AtomicLong(System.currentTimeMillis() % 100000);

    public RegistoCivilService(RegistoCivilRepository registoRepository,
                               CidadaoRepository cidadaoRepository,
                               DocumentoRepository documentoRepository,
                               RegistoCivilHistoricoRepository historicoRepository,
                               RegistoCivilMapper mapper,
                               RegistoCivilStateMachine stateMachine,
                               CertificadoService certificadoService,
                               ApplicationEventPublisher eventPublisher) {
        this.registoRepository = registoRepository;
        this.cidadaoRepository = cidadaoRepository;
        this.documentoRepository = documentoRepository;
        this.historicoRepository = historicoRepository;
        this.mapper = mapper;
        this.stateMachine = stateMachine;
        this.certificadoService = certificadoService;
        this.eventPublisher = eventPublisher;
    }

    public RegistoCivilResponse create(RegistoCivilCreateRequest request) {
        Cidadao cidadao = cidadaoRepository.findById(request.cidadaoId())
                .orElseThrow(() -> new ResourceNotFoundException("Cidadao", request.cidadaoId()));

        RegistoCivil registo = mapper.toEntity(request);
        registo.setCidadao(cidadao);
        registo.setEstado(EstadoRegistoCivil.RASCUNHO);
        registo.setNumeroRegisto(generateNumeroRegisto(request.tipo()));

        registo = registoRepository.save(registo);

        addHistorico(registo, null, EstadoRegistoCivil.RASCUNHO, "Registo civil criado");

        return mapper.toResponse(registo);
    }

    @Transactional(readOnly = true)
    public RegistoCivilResponse findById(UUID id) {
        RegistoCivil registo = registoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("RegistoCivil", id));
        return mapper.toResponse(registo);
    }

    @Transactional(readOnly = true)
    public Page<RegistoCivilResponse> findAll(Pageable pageable) {
        return registoRepository.findAll(pageable).map(mapper::toResponse);
    }

    @Transactional(readOnly = true)
    public Page<RegistoCivilResponse> findByCidadaoId(UUID cidadaoId, Pageable pageable) {
        return registoRepository.findByCidadaoId(cidadaoId, pageable).map(mapper::toResponse);
    }

    @Transactional(readOnly = true)
    public Page<RegistoCivilResponse> findByEstado(EstadoRegistoCivil estado, Pageable pageable) {
        return registoRepository.findByEstado(estado, pageable).map(mapper::toResponse);
    }

    @Transactional(readOnly = true)
    public Page<RegistoCivilResponse> findByTipo(TipoRegistoCivil tipo, Pageable pageable) {
        return registoRepository.findByTipo(tipo, pageable).map(mapper::toResponse);
    }

    @Transactional(readOnly = true)
    public Page<RegistoCivilHistoricoResponse> findHistorico(UUID registoId, Pageable pageable) {
        return historicoRepository.findByRegistoCivilIdOrderByCreatedAtDesc(registoId, pageable)
                .map(mapper::toHistoricoResponse);
    }

    public RegistoCivilResponse update(UUID id, RegistoCivilUpdateRequest request) {
        RegistoCivil registo = registoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("RegistoCivil", id));
        mapper.updateEntity(request, registo);
        registo = registoRepository.save(registo);
        return mapper.toResponse(registo);
    }

    public RegistoCivilResponse updateEstado(UUID id, EstadoRegistoCivil novoEstado, String comentario) {
        RegistoCivil registo = registoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("RegistoCivil", id));

        EstadoRegistoCivil estadoAnterior = registo.getEstado();
        stateMachine.validateTransition(estadoAnterior, novoEstado);

        registo.setEstado(novoEstado);

        switch (novoEstado) {
            case SUBMETIDO -> registo.setDataSubmissao(LocalDateTime.now());
            case VERIFICADO -> registo.setDataVerificacao(LocalDateTime.now());
            case CERTIFICADO_EMITIDO -> {
                registo.setDataCertificado(LocalDateTime.now());
                String objectKey = certificadoService.generateAndStore(registo);
                registo.setCertificadoObjectKey(objectKey);
                registo.setCertificadoUrl("/api/v1/registos-civis/" + registo.getId() + "/certificado");
            }
            case REJEITADO -> registo.setMotivoRejeicao(comentario);
            default -> { }
        }

        registo = registoRepository.save(registo);

        addHistorico(registo, estadoAnterior, novoEstado, comentario);

        eventPublisher.publishEvent(new WorkflowTransitionEvent(
                this, registo.getId(), "RegistoCivil",
                estadoAnterior.name(), novoEstado.name(), comentario));

        return mapper.toResponse(registo);
    }

    public RegistoCivilResponse addDocumento(UUID registoId, UUID documentoId) {
        RegistoCivil registo = registoRepository.findById(registoId)
                .orElseThrow(() -> new ResourceNotFoundException("RegistoCivil", registoId));
        Documento documento = documentoRepository.findById(documentoId)
                .orElseThrow(() -> new ResourceNotFoundException("Documento", documentoId));

        registo.getDocumentos().add(documento);
        registo = registoRepository.save(registo);
        return mapper.toResponse(registo);
    }

    public RegistoCivilResponse removeDocumento(UUID registoId, UUID documentoId) {
        RegistoCivil registo = registoRepository.findById(registoId)
                .orElseThrow(() -> new ResourceNotFoundException("RegistoCivil", registoId));

        registo.getDocumentos().removeIf(d -> d.getId().equals(documentoId));
        registo = registoRepository.save(registo);
        return mapper.toResponse(registo);
    }

    public void delete(UUID id) {
        if (!registoRepository.existsById(id)) {
            throw new ResourceNotFoundException("RegistoCivil", id);
        }
        registoRepository.deleteById(id);
    }

    private void addHistorico(RegistoCivil registo, EstadoRegistoCivil estadoAnterior,
                              EstadoRegistoCivil estadoNovo, String comentario) {
        RegistoCivilHistorico historico = new RegistoCivilHistorico();
        historico.setRegistoCivil(registo);
        historico.setEstadoAnterior(estadoAnterior);
        historico.setEstadoNovo(estadoNovo);
        historico.setComentario(comentario);
        historicoRepository.save(historico);
    }

    private String generateNumeroRegisto(TipoRegistoCivil tipo) {
        long seq = registoCounter.incrementAndGet();
        String prefix = switch (tipo) {
            case NASCIMENTO -> "SGC-NAS";
            case CASAMENTO -> "SGC-CAS";
            case OBITO -> "SGC-OBI";
        };
        return String.format("%s-%05d", prefix, seq);
    }
}

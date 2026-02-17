package ao.gov.embaixada.sgc.service;

import ao.gov.embaixada.sgc.dto.DocumentoCreateRequest;
import ao.gov.embaixada.sgc.dto.DocumentoResponse;
import ao.gov.embaixada.sgc.dto.DocumentoUpdateRequest;
import ao.gov.embaixada.sgc.entity.Cidadao;
import ao.gov.embaixada.sgc.entity.Documento;
import ao.gov.embaixada.sgc.enums.EstadoDocumento;
import ao.gov.embaixada.sgc.exception.ResourceNotFoundException;
import ao.gov.embaixada.sgc.mapper.DocumentoMapper;
import ao.gov.embaixada.sgc.repository.CidadaoRepository;
import ao.gov.embaixada.sgc.repository.DocumentoRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@Transactional
public class DocumentoService {

    private final DocumentoRepository documentoRepository;
    private final CidadaoRepository cidadaoRepository;
    private final DocumentoMapper documentoMapper;

    public DocumentoService(DocumentoRepository documentoRepository,
                            CidadaoRepository cidadaoRepository,
                            DocumentoMapper documentoMapper) {
        this.documentoRepository = documentoRepository;
        this.cidadaoRepository = cidadaoRepository;
        this.documentoMapper = documentoMapper;
    }

    public DocumentoResponse create(UUID cidadaoId, DocumentoCreateRequest request) {
        Cidadao cidadao = cidadaoRepository.findById(cidadaoId)
                .orElseThrow(() -> new ResourceNotFoundException("Cidadao", cidadaoId));
        Documento documento = documentoMapper.toEntity(request);
        documento.setCidadao(cidadao);
        documento.setEstado(EstadoDocumento.PENDENTE);
        documento = documentoRepository.save(documento);
        return documentoMapper.toResponse(documento);
    }

    @Transactional(readOnly = true)
    public DocumentoResponse findById(UUID id) {
        Documento documento = documentoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Documento", id));
        return documentoMapper.toResponse(documento);
    }

    @Transactional(readOnly = true)
    public Page<DocumentoResponse> findByCidadaoId(UUID cidadaoId, Pageable pageable) {
        return documentoRepository.findByCidadaoId(cidadaoId, pageable).map(documentoMapper::toResponse);
    }

    public DocumentoResponse update(UUID id, DocumentoUpdateRequest request) {
        Documento documento = documentoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Documento", id));
        documentoMapper.updateEntity(request, documento);
        documento = documentoRepository.save(documento);
        return documentoMapper.toResponse(documento);
    }

    public DocumentoResponse updateEstado(UUID id, EstadoDocumento estado) {
        Documento documento = documentoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Documento", id));
        documento.setEstado(estado);
        documento = documentoRepository.save(documento);
        return documentoMapper.toResponse(documento);
    }

    public void delete(UUID id) {
        if (!documentoRepository.existsById(id)) {
            throw new ResourceNotFoundException("Documento", id);
        }
        documentoRepository.deleteById(id);
    }
}

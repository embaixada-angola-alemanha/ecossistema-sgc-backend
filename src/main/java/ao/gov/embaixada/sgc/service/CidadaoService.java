package ao.gov.embaixada.sgc.service;

import ao.gov.embaixada.sgc.dto.CidadaoCreateRequest;
import ao.gov.embaixada.sgc.dto.CidadaoResponse;
import ao.gov.embaixada.sgc.dto.CidadaoUpdateRequest;
import ao.gov.embaixada.sgc.entity.Cidadao;
import ao.gov.embaixada.sgc.enums.EstadoCidadao;
import ao.gov.embaixada.sgc.enums.Sexo;
import ao.gov.embaixada.sgc.exception.DuplicateResourceException;
import ao.gov.embaixada.sgc.exception.ResourceNotFoundException;
import ao.gov.embaixada.sgc.mapper.CidadaoMapper;
import ao.gov.embaixada.sgc.repository.CidadaoRepository;
import ao.gov.embaixada.sgc.specification.CidadaoSpecification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@Transactional
public class CidadaoService {

    private final CidadaoRepository cidadaoRepository;
    private final CidadaoMapper cidadaoMapper;

    public CidadaoService(CidadaoRepository cidadaoRepository, CidadaoMapper cidadaoMapper) {
        this.cidadaoRepository = cidadaoRepository;
        this.cidadaoMapper = cidadaoMapper;
    }

    public CidadaoResponse create(CidadaoCreateRequest request) {
        if (cidadaoRepository.existsByNumeroPassaporte(request.numeroPassaporte())) {
            throw new DuplicateResourceException("Cidadao", "numeroPassaporte", request.numeroPassaporte());
        }
        Cidadao cidadao = cidadaoMapper.toEntity(request);
        cidadao.setEstado(EstadoCidadao.ACTIVO);
        cidadao = cidadaoRepository.save(cidadao);
        return cidadaoMapper.toResponse(cidadao);
    }

    @Transactional(readOnly = true)
    public CidadaoResponse findById(UUID id) {
        Cidadao cidadao = cidadaoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Cidadao", id));
        return cidadaoMapper.toResponse(cidadao);
    }

    @Transactional(readOnly = true)
    public Page<CidadaoResponse> findAll(String search, EstadoCidadao estado,
                                          Sexo sexo, String nacionalidade,
                                          Pageable pageable) {
        Specification<Cidadao> spec = Specification.where(CidadaoSpecification.withNome(search))
                .and(CidadaoSpecification.withEstado(estado))
                .and(CidadaoSpecification.withSexo(sexo))
                .and(CidadaoSpecification.withNacionalidade(nacionalidade));
        return cidadaoRepository.findAll(spec, pageable).map(cidadaoMapper::toResponse);
    }

    public CidadaoResponse update(UUID id, CidadaoUpdateRequest request) {
        Cidadao cidadao = cidadaoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Cidadao", id));
        cidadaoMapper.updateEntity(request, cidadao);
        cidadao = cidadaoRepository.save(cidadao);
        return cidadaoMapper.toResponse(cidadao);
    }

    public CidadaoResponse updateEstado(UUID id, EstadoCidadao estado) {
        Cidadao cidadao = cidadaoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Cidadao", id));
        cidadao.setEstado(estado);
        cidadao = cidadaoRepository.save(cidadao);
        return cidadaoMapper.toResponse(cidadao);
    }

    public void delete(UUID id) {
        if (!cidadaoRepository.existsById(id)) {
            throw new ResourceNotFoundException("Cidadao", id);
        }
        cidadaoRepository.deleteById(id);
    }
}

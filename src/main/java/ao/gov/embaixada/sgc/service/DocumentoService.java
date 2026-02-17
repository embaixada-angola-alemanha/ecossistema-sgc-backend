package ao.gov.embaixada.sgc.service;

import ao.gov.embaixada.commons.storage.FileValidationService;
import ao.gov.embaixada.commons.storage.StorageService;
import ao.gov.embaixada.sgc.dto.*;
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
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class DocumentoService {

    private final DocumentoRepository documentoRepository;
    private final CidadaoRepository cidadaoRepository;
    private final DocumentoMapper documentoMapper;
    private final StorageService storageService;
    private final FileValidationService fileValidationService;

    public DocumentoService(DocumentoRepository documentoRepository,
                            CidadaoRepository cidadaoRepository,
                            DocumentoMapper documentoMapper,
                            StorageService storageService,
                            FileValidationService fileValidationService) {
        this.documentoRepository = documentoRepository;
        this.cidadaoRepository = cidadaoRepository;
        this.documentoMapper = documentoMapper;
        this.storageService = storageService;
        this.fileValidationService = fileValidationService;
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
        Documento documento = documentoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Documento", id));
        if (documento.getFicheiroObjectKey() != null) {
            storageService.delete(storageService.getDefaultBucket(), documento.getFicheiroObjectKey());
        }
        documentoRepository.deleteById(id);
    }

    public DocumentoUploadResponse uploadFicheiro(UUID cidadaoId, UUID documentoId, MultipartFile file) {
        fileValidationService.validate(file);
        fileValidationService.scanForVirus(file);

        Documento documento = documentoRepository.findById(documentoId)
                .orElseThrow(() -> new ResourceNotFoundException("Documento", documentoId));

        String objectKey = buildObjectKey(cidadaoId, documentoId, file.getOriginalFilename());
        try (InputStream is = file.getInputStream()) {
            storageService.upload(storageService.getDefaultBucket(), objectKey, is,
                    file.getSize(), file.getContentType());
        } catch (IOException e) {
            throw new ao.gov.embaixada.commons.storage.StorageException("Erro ao ler ficheiro", e);
        }

        documento.setFicheiroObjectKey(objectKey);
        documento.setFicheiroNome(file.getOriginalFilename());
        documento.setFicheiroTamanho(file.getSize());
        documento.setFicheiroTipo(file.getContentType());
        documento.setFicheiroUrl("/api/v1/cidadaos/" + cidadaoId + "/documentos/" + documentoId + "/ficheiro");
        documento = documentoRepository.save(documento);

        return new DocumentoUploadResponse(
                documento.getId(),
                documento.getFicheiroNome(),
                documento.getFicheiroTamanho(),
                documento.getFicheiroTipo(),
                documento.getFicheiroUrl(),
                documento.getVersao()
        );
    }

    @Transactional(readOnly = true)
    public StorageDownloadResult downloadFicheiro(UUID documentoId) {
        Documento documento = documentoRepository.findById(documentoId)
                .orElseThrow(() -> new ResourceNotFoundException("Documento", documentoId));

        if (documento.getFicheiroObjectKey() == null) {
            throw new ResourceNotFoundException("Ficheiro", "documento", documentoId.toString());
        }

        InputStream is = storageService.download(storageService.getDefaultBucket(),
                documento.getFicheiroObjectKey());
        return new StorageDownloadResult(is, documento.getFicheiroTipo(),
                documento.getFicheiroNome(), documento.getFicheiroTamanho());
    }

    public DocumentoUploadResponse createNewVersion(UUID cidadaoId, UUID documentoId, MultipartFile file) {
        fileValidationService.validate(file);
        fileValidationService.scanForVirus(file);

        Documento original = documentoRepository.findById(documentoId)
                .orElseThrow(() -> new ResourceNotFoundException("Documento", documentoId));

        UUID originalId = original.getDocumentoOriginal() != null
                ? original.getDocumentoOriginal().getId() : original.getId();
        int nextVersao = documentoRepository.findMaxVersaoByOriginalId(originalId) + 1;

        Documento newVersion = new Documento();
        newVersion.setCidadao(original.getCidadao());
        newVersion.setTipo(original.getTipo());
        newVersion.setNumero(original.getNumero());
        newVersion.setDataEmissao(original.getDataEmissao());
        newVersion.setDataValidade(original.getDataValidade());
        newVersion.setEstado(EstadoDocumento.PENDENTE);
        newVersion.setVersao(nextVersao);
        newVersion.setDocumentoOriginal(documentoRepository.getReferenceById(originalId));

        String objectKey = buildObjectKey(cidadaoId, originalId, "v" + nextVersao + "_" + file.getOriginalFilename());
        try (InputStream is = file.getInputStream()) {
            storageService.upload(storageService.getDefaultBucket(), objectKey, is,
                    file.getSize(), file.getContentType());
        } catch (IOException e) {
            throw new ao.gov.embaixada.commons.storage.StorageException("Erro ao ler ficheiro", e);
        }

        newVersion.setFicheiroObjectKey(objectKey);
        newVersion.setFicheiroNome(file.getOriginalFilename());
        newVersion.setFicheiroTamanho(file.getSize());
        newVersion.setFicheiroTipo(file.getContentType());
        newVersion.setFicheiroUrl("/api/v1/cidadaos/" + cidadaoId + "/documentos/" + newVersion.getId() + "/ficheiro");
        newVersion = documentoRepository.save(newVersion);
        newVersion.setFicheiroUrl("/api/v1/cidadaos/" + cidadaoId + "/documentos/" + newVersion.getId() + "/ficheiro");
        newVersion = documentoRepository.save(newVersion);

        return new DocumentoUploadResponse(
                newVersion.getId(),
                newVersion.getFicheiroNome(),
                newVersion.getFicheiroTamanho(),
                newVersion.getFicheiroTipo(),
                newVersion.getFicheiroUrl(),
                newVersion.getVersao()
        );
    }

    @Transactional(readOnly = true)
    public List<DocumentoVersionResponse> findVersions(UUID documentoId) {
        Documento documento = documentoRepository.findById(documentoId)
                .orElseThrow(() -> new ResourceNotFoundException("Documento", documentoId));

        UUID originalId = documento.getDocumentoOriginal() != null
                ? documento.getDocumentoOriginal().getId() : documento.getId();

        List<Documento> versions = documentoRepository.findByDocumentoOriginalIdOrderByVersaoDesc(originalId);
        // Include the original itself
        Documento original = documentoRepository.findById(originalId)
                .orElseThrow(() -> new ResourceNotFoundException("Documento", originalId));

        List<DocumentoVersionResponse> result = new java.util.ArrayList<>(
                versions.stream().map(documentoMapper::toVersionResponse).toList());
        result.add(documentoMapper.toVersionResponse(original));
        result.sort((a, b) -> Integer.compare(b.versao(), a.versao()));
        return result;
    }

    private String buildObjectKey(UUID cidadaoId, UUID documentoId, String filename) {
        return "cidadaos/" + cidadaoId + "/documentos/" + documentoId + "/" + filename;
    }
}

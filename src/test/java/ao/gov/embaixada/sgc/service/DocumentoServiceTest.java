package ao.gov.embaixada.sgc.service;

import ao.gov.embaixada.commons.storage.InvalidFileException;
import ao.gov.embaixada.sgc.dto.*;
import ao.gov.embaixada.sgc.enums.EstadoDocumento;
import ao.gov.embaixada.sgc.enums.TipoDocumento;
import ao.gov.embaixada.sgc.exception.ResourceNotFoundException;
import ao.gov.embaixada.sgc.AbstractIntegrationTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@Transactional
class DocumentoServiceTest extends AbstractIntegrationTest {

    @Autowired
    private DocumentoService documentoService;

    @Autowired
    private CidadaoService cidadaoService;

    private UUID cidadaoId;

    @BeforeEach
    void setUp() {
        CidadaoCreateRequest cidadaoReq = new CidadaoCreateRequest(
                "DOC-TEST-" + System.nanoTime(), "Documento Test Cidadao", null,
                null, "Angolana", null, null, null, null, null);
        CidadaoResponse cidadao = cidadaoService.create(cidadaoReq);
        cidadaoId = cidadao.id();
    }

    @Test
    void shouldCreateDocumento() {
        DocumentoCreateRequest request = new DocumentoCreateRequest(
                TipoDocumento.PASSAPORTE, "P123456",
                LocalDate.of(2024, 1, 1), LocalDate.of(2029, 1, 1),
                null, null, null, null);

        DocumentoResponse response = documentoService.create(cidadaoId, request);

        assertNotNull(response.id());
        assertEquals(TipoDocumento.PASSAPORTE, response.tipo());
        assertEquals("P123456", response.numero());
        assertEquals(EstadoDocumento.PENDENTE, response.estado());
        assertEquals(cidadaoId, response.cidadaoId());
    }

    @Test
    void shouldThrowNotFoundForInvalidCidadao() {
        DocumentoCreateRequest request = new DocumentoCreateRequest(
                TipoDocumento.BILHETE_IDENTIDADE, "BI001",
                null, null, null, null, null, null);

        assertThrows(ResourceNotFoundException.class, () ->
                documentoService.create(UUID.randomUUID(), request));
    }

    @Test
    void shouldFindById() {
        DocumentoCreateRequest request = new DocumentoCreateRequest(
                TipoDocumento.CERTIDAO_NASCIMENTO, "CN001",
                null, null, null, null, null, null);

        DocumentoResponse created = documentoService.create(cidadaoId, request);
        DocumentoResponse found = documentoService.findById(created.id());

        assertEquals(created.id(), found.id());
        assertEquals("CN001", found.numero());
    }

    @Test
    void shouldFindByCidadao() {
        documentoService.create(cidadaoId, new DocumentoCreateRequest(
                TipoDocumento.PASSAPORTE, "P1", null, null, null, null, null, null));
        documentoService.create(cidadaoId, new DocumentoCreateRequest(
                TipoDocumento.BILHETE_IDENTIDADE, "BI1", null, null, null, null, null, null));

        Page<DocumentoResponse> result = documentoService.findByCidadaoId(cidadaoId, Pageable.unpaged());
        assertEquals(2, result.getTotalElements());
    }

    @Test
    void shouldUpdateDocumento() {
        DocumentoResponse created = documentoService.create(cidadaoId, new DocumentoCreateRequest(
                TipoDocumento.PROCURACAO, "PROC001", null, null, null, null, null, null));

        DocumentoUpdateRequest updateReq = new DocumentoUpdateRequest(
                TipoDocumento.PROCURACAO, "PROC001-UPDATED",
                LocalDate.of(2025, 6, 1), LocalDate.of(2026, 6, 1),
                null, null, null, null);

        DocumentoResponse updated = documentoService.update(created.id(), updateReq);
        assertEquals("PROC001-UPDATED", updated.numero());
    }

    @Test
    void shouldUpdateEstado() {
        DocumentoResponse created = documentoService.create(cidadaoId, new DocumentoCreateRequest(
                TipoDocumento.DECLARACAO, "DEC001", null, null, null, null, null, null));

        assertEquals(EstadoDocumento.PENDENTE, created.estado());

        DocumentoResponse updated = documentoService.updateEstado(created.id(), EstadoDocumento.VERIFICADO);
        assertEquals(EstadoDocumento.VERIFICADO, updated.estado());
    }

    @Test
    void shouldDeleteDocumento() {
        DocumentoResponse created = documentoService.create(cidadaoId, new DocumentoCreateRequest(
                TipoDocumento.OUTRO, "DEL001", null, null, null, null, null, null));

        documentoService.delete(created.id());

        assertThrows(ResourceNotFoundException.class, () -> documentoService.findById(created.id()));
    }

    @Test
    void shouldUploadFicheiro() {
        DocumentoResponse doc = documentoService.create(cidadaoId, new DocumentoCreateRequest(
                TipoDocumento.PASSAPORTE, "UP001", null, null, null, null, null, null));

        MockMultipartFile file = new MockMultipartFile(
                "file", "passport.pdf", "application/pdf", "PDF content".getBytes());

        DocumentoUploadResponse result = documentoService.uploadFicheiro(cidadaoId, doc.id(), file);

        assertNotNull(result);
        assertEquals(doc.id(), result.documentoId());
        assertEquals("passport.pdf", result.ficheiroNome());
        assertEquals("application/pdf", result.ficheiroTipo());
        assertEquals(1, result.versao());
    }

    @Test
    void shouldDownloadFicheiro() {
        DocumentoResponse doc = documentoService.create(cidadaoId, new DocumentoCreateRequest(
                TipoDocumento.PASSAPORTE, "DL001", null, null, null, null, null, null));

        byte[] content = "Test PDF content".getBytes();
        MockMultipartFile file = new MockMultipartFile(
                "file", "download-test.pdf", "application/pdf", content);
        documentoService.uploadFicheiro(cidadaoId, doc.id(), file);

        StorageDownloadResult result = documentoService.downloadFicheiro(doc.id());

        assertNotNull(result);
        assertEquals("application/pdf", result.contentType());
        assertEquals("download-test.pdf", result.filename());
    }

    @Test
    void shouldRejectOversizedFile() {
        DocumentoResponse doc = documentoService.create(cidadaoId, new DocumentoCreateRequest(
                TipoDocumento.PASSAPORTE, "BIG001", null, null, null, null, null, null));

        byte[] largeContent = new byte[21 * 1024 * 1024]; // 21MB
        MockMultipartFile file = new MockMultipartFile(
                "file", "big.pdf", "application/pdf", largeContent);

        assertThrows(InvalidFileException.class, () ->
                documentoService.uploadFicheiro(cidadaoId, doc.id(), file));
    }

    @Test
    void shouldRejectInvalidMimeType() {
        DocumentoResponse doc = documentoService.create(cidadaoId, new DocumentoCreateRequest(
                TipoDocumento.PASSAPORTE, "MIME001", null, null, null, null, null, null));

        MockMultipartFile file = new MockMultipartFile(
                "file", "virus.exe", "application/x-msdownload", "EXE content".getBytes());

        assertThrows(InvalidFileException.class, () ->
                documentoService.uploadFicheiro(cidadaoId, doc.id(), file));
    }

    @Test
    void shouldCreateNewVersion() {
        DocumentoResponse doc = documentoService.create(cidadaoId, new DocumentoCreateRequest(
                TipoDocumento.PASSAPORTE, "VER001", null, null, null, null, null, null));

        MockMultipartFile file1 = new MockMultipartFile(
                "file", "passport-v1.pdf", "application/pdf", "V1".getBytes());
        documentoService.uploadFicheiro(cidadaoId, doc.id(), file1);

        MockMultipartFile file2 = new MockMultipartFile(
                "file", "passport-v2.pdf", "application/pdf", "V2".getBytes());
        DocumentoUploadResponse newVersion = documentoService.createNewVersion(cidadaoId, doc.id(), file2);

        assertNotNull(newVersion);
        assertEquals(2, newVersion.versao());
        assertNotEquals(doc.id(), newVersion.documentoId());
    }

    @Test
    void shouldListVersions() {
        DocumentoResponse doc = documentoService.create(cidadaoId, new DocumentoCreateRequest(
                TipoDocumento.PASSAPORTE, "VLIST001", null, null, null, null, null, null));

        MockMultipartFile file1 = new MockMultipartFile(
                "file", "v1.pdf", "application/pdf", "V1".getBytes());
        documentoService.uploadFicheiro(cidadaoId, doc.id(), file1);

        MockMultipartFile file2 = new MockMultipartFile(
                "file", "v2.pdf", "application/pdf", "V2".getBytes());
        documentoService.createNewVersion(cidadaoId, doc.id(), file2);

        List<DocumentoVersionResponse> versions = documentoService.findVersions(doc.id());

        assertNotNull(versions);
        assertEquals(2, versions.size());
        assertEquals(2, versions.get(0).versao());
        assertEquals(1, versions.get(1).versao());
    }
}

package ao.gov.embaixada.sgc.service;

import ao.gov.embaixada.sgc.dto.*;
import ao.gov.embaixada.sgc.enums.EstadoDocumento;
import ao.gov.embaixada.sgc.enums.TipoDocumento;
import ao.gov.embaixada.sgc.exception.ResourceNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class DocumentoServiceTest {

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
}

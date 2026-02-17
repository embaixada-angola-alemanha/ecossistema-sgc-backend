package ao.gov.embaixada.sgc.controller;

import ao.gov.embaixada.commons.dto.ApiResponse;
import ao.gov.embaixada.commons.dto.PagedResponse;
import ao.gov.embaixada.sgc.dto.DocumentoCreateRequest;
import ao.gov.embaixada.sgc.dto.DocumentoResponse;
import ao.gov.embaixada.sgc.dto.DocumentoUpdateRequest;
import ao.gov.embaixada.sgc.enums.EstadoDocumento;
import ao.gov.embaixada.sgc.service.DocumentoService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/cidadaos/{cidadaoId}/documentos")
public class DocumentoController {

    private final DocumentoService documentoService;

    public DocumentoController(DocumentoService documentoService) {
        this.documentoService = documentoService;
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','CONSUL','OFFICER')")
    public ResponseEntity<ApiResponse<DocumentoResponse>> create(
            @PathVariable UUID cidadaoId, @Valid @RequestBody DocumentoCreateRequest request) {
        DocumentoResponse response = documentoService.create(cidadaoId, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Documento criado", response));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','CONSUL','OFFICER','VIEWER')")
    public ResponseEntity<ApiResponse<DocumentoResponse>> findById(
            @PathVariable UUID cidadaoId, @PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success(documentoService.findById(id)));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','CONSUL','OFFICER','VIEWER')")
    public ResponseEntity<ApiResponse<PagedResponse<DocumentoResponse>>> findByCidadao(
            @PathVariable UUID cidadaoId, @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.success(
                PagedResponse.of(documentoService.findByCidadaoId(cidadaoId, pageable))));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','CONSUL','OFFICER')")
    public ResponseEntity<ApiResponse<DocumentoResponse>> update(
            @PathVariable UUID cidadaoId, @PathVariable UUID id,
            @Valid @RequestBody DocumentoUpdateRequest request) {
        return ResponseEntity.ok(ApiResponse.success(documentoService.update(id, request)));
    }

    @PatchMapping("/{id}/estado")
    @PreAuthorize("hasAnyRole('ADMIN','CONSUL','OFFICER')")
    public ResponseEntity<ApiResponse<DocumentoResponse>> updateEstado(
            @PathVariable UUID cidadaoId, @PathVariable UUID id,
            @RequestBody Map<String, String> body) {
        EstadoDocumento estado = EstadoDocumento.valueOf(body.get("estado"));
        return ResponseEntity.ok(ApiResponse.success(documentoService.updateEstado(id, estado)));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','CONSUL')")
    public ResponseEntity<Void> delete(@PathVariable UUID cidadaoId, @PathVariable UUID id) {
        documentoService.delete(id);
        return ResponseEntity.noContent().build();
    }
}

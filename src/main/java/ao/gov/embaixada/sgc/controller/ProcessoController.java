package ao.gov.embaixada.sgc.controller;

import ao.gov.embaixada.commons.dto.ApiResponse;
import ao.gov.embaixada.commons.dto.PagedResponse;
import ao.gov.embaixada.sgc.dto.ProcessoCreateRequest;
import ao.gov.embaixada.sgc.dto.ProcessoHistoricoResponse;
import ao.gov.embaixada.sgc.dto.ProcessoResponse;
import ao.gov.embaixada.sgc.dto.ProcessoUpdateRequest;
import ao.gov.embaixada.sgc.enums.EstadoProcesso;
import ao.gov.embaixada.sgc.enums.TipoProcesso;
import ao.gov.embaixada.sgc.service.ProcessoService;
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
@RequestMapping("/api/v1/processos")
public class ProcessoController {

    private final ProcessoService processoService;

    public ProcessoController(ProcessoService processoService) {
        this.processoService = processoService;
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','CONSUL','OFFICER')")
    public ResponseEntity<ApiResponse<ProcessoResponse>> create(
            @Valid @RequestBody ProcessoCreateRequest request) {
        ProcessoResponse response = processoService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Processo criado", response));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','CONSUL','OFFICER','VIEWER')")
    public ResponseEntity<ApiResponse<ProcessoResponse>> findById(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success(processoService.findById(id)));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','CONSUL','OFFICER','VIEWER')")
    public ResponseEntity<ApiResponse<PagedResponse<ProcessoResponse>>> findAll(
            @RequestParam(required = false) UUID cidadaoId,
            @RequestParam(required = false) EstadoProcesso estado,
            @RequestParam(required = false) TipoProcesso tipo,
            @PageableDefault(size = 20) Pageable pageable) {
        if (cidadaoId != null) {
            return ResponseEntity.ok(ApiResponse.success(
                    PagedResponse.of(processoService.findByCidadaoId(cidadaoId, pageable))));
        }
        if (estado != null) {
            return ResponseEntity.ok(ApiResponse.success(
                    PagedResponse.of(processoService.findByEstado(estado, pageable))));
        }
        if (tipo != null) {
            return ResponseEntity.ok(ApiResponse.success(
                    PagedResponse.of(processoService.findByTipo(tipo, pageable))));
        }
        return ResponseEntity.ok(ApiResponse.success(
                PagedResponse.of(processoService.findAll(pageable))));
    }

    @GetMapping("/{id}/historico")
    @PreAuthorize("hasAnyRole('ADMIN','CONSUL','OFFICER','VIEWER')")
    public ResponseEntity<ApiResponse<PagedResponse<ProcessoHistoricoResponse>>> findHistorico(
            @PathVariable UUID id, @PageableDefault(size = 50) Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.success(
                PagedResponse.of(processoService.findHistorico(id, pageable))));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','CONSUL','OFFICER')")
    public ResponseEntity<ApiResponse<ProcessoResponse>> update(
            @PathVariable UUID id, @Valid @RequestBody ProcessoUpdateRequest request) {
        return ResponseEntity.ok(ApiResponse.success(processoService.update(id, request)));
    }

    @PatchMapping("/{id}/estado")
    @PreAuthorize("hasAnyRole('ADMIN','CONSUL','OFFICER')")
    public ResponseEntity<ApiResponse<ProcessoResponse>> updateEstado(
            @PathVariable UUID id, @RequestBody Map<String, String> body) {
        EstadoProcesso estado = EstadoProcesso.valueOf(body.get("estado"));
        String comentario = body.getOrDefault("comentario", null);
        return ResponseEntity.ok(ApiResponse.success(processoService.updateEstado(id, estado, comentario)));
    }

    @PostMapping("/{id}/documentos/{documentoId}")
    @PreAuthorize("hasAnyRole('ADMIN','CONSUL','OFFICER')")
    public ResponseEntity<ApiResponse<ProcessoResponse>> addDocumento(
            @PathVariable UUID id, @PathVariable UUID documentoId) {
        return ResponseEntity.ok(ApiResponse.success(processoService.addDocumento(id, documentoId)));
    }

    @DeleteMapping("/{id}/documentos/{documentoId}")
    @PreAuthorize("hasAnyRole('ADMIN','CONSUL','OFFICER')")
    public ResponseEntity<ApiResponse<ProcessoResponse>> removeDocumento(
            @PathVariable UUID id, @PathVariable UUID documentoId) {
        return ResponseEntity.ok(ApiResponse.success(processoService.removeDocumento(id, documentoId)));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        processoService.delete(id);
        return ResponseEntity.noContent().build();
    }
}

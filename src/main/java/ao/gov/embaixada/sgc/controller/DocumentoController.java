package ao.gov.embaixada.sgc.controller;

import ao.gov.embaixada.commons.dto.ApiResponse;
import ao.gov.embaixada.commons.dto.PagedResponse;
import ao.gov.embaixada.sgc.dto.*;
import ao.gov.embaixada.sgc.enums.EstadoDocumento;
import ao.gov.embaixada.sgc.service.CitizenContextService;
import ao.gov.embaixada.sgc.service.DocumentoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.core.io.InputStreamResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/cidadaos/{cidadaoId}/documentos")
@Tag(name = "Documentos", description = "Gestao de documentos de cidadaos")
public class DocumentoController {

    private final DocumentoService documentoService;
    private final CitizenContextService citizenContext;

    public DocumentoController(DocumentoService documentoService, CitizenContextService citizenContext) {
        this.documentoService = documentoService;
        this.citizenContext = citizenContext;
    }

    private void verifyCitizenAccess(UUID cidadaoId) {
        if (citizenContext.isCitizenOnly() && !citizenContext.canAccessCidadaoData(cidadaoId)) {
            throw new AccessDeniedException("Access denied");
        }
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','CONSUL','OFFICER','CITIZEN')")
    @Operation(summary = "Criar documento", description = "Associa um novo documento a um cidadao")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Documento criado"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Dados invalidos"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Cidadao nao encontrado")
    })
    public ResponseEntity<ApiResponse<DocumentoResponse>> create(
            @PathVariable UUID cidadaoId, @Valid @RequestBody DocumentoCreateRequest request) {
        if (citizenContext.isCitizenOnly()) {
            cidadaoId = citizenContext.requireCurrentCidadaoId();
        }
        verifyCitizenAccess(cidadaoId);
        DocumentoResponse response = documentoService.create(cidadaoId, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Documento criado", response));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','CONSUL','OFFICER','CITIZEN','VIEWER')")
    @Operation(summary = "Obter documento por ID", description = "Retorna os dados de um documento especifico")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Documento encontrado"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Documento nao encontrado")
    })
    public ResponseEntity<ApiResponse<DocumentoResponse>> findById(
            @PathVariable UUID cidadaoId, @PathVariable UUID id) {
        verifyCitizenAccess(cidadaoId);
        return ResponseEntity.ok(ApiResponse.success(documentoService.findById(id)));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','CONSUL','OFFICER','CITIZEN','VIEWER')")
    @Operation(summary = "Listar documentos do cidadao", description = "Lista todos os documentos associados a um cidadao")
    public ResponseEntity<ApiResponse<PagedResponse<DocumentoResponse>>> findByCidadao(
            @PathVariable UUID cidadaoId, @PageableDefault(size = 20) Pageable pageable) {
        if (citizenContext.isCitizenOnly()) {
            Optional<UUID> ownId = citizenContext.getCurrentCidadaoId();
            if (ownId.isEmpty()) {
                return ResponseEntity.ok(ApiResponse.success(PagedResponse.of(Page.empty(pageable))));
            }
            cidadaoId = ownId.get();
        }
        verifyCitizenAccess(cidadaoId);
        return ResponseEntity.ok(ApiResponse.success(
                PagedResponse.of(documentoService.findByCidadaoId(cidadaoId, pageable))));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','CONSUL','OFFICER')")
    @Operation(summary = "Actualizar documento", description = "Actualiza os dados de um documento existente")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Documento actualizado"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Documento nao encontrado")
    })
    public ResponseEntity<ApiResponse<DocumentoResponse>> update(
            @PathVariable UUID cidadaoId, @PathVariable UUID id,
            @Valid @RequestBody DocumentoUpdateRequest request) {
        return ResponseEntity.ok(ApiResponse.success(documentoService.update(id, request)));
    }

    @PatchMapping("/{id}/estado")
    @PreAuthorize("hasAnyRole('ADMIN','CONSUL','OFFICER')")
    @Operation(summary = "Alterar estado do documento", description = "Altera o estado de verificacao de um documento")
    public ResponseEntity<ApiResponse<DocumentoResponse>> updateEstado(
            @PathVariable UUID cidadaoId, @PathVariable UUID id,
            @RequestBody Map<String, String> body) {
        EstadoDocumento estado = EstadoDocumento.valueOf(body.get("estado"));
        return ResponseEntity.ok(ApiResponse.success(documentoService.updateEstado(id, estado)));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','CONSUL')")
    @Operation(summary = "Eliminar documento", description = "Remove permanentemente um documento")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "204", description = "Documento eliminado"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Documento nao encontrado")
    })
    public ResponseEntity<Void> delete(@PathVariable UUID cidadaoId, @PathVariable UUID id) {
        documentoService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping(value = "/{id}/ficheiro", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAnyRole('ADMIN','CONSUL','OFFICER','CITIZEN')")
    @Operation(summary = "Upload de ficheiro", description = "Faz upload de um ficheiro para um documento existente")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Ficheiro carregado"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Ficheiro invalido"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Documento nao encontrado"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "413", description = "Ficheiro demasiado grande")
    })
    public ResponseEntity<ApiResponse<DocumentoUploadResponse>> uploadFicheiro(
            @PathVariable UUID cidadaoId, @PathVariable UUID id,
            @RequestParam("file") MultipartFile file) {
        if (citizenContext.isCitizenOnly()) {
            cidadaoId = citizenContext.requireCurrentCidadaoId();
        }
        verifyCitizenAccess(cidadaoId);
        DocumentoUploadResponse response = documentoService.uploadFicheiro(cidadaoId, id, file);
        return ResponseEntity.ok(ApiResponse.success("Ficheiro carregado", response));
    }

    @GetMapping("/{id}/ficheiro")
    @PreAuthorize("hasAnyRole('ADMIN','CONSUL','OFFICER','CITIZEN','VIEWER')")
    @Operation(summary = "Download de ficheiro", description = "Faz download do ficheiro associado a um documento")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Ficheiro retornado"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Documento ou ficheiro nao encontrado")
    })
    public ResponseEntity<InputStreamResource> downloadFicheiro(
            @PathVariable UUID cidadaoId, @PathVariable UUID id) {
        verifyCitizenAccess(cidadaoId);
        StorageDownloadResult result = documentoService.downloadFicheiro(id);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + result.filename() + "\"")
                .contentType(MediaType.parseMediaType(result.contentType()))
                .contentLength(result.size())
                .body(new InputStreamResource(result.inputStream()));
    }

    @PostMapping(value = "/{id}/versoes", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAnyRole('ADMIN','CONSUL','OFFICER')")
    @Operation(summary = "Criar nova versao", description = "Cria uma nova versao do documento com um novo ficheiro")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Nova versao criada"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Ficheiro invalido"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Documento nao encontrado")
    })
    public ResponseEntity<ApiResponse<DocumentoUploadResponse>> createNewVersion(
            @PathVariable UUID cidadaoId, @PathVariable UUID id,
            @RequestParam("file") MultipartFile file) {
        DocumentoUploadResponse response = documentoService.createNewVersion(cidadaoId, id, file);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Nova versao criada", response));
    }

    @GetMapping("/{id}/versoes")
    @PreAuthorize("hasAnyRole('ADMIN','CONSUL','OFFICER','CITIZEN','VIEWER')")
    @Operation(summary = "Listar versoes", description = "Lista todas as versoes de um documento")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Versoes listadas"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Documento nao encontrado")
    })
    public ResponseEntity<ApiResponse<List<DocumentoVersionResponse>>> findVersions(
            @PathVariable UUID cidadaoId, @PathVariable UUID id) {
        verifyCitizenAccess(cidadaoId);
        List<DocumentoVersionResponse> versions = documentoService.findVersions(id);
        return ResponseEntity.ok(ApiResponse.success(versions));
    }
}

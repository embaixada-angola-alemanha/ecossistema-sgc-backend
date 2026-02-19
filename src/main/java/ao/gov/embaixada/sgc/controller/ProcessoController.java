package ao.gov.embaixada.sgc.controller;

import ao.gov.embaixada.commons.dto.ApiResponse;
import ao.gov.embaixada.commons.dto.PagedResponse;
import ao.gov.embaixada.sgc.dto.ProcessoCreateRequest;
import ao.gov.embaixada.sgc.dto.ProcessoHistoricoResponse;
import ao.gov.embaixada.sgc.dto.ProcessoResponse;
import ao.gov.embaixada.sgc.dto.ProcessoUpdateRequest;
import ao.gov.embaixada.sgc.enums.EstadoProcesso;
import ao.gov.embaixada.sgc.enums.TipoProcesso;
import ao.gov.embaixada.sgc.service.CitizenContextService;
import ao.gov.embaixada.sgc.service.ProcessoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/processos")
@Tag(name = "Processos", description = "Processos consulares e seu ciclo de vida")
public class ProcessoController {

    private final ProcessoService processoService;
    private final CitizenContextService citizenContext;

    public ProcessoController(ProcessoService processoService, CitizenContextService citizenContext) {
        this.processoService = processoService;
        this.citizenContext = citizenContext;
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','CONSUL','OFFICER')")
    @Operation(summary = "Criar processo", description = "Cria um novo processo consular")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Processo criado"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Dados invalidos"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Cidadao nao encontrado")
    })
    public ResponseEntity<ApiResponse<ProcessoResponse>> create(
            @Valid @RequestBody ProcessoCreateRequest request) {
        ProcessoResponse response = processoService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Processo criado", response));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','CONSUL','OFFICER','CITIZEN','VIEWER')")
    @Operation(summary = "Obter processo por ID", description = "Retorna os dados de um processo consular")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Processo encontrado"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Processo nao encontrado")
    })
    public ResponseEntity<ApiResponse<ProcessoResponse>> findById(@PathVariable UUID id) {
        ProcessoResponse response = processoService.findById(id);
        if (citizenContext.isCitizenOnly() && !citizenContext.canAccessCidadaoData(response.cidadaoId())) {
            throw new AccessDeniedException("Access denied");
        }
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','CONSUL','OFFICER','CITIZEN','VIEWER')")
    @Operation(summary = "Listar processos", description = "Lista processos com filtros opcionais por cidadao, estado e tipo")
    public ResponseEntity<ApiResponse<PagedResponse<ProcessoResponse>>> findAll(
            @RequestParam(required = false) UUID cidadaoId,
            @RequestParam(required = false) EstadoProcesso estado,
            @RequestParam(required = false) TipoProcesso tipo,
            @PageableDefault(size = 20) Pageable pageable) {
        if (citizenContext.isCitizenOnly()) {
            Optional<UUID> ownId = citizenContext.getCurrentCidadaoId();
            if (ownId.isEmpty()) {
                return ResponseEntity.ok(ApiResponse.success(PagedResponse.of(Page.empty(pageable))));
            }
            cidadaoId = ownId.get();
        }
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
    @PreAuthorize("hasAnyRole('ADMIN','CONSUL','OFFICER','CITIZEN','VIEWER')")
    @Operation(summary = "Historico do processo", description = "Retorna o historico de transicoes de estado do processo")
    public ResponseEntity<ApiResponse<PagedResponse<ProcessoHistoricoResponse>>> findHistorico(
            @PathVariable UUID id, @PageableDefault(size = 50) Pageable pageable) {
        if (citizenContext.isCitizenOnly()) {
            ProcessoResponse processo = processoService.findById(id);
            if (!citizenContext.canAccessCidadaoData(processo.cidadaoId())) {
                throw new AccessDeniedException("Access denied");
            }
        }
        return ResponseEntity.ok(ApiResponse.success(
                PagedResponse.of(processoService.findHistorico(id, pageable))));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','CONSUL','OFFICER')")
    @Operation(summary = "Actualizar processo", description = "Actualiza os dados de um processo existente")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Processo actualizado"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Processo nao encontrado")
    })
    public ResponseEntity<ApiResponse<ProcessoResponse>> update(
            @PathVariable UUID id, @Valid @RequestBody ProcessoUpdateRequest request) {
        return ResponseEntity.ok(ApiResponse.success(processoService.update(id, request)));
    }

    @PatchMapping("/{id}/estado")
    @PreAuthorize("hasAnyRole('ADMIN','CONSUL','OFFICER')")
    @Operation(summary = "Alterar estado do processo", description = "Transita o processo para um novo estado seguindo a maquina de estados")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Estado alterado"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "Transicao de estado invalida")
    })
    public ResponseEntity<ApiResponse<ProcessoResponse>> updateEstado(
            @PathVariable UUID id, @RequestBody Map<String, String> body) {
        EstadoProcesso estado = EstadoProcesso.valueOf(body.get("estado"));
        String comentario = body.getOrDefault("comentario", null);
        return ResponseEntity.ok(ApiResponse.success(processoService.updateEstado(id, estado, comentario)));
    }

    @PostMapping("/{id}/documentos/{documentoId}")
    @PreAuthorize("hasAnyRole('ADMIN','CONSUL','OFFICER')")
    @Operation(summary = "Associar documento ao processo", description = "Adiciona um documento existente a um processo")
    public ResponseEntity<ApiResponse<ProcessoResponse>> addDocumento(
            @PathVariable UUID id, @PathVariable UUID documentoId) {
        return ResponseEntity.ok(ApiResponse.success(processoService.addDocumento(id, documentoId)));
    }

    @DeleteMapping("/{id}/documentos/{documentoId}")
    @PreAuthorize("hasAnyRole('ADMIN','CONSUL','OFFICER')")
    @Operation(summary = "Desassociar documento do processo", description = "Remove a associacao de um documento a um processo")
    public ResponseEntity<ApiResponse<ProcessoResponse>> removeDocumento(
            @PathVariable UUID id, @PathVariable UUID documentoId) {
        return ResponseEntity.ok(ApiResponse.success(processoService.removeDocumento(id, documentoId)));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Eliminar processo", description = "Remove permanentemente um processo consular")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "204", description = "Processo eliminado"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Processo nao encontrado")
    })
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        processoService.delete(id);
        return ResponseEntity.noContent().build();
    }
}

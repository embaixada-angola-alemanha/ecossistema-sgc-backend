package ao.gov.embaixada.sgc.controller;

import ao.gov.embaixada.commons.dto.ApiResponse;
import ao.gov.embaixada.commons.dto.PagedResponse;
import ao.gov.embaixada.sgc.dto.CidadaoCreateRequest;
import ao.gov.embaixada.sgc.dto.CidadaoResponse;
import ao.gov.embaixada.sgc.dto.CidadaoUpdateRequest;
import ao.gov.embaixada.sgc.enums.EstadoCidadao;
import ao.gov.embaixada.sgc.enums.Sexo;
import ao.gov.embaixada.sgc.service.CidadaoService;
import ao.gov.embaixada.sgc.service.CitizenContextService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/cidadaos")
@Tag(name = "Cidadaos", description = "Gestao de cidadaos registados no sistema consular")
public class CidadaoController {

    private final CidadaoService cidadaoService;
    private final CitizenContextService citizenContext;

    public CidadaoController(CidadaoService cidadaoService, CitizenContextService citizenContext) {
        this.cidadaoService = cidadaoService;
        this.citizenContext = citizenContext;
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','CONSUL','OFFICER')")
    @Operation(summary = "Criar cidadao", description = "Regista um novo cidadao no sistema consular")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Cidadao criado"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Dados invalidos"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "Passaporte duplicado")
    })
    public ResponseEntity<ApiResponse<CidadaoResponse>> create(@Valid @RequestBody CidadaoCreateRequest request) {
        CidadaoResponse response = cidadaoService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Cidadao criado", response));
    }

    @GetMapping("/me")
    @PreAuthorize("hasRole('CITIZEN')")
    @Operation(summary = "Obter perfil proprio", description = "Retorna o perfil do cidadao associado a conta autenticada")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Perfil encontrado"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Perfil nao associado")
    })
    public ResponseEntity<ApiResponse<CidadaoResponse>> getMe() {
        UUID cidadaoId = citizenContext.requireCurrentCidadaoId();
        return ResponseEntity.ok(ApiResponse.success(cidadaoService.findById(cidadaoId)));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','CONSUL','OFFICER','CITIZEN','VIEWER')")
    @Operation(summary = "Obter cidadao por ID", description = "Retorna os dados de um cidadao pelo seu identificador")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Cidadao encontrado"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Cidadao nao encontrado")
    })
    public ResponseEntity<ApiResponse<CidadaoResponse>> findById(@PathVariable UUID id) {
        if (citizenContext.isCitizenOnly() && !citizenContext.canAccessCidadaoData(id)) {
            throw new AccessDeniedException("Access denied");
        }
        return ResponseEntity.ok(ApiResponse.success(cidadaoService.findById(id)));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','CONSUL','OFFICER','CITIZEN','VIEWER')")
    @Operation(summary = "Listar cidadaos", description = "Lista cidadaos com filtros opcionais por nome, estado, sexo e nacionalidade")
    public ResponseEntity<ApiResponse<PagedResponse<CidadaoResponse>>> findAll(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) EstadoCidadao estado,
            @RequestParam(required = false) Sexo sexo,
            @RequestParam(required = false) String nacionalidade,
            @PageableDefault(size = 20) Pageable pageable) {
        if (citizenContext.isCitizenOnly()) {
            Optional<UUID> ownId = citizenContext.getCurrentCidadaoId();
            if (ownId.isEmpty()) {
                Page<CidadaoResponse> empty = new PageImpl<>(Collections.emptyList(), pageable, 0);
                return ResponseEntity.ok(ApiResponse.success(PagedResponse.of(empty)));
            }
            CidadaoResponse own = cidadaoService.findById(ownId.get());
            Page<CidadaoResponse> page = new PageImpl<>(List.of(own), pageable, 1);
            return ResponseEntity.ok(ApiResponse.success(PagedResponse.of(page)));
        }
        return ResponseEntity.ok(ApiResponse.success(
                PagedResponse.of(cidadaoService.findAll(search, estado, sexo, nacionalidade, pageable))));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','CONSUL','OFFICER','CITIZEN')")
    @Operation(summary = "Actualizar cidadao", description = "Actualiza os dados pessoais de um cidadao")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Cidadao actualizado"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Cidadao nao encontrado")
    })
    public ResponseEntity<ApiResponse<CidadaoResponse>> update(
            @PathVariable UUID id, @Valid @RequestBody CidadaoUpdateRequest request) {
        if (citizenContext.isCitizenOnly() && !citizenContext.canAccessCidadaoData(id)) {
            throw new AccessDeniedException("Access denied");
        }
        return ResponseEntity.ok(ApiResponse.success(cidadaoService.update(id, request)));
    }

    @PatchMapping("/{id}/estado")
    @PreAuthorize("hasAnyRole('ADMIN','CONSUL')")
    @Operation(summary = "Alterar estado do cidadao", description = "Altera o estado de um cidadao (ACTIVO, INACTIVO, SUSPENSO)")
    public ResponseEntity<ApiResponse<CidadaoResponse>> updateEstado(
            @PathVariable UUID id, @RequestBody Map<String, String> body) {
        EstadoCidadao estado = EstadoCidadao.valueOf(body.get("estado"));
        return ResponseEntity.ok(ApiResponse.success(cidadaoService.updateEstado(id, estado)));
    }

    @PatchMapping("/{id}/link")
    @PreAuthorize("hasAnyRole('ADMIN','CONSUL')")
    @Operation(summary = "Associar conta Keycloak", description = "Associa um cidadao a uma conta Keycloak pelo keycloakId")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Conta associada"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Cidadao nao encontrado")
    })
    public ResponseEntity<ApiResponse<CidadaoResponse>> linkKeycloak(
            @PathVariable UUID id, @RequestBody Map<String, String> body) {
        String keycloakId = body.get("keycloakId");
        return ResponseEntity.ok(ApiResponse.success(cidadaoService.linkKeycloak(id, keycloakId)));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Eliminar cidadao", description = "Remove permanentemente um cidadao do sistema")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "204", description = "Cidadao eliminado"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Cidadao nao encontrado")
    })
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        cidadaoService.delete(id);
        return ResponseEntity.noContent().build();
    }
}

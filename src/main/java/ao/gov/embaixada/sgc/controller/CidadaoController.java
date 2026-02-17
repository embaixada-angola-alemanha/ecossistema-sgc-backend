package ao.gov.embaixada.sgc.controller;

import ao.gov.embaixada.commons.dto.ApiResponse;
import ao.gov.embaixada.commons.dto.PagedResponse;
import ao.gov.embaixada.sgc.dto.CidadaoCreateRequest;
import ao.gov.embaixada.sgc.dto.CidadaoResponse;
import ao.gov.embaixada.sgc.dto.CidadaoUpdateRequest;
import ao.gov.embaixada.sgc.enums.EstadoCidadao;
import ao.gov.embaixada.sgc.enums.Sexo;
import ao.gov.embaixada.sgc.service.CidadaoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@RequestMapping("/api/v1/cidadaos")
@Tag(name = "Cidadaos", description = "Gestao de cidadaos registados no sistema consular")
public class CidadaoController {

    private final CidadaoService cidadaoService;

    public CidadaoController(CidadaoService cidadaoService) {
        this.cidadaoService = cidadaoService;
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

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','CONSUL','OFFICER','VIEWER')")
    @Operation(summary = "Obter cidadao por ID", description = "Retorna os dados de um cidadao pelo seu identificador")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Cidadao encontrado"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Cidadao nao encontrado")
    })
    public ResponseEntity<ApiResponse<CidadaoResponse>> findById(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success(cidadaoService.findById(id)));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','CONSUL','OFFICER','VIEWER')")
    @Operation(summary = "Listar cidadaos", description = "Lista cidadaos com filtros opcionais por nome, estado, sexo e nacionalidade")
    public ResponseEntity<ApiResponse<PagedResponse<CidadaoResponse>>> findAll(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) EstadoCidadao estado,
            @RequestParam(required = false) Sexo sexo,
            @RequestParam(required = false) String nacionalidade,
            @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.success(
                PagedResponse.of(cidadaoService.findAll(search, estado, sexo, nacionalidade, pageable))));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','CONSUL','OFFICER')")
    @Operation(summary = "Actualizar cidadao", description = "Actualiza os dados pessoais de um cidadao")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Cidadao actualizado"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Cidadao nao encontrado")
    })
    public ResponseEntity<ApiResponse<CidadaoResponse>> update(
            @PathVariable UUID id, @Valid @RequestBody CidadaoUpdateRequest request) {
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

package ao.gov.embaixada.sgc.controller;

import ao.gov.embaixada.commons.dto.ApiResponse;
import ao.gov.embaixada.commons.dto.PagedResponse;
import ao.gov.embaixada.sgc.dto.CidadaoCreateRequest;
import ao.gov.embaixada.sgc.dto.CidadaoResponse;
import ao.gov.embaixada.sgc.dto.CidadaoUpdateRequest;
import ao.gov.embaixada.sgc.enums.EstadoCidadao;
import ao.gov.embaixada.sgc.enums.Sexo;
import ao.gov.embaixada.sgc.service.CidadaoService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/cidadaos")
public class CidadaoController {

    private final CidadaoService cidadaoService;

    public CidadaoController(CidadaoService cidadaoService) {
        this.cidadaoService = cidadaoService;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<CidadaoResponse>> create(@Valid @RequestBody CidadaoCreateRequest request) {
        CidadaoResponse response = cidadaoService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Cidadao criado", response));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<CidadaoResponse>> findById(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success(cidadaoService.findById(id)));
    }

    @GetMapping
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
    public ResponseEntity<ApiResponse<CidadaoResponse>> update(
            @PathVariable UUID id, @Valid @RequestBody CidadaoUpdateRequest request) {
        return ResponseEntity.ok(ApiResponse.success(cidadaoService.update(id, request)));
    }

    @PatchMapping("/{id}/estado")
    public ResponseEntity<ApiResponse<CidadaoResponse>> updateEstado(
            @PathVariable UUID id, @RequestBody Map<String, String> body) {
        EstadoCidadao estado = EstadoCidadao.valueOf(body.get("estado"));
        return ResponseEntity.ok(ApiResponse.success(cidadaoService.updateEstado(id, estado)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        cidadaoService.delete(id);
        return ResponseEntity.noContent().build();
    }
}

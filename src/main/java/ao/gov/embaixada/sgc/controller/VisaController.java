package ao.gov.embaixada.sgc.controller;

import ao.gov.embaixada.commons.dto.ApiResponse;
import ao.gov.embaixada.commons.dto.PagedResponse;
import ao.gov.embaixada.sgc.dto.*;
import ao.gov.embaixada.sgc.enums.EstadoVisto;
import ao.gov.embaixada.sgc.enums.TipoVisto;
import ao.gov.embaixada.sgc.service.VisaDocumentChecklistService;
import ao.gov.embaixada.sgc.service.VisaFeeCalculator;
import ao.gov.embaixada.sgc.service.VisaService;
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
@RequestMapping("/api/v1/visas")
@Tag(name = "Vistos", description = "Pedidos de visto e processamento")
public class VisaController {

    private final VisaService visaService;
    private final VisaFeeCalculator feeCalculator;
    private final VisaDocumentChecklistService checklistService;

    public VisaController(VisaService visaService,
                          VisaFeeCalculator feeCalculator,
                          VisaDocumentChecklistService checklistService) {
        this.visaService = visaService;
        this.feeCalculator = feeCalculator;
        this.checklistService = checklistService;
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','CONSUL','OFFICER')")
    @Operation(summary = "Criar pedido de visto", description = "Cria um novo pedido de visto para um cidadao")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Pedido criado"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Dados invalidos"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Cidadao nao encontrado")
    })
    public ResponseEntity<ApiResponse<VisaResponse>> create(
            @Valid @RequestBody VisaCreateRequest request) {
        VisaResponse response = visaService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Pedido de visto criado", response));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','CONSUL','OFFICER','VIEWER')")
    @Operation(summary = "Obter visto por ID", description = "Retorna os dados de um pedido de visto")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Visto encontrado"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Visto nao encontrado")
    })
    public ResponseEntity<ApiResponse<VisaResponse>> findById(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success(visaService.findById(id)));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','CONSUL','OFFICER','VIEWER')")
    @Operation(summary = "Listar vistos", description = "Lista pedidos de visto com filtros opcionais por cidadao, estado e tipo")
    public ResponseEntity<ApiResponse<PagedResponse<VisaResponse>>> findAll(
            @RequestParam(required = false) UUID cidadaoId,
            @RequestParam(required = false) EstadoVisto estado,
            @RequestParam(required = false) TipoVisto tipo,
            @PageableDefault(size = 20) Pageable pageable) {
        if (cidadaoId != null) {
            return ResponseEntity.ok(ApiResponse.success(
                    PagedResponse.of(visaService.findByCidadaoId(cidadaoId, pageable))));
        }
        if (estado != null) {
            return ResponseEntity.ok(ApiResponse.success(
                    PagedResponse.of(visaService.findByEstado(estado, pageable))));
        }
        if (tipo != null) {
            return ResponseEntity.ok(ApiResponse.success(
                    PagedResponse.of(visaService.findByTipo(tipo, pageable))));
        }
        return ResponseEntity.ok(ApiResponse.success(
                PagedResponse.of(visaService.findAll(pageable))));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','CONSUL','OFFICER')")
    @Operation(summary = "Actualizar visto", description = "Actualiza os dados de um pedido de visto")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Visto actualizado"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Visto nao encontrado")
    })
    public ResponseEntity<ApiResponse<VisaResponse>> update(
            @PathVariable UUID id, @Valid @RequestBody VisaUpdateRequest request) {
        return ResponseEntity.ok(ApiResponse.success(visaService.update(id, request)));
    }

    @PatchMapping("/{id}/estado")
    @PreAuthorize("hasAnyRole('ADMIN','CONSUL','OFFICER')")
    @Operation(summary = "Alterar estado do visto", description = "Transita o visto para um novo estado seguindo a maquina de estados")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Estado alterado"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "Transicao de estado invalida")
    })
    public ResponseEntity<ApiResponse<VisaResponse>> updateEstado(
            @PathVariable UUID id, @RequestBody Map<String, String> body) {
        EstadoVisto estado = EstadoVisto.valueOf(body.get("estado"));
        String comentario = body.getOrDefault("comentario", null);
        return ResponseEntity.ok(ApiResponse.success(visaService.updateEstado(id, estado, comentario)));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Eliminar visto", description = "Remove permanentemente um pedido de visto")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "204", description = "Visto eliminado"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Visto nao encontrado")
    })
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        visaService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}/historico")
    @PreAuthorize("hasAnyRole('ADMIN','CONSUL','OFFICER','VIEWER')")
    @Operation(summary = "Historico do visto", description = "Retorna o historico de transicoes de estado do visto")
    public ResponseEntity<ApiResponse<PagedResponse<VisaHistoricoResponse>>> findHistorico(
            @PathVariable UUID id, @PageableDefault(size = 50) Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.success(
                PagedResponse.of(visaService.findHistorico(id, pageable))));
    }

    @PostMapping("/{id}/documentos/{documentoId}")
    @PreAuthorize("hasAnyRole('ADMIN','CONSUL','OFFICER')")
    @Operation(summary = "Associar documento ao visto", description = "Adiciona um documento existente a um pedido de visto")
    public ResponseEntity<ApiResponse<VisaResponse>> addDocumento(
            @PathVariable UUID id, @PathVariable UUID documentoId) {
        return ResponseEntity.ok(ApiResponse.success(visaService.addDocumento(id, documentoId)));
    }

    @DeleteMapping("/{id}/documentos/{documentoId}")
    @PreAuthorize("hasAnyRole('ADMIN','CONSUL','OFFICER')")
    @Operation(summary = "Desassociar documento do visto", description = "Remove a associacao de um documento a um pedido de visto")
    public ResponseEntity<ApiResponse<VisaResponse>> removeDocumento(
            @PathVariable UUID id, @PathVariable UUID documentoId) {
        return ResponseEntity.ok(ApiResponse.success(visaService.removeDocumento(id, documentoId)));
    }

    @GetMapping("/fees")
    @PreAuthorize("hasAnyRole('ADMIN','CONSUL','OFFICER','VIEWER')")
    @Operation(summary = "Consultar taxa de visto", description = "Retorna o valor da taxa para um tipo de visto")
    public ResponseEntity<ApiResponse<VisaFeeResponse>> getFee(@RequestParam TipoVisto tipo) {
        return ResponseEntity.ok(ApiResponse.success(feeCalculator.getFeeResponse(tipo)));
    }

    @GetMapping("/checklist")
    @PreAuthorize("hasAnyRole('ADMIN','CONSUL','OFFICER','VIEWER')")
    @Operation(summary = "Consultar checklist de documentos", description = "Retorna a lista de documentos requeridos para um tipo de visto")
    public ResponseEntity<ApiResponse<VisaChecklistResponse>> getChecklist(@RequestParam TipoVisto tipo) {
        return ResponseEntity.ok(ApiResponse.success(checklistService.getChecklistResponse(tipo)));
    }
}

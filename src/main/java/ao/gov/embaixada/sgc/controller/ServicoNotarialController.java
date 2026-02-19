package ao.gov.embaixada.sgc.controller;

import ao.gov.embaixada.commons.dto.ApiResponse;
import ao.gov.embaixada.commons.dto.PagedResponse;
import ao.gov.embaixada.commons.storage.StorageService;
import ao.gov.embaixada.sgc.dto.NotarialFeeResponse;
import ao.gov.embaixada.sgc.dto.ServicoNotarialCreateRequest;
import ao.gov.embaixada.sgc.dto.ServicoNotarialHistoricoResponse;
import ao.gov.embaixada.sgc.dto.ServicoNotarialResponse;
import ao.gov.embaixada.sgc.dto.ServicoNotarialUpdateRequest;
import ao.gov.embaixada.sgc.enums.EstadoServicoNotarial;
import ao.gov.embaixada.sgc.enums.TipoServicoNotarial;
import ao.gov.embaixada.sgc.exception.ResourceNotFoundException;
import ao.gov.embaixada.sgc.service.CitizenContextService;
import ao.gov.embaixada.sgc.service.NotarialFeeCalculator;
import ao.gov.embaixada.sgc.service.ServicoNotarialService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.core.io.InputStreamResource;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.io.InputStream;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/servicos-notariais")
@Tag(name = "Servicos Notariais", description = "Gestao de servicos notariais (procuracoes, legalizacoes, apostilas, copias certificadas)")
public class ServicoNotarialController {

    private final ServicoNotarialService servicoNotarialService;
    private final NotarialFeeCalculator feeCalculator;
    private final StorageService storageService;
    private final CitizenContextService citizenContext;

    public ServicoNotarialController(ServicoNotarialService servicoNotarialService,
                                     NotarialFeeCalculator feeCalculator,
                                     StorageService storageService,
                                     CitizenContextService citizenContext) {
        this.servicoNotarialService = servicoNotarialService;
        this.feeCalculator = feeCalculator;
        this.storageService = storageService;
        this.citizenContext = citizenContext;
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','CONSUL','OFFICER','CITIZEN')")
    @Operation(summary = "Criar servico notarial", description = "Cria um novo pedido de servico notarial")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Servico criado"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Dados invalidos"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Cidadao nao encontrado")
    })
    public ResponseEntity<ApiResponse<ServicoNotarialResponse>> create(
            @Valid @RequestBody ServicoNotarialCreateRequest request) {
        if (citizenContext.isCitizenOnly()) {
            UUID ownId = citizenContext.requireCurrentCidadaoId();
            request = new ServicoNotarialCreateRequest(ownId, request.tipo(), request.descricao(),
                    request.observacoes(), request.agendamentoId(),
                    request.outorgante(), request.outorgado(), request.finalidadeProcuracao(),
                    request.documentoOrigem(), request.paisOrigem(), request.entidadeEmissora(),
                    request.documentoApostilado(), request.paisDestino(),
                    request.documentoOriginalRef(), request.numeroCopias());
        }
        ServicoNotarialResponse response = servicoNotarialService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Servico notarial criado", response));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','CONSUL','OFFICER','CITIZEN','VIEWER')")
    @Operation(summary = "Obter servico notarial por ID", description = "Retorna os dados de um servico notarial")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Servico encontrado"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Servico nao encontrado")
    })
    public ResponseEntity<ApiResponse<ServicoNotarialResponse>> findById(@PathVariable UUID id) {
        ServicoNotarialResponse response = servicoNotarialService.findById(id);
        if (citizenContext.isCitizenOnly() && !citizenContext.canAccessCidadaoData(response.cidadaoId())) {
            throw new AccessDeniedException("Access denied");
        }
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','CONSUL','OFFICER','CITIZEN','VIEWER')")
    @Operation(summary = "Listar servicos notariais", description = "Lista servicos notariais com filtros opcionais por cidadao, estado e tipo")
    public ResponseEntity<ApiResponse<PagedResponse<ServicoNotarialResponse>>> findAll(
            @RequestParam(required = false) UUID cidadaoId,
            @RequestParam(required = false) EstadoServicoNotarial estado,
            @RequestParam(required = false) TipoServicoNotarial tipo,
            @PageableDefault(size = 20) Pageable pageable) {
        if (citizenContext.isCitizenOnly()) {
            cidadaoId = citizenContext.requireCurrentCidadaoId();
        }
        if (cidadaoId != null) {
            return ResponseEntity.ok(ApiResponse.success(
                    PagedResponse.of(servicoNotarialService.findByCidadaoId(cidadaoId, pageable))));
        }
        if (estado != null) {
            return ResponseEntity.ok(ApiResponse.success(
                    PagedResponse.of(servicoNotarialService.findByEstado(estado, pageable))));
        }
        if (tipo != null) {
            return ResponseEntity.ok(ApiResponse.success(
                    PagedResponse.of(servicoNotarialService.findByTipo(tipo, pageable))));
        }
        return ResponseEntity.ok(ApiResponse.success(
                PagedResponse.of(servicoNotarialService.findAll(pageable))));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','CONSUL','OFFICER')")
    @Operation(summary = "Actualizar servico notarial", description = "Actualiza os dados de um servico notarial")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Servico actualizado"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Servico nao encontrado")
    })
    public ResponseEntity<ApiResponse<ServicoNotarialResponse>> update(
            @PathVariable UUID id, @Valid @RequestBody ServicoNotarialUpdateRequest request) {
        return ResponseEntity.ok(ApiResponse.success(servicoNotarialService.update(id, request)));
    }

    @PatchMapping("/{id}/estado")
    @PreAuthorize("hasAnyRole('ADMIN','CONSUL','OFFICER')")
    @Operation(summary = "Alterar estado do servico notarial", description = "Transita o servico para um novo estado seguindo a maquina de estados")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Estado alterado"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "Transicao de estado invalida")
    })
    public ResponseEntity<ApiResponse<ServicoNotarialResponse>> updateEstado(
            @PathVariable UUID id, @RequestBody Map<String, String> body) {
        EstadoServicoNotarial estado = EstadoServicoNotarial.valueOf(body.get("estado"));
        String comentario = body.getOrDefault("comentario", null);
        return ResponseEntity.ok(ApiResponse.success(
                servicoNotarialService.updateEstado(id, estado, comentario)));
    }

    @PatchMapping("/{id}/pagamento")
    @PreAuthorize("hasAnyRole('ADMIN','CONSUL','OFFICER')")
    @Operation(summary = "Marcar taxa como paga", description = "Marca a taxa do servico notarial como paga")
    public ResponseEntity<ApiResponse<ServicoNotarialResponse>> markTaxaPaga(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success(servicoNotarialService.markTaxaPaga(id)));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','CONSUL')")
    @Operation(summary = "Eliminar servico notarial", description = "Remove permanentemente um servico notarial")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "204", description = "Servico eliminado"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Servico nao encontrado")
    })
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        servicoNotarialService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}/historico")
    @PreAuthorize("hasAnyRole('ADMIN','CONSUL','OFFICER','CITIZEN','VIEWER')")
    @Operation(summary = "Historico do servico notarial", description = "Retorna o historico de transicoes de estado do servico")
    public ResponseEntity<ApiResponse<PagedResponse<ServicoNotarialHistoricoResponse>>> findHistorico(
            @PathVariable UUID id, @PageableDefault(size = 50) Pageable pageable) {
        if (citizenContext.isCitizenOnly()) {
            ServicoNotarialResponse servico = servicoNotarialService.findById(id);
            if (!citizenContext.canAccessCidadaoData(servico.cidadaoId())) {
                throw new AccessDeniedException("Access denied");
            }
        }
        return ResponseEntity.ok(ApiResponse.success(
                PagedResponse.of(servicoNotarialService.findHistorico(id, pageable))));
    }

    @PostMapping("/{id}/documentos/{documentoId}")
    @PreAuthorize("hasAnyRole('ADMIN','CONSUL','OFFICER')")
    @Operation(summary = "Associar documento ao servico", description = "Adiciona um documento existente a um servico notarial")
    public ResponseEntity<ApiResponse<ServicoNotarialResponse>> addDocumento(
            @PathVariable UUID id, @PathVariable UUID documentoId) {
        return ResponseEntity.ok(ApiResponse.success(
                servicoNotarialService.addDocumento(id, documentoId)));
    }

    @DeleteMapping("/{id}/documentos/{documentoId}")
    @PreAuthorize("hasAnyRole('ADMIN','CONSUL','OFFICER')")
    @Operation(summary = "Desassociar documento do servico", description = "Remove a associacao de um documento a um servico notarial")
    public ResponseEntity<ApiResponse<ServicoNotarialResponse>> removeDocumento(
            @PathVariable UUID id, @PathVariable UUID documentoId) {
        return ResponseEntity.ok(ApiResponse.success(
                servicoNotarialService.removeDocumento(id, documentoId)));
    }

    @GetMapping("/{id}/certificado")
    @PreAuthorize("hasAnyRole('ADMIN','CONSUL','OFFICER','CITIZEN','VIEWER')")
    @Operation(summary = "Descarregar certificado notarial", description = "Descarrega o certificado PDF do servico notarial")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Certificado encontrado"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Certificado nao encontrado")
    })
    public ResponseEntity<InputStreamResource> downloadCertificado(@PathVariable UUID id) {
        ServicoNotarialResponse servico = servicoNotarialService.findById(id);
        if (citizenContext.isCitizenOnly() && !citizenContext.canAccessCidadaoData(servico.cidadaoId())) {
            throw new AccessDeniedException("Access denied");
        }

        if (servico.certificadoUrl() == null) {
            throw new ResourceNotFoundException("Certificado", "servicoNotarial", id.toString());
        }

        String prefix = switch (servico.tipo()) {
            case PROCURACAO -> "procuracao";
            case LEGALIZACAO -> "legalizacao";
            case APOSTILA -> "apostila";
            case COPIA_CERTIFICADA -> "copia_certificada";
        };
        String objectKey = "notarial/" + prefix + "/" + servico.id() + "/" + servico.numeroServico() + ".pdf";

        InputStream is = storageService.download(storageService.getDefaultBucket(), objectKey);

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"" + servico.numeroServico() + ".pdf\"")
                .body(new InputStreamResource(is));
    }

    @GetMapping("/fees")
    @PreAuthorize("hasAnyRole('ADMIN','CONSUL','OFFICER','CITIZEN','VIEWER')")
    @Operation(summary = "Consultar taxa de servico notarial", description = "Retorna o valor da taxa para um tipo de servico notarial")
    public ResponseEntity<ApiResponse<NotarialFeeResponse>> getFee(@RequestParam TipoServicoNotarial tipo) {
        return ResponseEntity.ok(ApiResponse.success(feeCalculator.getFeeResponse(tipo)));
    }
}

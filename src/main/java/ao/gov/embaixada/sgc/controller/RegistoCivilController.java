package ao.gov.embaixada.sgc.controller;

import ao.gov.embaixada.commons.dto.ApiResponse;
import ao.gov.embaixada.commons.dto.PagedResponse;
import ao.gov.embaixada.commons.storage.StorageService;
import ao.gov.embaixada.sgc.dto.RegistoCivilHistoricoResponse;
import ao.gov.embaixada.sgc.dto.RegistoCivilCreateRequest;
import ao.gov.embaixada.sgc.dto.RegistoCivilResponse;
import ao.gov.embaixada.sgc.dto.RegistoCivilUpdateRequest;
import ao.gov.embaixada.sgc.enums.EstadoRegistoCivil;
import ao.gov.embaixada.sgc.enums.TipoRegistoCivil;
import ao.gov.embaixada.sgc.exception.ResourceNotFoundException;
import ao.gov.embaixada.sgc.service.CitizenContextService;
import ao.gov.embaixada.sgc.service.RegistoCivilService;
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
@RequestMapping("/api/v1/registos-civis")
@Tag(name = "Registos Civis", description = "Gestao de registos civis (nascimento, casamento, obito)")
public class RegistoCivilController {

    private final RegistoCivilService registoCivilService;
    private final StorageService storageService;
    private final CitizenContextService citizenContext;

    public RegistoCivilController(RegistoCivilService registoCivilService,
                                  StorageService storageService,
                                  CitizenContextService citizenContext) {
        this.registoCivilService = registoCivilService;
        this.storageService = storageService;
        this.citizenContext = citizenContext;
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','CONSUL','OFFICER','CITIZEN')")
    @Operation(summary = "Criar registo civil", description = "Cria um novo registo civil (nascimento, casamento ou obito)")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Registo criado"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Dados invalidos"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Cidadao nao encontrado")
    })
    public ResponseEntity<ApiResponse<RegistoCivilResponse>> create(
            @Valid @RequestBody RegistoCivilCreateRequest request) {
        if (citizenContext.isCitizenOnly()) {
            UUID ownId = citizenContext.requireCurrentCidadaoId();
            request = new RegistoCivilCreateRequest(ownId, request.tipo(), request.dataEvento(),
                    request.localEvento(), request.observacoes(),
                    request.nomePai(), request.nomeMae(), request.localNascimento(),
                    request.nomeConjuge1(), request.nomeConjuge2(), request.regimeCasamento(),
                    request.causaObito(), request.localObito(), request.dataObito());
        }
        RegistoCivilResponse response = registoCivilService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Registo civil criado", response));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','CONSUL','OFFICER','CITIZEN','VIEWER')")
    @Operation(summary = "Obter registo civil por ID", description = "Retorna os dados de um registo civil")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Registo encontrado"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Registo nao encontrado")
    })
    public ResponseEntity<ApiResponse<RegistoCivilResponse>> findById(@PathVariable UUID id) {
        RegistoCivilResponse response = registoCivilService.findById(id);
        if (citizenContext.isCitizenOnly() && !citizenContext.canAccessCidadaoData(response.cidadaoId())) {
            throw new AccessDeniedException("Access denied");
        }
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','CONSUL','OFFICER','CITIZEN','VIEWER')")
    @Operation(summary = "Listar registos civis", description = "Lista registos civis com filtros opcionais por cidadao, estado e tipo")
    public ResponseEntity<ApiResponse<PagedResponse<RegistoCivilResponse>>> findAll(
            @RequestParam(required = false) UUID cidadaoId,
            @RequestParam(required = false) EstadoRegistoCivil estado,
            @RequestParam(required = false) TipoRegistoCivil tipo,
            @PageableDefault(size = 20) Pageable pageable) {
        if (citizenContext.isCitizenOnly()) {
            cidadaoId = citizenContext.requireCurrentCidadaoId();
        }
        if (cidadaoId != null) {
            return ResponseEntity.ok(ApiResponse.success(
                    PagedResponse.of(registoCivilService.findByCidadaoId(cidadaoId, pageable))));
        }
        if (estado != null) {
            return ResponseEntity.ok(ApiResponse.success(
                    PagedResponse.of(registoCivilService.findByEstado(estado, pageable))));
        }
        if (tipo != null) {
            return ResponseEntity.ok(ApiResponse.success(
                    PagedResponse.of(registoCivilService.findByTipo(tipo, pageable))));
        }
        return ResponseEntity.ok(ApiResponse.success(
                PagedResponse.of(registoCivilService.findAll(pageable))));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','CONSUL','OFFICER')")
    @Operation(summary = "Actualizar registo civil", description = "Actualiza os dados de um registo civil")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Registo actualizado"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Registo nao encontrado")
    })
    public ResponseEntity<ApiResponse<RegistoCivilResponse>> update(
            @PathVariable UUID id, @Valid @RequestBody RegistoCivilUpdateRequest request) {
        return ResponseEntity.ok(ApiResponse.success(registoCivilService.update(id, request)));
    }

    @PatchMapping("/{id}/estado")
    @PreAuthorize("hasAnyRole('ADMIN','CONSUL','OFFICER')")
    @Operation(summary = "Alterar estado do registo civil", description = "Transita o registo para um novo estado seguindo a maquina de estados")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Estado alterado"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "Transicao de estado invalida")
    })
    public ResponseEntity<ApiResponse<RegistoCivilResponse>> updateEstado(
            @PathVariable UUID id, @RequestBody Map<String, String> body) {
        EstadoRegistoCivil estado = EstadoRegistoCivil.valueOf(body.get("estado"));
        String comentario = body.getOrDefault("comentario", null);
        return ResponseEntity.ok(ApiResponse.success(
                registoCivilService.updateEstado(id, estado, comentario)));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','CONSUL')")
    @Operation(summary = "Eliminar registo civil", description = "Remove permanentemente um registo civil")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "204", description = "Registo eliminado"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Registo nao encontrado")
    })
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        registoCivilService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}/historico")
    @PreAuthorize("hasAnyRole('ADMIN','CONSUL','OFFICER','CITIZEN','VIEWER')")
    @Operation(summary = "Historico do registo civil", description = "Retorna o historico de transicoes de estado do registo")
    public ResponseEntity<ApiResponse<PagedResponse<RegistoCivilHistoricoResponse>>> findHistorico(
            @PathVariable UUID id, @PageableDefault(size = 50) Pageable pageable) {
        if (citizenContext.isCitizenOnly()) {
            RegistoCivilResponse registo = registoCivilService.findById(id);
            if (!citizenContext.canAccessCidadaoData(registo.cidadaoId())) {
                throw new AccessDeniedException("Access denied");
            }
        }
        return ResponseEntity.ok(ApiResponse.success(
                PagedResponse.of(registoCivilService.findHistorico(id, pageable))));
    }

    @PostMapping("/{id}/documentos/{documentoId}")
    @PreAuthorize("hasAnyRole('ADMIN','CONSUL','OFFICER')")
    @Operation(summary = "Associar documento ao registo", description = "Adiciona um documento existente a um registo civil")
    public ResponseEntity<ApiResponse<RegistoCivilResponse>> addDocumento(
            @PathVariable UUID id, @PathVariable UUID documentoId) {
        return ResponseEntity.ok(ApiResponse.success(
                registoCivilService.addDocumento(id, documentoId)));
    }

    @DeleteMapping("/{id}/documentos/{documentoId}")
    @PreAuthorize("hasAnyRole('ADMIN','CONSUL','OFFICER')")
    @Operation(summary = "Desassociar documento do registo", description = "Remove a associacao de um documento a um registo civil")
    public ResponseEntity<ApiResponse<RegistoCivilResponse>> removeDocumento(
            @PathVariable UUID id, @PathVariable UUID documentoId) {
        return ResponseEntity.ok(ApiResponse.success(
                registoCivilService.removeDocumento(id, documentoId)));
    }

    @GetMapping("/{id}/certificado")
    @PreAuthorize("hasAnyRole('ADMIN','CONSUL','OFFICER','CITIZEN','VIEWER')")
    @Operation(summary = "Descarregar certificado", description = "Descarrega o certificado PDF do registo civil")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Certificado encontrado"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Certificado nao encontrado")
    })
    public ResponseEntity<InputStreamResource> downloadCertificado(@PathVariable UUID id) {
        RegistoCivilResponse registo = registoCivilService.findById(id);
        if (citizenContext.isCitizenOnly() && !citizenContext.canAccessCidadaoData(registo.cidadaoId())) {
            throw new AccessDeniedException("Access denied");
        }

        if (registo.certificadoUrl() == null) {
            throw new ResourceNotFoundException("Certificado", "registoCivil", id.toString());
        }

        String objectKey = buildCertificadoObjectKey(registo);
        InputStream is = storageService.download(storageService.getDefaultBucket(), objectKey);

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"" + registo.numeroRegisto() + ".pdf\"")
                .body(new InputStreamResource(is));
    }

    private String buildCertificadoObjectKey(RegistoCivilResponse registo) {
        String prefix = switch (registo.tipo()) {
            case NASCIMENTO -> "nascimento";
            case CASAMENTO -> "casamento";
            case OBITO -> "obito";
        };
        return "certificados/" + prefix + "/" + registo.id() + "/" + registo.numeroRegisto() + ".pdf";
    }
}

package ao.gov.embaixada.sgc.mapper;

import ao.gov.embaixada.sgc.dto.VisaCreateRequest;
import ao.gov.embaixada.sgc.dto.VisaHistoricoResponse;
import ao.gov.embaixada.sgc.dto.VisaResponse;
import ao.gov.embaixada.sgc.dto.VisaUpdateRequest;
import ao.gov.embaixada.sgc.entity.VisaApplication;
import ao.gov.embaixada.sgc.entity.VisaHistorico;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface VisaMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "cidadao", ignore = true)
    @Mapping(target = "numeroVisto", ignore = true)
    @Mapping(target = "estado", ignore = true)
    @Mapping(target = "responsavel", ignore = true)
    @Mapping(target = "valorTaxa", ignore = true)
    @Mapping(target = "taxaPaga", ignore = true)
    @Mapping(target = "dataSubmissao", ignore = true)
    @Mapping(target = "dataDecisao", ignore = true)
    @Mapping(target = "motivoRejeicao", ignore = true)
    @Mapping(target = "documentos", ignore = true)
    @Mapping(target = "historico", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "version", ignore = true)
    VisaApplication toEntity(VisaCreateRequest request);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "cidadao", ignore = true)
    @Mapping(target = "tipo", ignore = true)
    @Mapping(target = "numeroVisto", ignore = true)
    @Mapping(target = "estado", ignore = true)
    @Mapping(target = "nacionalidadePassaporte", ignore = true)
    @Mapping(target = "valorTaxa", ignore = true)
    @Mapping(target = "taxaPaga", ignore = true)
    @Mapping(target = "dataSubmissao", ignore = true)
    @Mapping(target = "dataDecisao", ignore = true)
    @Mapping(target = "motivoRejeicao", ignore = true)
    @Mapping(target = "documentos", ignore = true)
    @Mapping(target = "historico", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "version", ignore = true)
    void updateEntity(VisaUpdateRequest request, @MappingTarget VisaApplication visa);

    @Mapping(target = "cidadaoId", expression = "java(visa.getCidadao() != null ? visa.getCidadao().getId() : null)")
    @Mapping(target = "cidadaoNome", expression = "java(visa.getCidadao() != null ? visa.getCidadao().getNomeCompleto() : null)")
    @Mapping(target = "documentoCount", expression = "java(visa.getDocumentos() != null ? visa.getDocumentos().size() : 0)")
    VisaResponse toResponse(VisaApplication visa);

    @Mapping(target = "visaId", expression = "java(historico.getVisaApplication() != null ? historico.getVisaApplication().getId() : null)")
    VisaHistoricoResponse toHistoricoResponse(VisaHistorico historico);
}

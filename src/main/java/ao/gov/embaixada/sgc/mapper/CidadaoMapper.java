package ao.gov.embaixada.sgc.mapper;

import ao.gov.embaixada.sgc.dto.CidadaoCreateRequest;
import ao.gov.embaixada.sgc.dto.CidadaoResponse;
import ao.gov.embaixada.sgc.dto.CidadaoSummaryResponse;
import ao.gov.embaixada.sgc.dto.CidadaoUpdateRequest;
import ao.gov.embaixada.sgc.entity.Cidadao;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface CidadaoMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "estado", ignore = true)
    @Mapping(target = "keycloakId", ignore = true)
    @Mapping(target = "documentos", ignore = true)
    @Mapping(target = "processos", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "version", ignore = true)
    Cidadao toEntity(CidadaoCreateRequest request);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "numeroPassaporte", ignore = true)
    @Mapping(target = "estado", ignore = true)
    @Mapping(target = "keycloakId", ignore = true)
    @Mapping(target = "documentos", ignore = true)
    @Mapping(target = "processos", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "version", ignore = true)
    void updateEntity(CidadaoUpdateRequest request, @MappingTarget Cidadao cidadao);

    @Mapping(target = "documentoCount", expression = "java(cidadao.getDocumentos() != null ? cidadao.getDocumentos().size() : 0)")
    @Mapping(target = "processoCount", expression = "java(cidadao.getProcessos() != null ? cidadao.getProcessos().size() : 0)")
    CidadaoResponse toResponse(Cidadao cidadao);

    CidadaoSummaryResponse toSummaryResponse(Cidadao cidadao);
}

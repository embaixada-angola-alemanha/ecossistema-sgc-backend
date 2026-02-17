package ao.gov.embaixada.sgc.mapper;

import ao.gov.embaixada.sgc.dto.DocumentoCreateRequest;
import ao.gov.embaixada.sgc.dto.DocumentoResponse;
import ao.gov.embaixada.sgc.dto.DocumentoUpdateRequest;
import ao.gov.embaixada.sgc.entity.Documento;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface DocumentoMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "cidadao", ignore = true)
    @Mapping(target = "estado", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "version", ignore = true)
    Documento toEntity(DocumentoCreateRequest request);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "cidadao", ignore = true)
    @Mapping(target = "estado", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "version", ignore = true)
    void updateEntity(DocumentoUpdateRequest request, @MappingTarget Documento documento);

    @Mapping(target = "cidadaoId", expression = "java(documento.getCidadao() != null ? documento.getCidadao().getId() : null)")
    @Mapping(target = "cidadaoNome", expression = "java(documento.getCidadao() != null ? documento.getCidadao().getNomeCompleto() : null)")
    DocumentoResponse toResponse(Documento documento);
}

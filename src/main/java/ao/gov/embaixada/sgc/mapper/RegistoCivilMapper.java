package ao.gov.embaixada.sgc.mapper;

import ao.gov.embaixada.sgc.dto.RegistoCivilCreateRequest;
import ao.gov.embaixada.sgc.dto.RegistoCivilHistoricoResponse;
import ao.gov.embaixada.sgc.dto.RegistoCivilResponse;
import ao.gov.embaixada.sgc.dto.RegistoCivilUpdateRequest;
import ao.gov.embaixada.sgc.entity.RegistoCivil;
import ao.gov.embaixada.sgc.entity.RegistoCivilHistorico;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface RegistoCivilMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "cidadao", ignore = true)
    @Mapping(target = "numeroRegisto", ignore = true)
    @Mapping(target = "estado", ignore = true)
    @Mapping(target = "responsavel", ignore = true)
    @Mapping(target = "motivoRejeicao", ignore = true)
    @Mapping(target = "dataSubmissao", ignore = true)
    @Mapping(target = "dataVerificacao", ignore = true)
    @Mapping(target = "dataCertificado", ignore = true)
    @Mapping(target = "certificadoObjectKey", ignore = true)
    @Mapping(target = "certificadoUrl", ignore = true)
    @Mapping(target = "documentos", ignore = true)
    @Mapping(target = "historico", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "version", ignore = true)
    RegistoCivil toEntity(RegistoCivilCreateRequest request);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "cidadao", ignore = true)
    @Mapping(target = "tipo", ignore = true)
    @Mapping(target = "numeroRegisto", ignore = true)
    @Mapping(target = "estado", ignore = true)
    @Mapping(target = "motivoRejeicao", ignore = true)
    @Mapping(target = "dataSubmissao", ignore = true)
    @Mapping(target = "dataVerificacao", ignore = true)
    @Mapping(target = "dataCertificado", ignore = true)
    @Mapping(target = "certificadoObjectKey", ignore = true)
    @Mapping(target = "certificadoUrl", ignore = true)
    @Mapping(target = "documentos", ignore = true)
    @Mapping(target = "historico", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "version", ignore = true)
    void updateEntity(RegistoCivilUpdateRequest request, @MappingTarget RegistoCivil registoCivil);

    @Mapping(target = "cidadaoId", expression = "java(registoCivil.getCidadao() != null ? registoCivil.getCidadao().getId() : null)")
    @Mapping(target = "cidadaoNome", expression = "java(registoCivil.getCidadao() != null ? registoCivil.getCidadao().getNomeCompleto() : null)")
    @Mapping(target = "documentoCount", expression = "java(registoCivil.getDocumentos() != null ? registoCivil.getDocumentos().size() : 0)")
    RegistoCivilResponse toResponse(RegistoCivil registoCivil);

    @Mapping(target = "registoCivilId", expression = "java(historico.getRegistoCivil() != null ? historico.getRegistoCivil().getId() : null)")
    RegistoCivilHistoricoResponse toHistoricoResponse(RegistoCivilHistorico historico);
}

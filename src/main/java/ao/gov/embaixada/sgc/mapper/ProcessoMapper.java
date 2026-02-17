package ao.gov.embaixada.sgc.mapper;

import ao.gov.embaixada.sgc.dto.ProcessoCreateRequest;
import ao.gov.embaixada.sgc.dto.ProcessoHistoricoResponse;
import ao.gov.embaixada.sgc.dto.ProcessoResponse;
import ao.gov.embaixada.sgc.dto.ProcessoUpdateRequest;
import ao.gov.embaixada.sgc.entity.Processo;
import ao.gov.embaixada.sgc.entity.ProcessoHistorico;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface ProcessoMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "cidadao", ignore = true)
    @Mapping(target = "numeroProcesso", ignore = true)
    @Mapping(target = "estado", ignore = true)
    @Mapping(target = "taxaPaga", ignore = true)
    @Mapping(target = "dataSubmissao", ignore = true)
    @Mapping(target = "dataConclusao", ignore = true)
    @Mapping(target = "documentos", ignore = true)
    @Mapping(target = "historico", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "version", ignore = true)
    Processo toEntity(ProcessoCreateRequest request);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "cidadao", ignore = true)
    @Mapping(target = "tipo", ignore = true)
    @Mapping(target = "numeroProcesso", ignore = true)
    @Mapping(target = "estado", ignore = true)
    @Mapping(target = "dataSubmissao", ignore = true)
    @Mapping(target = "dataConclusao", ignore = true)
    @Mapping(target = "documentos", ignore = true)
    @Mapping(target = "historico", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "version", ignore = true)
    void updateEntity(ProcessoUpdateRequest request, @MappingTarget Processo processo);

    @Mapping(target = "cidadaoId", expression = "java(processo.getCidadao() != null ? processo.getCidadao().getId() : null)")
    @Mapping(target = "cidadaoNome", expression = "java(processo.getCidadao() != null ? processo.getCidadao().getNomeCompleto() : null)")
    @Mapping(target = "documentoCount", expression = "java(processo.getDocumentos() != null ? processo.getDocumentos().size() : 0)")
    ProcessoResponse toResponse(Processo processo);

    @Mapping(target = "processoId", expression = "java(historico.getProcesso() != null ? historico.getProcesso().getId() : null)")
    ProcessoHistoricoResponse toHistoricoResponse(ProcessoHistorico historico);
}

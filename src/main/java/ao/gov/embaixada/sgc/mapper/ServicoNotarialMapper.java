package ao.gov.embaixada.sgc.mapper;

import ao.gov.embaixada.sgc.dto.ServicoNotarialCreateRequest;
import ao.gov.embaixada.sgc.dto.ServicoNotarialHistoricoResponse;
import ao.gov.embaixada.sgc.dto.ServicoNotarialResponse;
import ao.gov.embaixada.sgc.dto.ServicoNotarialUpdateRequest;
import ao.gov.embaixada.sgc.entity.ServicoNotarial;
import ao.gov.embaixada.sgc.entity.ServicoNotarialHistorico;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface ServicoNotarialMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "cidadao", ignore = true)
    @Mapping(target = "numeroServico", ignore = true)
    @Mapping(target = "estado", ignore = true)
    @Mapping(target = "responsavel", ignore = true)
    @Mapping(target = "motivoRejeicao", ignore = true)
    @Mapping(target = "valorTaxa", ignore = true)
    @Mapping(target = "taxaPaga", ignore = true)
    @Mapping(target = "dataSubmissao", ignore = true)
    @Mapping(target = "dataConclusao", ignore = true)
    @Mapping(target = "certificadoObjectKey", ignore = true)
    @Mapping(target = "certificadoUrl", ignore = true)
    @Mapping(target = "documentos", ignore = true)
    @Mapping(target = "historico", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "version", ignore = true)
    ServicoNotarial toEntity(ServicoNotarialCreateRequest request);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "cidadao", ignore = true)
    @Mapping(target = "tipo", ignore = true)
    @Mapping(target = "numeroServico", ignore = true)
    @Mapping(target = "estado", ignore = true)
    @Mapping(target = "motivoRejeicao", ignore = true)
    @Mapping(target = "valorTaxa", ignore = true)
    @Mapping(target = "taxaPaga", ignore = true)
    @Mapping(target = "dataSubmissao", ignore = true)
    @Mapping(target = "dataConclusao", ignore = true)
    @Mapping(target = "certificadoObjectKey", ignore = true)
    @Mapping(target = "certificadoUrl", ignore = true)
    @Mapping(target = "agendamentoId", ignore = true)
    @Mapping(target = "documentos", ignore = true)
    @Mapping(target = "historico", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "version", ignore = true)
    void updateEntity(ServicoNotarialUpdateRequest request, @MappingTarget ServicoNotarial servicoNotarial);

    @Mapping(target = "cidadaoId", expression = "java(servicoNotarial.getCidadao() != null ? servicoNotarial.getCidadao().getId() : null)")
    @Mapping(target = "cidadaoNome", expression = "java(servicoNotarial.getCidadao() != null ? servicoNotarial.getCidadao().getNomeCompleto() : null)")
    @Mapping(target = "documentoCount", expression = "java(servicoNotarial.getDocumentos() != null ? servicoNotarial.getDocumentos().size() : 0)")
    ServicoNotarialResponse toResponse(ServicoNotarial servicoNotarial);

    @Mapping(target = "servicoNotarialId", expression = "java(historico.getServicoNotarial() != null ? historico.getServicoNotarial().getId() : null)")
    ServicoNotarialHistoricoResponse toHistoricoResponse(ServicoNotarialHistorico historico);
}

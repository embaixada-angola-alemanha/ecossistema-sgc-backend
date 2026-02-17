package ao.gov.embaixada.sgc.mapper;

import ao.gov.embaixada.sgc.dto.AgendamentoCreateRequest;
import ao.gov.embaixada.sgc.dto.AgendamentoHistoricoResponse;
import ao.gov.embaixada.sgc.dto.AgendamentoResponse;
import ao.gov.embaixada.sgc.entity.Agendamento;
import ao.gov.embaixada.sgc.entity.AgendamentoHistorico;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface AgendamentoMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "cidadao", ignore = true)
    @Mapping(target = "numeroAgendamento", ignore = true)
    @Mapping(target = "estado", ignore = true)
    @Mapping(target = "duracaoMinutos", ignore = true)
    @Mapping(target = "local", ignore = true)
    @Mapping(target = "motivoCancelamento", ignore = true)
    @Mapping(target = "historico", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "version", ignore = true)
    Agendamento toEntity(AgendamentoCreateRequest request);

    @Mapping(target = "cidadaoId", expression = "java(agendamento.getCidadao() != null ? agendamento.getCidadao().getId() : null)")
    @Mapping(target = "cidadaoNome", expression = "java(agendamento.getCidadao() != null ? agendamento.getCidadao().getNomeCompleto() : null)")
    @Mapping(target = "cidadaoEmail", expression = "java(agendamento.getCidadao() != null ? agendamento.getCidadao().getEmail() : null)")
    AgendamentoResponse toResponse(Agendamento agendamento);

    @Mapping(target = "agendamentoId", expression = "java(historico.getAgendamento() != null ? historico.getAgendamento().getId() : null)")
    AgendamentoHistoricoResponse toHistoricoResponse(AgendamentoHistorico historico);
}

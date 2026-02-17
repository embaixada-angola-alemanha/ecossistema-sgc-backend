package ao.gov.embaixada.sgc.dto;

import ao.gov.embaixada.sgc.enums.TipoAgendamento;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;
import java.util.UUID;

public record AgendamentoCreateRequest(
        @NotNull UUID cidadaoId,
        @NotNull TipoAgendamento tipo,
        @NotNull @Future LocalDateTime dataHora,
        String notas
) {}

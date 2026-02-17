package ao.gov.embaixada.sgc.dto;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

public record AgendamentoUpdateRequest(
        @NotNull @Future LocalDateTime dataHora,
        String notas
) {}

package ao.gov.embaixada.sgc.dto;

import ao.gov.embaixada.sgc.enums.TipoAgendamento;

import java.time.LocalDateTime;

public record SlotDisponivelResponse(
        LocalDateTime dataHora,
        int duracaoMinutos,
        TipoAgendamento tipo
) {}

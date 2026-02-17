package ao.gov.embaixada.sgc.dto;

import ao.gov.embaixada.sgc.enums.TipoVisto;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;
import java.util.UUID;

public record VisaCreateRequest(
        @NotNull UUID cidadaoId,
        @NotNull TipoVisto tipo,
        @Size(max = 100) String nacionalidadePassaporte,
        String motivoViagem,
        @Future LocalDate dataEntrada,
        @Future LocalDate dataSaida,
        String localAlojamento,
        @Size(max = 255) String entidadeConvite,
        String observacoes
) {}

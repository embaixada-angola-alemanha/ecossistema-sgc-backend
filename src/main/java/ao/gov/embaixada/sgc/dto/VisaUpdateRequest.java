package ao.gov.embaixada.sgc.dto;

import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record VisaUpdateRequest(
        @Size(max = 500) String motivoViagem,
        LocalDate dataEntrada,
        LocalDate dataSaida,
        @Size(max = 500) String localAlojamento,
        @Size(max = 255) String entidadeConvite,
        @Size(max = 100) String responsavel,
        @Size(max = 2000) String observacoes
) {}

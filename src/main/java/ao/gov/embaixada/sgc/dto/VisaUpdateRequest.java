package ao.gov.embaixada.sgc.dto;

import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record VisaUpdateRequest(
        String motivoViagem,
        LocalDate dataEntrada,
        LocalDate dataSaida,
        String localAlojamento,
        @Size(max = 255) String entidadeConvite,
        @Size(max = 100) String responsavel,
        String observacoes
) {}

package ao.gov.embaixada.sgc.dto;

import ao.gov.embaixada.sgc.enums.EstadoCidadao;
import ao.gov.embaixada.sgc.enums.EstadoCivil;
import ao.gov.embaixada.sgc.enums.Sexo;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public record CidadaoResponse(
        UUID id,
        String numeroPassaporte,
        String nomeCompleto,
        LocalDate dataNascimento,
        Sexo sexo,
        String nacionalidade,
        EstadoCivil estadoCivil,
        String email,
        String telefone,
        String enderecoAngola,
        String enderecoAlemanha,
        EstadoCidadao estado,
        String keycloakId,
        int documentoCount,
        int processoCount,
        Instant createdAt,
        Instant updatedAt
) {}

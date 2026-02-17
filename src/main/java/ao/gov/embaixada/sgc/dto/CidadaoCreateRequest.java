package ao.gov.embaixada.sgc.dto;

import ao.gov.embaixada.sgc.enums.EstadoCivil;
import ao.gov.embaixada.sgc.enums.Sexo;
import jakarta.validation.constraints.NotBlank;

import java.time.LocalDate;

public record CidadaoCreateRequest(
        @NotBlank String numeroPassaporte,
        @NotBlank String nomeCompleto,
        LocalDate dataNascimento,
        Sexo sexo,
        String nacionalidade,
        EstadoCivil estadoCivil,
        String email,
        String telefone,
        String enderecoAngola,
        String enderecoAlemanha
) {}

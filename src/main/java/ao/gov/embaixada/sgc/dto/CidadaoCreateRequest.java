package ao.gov.embaixada.sgc.dto;

import ao.gov.embaixada.sgc.enums.EstadoCivil;
import ao.gov.embaixada.sgc.enums.Sexo;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record CidadaoCreateRequest(
        @NotBlank @Size(max = 50) String numeroPassaporte,
        @NotBlank @Size(max = 255) String nomeCompleto,
        @Past LocalDate dataNascimento,
        Sexo sexo,
        @Size(max = 100) String nacionalidade,
        EstadoCivil estadoCivil,
        @Email String email,
        @Size(max = 50) String telefone,
        String enderecoAngola,
        String enderecoAlemanha
) {}

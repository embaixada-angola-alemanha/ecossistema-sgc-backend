package ao.gov.embaixada.sgc.dto;

import ao.gov.embaixada.sgc.enums.EstadoCivil;
import ao.gov.embaixada.sgc.enums.Sexo;
import jakarta.validation.constraints.*;

import java.time.LocalDate;

public record CidadaoCreateRequest(
        @NotBlank @Size(max = 50) @Pattern(regexp = "^[A-Za-z0-9\\-]+$", message = "Formato de passaporte invalido") String numeroPassaporte,
        @NotBlank @Size(max = 255) String nomeCompleto,
        @NotNull @Past LocalDate dataNascimento,
        @NotNull Sexo sexo,
        @NotBlank @Size(max = 100) String nacionalidade,
        @NotNull EstadoCivil estadoCivil,
        @NotBlank @Email String email,
        @Size(max = 50) @Pattern(regexp = "^[+]?[0-9\\s\\-()]*$", message = "Formato de telefone invalido") String telefone,
        @Size(max = 500) String enderecoAngola,
        @NotBlank @Size(max = 500) String enderecoAlemanha
) {}

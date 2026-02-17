package ao.gov.embaixada.sgc.dto;

import ao.gov.embaixada.sgc.enums.EstadoCivil;
import ao.gov.embaixada.sgc.enums.Sexo;

import java.time.LocalDate;

public record CidadaoUpdateRequest(
        String nomeCompleto,
        LocalDate dataNascimento,
        Sexo sexo,
        String nacionalidade,
        EstadoCivil estadoCivil,
        String email,
        String telefone,
        String enderecoAngola,
        String enderecoAlemanha
) {}

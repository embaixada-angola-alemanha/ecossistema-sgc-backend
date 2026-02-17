package ao.gov.embaixada.sgc.dto;

import ao.gov.embaixada.sgc.enums.TipoDocumento;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record DocumentoCreateRequest(
        @NotNull TipoDocumento tipo,
        String numero,
        LocalDate dataEmissao,
        LocalDate dataValidade,
        String ficheiroUrl,
        String ficheiroNome,
        Long ficheiroTamanho,
        String ficheiroTipo
) {}

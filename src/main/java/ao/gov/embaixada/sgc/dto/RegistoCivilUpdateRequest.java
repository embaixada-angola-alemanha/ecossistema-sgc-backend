package ao.gov.embaixada.sgc.dto;

import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record RegistoCivilUpdateRequest(
        LocalDate dataEvento,
        @Size(max = 255) String localEvento,
        String observacoes,
        @Size(max = 100) String responsavel,
        // Birth-specific
        @Size(max = 255) String nomePai,
        @Size(max = 255) String nomeMae,
        @Size(max = 255) String localNascimento,
        // Marriage-specific
        @Size(max = 255) String nomeConjuge1,
        @Size(max = 255) String nomeConjuge2,
        @Size(max = 100) String regimeCasamento,
        // Death-specific
        String causaObito,
        @Size(max = 255) String localObito,
        LocalDate dataObito
) {}

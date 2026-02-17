package ao.gov.embaixada.sgc.dto;

import ao.gov.embaixada.sgc.enums.EstadoRegistoCivil;
import ao.gov.embaixada.sgc.enums.TipoRegistoCivil;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

public record RegistoCivilResponse(
        UUID id,
        UUID cidadaoId,
        String cidadaoNome,
        TipoRegistoCivil tipo,
        String numeroRegisto,
        EstadoRegistoCivil estado,
        LocalDate dataEvento,
        String localEvento,
        String observacoes,
        String responsavel,
        String motivoRejeicao,
        LocalDateTime dataSubmissao,
        LocalDateTime dataVerificacao,
        LocalDateTime dataCertificado,
        // Birth-specific
        String nomePai,
        String nomeMae,
        String localNascimento,
        // Marriage-specific
        String nomeConjuge1,
        String nomeConjuge2,
        String regimeCasamento,
        // Death-specific
        String causaObito,
        String localObito,
        LocalDate dataObito,
        // Certificate
        String certificadoUrl,
        int documentoCount,
        Instant createdAt,
        Instant updatedAt
) {}

package ao.gov.embaixada.sgc.dto;

import java.util.UUID;

public record DocumentoUploadResponse(
        UUID documentoId,
        String ficheiroNome,
        Long ficheiroTamanho,
        String ficheiroTipo,
        String ficheiroUrl,
        Integer versao
) {}

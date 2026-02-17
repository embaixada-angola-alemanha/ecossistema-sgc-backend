package ao.gov.embaixada.sgc.dto;

import java.io.InputStream;

public record StorageDownloadResult(
        InputStream inputStream,
        String contentType,
        String filename,
        long size
) {}

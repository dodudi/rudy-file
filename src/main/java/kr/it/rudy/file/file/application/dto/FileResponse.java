package kr.it.rudy.file.file.application.dto;

import kr.it.rudy.file.file.domain.FileMetadata;

import java.time.Instant;

public record FileResponse(
        String id,
        String originalName,
        String contentType,
        Long size,
        String extension,
        String uploadedBy,
        String downloadUrl,
        Instant createdDt
) {
    public static FileResponse from(FileMetadata metadata, String downloadUrl) {
        return new FileResponse(
                metadata.getId().getValue(),
                metadata.getOriginalName(),
                metadata.getContentType(),
                metadata.getSize(),
                metadata.getExtension(),
                metadata.getUploadedBy(),
                downloadUrl,
                metadata.getCreatedDt()
        );
    }
}

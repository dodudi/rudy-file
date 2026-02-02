package kr.it.rudy.file.file.domain;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class FileMetadata {

    private FileId id;
    private String originalName;
    private String storedName;
    private String contentType;
    private Long size;
    private String extension;
    private String uploadedBy;
    private String bucket;
    private Instant createdDt;
    private Instant updatedDt;

    public static FileMetadata create(
            String originalName,
            String storedName,
            String contentType,
            Long size,
            String extension,
            String uploadedBy,
            String bucket
    ) {
        return new FileMetadata(
                FileId.generate(),
                originalName,
                storedName,
                contentType,
                size,
                extension,
                uploadedBy,
                bucket,
                null,
                null
        );
    }

    public static FileMetadata reconstitute(
            FileId id,
            String originalName,
            String storedName,
            String contentType,
            Long size,
            String extension,
            String uploadedBy,
            String bucket,
            Instant createdDt,
            Instant updatedDt
    ) {
        return new FileMetadata(
                id,
                originalName,
                storedName,
                contentType,
                size,
                extension,
                uploadedBy,
                bucket,
                createdDt,
                updatedDt
        );
    }
}

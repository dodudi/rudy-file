package kr.it.rudy.file.file.infrastructure.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import kr.it.rudy.file.common.persistence.BaseEntity;
import kr.it.rudy.file.file.domain.FileId;
import kr.it.rudy.file.file.domain.FileMetadata;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Entity
@Table(name = "file_metadata")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class FileMetadataJpaEntity extends BaseEntity {

    @Id
    @Column(length = 36)
    private String id;

    @Column(nullable = false)
    private String originalName;

    @Column(nullable = false)
    private String storedName;

    @Column(nullable = false)
    private String contentType;

    @Column(nullable = false)
    private Long size;

    @Column(length = 20)
    private String extension;

    @Column(nullable = false)
    private String uploadedBy;

    @Column(nullable = false)
    private String bucket;

    private FileMetadataJpaEntity(String id, String originalName, String storedName, String contentType, Long size, String extension, String uploadedBy, String bucket, Instant createdDt) {
        this.id = id;
        this.originalName = originalName;
        this.storedName = storedName;
        this.contentType = contentType;
        this.size = size;
        this.extension = extension;
        this.uploadedBy = uploadedBy;
        this.bucket = bucket;
        if (createdDt != null) {
            this.setCreatedDt(createdDt);
        }
    }

    public static FileMetadataJpaEntity fromDomain(FileMetadata fileMetadata) {
        return new FileMetadataJpaEntity(
                fileMetadata.getId().getValue(),
                fileMetadata.getOriginalName(),
                fileMetadata.getStoredName(),
                fileMetadata.getContentType(),
                fileMetadata.getSize(),
                fileMetadata.getExtension(),
                fileMetadata.getUploadedBy(),
                fileMetadata.getBucket(),
                fileMetadata.getCreatedDt()
        );
    }

    public FileMetadata toDomain() {
        return FileMetadata.reconstitute(
                FileId.of(id),
                originalName,
                storedName,
                contentType,
                size,
                extension,
                uploadedBy,
                bucket,
                getCreatedDt(),
                getUpdatedDt()
        );
    }
}

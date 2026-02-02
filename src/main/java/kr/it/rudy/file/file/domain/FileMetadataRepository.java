package kr.it.rudy.file.file.domain;

import java.util.List;
import java.util.Optional;

public interface FileMetadataRepository {

    FileMetadata save(FileMetadata fileMetadata);

    Optional<FileMetadata> findById(FileId id);

    List<FileMetadata> findByUploadedBy(String uploadedBy);

    List<FileMetadata> findAll();

    void delete(FileId id);

    boolean existsById(FileId id);
}

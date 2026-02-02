package kr.it.rudy.file.file.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface FileMetadataJpaRepository extends JpaRepository<FileMetadataJpaEntity, String> {

    List<FileMetadataJpaEntity> findByUploadedBy(String uploadedBy);
}

package kr.it.rudy.file.file.infrastructure.persistence;

import kr.it.rudy.file.file.domain.FileId;
import kr.it.rudy.file.file.domain.FileMetadata;
import kr.it.rudy.file.file.domain.FileMetadataRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
public class FileMetadataRepositoryImpl implements FileMetadataRepository {

    private final FileMetadataJpaRepository jpaRepository;

    @Override
    public FileMetadata save(FileMetadata fileMetadata) {
        FileMetadataJpaEntity entity = FileMetadataJpaEntity.fromDomain(fileMetadata);
        FileMetadataJpaEntity saved = jpaRepository.save(entity);
        return saved.toDomain();
    }

    @Override
    public Optional<FileMetadata> findById(FileId id) {
        return jpaRepository.findById(id.getValue())
                .map(FileMetadataJpaEntity::toDomain);
    }

    @Override
    public List<FileMetadata> findByUploadedBy(String uploadedBy) {
        return jpaRepository.findByUploadedBy(uploadedBy).stream()
                .map(FileMetadataJpaEntity::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<FileMetadata> findAll() {
        return jpaRepository.findAll().stream()
                .map(FileMetadataJpaEntity::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public void delete(FileId id) {
        jpaRepository.deleteById(id.getValue());
    }

    @Override
    public boolean existsById(FileId id) {
        return jpaRepository.existsById(id.getValue());
    }
}

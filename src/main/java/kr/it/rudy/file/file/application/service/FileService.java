package kr.it.rudy.file.file.application.service;

import io.minio.*;
import io.minio.http.Method;
import kr.it.rudy.file.file.application.dto.FileResponse;
import kr.it.rudy.file.file.application.dto.PresignedUrlResponse;
import kr.it.rudy.file.file.domain.FileId;
import kr.it.rudy.file.file.domain.FileMetadata;
import kr.it.rudy.file.file.domain.FileMetadataRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class FileService {

    private final MinioClient minioClient;
    private final FileMetadataRepository fileMetadataRepository;

    @Value("${minio.bucket}")
    private String bucket;

    @Value("${file.allowed-extensions}")
    private String allowedExtensions;

    @Value("${file.max-file-size}")
    private Long maxFileSize;

    @Transactional
    public FileResponse upload(MultipartFile file, String uploadedBy) {
        validateFile(file);

        String originalName = file.getOriginalFilename();
        String extension = getExtension(originalName);
        String storedName = generateStoredName(extension);

        try {
            // MinIO에 파일 업로드
            minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(bucket)
                            .object(storedName)
                            .stream(file.getInputStream(), file.getSize(), -1)
                            .contentType(file.getContentType())
                            .build()
            );

            // 메타데이터 저장
            FileMetadata metadata = FileMetadata.create(
                    originalName,
                    storedName,
                    file.getContentType(),
                    file.getSize(),
                    extension,
                    uploadedBy,
                    bucket
            );

            FileMetadata saved = fileMetadataRepository.save(metadata);
            String downloadUrl = "/api/files/" + saved.getId().getValue() + "/download";

            log.info("File uploaded: {} -> {}", originalName, storedName);
            return FileResponse.from(saved, downloadUrl);

        } catch (Exception e) {
            log.error("Failed to upload file: {}", e.getMessage());
            throw new RuntimeException("파일 업로드에 실패했습니다.", e);
        }
    }

    public InputStream download(String id) {
        FileMetadata metadata = fileMetadataRepository.findById(FileId.of(id))
                .orElseThrow(() -> new IllegalArgumentException("파일을 찾을 수 없습니다: " + id));

        try {
            return minioClient.getObject(
                    GetObjectArgs.builder()
                            .bucket(metadata.getBucket())
                            .object(metadata.getStoredName())
                            .build()
            );
        } catch (Exception e) {
            log.error("Failed to download file: {}", e.getMessage());
            throw new RuntimeException("파일 다운로드에 실패했습니다.", e);
        }
    }

    public FileResponse getFileInfo(String id) {
        FileMetadata metadata = fileMetadataRepository.findById(FileId.of(id))
                .orElseThrow(() -> new IllegalArgumentException("파일을 찾을 수 없습니다: " + id));

        String downloadUrl = "/api/files/" + id + "/download";
        return FileResponse.from(metadata, downloadUrl);
    }

    public List<FileResponse> getFilesByUser(String uploadedBy) {
        return fileMetadataRepository.findByUploadedBy(uploadedBy).stream()
                .map(metadata -> {
                    String downloadUrl = "/api/files/" + metadata.getId().getValue() + "/download";
                    return FileResponse.from(metadata, downloadUrl);
                })
                .collect(Collectors.toList());
    }

    public List<FileResponse> getAllFiles() {
        return fileMetadataRepository.findAll().stream()
                .map(metadata -> {
                    String downloadUrl = "/api/files/" + metadata.getId().getValue() + "/download";
                    return FileResponse.from(metadata, downloadUrl);
                })
                .collect(Collectors.toList());
    }

    @Transactional
    public void delete(String id) {
        FileMetadata metadata = fileMetadataRepository.findById(FileId.of(id))
                .orElseThrow(() -> new IllegalArgumentException("파일을 찾을 수 없습니다: " + id));

        try {
            // MinIO에서 파일 삭제
            minioClient.removeObject(
                    RemoveObjectArgs.builder()
                            .bucket(metadata.getBucket())
                            .object(metadata.getStoredName())
                            .build()
            );

            // 메타데이터 삭제
            fileMetadataRepository.delete(FileId.of(id));

            log.info("File deleted: {}", metadata.getStoredName());
        } catch (Exception e) {
            log.error("Failed to delete file: {}", e.getMessage());
            throw new RuntimeException("파일 삭제에 실패했습니다.", e);
        }
    }

    public PresignedUrlResponse getPresignedDownloadUrl(String id, int expiryMinutes) {
        FileMetadata metadata = fileMetadataRepository.findById(FileId.of(id))
                .orElseThrow(() -> new IllegalArgumentException("파일을 찾을 수 없습니다: " + id));

        try {
            String url = minioClient.getPresignedObjectUrl(
                    GetPresignedObjectUrlArgs.builder()
                            .bucket(metadata.getBucket())
                            .object(metadata.getStoredName())
                            .method(Method.GET)
                            .expiry(expiryMinutes, TimeUnit.MINUTES)
                            .build()
            );

            LocalDateTime expiresAt = LocalDateTime.now().plusMinutes(expiryMinutes);
            return new PresignedUrlResponse(url, expiresAt);
        } catch (Exception e) {
            log.error("Failed to generate presigned URL: {}", e.getMessage());
            throw new RuntimeException("Presigned URL 생성에 실패했습니다.", e);
        }
    }

    public PresignedUrlResponse getPresignedUploadUrl(String fileName, int expiryMinutes) {
        String extension = getExtension(fileName);
        validateExtension(extension);
        String storedName = generateStoredName(extension);

        try {
            String url = minioClient.getPresignedObjectUrl(
                    GetPresignedObjectUrlArgs.builder()
                            .bucket(bucket)
                            .object(storedName)
                            .method(Method.PUT)
                            .expiry(expiryMinutes, TimeUnit.MINUTES)
                            .build()
            );

            LocalDateTime expiresAt = LocalDateTime.now().plusMinutes(expiryMinutes);
            return new PresignedUrlResponse(url, expiresAt);
        } catch (Exception e) {
            log.error("Failed to generate presigned upload URL: {}", e.getMessage());
            throw new RuntimeException("Presigned Upload URL 생성에 실패했습니다.", e);
        }
    }

    private void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("파일이 비어있습니다.");
        }

        if (file.getSize() > maxFileSize) {
            throw new IllegalArgumentException("파일 크기가 제한을 초과했습니다. (최대: " + (maxFileSize / 1024 / 1024) + "MB)");
        }

        String extension = getExtension(file.getOriginalFilename());
        validateExtension(extension);
    }

    private void validateExtension(String extension) {
        Set<String> allowed = Arrays.stream(allowedExtensions.split(","))
                .map(String::trim)
                .map(String::toLowerCase)
                .collect(Collectors.toSet());

        if (!allowed.contains(extension.toLowerCase())) {
            throw new IllegalArgumentException("허용되지 않는 파일 형식입니다: " + extension);
        }
    }

    private String getExtension(String fileName) {
        if (fileName == null || !fileName.contains(".")) {
            return "";
        }
        return fileName.substring(fileName.lastIndexOf(".") + 1);
    }

    private String generateStoredName(String extension) {
        String uuid = UUID.randomUUID().toString();
        return extension.isEmpty() ? uuid : uuid + "." + extension;
    }
}

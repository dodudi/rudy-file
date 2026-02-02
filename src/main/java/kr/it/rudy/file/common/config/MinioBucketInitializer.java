package kr.it.rudy.file.common.config;

import io.minio.BucketExistsArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class MinioBucketInitializer {

    private final MinioClient minioClient;

    @Value("${minio.bucket}")
    private String bucket;

    @EventListener(ApplicationReadyEvent.class)
    public void initBucket() {
        try {
            boolean exists = minioClient.bucketExists(
                    BucketExistsArgs.builder().bucket(bucket).build()
            );
            if (!exists) {
                minioClient.makeBucket(
                        MakeBucketArgs.builder().bucket(bucket).build()
                );
                log.info("Bucket '{}' created successfully", bucket);
            } else {
                log.info("Bucket '{}' already exists", bucket);
            }
        } catch (Exception e) {
            log.error("Failed to initialize MinIO bucket: {}", e.getMessage());
        }
    }
}

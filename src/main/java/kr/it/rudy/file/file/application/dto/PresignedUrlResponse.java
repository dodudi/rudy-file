package kr.it.rudy.file.file.application.dto;

import java.time.LocalDateTime;

public record PresignedUrlResponse(
        String url,
        LocalDateTime expiresAt
) {
}

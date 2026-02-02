package kr.it.rudy.file.file.presentation.controller;

import kr.it.rudy.file.file.application.dto.FileResponse;
import kr.it.rudy.file.file.application.dto.PresignedUrlResponse;
import kr.it.rudy.file.file.application.service.FileService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/files")
public class FileController {

    private final FileService fileService;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<FileResponse> upload(
            @RequestParam("file") MultipartFile file,
            @AuthenticationPrincipal Jwt jwt
    ) {
        String uploadedBy = jwt.getSubject();
        FileResponse response = fileService.upload(file, uploadedBy);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<FileResponse> getFileInfo(@PathVariable String id) {
        FileResponse response = fileService.getFileInfo(id);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}/download")
    public ResponseEntity<InputStreamResource> download(@PathVariable String id) {
        FileResponse fileInfo = fileService.getFileInfo(id);
        InputStream inputStream = fileService.download(id);

        String encodedFileName = URLEncoder.encode(fileInfo.originalName(), StandardCharsets.UTF_8)
                .replaceAll("\\+", "%20");

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(fileInfo.contentType()))
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename*=UTF-8''" + encodedFileName)
                .header(HttpHeaders.CONTENT_LENGTH, String.valueOf(fileInfo.size()))
                .body(new InputStreamResource(inputStream));
    }

    @GetMapping("/my")
    public ResponseEntity<List<FileResponse>> getMyFiles(@AuthenticationPrincipal Jwt jwt) {
        String uploadedBy = jwt.getSubject();
        List<FileResponse> responses = fileService.getFilesByUser(uploadedBy);
        return ResponseEntity.ok(responses);
    }

    @GetMapping
    public ResponseEntity<List<FileResponse>> getAllFiles() {
        List<FileResponse> responses = fileService.getAllFiles();
        return ResponseEntity.ok(responses);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable String id) {
        fileService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}/presigned")
    public ResponseEntity<PresignedUrlResponse> getPresignedDownloadUrl(
            @PathVariable String id,
            @RequestParam(defaultValue = "60") int expiryMinutes
    ) {
        PresignedUrlResponse response = fileService.getPresignedDownloadUrl(id, expiryMinutes);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/presigned/upload")
    public ResponseEntity<PresignedUrlResponse> getPresignedUploadUrl(
            @RequestParam String fileName,
            @RequestParam(defaultValue = "60") int expiryMinutes
    ) {
        PresignedUrlResponse response = fileService.getPresignedUploadUrl(fileName, expiryMinutes);
        return ResponseEntity.ok(response);
    }
}

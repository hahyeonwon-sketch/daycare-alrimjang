package com.daycare.alrimjang.global;

import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.UUID;

public class FileUploadUtils {

    // 허용 확장자 화이트리스트 (이미지만)
    private static final List<String> ALLOWED_EXTENSIONS = List.of("jpg", "jpeg", "png", "gif", "webp");

    // 파일 검증 + 저장, 저장된 파일명(UUID 기반) 반환
    public static String store(MultipartFile file, String baseDir, String uploadDir) throws java.io.IOException {

        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || !originalFilename.contains(".")) {
            throw new IllegalArgumentException("올바르지 않은 파일입니다.");
        }

        String extension = originalFilename.substring(originalFilename.lastIndexOf(".") + 1).toLowerCase();
        if (!ALLOWED_EXTENSIONS.contains(extension)) {
            throw new IllegalArgumentException("허용되지 않는 파일 형식입니다. (jpg, jpeg, png, gif, webp만 가능)");
        }

        // 파일 크기 체크 (10MB)
        if (file.getSize() > 10 * 1024 * 1024) {
            throw new IllegalArgumentException("파일 크기는 10MB를 초과할 수 없습니다.");
        }

        Path uploadPath = Paths.get(baseDir, uploadDir);
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }

        // 원본 파일명은 버리고 UUID + 확장자만 사용 (경로 조작 방지)
        String fileName = UUID.randomUUID() + "." + extension;
        Path filePath = uploadPath.resolve(fileName);

        // resolve 후 실제로 uploadPath 내부인지 재검증 (경로 탈출 방지)
        if (!filePath.normalize().startsWith(uploadPath.normalize())) {
            throw new IllegalArgumentException("올바르지 않은 파일 경로입니다.");
        }

        Files.copy(file.getInputStream(), filePath);

        return fileName;
    }
}
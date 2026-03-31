package com.alotra.storage;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

/**
 * Adapter for local file system storage.
 * Implements ImageStorageService interface for development/testing.
 */
@Component("localStorageAdapter")
@ConditionalOnProperty(name = "storage.provider", havingValue = "local")
public class LocalStorageAdapter implements ImageStorageService {

    @Value("${storage.local.path:/uploads}")
    private String uploadPath;

    @Value("${storage.local.url-prefix:/uploads}")
    private String urlPrefix;

    @Override
    public String uploadImage(MultipartFile file) {
        try {
            if (file == null || file.isEmpty()) {
                throw new IllegalArgumentException("File rỗng");
            }

            // Create upload directory if not exists
            File uploadDir = new File(uploadPath);
            if (!uploadDir.exists() && !uploadDir.mkdirs()) {
                throw new IOException("Không thể tạo thư mục upload: " + uploadPath);
            }

            // Generate unique filename
            String originalName = file.getOriginalFilename();
            String extension = originalName != null ? originalName.substring(originalName.lastIndexOf(".")) : ".jpg";
            String filename = UUID.randomUUID().toString() + extension;
            String filepath = uploadPath + File.separator + filename;

            // Save file
            file.transferTo(new File(filepath));

            // Return accessible URL
            return urlPrefix + "/" + filename;
        } catch (IOException e) {
            throw new RuntimeException("Lỗi lưu ảnh cục bộ", e);
        }
    }

    @Override
    public boolean deleteImage(String imageIdentifier) {
        try {
            if (imageIdentifier == null || imageIdentifier.isBlank()) {
                return false;
            }

            // Extract filename from URL if necessary
            String filename = imageIdentifier;
            if (imageIdentifier.contains("/")) {
                filename = imageIdentifier.substring(imageIdentifier.lastIndexOf("/") + 1);
            }

            Path filePath = Paths.get(uploadPath, filename);
            return Files.deleteIfExists(filePath);
        } catch (IOException e) {
            throw new RuntimeException("Lỗi xóa ảnh cục bộ: " + imageIdentifier, e);
        }
    }

    @Override
    public String getProviderName() {
        return "local";
    }
}

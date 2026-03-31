package com.alotra.storage;

import java.io.IOException;
import java.util.Map;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;

/**
 * Adapter for Cloudinary cloud storage provider.
 * Implements ImageStorageService interface to enable multi-provider support.
 */
@Component("cloudinaryAdapter")
@ConditionalOnProperty(name = "storage.provider", havingValue = "cloudinary", matchIfMissing = true)
public class CloudinaryAdapter implements ImageStorageService {

    private final Cloudinary cloudinary;

    public CloudinaryAdapter(Cloudinary cloudinary) {
        this.cloudinary = cloudinary;
    }

    @Override
    public String uploadImage(MultipartFile file) {
        try {
            Map<?, ?> result = cloudinary.uploader().upload(file.getBytes(), ObjectUtils.emptyMap());
            // Prefer HTTPS URL to avoid mixed-content issues
            Object secureUrl = result.get("secure_url");
            Object url = result.get("url");
            return secureUrl != null ? secureUrl.toString() : (url != null ? url.toString() : null);
        } catch (IOException e) {
            throw new RuntimeException("Lỗi tải ảnh lên Cloudinary", e);
        }
    }

    @Override
    public boolean deleteImage(String imageIdentifier) {
        try {
            if (imageIdentifier == null || imageIdentifier.isBlank()) {
                return false;
            }
            
            // Extract public_id from Cloudinary URL or use directly
            String publicId = extractPublicId(imageIdentifier);
            
            Map<?, ?> result = cloudinary.uploader().destroy(publicId, ObjectUtils.emptyMap());
            Object resultCode = result.get("result");
            return "ok".equals(resultCode);
        } catch (IOException e) {
            throw new RuntimeException("Lỗi xóa ảnh khỏi Cloudinary: " + imageIdentifier, e);
        }
    }

    @Override
    public String getProviderName() {
        return "cloudinary";
    }

    /**
     * Extract public_id from Cloudinary URL.
     * Handles both secure_url and regular URL formats.
     */
    private String extractPublicId(String identifier) {
        if (identifier == null) return "";
        
        // If it's a Cloudinary URL, extract the public_id
        if (identifier.contains("cloudinary.com") || identifier.contains("res.cloudinary.com")) {
            // Example: https://res.cloudinary.com/dzzl3rj/image/upload/v1234567890/folder/image_abc123.jpg
            // Extract folder/image_abc123
            String[] parts = identifier.split("/upload/");
            if (parts.length > 1) {
                String path = parts[1];
                // Remove version segment if present
                path = path.replaceAll("^v\\d+/", "");
                // Remove file extension
                path = path.replaceAll("\\.[^.]+$", "");
                return path;
            }
        }
        
        // Otherwise assume it's already the public_id
        return identifier;
    }
}

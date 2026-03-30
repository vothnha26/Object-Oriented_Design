package com.alotra.storage;

import org.springframework.web.multipart.MultipartFile;

/**
 * Contract for image storage operations (local or cloud).
 * Abstraction layer to support multiple storage providers.
 */
public interface ImageStorageService {
    
    /**
     * Upload image file and return the accessible URL.
     * @param file the image file to upload
     * @return the accessible URL of the uploaded image
     * @throws RuntimeException if upload fails
     */
    String uploadImage(MultipartFile file);
    
    /**
     * Delete image by URL or identifier.
     * @param imageIdentifier the image URL or ID to delete
     * @return true if deletion succeeded, false if not found
     * @throws RuntimeException if deletion fails
     */
    boolean deleteImage(String imageIdentifier);
    
    /**
     * Get the name of this storage provider.
     * @return provider name (e.g., "cloudinary", "local")
     */
    String getProviderName();
}

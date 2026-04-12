package com.alotra.storage;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Factory for selecting appropriate ImageStorageService implementation.
 * Reads storage.provider property to determine which adapter to use.
 */
@Component
public class StorageFactory {

    @Value("${storage.provider:cloudinary}")
    private String storageProvider;

    @Autowired(required = false)
    private CloudinaryAdapter cloudinaryAdapter;

    @Autowired(required = false)
    private LocalStorageAdapter localStorageAdapter;

    /**
     * Get the active storage service based on configuration.
     * @return the configured ImageStorageService implementation
     */
    public ImageStorageService getStorageService() {
        if ("local".equalsIgnoreCase(storageProvider)) {
            if (localStorageAdapter == null) throw new IllegalStateException("LocalStorageAdapter is not configured but provider is 'local'");
            return localStorageAdapter;
        } else if ("cloudinary".equalsIgnoreCase(storageProvider)) {
            if (cloudinaryAdapter == null) throw new IllegalStateException("CloudinaryAdapter is not configured but provider is 'cloudinary'");
            return cloudinaryAdapter;
        } else {
            throw new IllegalArgumentException("Unsupported storage provider: " + storageProvider);
        }
    }

    /**
     * Get storage service by name.
     * @param providerName the name of the storage provider ("cloudinary" or "local")
     * @return the requested ImageStorageService implementation
     */
    public ImageStorageService getStorageService(String providerName) {
        if ("local".equalsIgnoreCase(providerName)) {
            return localStorageAdapter;
        } else if ("cloudinary".equalsIgnoreCase(providerName)) {
            return cloudinaryAdapter;
        } else {
            throw new IllegalArgumentException("Unsupported storage provider: " + providerName);
        }
    }
}

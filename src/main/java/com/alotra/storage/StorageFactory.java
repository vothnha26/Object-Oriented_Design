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

    @Autowired
    private org.springframework.beans.factory.ObjectProvider<CloudinaryAdapter> cloudinaryAdapterProvider;

    @Autowired
    private org.springframework.beans.factory.ObjectProvider<LocalStorageAdapter> localStorageAdapterProvider;

    /**
     * Get the active storage service based on configuration.
     * @return the configured ImageStorageService implementation
     */
    public ImageStorageService getStorageService() {
        if ("local".equalsIgnoreCase(storageProvider)) {
            LocalStorageAdapter local = localStorageAdapterProvider.getIfAvailable();
            if (local != null) return local;
            throw new IllegalStateException("LocalStorageAdapter bean not available");
        } else if ("cloudinary".equalsIgnoreCase(storageProvider)) {
            CloudinaryAdapter cloud = cloudinaryAdapterProvider.getIfAvailable();
            if (cloud != null) return cloud;
            throw new IllegalStateException("CloudinaryAdapter bean not available");
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
            LocalStorageAdapter local = localStorageAdapterProvider.getIfAvailable();
            if (local != null) return local;
            throw new IllegalStateException("LocalStorageAdapter bean not available");
        } else if ("cloudinary".equalsIgnoreCase(providerName)) {
            CloudinaryAdapter cloud = cloudinaryAdapterProvider.getIfAvailable();
            if (cloud != null) return cloud;
            throw new IllegalStateException("CloudinaryAdapter bean not available");
        } else {
            throw new IllegalArgumentException("Unsupported storage provider: " + providerName);
        }
    }
}

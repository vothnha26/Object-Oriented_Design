package com.alotra.storage;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for storage factory and adapters.
 */
public class StorageFactoryTest {

    private StorageFactory factory;
    private CloudinaryAdapter cloudinaryAdapter;
    private LocalStorageAdapter localStorageAdapter;

    @BeforeEach
    public void setUp() {
        factory = new StorageFactory();
        cloudinaryAdapter = new CloudinaryAdapter(null);
        localStorageAdapter = new LocalStorageAdapter();
    }

    @Test
    public void testCloudinaryAdapterProviderName() {
        assertEquals("cloudinary", cloudinaryAdapter.getProviderName());
    }

    @Test
    public void testLocalStorageAdapterProviderName() {
        assertEquals("local", localStorageAdapter.getProviderName());
    }

    @Test
    public void testStorageFactoryInvalidProvider() {
        assertThrows(IllegalArgumentException.class, () -> {
            factory.getStorageService("unknown");
        });
    }

    @Test
    public void testStorageFactoryGetServiceByName() {
        ImageStorageService local = factory.getStorageService("local");
        assertNotNull(local);
        assertEquals("local", local.getProviderName());
        
        ImageStorageService cloudinary = factory.getStorageService("cloudinary");
        assertNotNull(cloudinary);
        assertEquals("cloudinary", cloudinary.getProviderName());
    }
}

package com.skillproof.backend.media;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

/**
 * Available only to the existing @ActiveProfiles("test") context tests.
 */
@Component
@Profile("test")
public class TestMediaObjectStore implements MediaObjectStore {

    private final Map<UUID, byte[]> bytes = new ConcurrentHashMap<>();

    @Override
    public void put(UUID key, InputStream input, long length, String mime) {
        try {
            bytes.put(key, input.readAllBytes());
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    @Override
    public InputStream open(UUID key) {
        byte[] data = bytes.get(key);
        if (data == null) {
            throw new NoSuchElementException();
        
        }return new ByteArrayInputStream(data);
    }

    @Override
    public void delete(UUID key) {
        bytes.remove(key);
    }
}

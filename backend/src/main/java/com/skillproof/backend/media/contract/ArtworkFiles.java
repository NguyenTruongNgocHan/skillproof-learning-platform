package com.skillproof.backend.media.contract;

import java.io.InputStream;
import java.util.UUID;

public interface ArtworkFiles {
    void requireImage(UUID media, String scope, UUID owner);

    record Image(String mime, long size, InputStream stream) {}

    Image image(UUID media);
}

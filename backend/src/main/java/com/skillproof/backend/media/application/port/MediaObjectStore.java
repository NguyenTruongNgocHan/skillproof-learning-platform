package com.skillproof.backend.media.application.port;

import java.io.InputStream;
import java.util.UUID;

/**
 * Private durable byte storage. Business metadata and permissions remain in
 * PostgreSQL.
 */
public interface MediaObjectStore {

    void put(UUID key, InputStream input, long length, String mime);

    InputStream open(UUID key);

    void delete(UUID key);
}

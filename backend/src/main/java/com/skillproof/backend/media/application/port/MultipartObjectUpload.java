package com.skillproof.backend.media.application.port;

import java.util.*;

public interface MultipartObjectUpload {
    record Part(int number, long size, String etag) {}

    String start(UUID key);

    String signedPart(UUID key, String uploadId, int number);

    List<Part> parts(UUID key, String uploadId);

    void assemble(UUID key, String uploadId, List<Part> parts);

    void abort(UUID key, String uploadId);
}

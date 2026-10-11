package com.skillproof.backend.media.application.port;

import java.util.UUID;

public interface DirectObjectUpload {
    String signedPut(UUID key);

    long size(UUID key);
}

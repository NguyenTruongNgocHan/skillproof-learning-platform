package com.skillproof.backend.media.application.port;

import java.util.UUID;

public interface MediaPlaybackLinks {
    String signedGet(UUID storageKey);
}

package com.skillproof.backend.media.contract;

import java.util.UUID;

public interface LibraryMediaQuery {

    boolean hasLibraryFiles(UUID resourceId);
}

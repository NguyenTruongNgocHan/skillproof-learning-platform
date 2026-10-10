package com.skillproof.backend.library.contract;

import java.util.UUID;

public interface LibraryMediaAccess {

    void requireWrite(UUID actor, UUID resourceId);

    void requireRead(UUID actor, UUID resourceId);
}

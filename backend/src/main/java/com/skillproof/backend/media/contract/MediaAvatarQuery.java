package com.skillproof.backend.media.contract;

import java.util.UUID;

/**
 * Public Media contract used by Identity to validate avatar ownership.
 */
public interface MediaAvatarQuery {

    void verifyAvatar(UUID ownerUserId, UUID mediaAssetId);
}

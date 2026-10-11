package com.skillproof.backend.organization.contract;

import java.util.*;

public interface OrganizationNotificationRecipients {
    List<UUID> forAuthority(UUID organization, String authority);
}

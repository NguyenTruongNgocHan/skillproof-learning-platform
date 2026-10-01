CREATE UNIQUE INDEX uq_organization_membership_active_user
    ON organization_membership (user_id)
    WHERE active = TRUE;
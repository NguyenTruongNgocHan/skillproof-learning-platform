CREATE TABLE user_account
(
    id UUID PRIMARY KEY,

    email VARCHAR(320) NOT NULL,

    password_hash VARCHAR(100) NOT NULL,

    role VARCHAR(32) NOT NULL,

    status VARCHAR(32) NOT NULL,

    created_at TIMESTAMP WITH TIME ZONE NOT NULL,

    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,

    CONSTRAINT uq_user_account_email
        UNIQUE (email),

    CONSTRAINT ck_user_account_role
        CHECK (
            role IN (
                'LEARNER',
                'ORGANIZER',
                'ADMIN'
            )
        ),

    CONSTRAINT ck_user_account_status
        CHECK (
            status IN (
                'PENDING_VERIFICATION',
                'ACTIVE',
                'DISABLED'
            )
        )
);
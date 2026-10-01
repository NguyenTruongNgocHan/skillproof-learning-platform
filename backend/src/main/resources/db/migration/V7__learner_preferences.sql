CREATE TABLE learner_preferences (
    user_id UUID PRIMARY KEY
        REFERENCES user_account(id) ON DELETE CASCADE,

    career_goal VARCHAR(100) NOT NULL,
    target_role VARCHAR(100) NOT NULL,

    skill_level VARCHAR(20) NOT NULL
        CHECK (
            skill_level IN (
                'Beginner',
                'Intermediate',
                'Advanced'
            )
        ),

    weekly_goal VARCHAR(20) NOT NULL,
    learning_methods VARCHAR(500) NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL
);
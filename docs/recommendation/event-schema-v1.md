# SkillProof Interaction Event Schema v1

## Purpose

Interaction events provide controlled behavioral signals for the
SkillProof Recommendation research pipeline.

They are not intended for generic analytics or arbitrary UI tracking.

## Proposed event envelope

- event_id
- event_type
- learner_id
- occurred_at
- source_type
- source_id
- source_version_id
- skill_ids
- metadata
- schema_version

## Initial controlled event types

- LEARNING_PATH_VIEWED
- LEARNING_PATH_ENROLLED
- RESOURCE_OPENED
- RESOURCE_COMPLETED
- QUIZ_COMPLETED
- RECOMMENDATION_SHOWN
- RECOMMENDATION_SELECTED
- COMMUNITY_CONTENT_USED
- BATTLE_COMPLETED

## Data minimization

Interaction event payloads must not contain:

- learner email
- learner name
- access token
- refresh token
- raw password
- raw quiz answers
- arbitrary free-text learner data

Quiz-related recommendation signals should use derived values such as:

- skill identifier
- correctness aggregate
- score bucket
- attempt completion state

instead of copying raw answer payloads.

## Version

schema_version = 1
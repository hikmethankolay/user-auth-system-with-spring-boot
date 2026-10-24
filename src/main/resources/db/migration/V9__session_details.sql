-- What GET /api/v1/users/me/sessions shows about each session, so users can recognise their devices and end the
-- others. Every refresh token carries them: the start time is passed on at each rotation, the client details are
-- those of the request that issued the token (the login or the latest refresh).

ALTER TABLE refresh_tokens
    ADD COLUMN session_started_at TIMESTAMPTZ,
    ADD COLUMN user_agent         VARCHAR(255),
    ADD COLUMN ip_address         VARCHAR(64);

-- Sessions from before this migration: the start is unknown, the current token's issue time is the best guess.
UPDATE refresh_tokens SET session_started_at = created_at;

ALTER TABLE refresh_tokens ALTER COLUMN session_started_at SET NOT NULL;

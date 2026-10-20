-- Optimistic locking for users, so a write based on a stale read (e.g. a profile edit racing a
-- demotion) fails instead of silently restoring the old role or password.
ALTER TABLE users ADD COLUMN version BIGINT NOT NULL DEFAULT 0;

-- Bumped whenever every session of a user must end. Refresh tokens remember the value they were
-- issued under, so a login or refresh racing a password change cannot keep a session alive.
ALTER TABLE users ADD COLUMN session_version INTEGER NOT NULL DEFAULT 0;
ALTER TABLE refresh_tokens ADD COLUMN session_version INTEGER NOT NULL DEFAULT 0;

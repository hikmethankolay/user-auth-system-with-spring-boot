-- The OWNER role: the account AdminBootstrap creates. Only the owner changes roles and manages
-- administrators, and the API can neither delete nor demote it.

ALTER TABLE users DROP CONSTRAINT ck_users_role;
ALTER TABLE users ADD CONSTRAINT ck_users_role CHECK (role IN ('USER', 'ADMIN', 'OWNER'));

-- At most one owner.
CREATE UNIQUE INDEX uq_users_single_owner ON users (role) WHERE role = 'OWNER';

-- Existing installations: the oldest administrator, the one AdminBootstrap created, becomes the owner.
-- Its access tokens still say ADMIN until they are refreshed; owner-only checks read the role from this table.
UPDATE users
SET role = 'OWNER'
WHERE id = (SELECT id FROM users WHERE role = 'ADMIN' ORDER BY created_at, id LIMIT 1);

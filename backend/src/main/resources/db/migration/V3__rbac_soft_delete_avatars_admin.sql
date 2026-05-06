-- Soft-delete, avatars as BYTEA, user blocking, normalized timestamps, bootstrap admin

-- USERS: new columns (keep legacy created_at varchar until migrated)
ALTER TABLE users ADD COLUMN IF NOT EXISTS blocked_at TIMESTAMP WITHOUT TIME ZONE;
ALTER TABLE users ADD COLUMN IF NOT EXISTS removed_at TIMESTAMP WITHOUT TIME ZONE;
ALTER TABLE users ADD COLUMN IF NOT EXISTS avatar_full BYTEA;
ALTER TABLE users ADD COLUMN IF NOT EXISTS avatar_thumb BYTEA;

-- Migrate created_at VARCHAR -> TIMESTAMP
ALTER TABLE users ADD COLUMN IF NOT EXISTS created_at_ts TIMESTAMP WITHOUT TIME ZONE;
UPDATE users SET created_at_ts = CURRENT_TIMESTAMP;
UPDATE users SET created_at_ts = trim(created_at)::timestamp
WHERE created_at IS NOT NULL
  AND trim(created_at) <> ''
  AND trim(created_at) ~ '^[0-9]{4}-[0-9]{2}-[0-9]{2}';
ALTER TABLE users DROP COLUMN created_at;
ALTER TABLE users RENAME COLUMN created_at_ts TO created_at;
ALTER TABLE users ALTER COLUMN created_at SET NOT NULL;
ALTER TABLE users ALTER COLUMN created_at SET DEFAULT CURRENT_TIMESTAMP;

ALTER TABLE users DROP COLUMN IF EXISTS avatar_data;

-- SCAN_HISTORY: created_at + soft delete
ALTER TABLE scan_history ADD COLUMN IF NOT EXISTS created_at TIMESTAMP WITHOUT TIME ZONE;
ALTER TABLE scan_history ADD COLUMN IF NOT EXISTS removed_at TIMESTAMP WITHOUT TIME ZONE;
UPDATE scan_history SET created_at = COALESCE(scanned_at, CURRENT_TIMESTAMP) WHERE created_at IS NULL;
ALTER TABLE scan_history ALTER COLUMN created_at SET NOT NULL;
ALTER TABLE scan_history ALTER COLUMN created_at SET DEFAULT CURRENT_TIMESTAMP;

-- Bootstrap admin (bcrypt for plaintext: password)
INSERT INTO users (email, password, full_name, my_allergens, role, created_at, blocked_at, removed_at, avatar_full, avatar_thumb)
SELECT 'arsen.admin@gmail.com',
       '$2b$10$UkzvAMZa0tcrFDh61eG3rOeur.MAsYZttrMwsk8Hm/3qLt/xwW/Fm',
       'System Admin',
       '',
       'ADMIN',
       CURRENT_TIMESTAMP,
       NULL,
       NULL,
       NULL,
       NULL
WHERE NOT EXISTS (
    SELECT 1 FROM users u
    WHERE lower(u.email) = lower('arsen.admin@gmail.com')
      AND u.removed_at IS NULL
);

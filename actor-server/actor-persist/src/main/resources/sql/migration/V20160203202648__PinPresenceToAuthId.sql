ALTER TABLE user_presences ADD COLUMN auth_id BIGINT NOT NULL DEFAULT 0;
CREATE INDEX ON user_presences (user_id, last_seen_at);
CREATE INDEX ON user_presences (user_id, auth_id);
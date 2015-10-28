ALTER TABLE dialogs ADD COLUMN created_at TIMESTAMP NOT NULL DEFAULT NOW();

CREATE INDEX ON dialogs(user_id, created_at);

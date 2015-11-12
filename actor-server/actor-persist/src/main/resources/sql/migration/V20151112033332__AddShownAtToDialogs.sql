ALTER TABLE dialogs ADD COLUMN shown_at TIMESTAMP DEFAULT now();
ALTER TABLE dialogs DROP COLUMN is_hidden;
CREATE INDEX dialogs_user_id_shown_at_idx ON dialogs(user_id, shown_at);

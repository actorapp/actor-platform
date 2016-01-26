ALTER TABLE user_sequence ADD COLUMN reduce_key TEXT;
CREATE INDEX on user_sequence(user_id, reduce_key, seq);
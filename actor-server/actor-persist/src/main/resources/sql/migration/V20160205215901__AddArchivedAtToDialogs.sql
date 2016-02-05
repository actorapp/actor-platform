ALTER TABLE user_dialogs ADD COLUMN archived_at TIMESTAMP DEFAULT NULL;
ALTER TABLE user_dialogs DROP COLUMN is_archived;
CREATE INDEX ON dialogs(user_id, archived_at);
UPDATE user_dialogs SET archived_at = (
  CASE shown_at
    WHEN NULL THEN NOW() + ((RANDOM()*1000)::INT::TEXT || ' seconds')::INTERVAL
    ELSE NULL
  END
);
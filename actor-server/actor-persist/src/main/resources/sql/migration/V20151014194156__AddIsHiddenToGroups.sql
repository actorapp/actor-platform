ALTER TABLE groups ADD COLUMN is_hidden BOOLEAN DEFAULT FALSE;
ALTER TABLE dialogs DROP COLUMN is_hidden;
ALTER TABLE dialogs ADD COLUMN is_archived BOOLEAN DEFAULT FALSE;
CREATE INDEX idx_groups_id_is_hidden ON groups (id, is_hidden);
CREATE INDEX idx_dialogs_user_id_is_archived ON dialogs (user_id, is_archived);

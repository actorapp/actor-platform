ALTER TABLE dialogs ADD COLUMN is_hidden BOOLEAN default false;
CREATE INDEX on dialogs(is_hidden);
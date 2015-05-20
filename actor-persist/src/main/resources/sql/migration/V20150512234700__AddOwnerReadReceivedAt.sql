ALTER TABLE dialogs
  ADD COLUMN owner_last_received_at timestamp,
  ADD COLUMN owner_last_read_at timestamp;

UPDATE dialogs SET owner_last_received_at = last_received_at;
UPDATE dialogs SET owner_last_read_at = last_read_at;

ALTER TABLE dialogs
  ALTER COLUMN owner_last_received_at SET NOT NULL,
  ALTER COLUMN owner_last_read_at SET NOT NULL;
ALTER TABLE dialogs
  ADD COLUMN owner_last_received_at timestamp NOT NULL DEFAULT now(),
  ADD COLUMN owner_last_read_at timestamp NOT NULL DEFAULT now();
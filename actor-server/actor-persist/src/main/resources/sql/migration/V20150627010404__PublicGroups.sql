ALTER TABLE groups ADD COLUMN is_public BOOLEAN NOT NULL DEFAULT false;

CREATE INDEX ON groups(is_public);
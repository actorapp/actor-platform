DELETE from apple_push_credentials;
ALTER TABLE apple_push_credentials DROP COLUMN token;
ALTER TABLE apple_push_credentials ADD COLUMN token BYTEA NOT NULL;
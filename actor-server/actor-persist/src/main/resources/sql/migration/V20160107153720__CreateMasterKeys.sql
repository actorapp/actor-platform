CREATE TABLE master_keys (
  auth_id BIGINT NOT NULL,
  body BYTEA NOT NULL,
  PRIMARY KEY (auth_id)
);
CREATE TABLE device_features (
  auth_id BIGINT NOT NULL,
  name TEXT NOT NULL,
  args BYTEA NOT NULL,
  PRIMARY KEY (auth_id)
)
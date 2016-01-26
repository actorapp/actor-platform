CREATE TABLE encryption_key_groups (
  user_id INT NOT NULL,
  id BIGINT NOT NULL,
  body BYTEA NOT NULL,
  PRIMARY KEY (user_id, id)
);

CREATE INDEX ON encryption_key_groups(user_id);

CREATE TABLE ephermal_public_keys (
  user_id INT NOT NULL,
  key_group_id INT NOT NULL,
  key_id BIGINT NOT NULL,
  body BYTEA,
  PRIMARY KEY (user_id, key_group_id, key_id)
);

CREATE INDEX ON ephermal_public_keys(user_id, key_group_id);

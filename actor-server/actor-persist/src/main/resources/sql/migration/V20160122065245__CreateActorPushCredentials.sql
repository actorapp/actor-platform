CREATE TABLE actor_push_credentials (
  auth_id BIGINT NOT NULL,
  topic TEXT NOT NULL,
  PRIMARY KEY (auth_id)
);

CREATE UNIQUE INDEX ON actor_push_credentials (topic);

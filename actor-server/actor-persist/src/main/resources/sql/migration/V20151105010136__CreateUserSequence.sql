CREATE TABLE user_sequence(
  user_id INT NOT NULL,
  seq INT NOT NULL,
  timestamp BIGINT NOT NULL,
  mapping BYTEA NOT NULL,
  PRIMARY KEY (user_id, seq)
);
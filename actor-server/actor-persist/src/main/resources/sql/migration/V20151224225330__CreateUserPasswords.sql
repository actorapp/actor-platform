CREATE TABLE user_passwords (
  user_id INT NOT NULL,
  salt BYTEA NOT NULL,
  hash TEXT NOT NULL,
  PRIMARY KEY (user_id)
)
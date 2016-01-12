DROP TABLE user_passwords;
CREATE TABLE user_passwords (
  user_id INT NOT NULL,
  hash BYTEA NOT NULL,
  salt BYTEA NOT NULL,
  PRIMARY KEY (user_id)
);
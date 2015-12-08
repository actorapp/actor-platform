CREATE TABLE reactions (
  dialog_type INT NOT NULL,
  dialog_id VARCHAR NOT NULL,
  random_id BIGINT NOT NULL,
  code VARCHAR NOT NULL,
  user_id INT NOT NULL,
  PRIMARY KEY (dialog_type, dialog_id, random_id, code, user_id)
);


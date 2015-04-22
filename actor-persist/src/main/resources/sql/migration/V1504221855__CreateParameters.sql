CREATE TABLE config_parameters (
  user_id int NOT NULL,
  key text NOT NULL,
  value text NOT NULL,
  PRIMARY KEY (user_id, key)
)
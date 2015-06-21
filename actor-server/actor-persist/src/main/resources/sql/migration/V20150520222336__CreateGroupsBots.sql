CREATE TABLE groups_bots (
  group_id int NOT NULL,
  user_id int NOT NULL,
  token varchar(64) NOT NULL,
  PRIMARY KEY (group_id, user_id)
);
CREATE UNIQUE INDEX bot_token_idx ON groups_bots (token);
CREATE TABLE session_infos (
  auth_id bigint NOT NULL,
  session_id bigint NOT NULL,
  user_id int,
  PRIMARY KEY (auth_id, session_id)
)
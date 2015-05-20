CREATE TABLE user_presences (
  user_id int NOT NULL,
  last_seen_at timestamp,
  PRIMARY KEY(user_id)
)